package rpg.systems;

import rpg.components.CollisionComponent;
import rpg.components.TransformComponent;
import rpg.components.MovementComponent;
import rpg.engine.Entity;
import java.awt.Rectangle;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * System responsible for collision detection and response.
 * Uses spatial partitioning for efficient collision detection.
 */
public class CollisionSystem extends GameSystem {
    private SpatialGrid spatialGrid;
    private List<CollisionPair> collisionPairs;
    private Map<Integer, Set<Integer>> previousCollisions;
    
    // Collision settings
    private int gridCellSize = 64;
    private boolean enableSpatialPartitioning = true;
    
    @Override
    public void initialize() {
        this.spatialGrid = new SpatialGrid(gridCellSize);
        this.collisionPairs = new ArrayList<>();
        this.previousCollisions = new HashMap<>();
    }
    
    /**
     * Set the grid cell size for spatial partitioning
     */
    public void setGridCellSize(int cellSize) {
        this.gridCellSize = cellSize;
        this.spatialGrid = new SpatialGrid(cellSize);
    }
    
    /**
     * Enable or disable spatial partitioning optimization
     */
    public void setSpatialPartitioningEnabled(boolean enabled) {
        this.enableSpatialPartitioning = enabled;
    }
    
    @Override
    public void update(float deltaTime) {
        if (!isEnabled()) return;
        
        // Clear previous frame data
        collisionPairs.clear();
        spatialGrid.clear();
        
        // Get all entities with collision components
        List<Entity> collidableEntities = entityManager.getEntitiesWith(CollisionComponent.class);
        
        if (collidableEntities.isEmpty()) return;
        
        // Update spatial grid
        if (enableSpatialPartitioning) {
            updateSpatialGrid(collidableEntities);
            generateCollisionPairsFromGrid();
        } else {
            generateCollisionPairsBruteForce(collidableEntities);
        }
        
        // Process collisions
        processCollisions();
        
        // Update collision states
        updateCollisionStates();
    }
    
    private void updateSpatialGrid(List<Entity> entities) {
        for (Entity entity : entities) {
            CollisionComponent collision = entity.getComponent(CollisionComponent.class);
            if (collision != null) {
                Rectangle bounds = collision.getWorldBounds();
                spatialGrid.insert(entity, bounds);
            }
        }
    }
    
    private void generateCollisionPairsFromGrid() {
        Set<Entity> processedEntities = new HashSet<>();
        
        for (Entity entity : spatialGrid.getAllEntities()) {
            if (processedEntities.contains(entity)) continue;
            
            CollisionComponent collision = entity.getComponent(CollisionComponent.class);
            if (collision == null) continue;
            
            Rectangle bounds = collision.getWorldBounds();
            List<Entity> nearbyEntities = spatialGrid.query(bounds);
            
            for (Entity other : nearbyEntities) {
                if (entity == other || processedEntities.contains(other)) continue;
                
                CollisionComponent otherCollision = other.getComponent(CollisionComponent.class);
                if (otherCollision != null && collision.canCollideWith(otherCollision)) {
                    collisionPairs.add(new CollisionPair(entity, other));
                }
            }
            
            processedEntities.add(entity);
        }
    }
    
    private void generateCollisionPairsBruteForce(List<Entity> entities) {
        for (int i = 0; i < entities.size(); i++) {
            Entity entityA = entities.get(i);
            CollisionComponent collisionA = entityA.getComponent(CollisionComponent.class);
            if (collisionA == null) continue;
            
            for (int j = i + 1; j < entities.size(); j++) {
                Entity entityB = entities.get(j);
                CollisionComponent collisionB = entityB.getComponent(CollisionComponent.class);
                
                if (collisionB != null && collisionA.canCollideWith(collisionB)) {
                    collisionPairs.add(new CollisionPair(entityA, entityB));
                }
            }
        }
    }
    
    private void processCollisions() {
        for (CollisionPair pair : collisionPairs) {
            CollisionComponent collisionA = pair.entityA.getComponent(CollisionComponent.class);
            CollisionComponent collisionB = pair.entityB.getComponent(CollisionComponent.class);
            
            if (collisionA == null || collisionB == null) continue;
            
            // Check if collision actually occurs
            if (collisionA.intersects(collisionB)) {
                handleCollision(pair.entityA, pair.entityB, collisionA, collisionB);
            }
        }
    }
    
    private void handleCollision(Entity entityA, Entity entityB, 
                               CollisionComponent collisionA, CollisionComponent collisionB) {
        
        // Update collision state
        collisionA.addCurrentCollision(entityB.getId());
        collisionB.addCurrentCollision(entityA.getId());
        
        // Handle solid collision response
        if (collisionA.isSolid() && collisionB.isSolid()) {
            resolveCollision(entityA, entityB, collisionA, collisionB);
        }
        
        // Handle trigger events (implement event system later)
        if (collisionA.isTrigger() || collisionB.isTrigger()) {
            handleTriggerCollision(entityA, entityB, collisionA, collisionB);
        }
    }
    
