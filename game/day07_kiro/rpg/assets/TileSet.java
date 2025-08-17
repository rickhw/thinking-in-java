package rpg.assets;

import rpg.exceptions.AssetLoadException;
import rpg.utils.GameLogger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced TileSet class that manages tile properties, metadata, and animations.
 * Supports loading tiles from sprite sheets and individual files.
 */
public class TileSet {
    
    public enum TileType {
        GROUND,
        WALL,
        WATER,
        DECORATION,
        INTERACTIVE,
        ANIMATED
    }
    
    /**
     * Properties for individual tiles including collision, type, and custom properties.
     */
    public static class TileProperties {
        private final boolean collidable;
        private final TileType type;
        private final Map<String, Object> customProperties;
        private final boolean animated;
        private final int animationFrames;
        private final float animationSpeed;
        
        public TileProperties(boolean collidable, TileType type) {
            this(collidable, type, new HashMap<>(), false, 1, 0.0f);
        }
        
        public TileProperties(boolean collidable, TileType type, Map<String, Object> customProperties,
                            boolean animated, int animationFrames, float animationSpeed) {
            this.collidable = collidable;
            this.type = type;
            this.customProperties = new HashMap<>(customProperties);
            this.animated = animated;
            this.animationFrames = animationFrames;
            this.animationSpeed = animationSpeed;
        }
        
        public boolean isCollidable() { return collidable; }
        public TileType getType() { return type; }
        public Map<String, Object> getCustomProperties() { return new HashMap<>(customProperties); }
        public boolean isAnimated() { return animated; }
        public int getAnimationFrames() { return animationFrames; }
        public float getAnimationSpeed() { return animationSpeed; }
        
        public Object getCustomProperty(String key) {
            return customProperties.get(key);
        }
        
        public <T> T getCustomProperty(String key, Class<T> type, T defaultValue) {
            Object value = customProperties.get(key);
            if (value != null && type.isInstance(value)) {
                return type.cast(value);
            }
            return defaultValue;
        }
    }
    
    /**
     * Individual tile data with image and animation frames.
     */
    public static class Tile {
        private final BufferedImage[] frames;
        private final TileProperties properties;
        private final int tileId;
        
        public Tile(int tileId, BufferedImage image, TileProperties properties) {
            this.tileId = tileId;
            this.frames = new BufferedImage[]{image};
            this.properties = properties;
        }
        
        public Tile(int tileId, BufferedImage[] frames, TileProperties properties) {
            this.tileId = tileId;
            this.frames = frames.clone();
            this.properties = properties;
        }
        
        public BufferedImage getFrame(int frameIndex) {
            if (frameIndex >= 0 && frameIndex < frames.length) {
                return frames[frameIndex];
            }
            return frames[0]; // Default to first frame
        }
        
        public BufferedImage getCurrentFrame(float animationTime) {
            if (!properties.isAnimated() || frames.length <= 1) {
                return frames[0];
            }
            
            int frameIndex = (int) (animationTime * properties.getAnimationSpeed()) % frames.length;
            return frames[frameIndex];
        }
        
        public int getFrameCount() { return frames.length; }
        public TileProperties getProperties() { return properties; }
        public int getTileId() { return tileId; }
    }
    
    private final Map<Integer, Tile> tiles;
    private final String name;
    private final int tileSize;
    private final String imagePath;
    
    public TileSet(String name, int tileSize) {
        this.name = name;
        this.tileSize = tileSize;
        this.imagePath = null;
        this.tiles = new HashMap<>();
    }
    
    public TileSet(String name, String imagePath, int tileSize) {
        this.name = name;
        this.imagePath = imagePath;
        this.tileSize = tileSize;
        this.tiles = new HashMap<>();
    }
    
    /**
     * Add a tile to the tileset.
     */
    public void addTile(int tileId, BufferedImage image, TileProperties properties) {
        tiles.put(tileId, new Tile(tileId, image, properties));
    }
    
    /**
     * Add an animated tile to the tileset.
     */
    public void addAnimatedTile(int tileId, BufferedImage[] frames, TileProperties properties) {
        tiles.put(tileId, new Tile(tileId, frames, properties));
    }
    
    /**
     * Get a tile by ID.
     */
    public Tile getTile(int tileId) {
        return tiles.get(tileId);
    }
    
    /**
     * Check if a tile exists.
     */
    public boolean hasTile(int tileId) {
        return tiles.containsKey(tileId);
    }
    
    /**
     * Get tile properties by ID.
     */
    public TileProperties getTileProperties(int tileId) {
        Tile tile = tiles.get(tileId);
        return tile != null ? tile.getProperties() : null;
    }
    
    /**
     * Check if a tile is collidable.
     */
    public boolean isCollidable(int tileId) {
        TileProperties props = getTileProperties(tileId);
        return props != null && props.isCollidable();
    }
    
    /**
     * Load tileset from individual tile images.
     */
    public static TileSet loadFromIndividualTiles(String name, Map<Integer, String> tileImagePaths,
                                                 Map<Integer, TileProperties> tileProperties) throws AssetLoadException {
        TileSet tileSet = new TileSet(name, 48); // Default tile size
        
        for (Map.Entry<Integer, String> entry : tileImagePaths.entrySet()) {
            int tileId = entry.getKey();
            String imagePath = entry.getValue();
            
            try {
                InputStream is = TileSet.class.getResourceAsStream(imagePath);
                if (is == null) {
                    throw new AssetLoadException(imagePath, "tile image", "Could not find tile image");
                }
                
                BufferedImage image = ImageIO.read(is);
                TileProperties props = tileProperties.getOrDefault(tileId, 
                    new TileProperties(false, TileType.GROUND));
                
                tileSet.addTile(tileId, image, props);
                is.close();
                
            } catch (IOException e) {
                throw new AssetLoadException(imagePath, "tile image", e);
            }
        }
        
        GameLogger.info("Loaded tileset '" + name + "' with " + tileSet.tiles.size() + " tiles");
        return tileSet;
    }
    
    /**
     * Create default tileset with basic tiles for backward compatibility.
     */
    public static TileSet createDefaultTileSet() throws AssetLoadException {
        Map<Integer, String> tileImagePaths = new HashMap<>();
        tileImagePaths.put(0, "/rpg/assets/tiles/grass.png");
        tileImagePaths.put(1, "/rpg/assets/tiles/wall.png");
        tileImagePaths.put(2, "/rpg/assets/tiles/water.png");
        tileImagePaths.put(3, "/rpg/assets/tiles/earth.png");
        tileImagePaths.put(4, "/rpg/assets/tiles/tree.png");
        tileImagePaths.put(5, "/rpg/assets/tiles/sand.png");
        
        Map<Integer, TileProperties> tileProperties = new HashMap<>();
        tileProperties.put(0, new TileProperties(false, TileType.GROUND));
        tileProperties.put(1, new TileProperties(true, TileType.WALL));
        tileProperties.put(2, new TileProperties(true, TileType.WATER));
        tileProperties.put(3, new TileProperties(false, TileType.GROUND));
        tileProperties.put(4, new TileProperties(true, TileType.DECORATION));
        tileProperties.put(5, new TileProperties(false, TileType.GROUND));
        
        return loadFromIndividualTiles("default", tileImagePaths, tileProperties);
    }
    
    // Getters
    public String getName() { return name; }
    public int getTileSize() { return tileSize; }
    public String getImagePath() { return imagePath; }
    public int getTileCount() { return tiles.size(); }
    
    /**
     * Get all tile IDs in this tileset.
     */
    public java.util.Set<Integer> getTileIds() {
        return new java.util.HashSet<>(tiles.keySet());
    }
}