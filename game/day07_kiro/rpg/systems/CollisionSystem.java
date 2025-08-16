package rpg.systems;

import rpg.components.CollisionComponent;
import rpg.components.TransformComponent;
import rpg.components.MovementComponent;
import rpg.engine.Entity;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * System responsible for collision detection and response.
 * Uses QuadTree spatial partitioning for efficient collision detection.
 */
public class CollisionSystem extends GameSystem {
    private QuadTree quadTree;
    private List<CollisionPair> collisionPairs;
    private Map<Integer, Set<Integer>> previousCollisions;
    
    // Collision settings
    private Rectangle worldBounds;
    private boolean enableSpatialPartitioning = true;
    private boolean enableTileCollision = true;
    
    @Override
    public void initialize() {
        // Initialize with default world bounds - can be updated later
        this.worldBounds = new Rectangle(0, 0, 3200, 2400); // 50x37.5 tiles at 64px each
        this.quadTree = new QuadTree(0, worldBounds);
        this.collisionPairs = new ArrayList<>();
        this.previousCollisions = new HashMap<>();
    }
    
    /**
     * Set the world bounds for the QuadTree
     */
    public void setWorldBounds(Rectangle bounds) {
        this.worldBounds = new Rectangle(bounds);
        this.quadTree = new QuadTree(0, worldBounds);
    }
    
    /**
     * Enable or disable spatial partitioning optimization
     */
    public void setSpatialPartitioningEnabled(boolean enabled) {
        this.enableSpatialPartitioning = enabled;
    }
    
    /**
     * Enable or disable tile collision detection
     */
    public void setTileCollisionEnabled(boolean enabled) {
        this.enableTileCollision = enabled;
    }
    
    @Override
    public void update(float deltaTime) {
        if (!isEnabled()) return;
        
        // Clear previous frame data
        collisionPairs.clear();
        quadTree.clear();
        
        // Get all entities with collision components
        List<Entity> collidableEntities = getCollidableEntities();
        
        if (collidableEntities.isEmpty()) return;
        
        // Update QuadTree
        if (enableSpatialPartitioning) {
            updateQuadTree(collidableEntities);
            generateCollisionPairsFromQuadTree();
        } else {
            generateCollisionPairsBruteForce(collidableEntities);
        }
        
        // Process entity-entity collisions
        processCollisions();
        
        // Process tile collisions
        if (enableTileCollision) {
            for (Entity entity : collidableEntities) {
                checkTileCollision(entity);
            }
        }
        
        // Update collision states
        updateCollisionStates();
    }
    
    /**
     * Get all entities that have collision components
     */
    private List<Entity> getCollidableEntities() {
        if (entityManager != null) {
            return entityManager.getEntitiesWithComponent(CollisionComponent.class);
        }
        return new ArrayList<>();
    }
    
    private void updateQuadTree(List<Entity> entities) {
        for (Entity entity : entities) {
            CollisionComponent collision = entity.getComponent(CollisionComponent.class);
            if (collision != null) {
                Rectangle bounds = collision.getWorldBounds();
                quadTree.insert(entity, bounds);
            }
        }
    }
    
    private void generateCollisionPairsFromQuadTree() {
        Set<Entity> processedEntities = new HashSet<>();
        
        for (Entity entity : quadTree.getAllEntities()) {
            if (processedEntities.contains(entity)) continue;
            
            CollisionComponent collision = entity.getComponent(CollisionComponent.class);
            if (collision == null) continue;
            
            Rectangle bounds = collision.getWorldBounds();
            List<Entity> nearbyEntities = new ArrayList<>();
            quadTree.retrieve(nearbyEntities, bounds);
            
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
            
            // Check if collision actually occurs using enhanced detection
            if (detectCollision(collisionA, collisionB)) {
                handleCollision(pair.entityA, pair.entityB, collisionA, collisionB);
            }
        }
    }
    
