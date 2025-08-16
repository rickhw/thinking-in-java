package rpg.assets;

import rpg.exceptions.AssetLoadException;
import rpg.utils.GameLogger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for loading different types of assets with proper error handling.
 * Provides static methods for common asset loading operations.
 */
public class AssetLoader {
    private static final GameLogger logger = GameLogger.getInstance();
    
    // Supported image formats
    private static final String[] SUPPORTED_IMAGE_FORMATS = {".png", ".jpg", ".jpeg", ".gif", ".bmp"};
    
    // Asset type validators
    private static final Map<String, AssetValidator> validators = new HashMap<>();
    
    static {
        // Initialize validators
        validators.put("image", AssetLoader::validateImageAsset);
        validators.put("map", AssetLoader::validateMapAsset);
    }
    
    /**
     * Load an image asset with validation and error handling.
     */
    public static BufferedImage loadImage(String path) throws AssetLoadException {
        validatePath(path);
        
        if (!isSupportedImageFormat(path)) {
            throw new AssetLoadException(path, "BufferedImage", "Unsupported image format");
        }
        
        try {
            InputStream stream = AssetLoader.class.getResourceAsStream(path);
            if (stream == null) {
                throw new AssetLoadException(path, "BufferedImage", "Resource not found");
            }
            
            BufferedImage image = ImageIO.read(stream);
            stream.close();
            
            if (image == null) {
                throw new AssetLoadException(path, "BufferedImage", "Failed to decode image");
            }
            
            // Validate loaded image
            validateImageAsset(image, path);
            
            logger.debug("Successfully loaded image: " + path + " (" + 
                        image.getWidth() + "x" + image.getHeight() + ")");
            
            return image;
            
        } catch (Exception e) {
            if (e instanceof AssetLoadException) {
                throw e;
            }
            throw new AssetLoadException(path, "BufferedImage", e);
        }
    }
    
    /**
     * Load a sprite sheet with frame extraction.
     */
    public static SpriteSheet loadSpriteSheet(String path, int spriteWidth, int spriteHeight) 
            throws AssetLoadException {
        BufferedImage sheetImage = loadImage(path);
        
        // Validate sprite dimensions
        if (spriteWidth <= 0 || spriteHeight <= 0) {
            throw new AssetLoadException(path, "SpriteSheet", "Invalid sprite dimensions");
        }
        
        if (sheetImage.getWidth() % spriteWidth != 0 || sheetImage.getHeight() % spriteHeight != 0) {
            logger.warn("Sprite sheet dimensions don't align perfectly with sprite size: " + path);
        }
        
        String name = extractNameFromPath(path);
        return new SpriteSheet(name, sheetImage, spriteWidth, spriteHeight);
    }
    
    /**
     * Load a texture atlas from an image.
     */
    public static TextureAtlas loadTextureAtlas(String path) throws AssetLoadException {
        BufferedImage atlasImage = loadImage(path);
        String name = extractNameFromPath(path);
        return new TextureAtlas(name, atlasImage);
    }
    
    /**
     * Load a texture atlas from a sprite sheet with grid layout.
     */
    public static TextureAtlas loadTextureAtlasFromGrid(String path, int spriteWidth, int spriteHeight) 
            throws AssetLoadException {
        BufferedImage sheetImage = loadImage(path);
        String name = extractNameFromPath(path);
        return TextureAtlas.createFromSpriteSheet(name, sheetImage, spriteWidth, spriteHeight);
    }
    
    /**
     * Load a map file as text content.
     */
    public static String loadMapText(String path) throws AssetLoadException {
        validatePath(path);
        
        try {
            InputStream stream = AssetLoader.class.getResourceAsStream(path);
            if (stream == null) {
                throw new AssetLoadException(path, "Map", "Resource not found");
            }
            
            StringBuilder content = new StringBuilder();
            byte[] buffer = new byte[1024];
            int bytesRead;
            
            while ((bytesRead = stream.read(buffer)) != -1) {
                content.append(new String(buffer, 0, bytesRead));
            }
            stream.close();
            
            String mapContent = content.toString();
            validateMapAsset(mapContent, path);
            
            logger.debug("Successfully loaded map text: " + path);
            return mapContent;
            
        } catch (Exception e) {
            if (e instanceof AssetLoadException) {
                throw e;
            }
            throw new AssetLoadException(path, "Map", e);
        }
    }
    
