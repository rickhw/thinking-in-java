package rpg.systems;

import rpg.components.CollisionComponent;
import rpg.engine.Entity;
import rpg.tile.TileManager;

import java.awt.Rectangle;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * High-level collision manager that coordinates all collision detection and response.
 * Provides a unified interface for collision detection, spatial partitioning, and response handling.
 */
public class CollisionManager {
    private QuadTree quadTree;
    private TileCollisionDetector tileCollisionDetector;
    private EventBus eventBus;
    
    // Collision settings
    private Rectangle worldBounds;
    private boolean spatialPartitioningEnabled = true;
    private boolean tileCollisionEnabled = true;
    private boolean debugMode = false;
    
    // Collision statistics
    private CollisionStats stats;
    
    // Collision layer matrix for custom collision rules
    private Map<Integer, Set<Integer>> collisionMatrix;
    
    public CollisionManager(Rectangle worldBounds) {
        this.worldBounds = new Rectangle(worldBounds);
        this.quadTree = new QuadTree(0, worldBounds);
        this.stats = new CollisionStats();
        this.collisionMatrix = new HashMap<>();
        initializeDefaultCollisionMatrix();
    }
    
    /**
     * Initialize default collision matrix based on CollisionLayer rules
     */
    private void initializeDefaultCollisionMatrix() {
        for (int i = 0; i < 32; i++) {
            Set<Integer> collidesWithSet = new HashSet<>();
            for (int j = 0; j < 32; j++) {
                if (CollisionLayer.shouldCollide(i, j)) {
                    collidesWithSet.add(j);
                }
            }
            collisionMatrix.put(i, collidesWithSet);
        }
    }
    
    /**
     * Set the tile collision detector
     */
    public void setTileCollisionDetector(TileCollisionDetector detector) {
        this.tileCollisionDetector = detector;
    }
    
