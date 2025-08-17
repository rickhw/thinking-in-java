package rpg.assets;

import rpg.exceptions.AssetLoadException;
import rpg.utils.GameLogger;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enhanced GameMap class with support for multiple layers, validation, and efficient tile access.
 * Supports loading from various map formats and provides streaming capabilities for large worlds.
 */
public class GameMap {
    
    /**
     * Represents a single layer in the map (background, foreground, collision, etc.)
     */
    public static class MapLayer {
        private final String name;
        private final int[][] tileData;
        private final int width;
        private final int height;
        private final boolean visible;
        private final float opacity;
        private final Map<String, Object> properties;
        
        public MapLayer(String name, int width, int height) {
            this(name, width, height, true, 1.0f, new HashMap<>());
        }
        
        public MapLayer(String name, int width, int height, boolean visible, float opacity, Map<String, Object> properties) {
            this.name = name;
            this.width = width;
            this.height = height;
            this.visible = visible;
            this.opacity = opacity;
            this.properties = new HashMap<>(properties);
            this.tileData = new int[width][height];
        }
        
        public void setTile(int x, int y, int tileId) {
            if (isValidCoordinate(x, y)) {
                tileData[x][y] = tileId;
            }
        }
        
        public int getTile(int x, int y) {
            if (isValidCoordinate(x, y)) {
                return tileData[x][y];
            }
            return -1; // Invalid tile
        }
        
        public boolean isValidCoordinate(int x, int y) {
            return x >= 0 && x < width && y >= 0 && y < height;
        }
        
        // Getters
        public String getName() { return name; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public boolean isVisible() { return visible; }
        public float getOpacity() { return opacity; }
        public Map<String, Object> getProperties() { return new HashMap<>(properties); }
        public int[][] getTileData() { return tileData; }
    }
    
    private final String name;
    private final int width;
    private final int height;
    private final int tileSize;
    private final List<MapLayer> layers;
    private final Map<String, MapLayer> layerMap;
    private final TileSet tileSet;
    private final Map<String, Object> properties;
    private final Rectangle bounds;
    
    // Animation tracking
    private float animationTime;
    
    public GameMap(String name, int width, int height, int tileSize, TileSet tileSet) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.tileSize = tileSize;
        this.tileSet = tileSet;
        this.layers = new ArrayList<>();
        this.layerMap = new HashMap<>();
        this.properties = new HashMap<>();
        this.bounds = new Rectangle(0, 0, width * tileSize, height * tileSize);
        this.animationTime = 0.0f;
    }
    
    /**
     * Add a layer to the map.
     */
    public void addLayer(MapLayer layer) {
        if (layer.getWidth() != width || layer.getHeight() != height) {
            throw new IllegalArgumentException("Layer dimensions must match map dimensions");
        }
        
        layers.add(layer);
        layerMap.put(layer.getName(), layer);
    }
    
    /**
     * Get a layer by name.
     */
    public MapLayer getLayer(String layerName) {
        return layerMap.get(layerName);
    }
    
    /**
     * Get tile ID at coordinates from a specific layer.
     */
    public int getTile(String layerName, int x, int y) {
        MapLayer layer = layerMap.get(layerName);
        return layer != null ? layer.getTile(x, y) : -1;
    }
    
    /**
     * Get tile ID at coordinates from the first layer (for backward compatibility).
     */
    public int getTile(int x, int y) {
        if (!layers.isEmpty()) {
            return layers.get(0).getTile(x, y);
        }
        return -1;
    }
    
    /**
     * Set tile ID at coordinates in a specific layer.
     */
    public void setTile(String layerName, int x, int y, int tileId) {
        MapLayer layer = layerMap.get(layerName);
        if (layer != null) {
            layer.setTile(x, y, tileId);
        }
    }
    