    /**
     * Check if a resource exists.
     */
    public static boolean resourceExists(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }
        
        InputStream stream = AssetLoader.class.getResourceAsStream(path);
        if (stream != null) {
            try {
                stream.close();
                return true;
            } catch (Exception e) {
                logger.warn("Error checking resource existence: " + path, e);
            }
        }
        return false;
    }
    
    /**
     * Get resource size in bytes.
     */
    public static long getResourceSize(String path) {
        try {
            InputStream stream = AssetLoader.class.getResourceAsStream(path);
            if (stream == null) {
                return -1;
            }
            
            long size = 0;
            byte[] buffer = new byte[1024];
            int bytesRead;
            
            while ((bytesRead = stream.read(buffer)) != -1) {
                size += bytesRead;
            }
            stream.close();
            
            return size;
            
        } catch (Exception e) {
            logger.warn("Error getting resource size: " + path, e);
            return -1;
        }
    }
    
    /**
     * Validate asset path.
     */
    private static void validatePath(String path) throws AssetLoadException {
        if (path == null || path.trim().isEmpty()) {
            throw new AssetLoadException(path, "Unknown", "Asset path cannot be null or empty");
        }
        
        if (!path.startsWith("/")) {
            throw new AssetLoadException(path, "Unknown", "Asset path must start with '/'");
        }
    }
    
    /**
     * Check if image format is supported.
     */
    private static boolean isSupportedImageFormat(String path) {
        String lowerPath = path.toLowerCase();
        for (String format : SUPPORTED_IMAGE_FORMATS) {
            if (lowerPath.endsWith(format)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Validate loaded image asset.
     */
    private static void validateImageAsset(Object asset, String path) throws AssetLoadException {
        BufferedImage image = (BufferedImage) asset;
        
        if (image.getWidth() <= 0 || image.getHeight() <= 0) {
            throw new AssetLoadException(path, "BufferedImage", "Invalid image dimensions");
        }
        
        if (image.getWidth() > 4096 || image.getHeight() > 4096) {
            logger.warn("Large image loaded: " + path + " (" + 
                       image.getWidth() + "x" + image.getHeight() + ")");
        }
    }
    
    /**
     * Validate loaded map asset.
     */
    private static void validateMapAsset(Object asset, String path) throws AssetLoadException {
        String mapContent = (String) asset;
        
        if (mapContent.trim().isEmpty()) {
            throw new AssetLoadException(path, "Map", "Map file is empty");
        }
        
        // Basic validation - check for numeric content
        String[] lines = mapContent.split("\n");
        boolean hasValidData = false;
        
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty()) {
                String[] tokens = line.split("\\s+");
                for (String token : tokens) {
                    try {
                        Integer.parseInt(token);
                        hasValidData = true;
                        break;
                    } catch (NumberFormatException e) {
                        // Continue checking other tokens
                    }
                }
                if (hasValidData) break;
            }
        }
        
        if (!hasValidData) {
            throw new AssetLoadException(path, "Map", "Map file contains no valid tile data");
        }
    }
    
    /**
     * Extract filename from path.
     */
    private static String extractNameFromPath(String path) {
        if (path == null) return "unknown";
        
        String filename = path.substring(path.lastIndexOf('/') + 1);
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(0, dotIndex) : filename;
    }
    
    /**
     * Asset validator interface.
     */
    @FunctionalInterface
    private interface AssetValidator {
        void validate(Object asset, String path) throws AssetLoadException;
    }
}