package rpg.assets;

import rpg.exceptions.AssetLoadException;
import rpg.utils.GameLogger;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Texture atlas for efficient sprite storage and retrieval.
 * Combines multiple sprites into a single texture for better performance.
 */
public class TextureAtlas {
    private static final GameLogger logger = GameLogger.getInstance();
    
    private final String name;
    private final BufferedImage atlasImage;
    private final Map<String, AtlasRegion> regions = new HashMap<>();
    private final int textureWidth;
    private final int textureHeight;
    
    /**
     * Create a texture atlas from an image.
     */
    public TextureAtlas(String name, BufferedImage atlasImage) {
        this.name = name;
        this.atlasImage = atlasImage;
        this.textureWidth = atlasImage.getWidth();
        this.textureHeight = atlasImage.getHeight();
    }
    
    /**
     * Add a region to the atlas.
     */
    public void addRegion(String regionName, int x, int y, int width, int height) {
        if (x < 0 || y < 0 || x + width > textureWidth || y + height > textureHeight) {
            throw new IllegalArgumentException("Region bounds exceed atlas dimensions");
        }
        
        AtlasRegion region = new AtlasRegion(regionName, x, y, width, height);
        regions.put(regionName, region);
        
        logger.debug("Added atlas region: " + regionName + " at (" + x + ", " + y + 
                    ") size (" + width + "x" + height + ")");
    }
    
    /**
     * Get a sprite from the atlas by region name.
     */
    public BufferedImage getSprite(String regionName) {
        AtlasRegion region = regions.get(regionName);
        if (region == null) {
            logger.warn("Atlas region not found: " + regionName);
            return null;
        }
        
        return atlasImage.getSubimage(region.x, region.y, region.width, region.height);
    }
    
    /**
     * Get a sprite from the atlas by coordinates.
     */
    public BufferedImage getSprite(int x, int y, int width, int height) {
        if (x < 0 || y < 0 || x + width > textureWidth || y + height > textureHeight) {
            logger.warn("Invalid sprite coordinates: (" + x + ", " + y + 
                       ") size (" + width + "x" + height + ")");
            return null;
        }
        
        return atlasImage.getSubimage(x, y, width, height);
    }
    
    /**
     * Check if a region exists in the atlas.
     */
    public boolean hasRegion(String regionName) {
        return regions.containsKey(regionName);
    }
    
    /**
     * Get all region names.
     */
    public String[] getRegionNames() {
        return regions.keySet().toArray(new String[0]);
    }
    
    /**
     * Get atlas name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get atlas dimensions.
     */
    public int getWidth() {
        return textureWidth;
    }
    
    public int getHeight() {
        return textureHeight;
    }
    
    /**
     * Get the full atlas image.
     */
    public BufferedImage getAtlasImage() {
        return atlasImage;
    }
    
    /**
     * Get region information.
     */
    public AtlasRegion getRegion(String regionName) {
        return regions.get(regionName);
    }
    
    /**
     * Create a grid-based atlas from a sprite sheet.
     */
    public static TextureAtlas createFromSpriteSheet(String name, BufferedImage spriteSheet, 
                                                    int spriteWidth, int spriteHeight) {
        TextureAtlas atlas = new TextureAtlas(name, spriteSheet);
        
        int cols = spriteSheet.getWidth() / spriteWidth;
        int rows = spriteSheet.getHeight() / spriteHeight;
        
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                String regionName = name + "_" + row + "_" + col;
                int x = col * spriteWidth;
                int y = row * spriteHeight;
                
                atlas.addRegion(regionName, x, y, spriteWidth, spriteHeight);
            }
        }
        
        logger.info("Created sprite sheet atlas: " + name + " with " + 
                   (rows * cols) + " sprites (" + cols + "x" + rows + ")");
        
        return atlas;
    }
    
    /**
     * Atlas region information.
     */
    public static class AtlasRegion {
        public final String name;
        public final int x;
        public final int y;
        public final int width;
        public final int height;
        
        public AtlasRegion(String name, int x, int y, int width, int height) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
        @Override
        public String toString() {
            return "AtlasRegion{" +
                   "name='" + name + '\'' +
                   ", x=" + x +
                   ", y=" + y +
                   ", width=" + width +
                   ", height=" + height +
                   '}';
        }
    }
}