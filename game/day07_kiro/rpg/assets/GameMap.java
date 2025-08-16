package rpg.assets;

import rpg.exceptions.AssetLoadException;
import rpg.utils.GameLogger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a game map with tile data and metadata.
 * Supports loading from text files and provides efficient tile access.
 */
public class GameMap {
    private static final GameLogger logger = GameLogger.getInstance();
    
    private final String name;
    private final String mapPath;
    private int[][] tileData;
    private int width;
    private int height;
    private TileSet tileSet;
    private final List<MapLayer> layers = new ArrayList<>();
    
    /**
     * Create a game map from a file path.
     */
    public GameMap(String mapPath, AssetManager assetManager) throws AssetLoadException {
        this.mapPath = mapPath;
        this.name = extractNameFromPath(mapPath);
        loadFromFile(mapPath);
    }
    
    /**
     * Create a game map with specified dimensions.
     */
    public GameMap(String name, int width, int height) {
        this.name = name;
        this.mapPath = null;
        this.width = width;
        this.height = height;
        this.tileData = new int[width][height];
    }
    
    /**
     * Load map data from a text file.
     */
    private void loadFromFile(String mapPath) throws AssetLoadException {
        try {
            InputStream stream = getClass().getResourceAsStream(mapPath);
            if (stream == null) {
                throw new AssetLoadException(mapPath, "GameMap", "Map file not found");
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            List<String> lines = new ArrayList<>();
            String line;
            
            // Read all lines
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line.trim());
                }
            }
            reader.close();
            
            if (lines.isEmpty()) {
                throw new AssetLoadException(mapPath, "GameMap", "Map file is empty");
            }
            
            // Parse dimensions from first line
            String[] firstLineTokens = lines.get(0).split("\\s+");
            width = firstLineTokens.length;
            height = lines.size();
            
            // Initialize tile data array
            tileData = new int[width][height];
            
            // Parse tile data
            for (int row = 0; row < height; row++) {
                String[] tokens = lines.get(row).split("\\s+");
                if (tokens.length != width) {
                    throw new AssetLoadException(mapPath, "GameMap", 
                        "Inconsistent row width at line " + (row + 1));
                }
                
                for (int col = 0; col < width; col++) {
                    try {
                        tileData[col][row] = Integer.parseInt(tokens[col]);
                    } catch (NumberFormatException e) {
                        throw new AssetLoadException(mapPath, "GameMap", 
                            "Invalid tile ID at (" + col + ", " + row + "): " + tokens[col]);
                    }
                }
            }
            
            logger.info("Loaded map: " + name + " (" + width + "x" + height + ")");
            
        } catch (Exception e) {
            if (e instanceof AssetLoadException) {
                throw e;
            }
            throw new AssetLoadException(mapPath, "GameMap", e);
        }
    }
    
    /**
     * Get tile ID at specified coordinates.
     */
    public int getTileAt(int x, int y) {
        if (isValidCoordinate(x, y)) {
            return tileData[x][y];
        }
        return -1; // Invalid tile
    }
    
    /**
     * Set tile ID at specified coordinates.
     */
    public void setTileAt(int x, int y, int tileId) {
        if (isValidCoordinate(x, y)) {
            tileData[x][y] = tileId;
        }
    }
    
    /**
     * Check if coordinates are within map bounds.
     */
    public boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
    
    /**
     * Check if a tile at coordinates is collidable.
     */
    public boolean isCollidableAt(int x, int y) {
        if (!isValidCoordinate(x, y) || tileSet == null) {
            return true; // Treat out-of-bounds as collidable
        }
        
        int tileId = getTileAt(x, y);
        return tileSet.isCollidable(tileId);
    }
    
    /**
     * Get map width in tiles.
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Get map height in tiles.
     */
    public int getHeight() {
        return height;
    }
    
    /**
     * Get map name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get map file path.
     */
    public String getMapPath() {
        return mapPath;
    }
    
    /**
     * Set the tileset for this map.
     */
    public void setTileSet(TileSet tileSet) {
        this.tileSet = tileSet;
    }
    
    /**
     * Get the tileset for this map.
     */
    public TileSet getTileSet() {
        return tileSet;
    }
    
    /**
     * Get a copy of the tile data array.
     */
    public int[][] getTileData() {
        int[][] copy = new int[width][height];
        for (int x = 0; x < width; x++) {
            System.arraycopy(tileData[x], 0, copy[x], 0, height);
        }
        return copy;
    }
    
    /**
     * Add a map layer.
     */
    public void addLayer(MapLayer layer) {
        layers.add(layer);
    }
    
    /**
     * Get map layers.
     */
    public List<MapLayer> getLayers() {
        return new ArrayList<>(layers);
    }
    
    /**
     * Get tiles in a rectangular region.
     */
    public int[][] getTilesInRegion(int startX, int startY, int regionWidth, int regionHeight) {
        int[][] region = new int[regionWidth][regionHeight];
        
        for (int x = 0; x < regionWidth; x++) {
            for (int y = 0; y < regionHeight; y++) {
                int mapX = startX + x;
                int mapY = startY + y;
                region[x][y] = isValidCoordinate(mapX, mapY) ? getTileAt(mapX, mapY) : -1;
            }
        }
        
        return region;
    }
    
    /**
     * Validate map integrity.
     */
    public boolean validateMap() {
        if (tileData == null || width <= 0 || height <= 0) {
            return false;
        }
        
        // Check for consistent dimensions
        if (tileData.length != width) {
            return false;
        }
        
        for (int x = 0; x < width; x++) {
            if (tileData[x] == null || tileData[x].length != height) {
                return false;
            }
        }
        
        return true;
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
     * Map layer for multi-layer support.
     */
    public static class MapLayer {
        private final String name;
        private final int[][] layerData;
        private final int width;
        private final int height;
        private boolean visible = true;
        private float opacity = 1.0f;
        
        public MapLayer(String name, int width, int height) {
            this.name = name;
            this.width = width;
            this.height = height;
            this.layerData = new int[width][height];
        }
        
        public String getName() {
            return name;
        }
        
        public int getTileAt(int x, int y) {
            if (x >= 0 && x < width && y >= 0 && y < height) {
                return layerData[x][y];
            }
            return -1;
        }
        
        public void setTileAt(int x, int y, int tileId) {
            if (x >= 0 && x < width && y >= 0 && y < height) {
                layerData[x][y] = tileId;
            }
        }
        
        public boolean isVisible() {
            return visible;
        }
        
        public void setVisible(boolean visible) {
            this.visible = visible;
        }
        
        public float getOpacity() {
            return opacity;
        }
        
        public void setOpacity(float opacity) {
            this.opacity = Math.max(0.0f, Math.min(1.0f, opacity));
        }
        
        public int getWidth() {
            return width;
        }
        
        public int getHeight() {
            return height;
        }
    }
}