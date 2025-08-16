package rpg.systems;

/**
 * Defines collision layers for selective collision detection.
 * Each layer represents a different type of collision interaction.
 */
public class CollisionLayer {
    // Default collision layers
    public static final int DEFAULT = 0;
    public static final int PLAYER = 1;
    public static final int ENEMY = 2;
    public static final int ENVIRONMENT = 3;
    public static final int PROJECTILE = 4;
    public static final int TRIGGER = 5;
    public static final int PICKUP = 6;
    public static final int WALL = 7;
    public static final int WATER = 8;
    public static final int PLATFORM = 9;
    
    // Layer names for debugging
    private static final String[] LAYER_NAMES = {
        "Default", "Player", "Enemy", "Environment", "Projectile",
        "Trigger", "Pickup", "Wall", "Water", "Platform"
    };
    
    /**
     * Get the name of a collision layer
     * @param layer The layer number
     * @return The layer name, or "Unknown" if invalid
     */
    public static String getLayerName(int layer) {
        if (layer >= 0 && layer < LAYER_NAMES.length) {
            return LAYER_NAMES[layer];
        }
        return "Unknown (" + layer + ")";
    }
    
    /**
     * Check if two layers should collide based on default rules
     * @param layerA First layer
     * @param layerB Second layer
     * @return true if layers should collide
     */
    public static boolean shouldCollide(int layerA, int layerB) {
        // Default collision matrix - can be customized
        switch (layerA) {
            case PLAYER:
                return layerB == ENEMY || layerB == ENVIRONMENT || layerB == WALL || 
                       layerB == WATER || layerB == PLATFORM || layerB == TRIGGER || layerB == PICKUP;
            
            case ENEMY:
                return layerB == PLAYER || layerB == ENVIRONMENT || layerB == WALL || 
                       layerB == WATER || layerB == PLATFORM || layerB == PROJECTILE;
            
            case ENVIRONMENT:
                return layerB == PLAYER || layerB == ENEMY || layerB == PROJECTILE;
            
            case PROJECTILE:
                return layerB == PLAYER || layerB == ENEMY || layerB == ENVIRONMENT || 
                       layerB == WALL || layerB == WATER;
            
            case TRIGGER:
                return layerB == PLAYER || layerB == ENEMY;
            
            case PICKUP:
                return layerB == PLAYER;
            
            case WALL:
                return layerB == PLAYER || layerB == ENEMY || layerB == PROJECTILE;
            
            case WATER:
                return layerB == PLAYER || layerB == ENEMY || layerB == PROJECTILE;
            
            case PLATFORM:
                return layerB == PLAYER || layerB == ENEMY;
            
            default:
                return true; // Default layer collides with everything
        }
    }
    
    /**
     * Check if a layer represents a solid object
     * @param layer The layer to check
     * @return true if the layer is solid
     */
    public static boolean isSolid(int layer) {
        return layer == WALL || layer == ENVIRONMENT || layer == PLATFORM;
    }
    
    /**
     * Check if a layer represents a trigger
     * @param layer The layer to check
     * @return true if the layer is a trigger
     */
    public static boolean isTrigger(int layer) {
        return layer == TRIGGER || layer == PICKUP;
    }
}