    /**
     * Check if a tile is collidable at the given coordinates.
     */
    public boolean isCollidable(int x, int y) {
        // Check all layers for collision
        for (MapLayer layer : layers) {
            int tileId = layer.getTile(x, y);
            if (tileId >= 0 && tileSet.isCollidable(tileId)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get tile properties at coordinates.
     */
    public TileSet.TileProperties getTileProperties(int x, int y) {
        // Return properties from the first non-empty tile found
        for (MapLayer layer : layers) {
            int tileId = layer.getTile(x, y);
            if (tileId >= 0) {
                TileSet.TileProperties props = tileSet.getTileProperties(tileId);
                if (props != null) {
                    return props;
                }
            }
        }
        return null;
    }
    
    /**
     * Update animation time for animated tiles.
     */
    public void update(float deltaTime) {
        animationTime += deltaTime;
    }
    
    /**
     * Render the map with camera culling.
     */
    public void render(Graphics2D g2, Rectangle viewBounds) {
        // Calculate visible tile range
        int startX = Math.max(0, viewBounds.x / tileSize);
        int endX = Math.min(width - 1, (viewBounds.x + viewBounds.width) / tileSize + 1);
        int startY = Math.max(0, viewBounds.y / tileSize);
        int endY = Math.min(height - 1, (viewBounds.y + viewBounds.height) / tileSize + 1);
        
        // Render each visible layer
        for (MapLayer layer : layers) {
            if (!layer.isVisible()) continue;
            
            // Set layer opacity
            Composite originalComposite = g2.getComposite();
            if (layer.getOpacity() < 1.0f) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, layer.getOpacity()));
            }
            
            // Render visible tiles in this layer
            for (int x = startX; x <= endX; x++) {
                for (int y = startY; y <= endY; y++) {
                    int tileId = layer.getTile(x, y);
                    if (tileId >= 0) {
                        TileSet.Tile tile = tileSet.getTile(tileId);
                        if (tile != null) {
                            int screenX = x * tileSize - viewBounds.x;
                            int screenY = y * tileSize - viewBounds.y;
                            
                            // Get current frame for animated tiles
                            java.awt.image.BufferedImage image = tile.getCurrentFrame(animationTime);
                            g2.drawImage(image, screenX, screenY, tileSize, tileSize, null);
                        }
                    }
                }
            }
            
            // Restore original composite
            g2.setComposite(originalComposite);
        }
    }
    
    /**
     * Validate map integrity.
     */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        
        // Check dimensions
        if (width <= 0 || height <= 0) {
            errors.add("Invalid map dimensions: " + width + "x" + height);
        }
        
        if (tileSize <= 0) {
            errors.add("Invalid tile size: " + tileSize);
        }
        
        // Check layers
        if (layers.isEmpty()) {
            errors.add("Map has no layers");
        }
        
        // Validate each layer
        for (MapLayer layer : layers) {
            if (layer.getWidth() != width || layer.getHeight() != height) {
                errors.add("Layer '" + layer.getName() + "' has incorrect dimensions");
            }
            
            // Check for invalid tile IDs
            for (int x = 0; x < layer.getWidth(); x++) {
                for (int y = 0; y < layer.getHeight(); y++) {
                    int tileId = layer.getTile(x, y);
                    if (tileId >= 0 && !tileSet.hasTile(tileId)) {
                        errors.add("Layer '" + layer.getName() + "' contains invalid tile ID: " + tileId + " at (" + x + "," + y + ")");
                    }
                }
            }
        }
        
        return errors;
    }
    
    /**
     * Load map from simple text format (backward compatibility).
     */
    public static GameMap loadFromTextFile(String mapPath, TileSet tileSet) throws AssetLoadException {
        return MapLoaderSimple.loadTextMap(mapPath, tileSet);
    }
    
    // Getters
    public String getName() { return name; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getTileSize() { return tileSize; }
    public List<MapLayer> getLayers() { return new ArrayList<>(layers); }
    public TileSet getTileSet() { return tileSet; }
    public Map<String, Object> getProperties() { return new HashMap<>(properties); }
    public Rectangle getBounds() { return new Rectangle(bounds); }
    public float getAnimationTime() { return animationTime; }
    
    /**
     * Get world coordinates bounds.
     */
    public int getWorldWidth() { return width * tileSize; }
    public int getWorldHeight() { return height * tileSize; }
    
    /**
     * Convert world coordinates to tile coordinates.
     */
    public Point worldToTile(int worldX, int worldY) {
        return new Point(worldX / tileSize, worldY / tileSize);
    }
    
    /**
     * Convert tile coordinates to world coordinates.
     */
    public Point tileToWorld(int tileX, int tileY) {
        return new Point(tileX * tileSize, tileY * tileSize);
    }
}