    /**
     * Set the event bus for collision events
     */
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }
    
    /**
     * Update collision detection and response for all entities
     * @param entities List of entities to check for collisions
     * @param deltaTime Time since last update
     */
    public void update(List<Entity> entities, float deltaTime) {
        stats.reset();
        
        // Clear and rebuild spatial structure
        quadTree.clear();
        
        // Filter entities with collision components
        List<Entity> collidableEntities = filterCollidableEntities(entities);
        stats.totalEntities = collidableEntities.size();
        
        if (collidableEntities.isEmpty()) return;
        
        // Update spatial partitioning
        if (spatialPartitioningEnabled) {
            updateSpatialPartitioning(collidableEntities);
        }
        
        // Detect and resolve entity-entity collisions
        List<CollisionPair> collisionPairs = detectEntityCollisions(collidableEntities);
        stats.entityCollisions = collisionPairs.size();
        
        for (CollisionPair pair : collisionPairs) {
            resolveEntityCollision(pair);
        }
        
        // Detect and resolve tile collisions
        if (tileCollisionEnabled && tileCollisionDetector != null) {
            int tileCollisions = detectAndResolveTileCollisions(collidableEntities);
            stats.tileCollisions = tileCollisions;
        }
        
        // Update collision states
        updateCollisionStates(collidableEntities);
    }
    
    /**
     * Filter entities that have collision components
     */
    private List<Entity> filterCollidableEntities(List<Entity> entities) {
        List<Entity> collidableEntities = new ArrayList<>();
        for (Entity entity : entities) {
            if (entity.getComponent(CollisionComponent.class) != null) {
                collidableEntities.add(entity);
            }
        }
        return collidableEntities;
    }
    
    /**
     * Update spatial partitioning structure
     */
    private void updateSpatialPartitioning(List<Entity> entities) {
        for (Entity entity : entities) {
            CollisionComponent collision = entity.getComponent(CollisionComponent.class);
            if (collision != null) {
                Rectangle bounds = collision.getWorldBounds();
                quadTree.insert(entity, bounds);
            }
        }
    }
    
    /**
     * Detect collisions between entities
     */
    private List<CollisionPair> detectEntityCollisions(List<Entity> entities) {
        List<CollisionPair> pairs = new ArrayList<>();
        
        if (spatialPartitioningEnabled) {
            pairs = detectCollisionsWithSpatialPartitioning();
        } else {
            pairs = detectCollisionsBruteForce(entities);
        }
        
        return pairs;
    }
    
    /**
     * Detect collisions using spatial partitioning
     */
    private List<CollisionPair> detectCollisionsWithSpatialPartitioning() {
        List<CollisionPair> pairs = new ArrayList<>();
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
                if (otherCollision != null && canCollide(collision, otherCollision)) {
                    if (CollisionDetection.aabbCollision(bounds, otherCollision.getWorldBounds())) {
                        pairs.add(new CollisionPair(entity, other));
                    }
                }
            }
            
            processedEntities.add(entity);
        }
        
        return pairs;
    }
    
    /**
     * Detect collisions using brute force method
     */
    private List<CollisionPair> detectCollisionsBruteForce(List<Entity> entities) {
        List<CollisionPair> pairs = new ArrayList<>();
        
        for (int i = 0; i < entities.size(); i++) {
            Entity entityA = entities.get(i);
            CollisionComponent collisionA = entityA.getComponent(CollisionComponent.class);
            if (collisionA == null) continue;
            
            for (int j = i + 1; j < entities.size(); j++) {
                Entity entityB = entities.get(j);
                CollisionComponent collisionB = entityB.getComponent(CollisionComponent.class);
                
                if (collisionB != null && canCollide(collisionA, collisionB)) {
                    if (CollisionDetection.aabbCollision(collisionA.getWorldBounds(), collisionB.getWorldBounds())) {
                        pairs.add(new CollisionPair(entityA, entityB));
                    }
                }
            }
        }
        
        return pairs;
    }
    
    /**
     * Check if two collision components can collide based on layers
     */
    private boolean canCollide(CollisionComponent collisionA, CollisionComponent collisionB) {
        int layerA = collisionA.getCollisionLayer();
        int layerB = collisionB.getCollisionLayer();
        
        Set<Integer> layerACollisions = collisionMatrix.get(layerA);
        return layerACollisions != null && layerACollisions.contains(layerB);
    }
    
    /**
     * Resolve collision between two entities
     */
    private void resolveEntityCollision(CollisionPair pair) {
        CollisionComponent collisionA = pair.entityA.getComponent(CollisionComponent.class);
        CollisionComponent collisionB = pair.entityB.getComponent(CollisionComponent.class);
        
        if (collisionA == null || collisionB == null) return;
        
        // Check if this is a new collision
        boolean wasCollidingA = collisionA.isCollidingWith(pair.entityB.getId());
        boolean wasCollidingB = collisionB.isCollidingWith(pair.entityA.getId());
        
        // Update collision state
        collisionA.addCurrentCollision(pair.entityB.getId());
        collisionB.addCurrentCollision(pair.entityA.getId());
        
        // Publish collision events
        if (eventBus != null) {
            CollisionEvent.CollisionType eventType = (!wasCollidingA && !wasCollidingB) ? 
                CollisionEvent.CollisionType.ENTER : CollisionEvent.CollisionType.STAY;
            
            eventBus.publish(new CollisionEvent(pair.entityA.getId(), pair.entityB.getId(), eventType));
        }
        
        // Handle trigger collisions
        if (collisionA.isTrigger() || collisionB.isTrigger()) {
            handleTriggerCollision(pair.entityA, pair.entityB, collisionA, collisionB);
        }
        
        // Handle solid collisions
        if (collisionA.isSolid() && collisionB.isSolid()) {
            CollisionResponse.ResponseType responseType = CollisionResponse.getResponseType(collisionA, collisionB);
            CollisionResponse.applyCollisionResponse(pair.entityA, pair.entityB, responseType);
        }
    }
    
    /**
     * Handle trigger collision events
     */
    private void handleTriggerCollision(Entity entityA, Entity entityB,
                                      CollisionComponent collisionA, CollisionComponent collisionB) {
        if (eventBus != null) {
            if (collisionA.isTrigger()) {
                eventBus.publish(new TriggerEvent(entityA.getId(), entityB.getId(), TriggerEvent.TriggerType.ENTER));
            }
            if (collisionB.isTrigger()) {
                eventBus.publish(new TriggerEvent(entityB.getId(), entityA.getId(), TriggerEvent.TriggerType.ENTER));
            }
        }
    }
    
    /**
     * Detect and resolve tile collisions
     */
    private int detectAndResolveTileCollisions(List<Entity> entities) {
        int collisionCount = 0;
        
        for (Entity entity : entities) {
            CollisionComponent collision = entity.getComponent(CollisionComponent.class);
            if (collision == null || collision.isStatic()) continue;
            
            if (tileCollisionDetector.checkTileCollision(entity)) {
                collisionCount++;
                
                // Publish tile collision event
                if (eventBus != null) {
                    eventBus.publish(new CollisionEvent(entity.getId(), -1, CollisionEvent.CollisionType.ENTER));
                }
            }
        }
        
        return collisionCount;
    }
    
    /**
     * Update collision states for all entities
     */
    private void updateCollisionStates(List<Entity> entities) {
        for (Entity entity : entities) {
            CollisionComponent collision = entity.getComponent(CollisionComponent.class);
            if (collision != null) {
                collision.update(0);
            }
        }
    }
    
    /**
     * Set custom collision rules between layers
     * @param layerA First layer
     * @param layerB Second layer
     * @param shouldCollide Whether these layers should collide
     */
    public void setLayerCollision(int layerA, int layerB, boolean shouldCollide) {
        Set<Integer> layerACollisions = collisionMatrix.computeIfAbsent(layerA, k -> new HashSet<>());
        Set<Integer> layerBCollisions = collisionMatrix.computeIfAbsent(layerB, k -> new HashSet<>());
        
        if (shouldCollide) {
            layerACollisions.add(layerB);
            layerBCollisions.add(layerA);
        } else {
            layerACollisions.remove(layerB);
            layerBCollisions.remove(layerA);
        }
    }
    
    /**
     * Enable or disable spatial partitioning
     */
    public void setSpatialPartitioningEnabled(boolean enabled) {
        this.spatialPartitioningEnabled = enabled;
    }
    
    /**
     * Enable or disable tile collision detection
     */
    public void setTileCollisionEnabled(boolean enabled) {
        this.tileCollisionEnabled = enabled;
    }
    
    /**
     * Enable or disable debug mode
     */
    public void setDebugMode(boolean enabled) {
        this.debugMode = enabled;
    }
    
    /**
     * Get collision statistics
     */
    public CollisionStats getStats() {
        return stats;
    }
    
    /**
     * Get the QuadTree for debugging
     */
    public QuadTree getQuadTree() {
        return quadTree;
    }
    
    /**
     * Collision pair helper class
     */
    public static class CollisionPair {
        public final Entity entityA;
        public final Entity entityB;
        
        public CollisionPair(Entity entityA, Entity entityB) {
            this.entityA = entityA;
            this.entityB = entityB;
        }
    }
    
    /**
     * Collision statistics for performance monitoring
     */
    public static class CollisionStats {
        public int totalEntities = 0;
        public int entityCollisions = 0;
        public int tileCollisions = 0;
        public long processingTimeNanos = 0;
        
        public void reset() {
            totalEntities = 0;
            entityCollisions = 0;
            tileCollisions = 0;
            processingTimeNanos = 0;
        }
        
        @Override
        public String toString() {
            return String.format("CollisionStats{entities=%d, entityCollisions=%d, tileCollisions=%d, timeNs=%d}",
                totalEntities, entityCollisions, tileCollisions, processingTimeNanos);
        }
    }
}