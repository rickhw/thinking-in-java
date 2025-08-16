package rpg.systems;

import rpg.components.CollisionComponent;
import rpg.components.TransformComponent;
import rpg.components.MovementComponent;
import rpg.engine.Entity;
import rpg.tile.TileManager;
import rpg.Config;

import java.awt.Rectangle;

/**
 * Utility class for detecting collisions between entities and map tiles.
 * Provides efficient tile-based collision detection for grid-based maps.
 */
public class TileCollisionDetector {
    private final TileManager tileManager;
    
    public TileCollisionDetector(TileManager tileManager) {
        this.tileManager = tileManager;
    }
    
    /**
     * Check collision between an entity and map tiles
     * @param entity The entity to check
     * @return true if collision detected
     */
    public boolean checkTileCollision(Entity entity) {
        CollisionComponent collision = entity.getComponent(CollisionComponent.class);
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        MovementComponent movement = entity.getComponent(MovementComponent.class);
        
        if (collision == null || transform == null || movement == null) {
            return false;
        }
        
        Rectangle entityBounds = collision.getWorldBounds();
        
        // Calculate tile coordinates that the entity overlaps
        int leftTile = entityBounds.x / Config.TILE_SIZE;
        int rightTile = (entityBounds.x + entityBounds.width - 1) / Config.TILE_SIZE;
        int topTile = entityBounds.y / Config.TILE_SIZE;
        int bottomTile = (entityBounds.y + entityBounds.height - 1) / Config.TILE_SIZE;
        
        // Check bounds
        if (leftTile < 0 || rightTile >= Config.MAX_WORLD_COL || 
            topTile < 0 || bottomTile >= Config.MAX_WORLD_ROW) {
            return true; // Collision with world bounds
        }
        
        // Check each tile the entity overlaps
        for (int tileX = leftTile; tileX <= rightTile; tileX++) {
            for (int tileY = topTile; tileY <= bottomTile; tileY++) {
                if (isTileCollidable(tileX, tileY)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Check collision in a specific direction before movement
     * @param entity The entity to check
     * @param deltaX Movement in X direction
     * @param deltaY Movement in Y direction
     * @return true if collision would occur
     */
    public boolean checkTileCollisionWithMovement(Entity entity, float deltaX, float deltaY) {
        CollisionComponent collision = entity.getComponent(CollisionComponent.class);
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        
        if (collision == null || transform == null) {
            return false;
        }
        
        // Get current bounds
        Rectangle currentBounds = collision.getWorldBounds();
        
        // Calculate future bounds
        Rectangle futureBounds = new Rectangle(
            (int)(currentBounds.x + deltaX),
            (int)(currentBounds.y + deltaY),
            currentBounds.width,
            currentBounds.height
        );
        
        return checkTileCollisionWithBounds(futureBounds);
    }
    
    /**
     * Check collision with specific bounds
     * @param bounds The bounds to check
     * @return true if collision detected
     */
    public boolean checkTileCollisionWithBounds(Rectangle bounds) {
        // Calculate tile coordinates
        int leftTile = bounds.x / Config.TILE_SIZE;
        int rightTile = (bounds.x + bounds.width - 1) / Config.TILE_SIZE;
        int topTile = bounds.y / Config.TILE_SIZE;
        int bottomTile = (bounds.y + bounds.height - 1) / Config.TILE_SIZE;
        
        // Check bounds
        if (leftTile < 0 || rightTile >= Config.MAX_WORLD_COL || 
            topTile < 0 || bottomTile >= Config.MAX_WORLD_ROW) {
            return true; // Collision with world bounds
        }
        
        // Check each tile
        for (int tileX = leftTile; tileX <= rightTile; tileX++) {
            for (int tileY = topTile; tileY <= bottomTile; tileY++) {
                if (isTileCollidable(tileX, tileY)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Get the collision response for tile collision
     * @param entity The entity that collided
     * @param deltaX Attempted movement in X
     * @param deltaY Attempted movement in Y
     * @return Corrected movement vector
     */
    public CollisionResponse getTileCollisionResponse(Entity entity, float deltaX, float deltaY) {
        CollisionComponent collision = entity.getComponent(CollisionComponent.class);
        if (collision == null) {
            return new CollisionResponse(deltaX, deltaY, false);
        }
        
        Rectangle bounds = collision.getWorldBounds();
        
        // Test X movement only
        boolean xCollision = checkTileCollisionWithBounds(new Rectangle(
            (int)(bounds.x + deltaX), bounds.y, bounds.width, bounds.height));
        
        // Test Y movement only
        boolean yCollision = checkTileCollisionWithBounds(new Rectangle(
            bounds.x, (int)(bounds.y + deltaY), bounds.width, bounds.height));
        
        float correctedDeltaX = xCollision ? 0 : deltaX;
        float correctedDeltaY = yCollision ? 0 : deltaY;
        
        return new CollisionResponse(correctedDeltaX, correctedDeltaY, xCollision || yCollision);
    }
    
    /**
     * Check if a specific tile is collidable
     * @param tileX Tile X coordinate
     * @param tileY Tile Y coordinate
     * @return true if tile is collidable
     */
    private boolean isTileCollidable(int tileX, int tileY) {
        if (tileManager == null || tileManager.mapTileNum == null) {
            return false;
        }
        
        // Check bounds
        if (tileX < 0 || tileX >= Config.MAX_WORLD_COL || 
            tileY < 0 || tileY >= Config.MAX_WORLD_ROW) {
            return true; // Treat out-of-bounds as collidable
        }
        
        int tileNum = tileManager.mapTileNum[tileX][tileY];
        
        if (tileNum < 0 || tileNum >= tileManager.tiles.length) {
            return false;
        }
        
        return tileManager.tiles[tileNum].collision;
    }
    
    /**
     * Get the tile at specific world coordinates
     * @param worldX World X coordinate
     * @param worldY World Y coordinate
     * @return Tile number, or -1 if out of bounds
     */
    public int getTileAt(int worldX, int worldY) {
        int tileX = worldX / Config.TILE_SIZE;
        int tileY = worldY / Config.TILE_SIZE;
        
        if (tileX < 0 || tileX >= Config.MAX_WORLD_COL || 
            tileY < 0 || tileY >= Config.MAX_WORLD_ROW) {
            return -1;
        }
        
        if (tileManager == null || tileManager.mapTileNum == null) {
            return -1;
        }
        
        return tileManager.mapTileNum[tileX][tileY];
    }
    
    /**
     * Response class for tile collision detection
     */
    public static class CollisionResponse {
        public final float correctedDeltaX;
        public final float correctedDeltaY;
        public final boolean collisionOccurred;
        
        public CollisionResponse(float correctedDeltaX, float correctedDeltaY, boolean collisionOccurred) {
            this.correctedDeltaX = correctedDeltaX;
            this.correctedDeltaY = correctedDeltaY;
            this.collisionOccurred = collisionOccurred;
        }
    }
}