    private void resolveCollision(Entity entityA, Entity entityB,
                                CollisionComponent collisionA, CollisionComponent collisionB) {
        
        // Skip if both entities are static
        if (collisionA.isStatic() && collisionB.isStatic()) return;
        
        TransformComponent transformA = entityA.getComponent(TransformComponent.class);
        TransformComponent transformB = entityB.getComponent(TransformComponent.class);
        MovementComponent movementA = entityA.getComponent(MovementComponent.class);
        MovementComponent movementB = entityB.getComponent(MovementComponent.class);
        
        if (transformA == null || transformB == null) return;
        
        Rectangle boundsA = collisionA.getWorldBounds();
        Rectangle boundsB = collisionB.getWorldBounds();
        
        // Calculate overlap
        float overlapX = Math.min(boundsA.x + boundsA.width - boundsB.x,
                                 boundsB.x + boundsB.width - boundsA.x);
        float overlapY = Math.min(boundsA.y + boundsA.height - boundsB.y,
                                 boundsB.y + boundsB.height - boundsA.y);
        
        // Separate along the axis with smallest overlap
        if (overlapX < overlapY) {
            // Separate horizontally
            float separation = overlapX / 2;
            if (boundsA.x < boundsB.x) {
                if (!collisionA.isStatic()) transformA.x -= separation;
                if (!collisionB.isStatic()) transformB.x += separation;
            } else {
                if (!collisionA.isStatic()) transformA.x += separation;
                if (!collisionB.isStatic()) transformB.x -= separation;
            }
            
            // Stop horizontal movement
            if (movementA != null && !collisionA.isStatic()) movementA.velocityX = 0;
            if (movementB != null && !collisionB.isStatic()) movementB.velocityX = 0;
            
        } else {
            // Separate vertically
            float separation = overlapY / 2;
            if (boundsA.y < boundsB.y) {
                if (!collisionA.isStatic()) transformA.y -= separation;
                if (!collisionB.isStatic()) transformB.y += separation;
            } else {
                if (!collisionA.isStatic()) transformA.y += separation;
                if (!collisionB.isStatic()) transformB.y -= separation;
            }
            
            // Stop vertical movement
            if (movementA != null && !collisionA.isStatic()) movementA.velocityY = 0;
            if (movementB != null && !collisionB.isStatic()) movementB.velocityY = 0;
        }
    }
    
    private void handleTriggerCollision(Entity entityA, Entity entityB,
                                      CollisionComponent collisionA, CollisionComponent collisionB) {
        // Trigger collision handling - will be enhanced when event system is implemented
        // For now, just mark the collision state
    }
    
    private void updateCollisionStates() {
        List<Entity> collidableEntities = entityManager.getEntitiesWith(CollisionComponent.class);
        
        for (Entity entity : collidableEntities) {
            CollisionComponent collision = entity.getComponent(CollisionComponent.class);
            if (collision != null) {
                collision.update(0); // Update collision state
            }
        }
    }
    
    @Override
    public void cleanup() {
        if (collisionPairs != null) {
            collisionPairs.clear();
        }
        if (spatialGrid != null) {
            spatialGrid.clear();
        }
        if (previousCollisions != null) {
            previousCollisions.clear();
        }
    }
    
    @Override
    public int getPriority() {
        return 200; // Collision should happen after movement but before rendering
    }
    
    /**
     * Simple spatial grid for collision optimization
     */
    private static class SpatialGrid {
        private final int cellSize;
        private final Map<String, List<Entity>> grid;
        
        public SpatialGrid(int cellSize) {
            this.cellSize = cellSize;
            this.grid = new HashMap<>();
        }
        
        public void clear() {
            grid.clear();
        }
        
        public void insert(Entity entity, Rectangle bounds) {
            int startX = bounds.x / cellSize;
            int startY = bounds.y / cellSize;
            int endX = (bounds.x + bounds.width) / cellSize;
            int endY = (bounds.y + bounds.height) / cellSize;
            
            for (int x = startX; x <= endX; x++) {
                for (int y = startY; y <= endY; y++) {
                    String key = x + "," + y;
                    grid.computeIfAbsent(key, k -> new ArrayList<>()).add(entity);
                }
            }
        }
        
        public List<Entity> query(Rectangle bounds) {
            Set<Entity> result = new HashSet<>();
            
            int startX = bounds.x / cellSize;
            int startY = bounds.y / cellSize;
            int endX = (bounds.x + bounds.width) / cellSize;
            int endY = (bounds.y + bounds.height) / cellSize;
            
            for (int x = startX; x <= endX; x++) {
                for (int y = startY; y <= endY; y++) {
                    String key = x + "," + y;
                    List<Entity> entities = grid.get(key);
                    if (entities != null) {
                        result.addAll(entities);
                    }
                }
            }
            
            return new ArrayList<>(result);
        }
        
        public Set<Entity> getAllEntities() {
            Set<Entity> result = new HashSet<>();
            for (List<Entity> entities : grid.values()) {
                result.addAll(entities);
            }
            return result;
        }
    }
    
    /**
     * Helper class to represent a collision pair
     */
    private static class CollisionPair {
        final Entity entityA;
        final Entity entityB;
        
        CollisionPair(Entity entityA, Entity entityB) {
            this.entityA = entityA;
            this.entityB = entityB;
        }
    }
}