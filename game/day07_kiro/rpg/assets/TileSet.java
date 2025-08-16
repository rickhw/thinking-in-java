package rpg.assets;

import rpg.exceptions.AssetLoadException;
import rpg.utils.GameLogger;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a collection of tiles with properties and metadata.
 * Supports efficient tile storage and retrieval with collision and animation data.
 */
public class TileSet {
    private static final GameLogger logger = GameLogger.getInstance();
    
    private final Map<Integer, Tile> tiles = new HashMap<>();
    private final Map<Integer, TileProperties> tileProperties = new HashMap<>();
    private String name;
    private String imagePath;
    private int tileWidth = 32;
    private int tileHeight = 32;
    
    /**
     * Default constructor for fallback tileset.
     */
    public TileSet() {
        this.name = "fallback";
        this.imagePath = null;
    }
    
    /**
     * Create a tileset from an image path.
     */
    public TileSet(String imagePath, AssetManager assetManager) throws AssetLoadException {
        this.imagePath = imagePath;
        this.name = extractNameFromPath(imagePath);
        loadFromImage(imagePath, assetManager);
    }
    
    /**
     * Create a tileset with custom tile dimensions.
     */
    public TileSet(String imagePath, int tileWidth, int tileHeight, AssetManager assetManager) throws AssetLoadException {
        this.imagePath = imagePath;
        this.name = extractNameFromPath(imagePath);
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        loadFromImage(imagePath, assetManager);
    }
    
    /**
     * Load tiles from a tileset image.
     */
    private void loadFromImage(String imagePath, AssetManager assetManager) throws AssetLoadException {
        try {
            BufferedImage tilesetImage = assetManager.loadImage(imagePath);
            
            int tilesPerRow = tilesetImage.getWidth() / tileWidth;
            int tilesPerColumn = tilesetImage.getHeight() / tileHeight;
            
            int tileId = 0;
            for (int row = 0; row < tilesPerColumn; row++) {
                for (int col = 0; col < tilesPerRow; col++) {
                    BufferedImage tileImage = tilesetImage.getSubimage(
                        col * tileWidth, row * tileHeight, tileWidth, tileHeight);
                    
                    Tile tile = new Tile(tileId, tileImage);
                    tiles.put(tileId, tile);
                    tileProperties.put(tileId, new TileProperties());
                    
                    tileId++;
                }
            }
            
            logger.info("Loaded tileset: " + name + " with " + tiles.size() + " tiles");
            
        } catch (Exception e) {
            throw new AssetLoadException(imagePath, "TileSet", e);
        }
    }
    
    /**
     * Add a single tile to the tileset.
     */
    public void addTile(int tileId, BufferedImage image, boolean collidable) {
        Tile tile = new Tile(tileId, image);
        tiles.put(tileId, tile);
        
        TileProperties properties = new TileProperties();
        properties.setCollidable(collidable);
        tileProperties.put(tileId, properties);
    }
    
    /**
     * Get a tile by ID.
     */
    public Tile getTile(int tileId) {
        return tiles.get(tileId);
    }
    
    /**
     * Get tile properties by ID.
     */
    public TileProperties getTileProperties(int tileId) {
        return tileProperties.get(tileId);
    }
    
    /**
     * Check if a tile is collidable.
     */
    public boolean isCollidable(int tileId) {
        TileProperties props = tileProperties.get(tileId);
        return props != null && props.isCollidable();
    }
    
    /**
     * Set tile collision property.
     */
    public void setTileCollidable(int tileId, boolean collidable) {
        TileProperties props = tileProperties.get(tileId);
        if (props != null) {
            props.setCollidable(collidable);
        }
    }
    
    /**
     * Get the number of tiles in this tileset.
     */
    public int getTileCount() {
        return tiles.size();
    }
    
    /**
     * Get tileset name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get tile dimensions.
     */
    public int getTileWidth() {
        return tileWidth;
    }
    
    public int getTileHeight() {
        return tileHeight;
    }
    
    /**
     * Check if tileset contains a specific tile ID.
     */
    public boolean hasTile(int tileId) {
        return tiles.containsKey(tileId);
    }
    
    /**
     * Get all tile IDs in this tileset.
     */
    public Integer[] getTileIds() {
        return tiles.keySet().toArray(new Integer[0]);
    }
    
    /**
     * Extract name from file path.
     */
    private String extractNameFromPath(String path) {
        if (path == null) return "unknown";
        
        String filename = path.substring(path.lastIndexOf('/') + 1);
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(0, dotIndex) : filename;
    }
    
    /**
     * Individual tile representation.
     */
    public static class Tile {
        private final int id;
        private final BufferedImage image;
        
        public Tile(int id, BufferedImage image) {
            this.id = id;
            this.image = image;
        }
        
        public int getId() {
            return id;
        }
        
        public BufferedImage getImage() {
            return image;
        }
    }
    
    /**
     * Tile properties and metadata.
     */
    public static class TileProperties {
        private boolean collidable = false;
        private TileType type = TileType.NORMAL;
        private final Map<String, Object> customProperties = new HashMap<>();
        
        public boolean isCollidable() {
            return collidable;
        }
        
        public void setCollidable(boolean collidable) {
            this.collidable = collidable;
        }
        
        public TileType getType() {
            return type;
        }
        
        public void setType(TileType type) {
            this.type = type;
        }
        
        public void setCustomProperty(String key, Object value) {
            customProperties.put(key, value);
        }
        
        public Object getCustomProperty(String key) {
            return customProperties.get(key);
        }
        
        public boolean hasCustomProperty(String key) {
            return customProperties.containsKey(key);
        }
    }
    
    /**
     * Tile type enumeration.
     */
    public enum TileType {
        NORMAL,
        ANIMATED,
        TRIGGER,
        DECORATION,
        COLLISION_ONLY
    }
}