    private void handleCollision(Entity entityA, Entity entityB, 
                               CollisionComponent collisionA, CollisionComponent collisionB) {
        
        // Check if this is a new collision
        boolean wasCollidingA = collisionA.isCollidingWith(entityB.getId());
        boolean wasCollidingB = collisionB.isCollidingWith(entityA.getId());
        
        // Update collision state
        collisionA.addCurrentCollision(entityB.getId());
        collisionB.addCurrentCollision(entityA.getId());
        
        // Publish collision events
        if (eventBus != null) {
            CollisionEvent.CollisionType eventType = (!wasCollidingA && !wasCollidingB) ? 
                CollisionEvent.CollisionType.ENTER : CollisionEvent.CollisionType.STAY;
            
            eventBus.publish(new CollisionEvent(entityA.getId(), entityB.getId(), eventType));
        }
        
        // Handle solid collision response
        if (collisionA.isSolid() && collisionB.isSolid()) {
            CollisionResponse.ResponseType responseType = CollisionResponse.getResponseType(collisionA, collisionB);
            CollisionResponse.applyCollisionResponse(entityA, entityB, responseType);
        }
        
        // Handle trigger events
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
    
    /**
     * Enhanced collision detection using multiple algorithms
     */
    private boolean detectCollision(CollisionComponent collisionA, CollisionComponent collisionB) {
        if (collisionA == null || collisionB == null || !collisionA.canCollideWith(collisionB)) {
            return false;
        }
        
        Rectangle boundsA = collisionA.getWorldBounds();
        Rectangle boundsB = collisionB.getWorldBounds();
        
        // Use AABB collision detection for rectangular objects
        return CollisionDetection.aabbCollision(boundsA, boundsB);
    }
    
    /**
     * Enhanced collision resolution using minimum translation vector
     */
    private void resolveCollisionEnhanced(Entity entityA, Entity entityB,
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
        
        // Calculate minimum translation vector
        Point2D.Float mtv = CollisionDetection.calculateMTV(boundsA, boundsB);
        
        if (mtv.x == 0 && mtv.y == 0) return; // No collision
        
        // Calculate mass ratios for realistic separation
        float massA = collisionA.getMass();
        float massB = collisionB.getMass();
        float totalMass = massA + massB;
        
        float ratioA = collisionA.isStatic() ? 0 : massB / totalMass;
        float ratioB = collisionB.isStatic() ? 0 : massA / totalMass;
        
        // Apply separation
        if (!collisionA.isStatic()) {
            transformA.x += mtv.x * ratioA;
            transformA.y += mtv.y * ratioA;
            
            // Stop movement in collision direction
            if (movementA != null) {
                if (Math.abs(mtv.x) > Math.abs(mtv.y)) {
                    movementA.velocityX = 0;
                } else {
                    movementA.velocityY = 0;
                }
            }
        }
        
        if (!collisionB.isStatic()) {
            transformB.x -= mtv.x * ratioB;
            transformB.y -= mtv.y * ratioB;
            
            // Stop movement in collision direction
            if (movementB != null) {
                if (Math.abs(mtv.x) > Math.abs(mtv.y)) {
                    movementB.velocityX = 0;
                } else {
                    movementB.velocityY = 0;
                }
            }
        }
    }
    
    private void handleTriggerCollision(Entity entityA, Entity entityB,
                                      CollisionComponent collisionA, CollisionComponent collisionB) {
        // Publish trigger events
        if (eventBus != null) {
            if (collisionA.isTrigger()) {
                eventBus.publish(new TriggerEvent(entityA.getId(), entityB.getId(), TriggerEvent.TriggerType.ENTER));
            }
            if (collisionB.isTrigger()) {
                eventBus.publish(new TriggerEvent(entityB.getId(), entityA.getId(), TriggerEvent.TriggerType.ENTER));
            }
        }
    }
    
    private void updateCollisionStates() {
        List<Entity> collidableEntities = getCollidableEntities();
        
        for (Entity entity : collidableEntities) {
            CollisionComponent collision = entity.getComponent(CollisionComponent.class);
            if (collision != null) {
                collision.update(0); // Update collision state
            }
        }
    }
    
    private TileCollisionDetector tileCollisionDetector;
    
    /**
     * Set the tile collision detector
     */
    public void setTileCollisionDetector(TileCollisionDetector detector) {
        this.tileCollisionDetector = detector;
    }
    
    /**
     * Add tile collision detection for map tiles
     */
    public void checkTileCollision(Entity entity) {
        if (!enableTileCollision || tileCollisionDetector == null) return;
        
        CollisionComponent collision = entity.getComponent(CollisionComponent.class);
        MovementComponent movement = entity.getComponent(MovementComponent.class);
        
        if (collision == null || movement == null) return;
        
        // Check if entity would collide with tiles
        TileCollisionDetector.CollisionResponse response = tileCollisionDetector.getTileCollisionResponse(
            entity, movement.velocityX, movement.velocityY);
        
        if (response.collisionOccurred) {
            // Adjust movement based on collision response
            movement.velocityX = response.correctedDeltaX;
            movement.velocityY = response.correctedDeltaY;
            
            // Publish tile collision event
            if (eventBus != null) {
                eventBus.publish(new CollisionEvent(entity.getId(), -1, CollisionEvent.CollisionType.ENTER));
            }
        }
    }
    
    /**
     * Get collision statistics for debugging
     */
    public CollisionStats getCollisionStats() {
        return new CollisionStats(
            collisionPairs.size(),
            quadTree.getTotalObjectCount(),
            quadTree.isSubdivided()
        );
    }
    
    @Override
    public void cleanup() {
        if (collisionPairs != null) {
            collisionPairs.clear();
        }
        if (quadTree != null) {
            quadTree.clear();
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
     * Statistics class for collision system performance monitoring
     */
    public static class CollisionStats {
        public final int collisionPairs;
        public final int entitiesInQuadTree;
        public final boolean quadTreeSubdivided;
        
        public CollisionStats(int collisionPairs, int entitiesInQuadTree, boolean quadTreeSubdivided) {
            this.collisionPairs = collisionPairs;
            this.entitiesInQuadTree = entitiesInQuadTree;
            this.quadTreeSubdivided = quadTreeSubdivided;
        }
        
        @Override
        public String toString() {
            return String.format("CollisionStats{pairs=%d, entities=%d, subdivided=%s}",
                collisionPairs, entitiesInQuadTree, quadTreeSubdivided);
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