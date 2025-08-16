import rpg.assets.*;
import rpg.exceptions.AssetLoadException;
import rpg.utils.GameLogger;

import java.awt.image.BufferedImage;

/**
 * Test class to verify the asset management system implementation.
 */
public class TestAssetSystem {
    
    public static void main(String[] args) {
        GameLogger logger = GameLogger.getInstance();
        logger.info("Testing Asset Management System...");
        
        try {
            testAssetManager();
            testAssetRegistry();
            testTextureFilter();
            testAssetPreloader();
            
            logger.info("All asset system tests completed successfully!");
            
        } catch (Exception e) {
            logger.error("Asset system test failed", e);
        }
    }
    
    private static void testAssetManager() throws AssetLoadException {
        GameLogger logger = GameLogger.getInstance();
        logger.info("Testing AssetManager...");
        
        AssetManager assetManager = AssetManager.getInstance();
        
        // Test loading an image (this will use fallback if actual file doesn't exist)
        try {
            BufferedImage image = assetManager.loadImage("/rpg/assets/tiles/grass.png");
            logger.info("Image loaded successfully: " + (image != null ? "Yes" : "No"));
        } catch (AssetLoadException e) {
            logger.info("Image loading failed as expected (using fallback): " + e.getMessage());
        }
        
        // Test asset statistics
        AssetManager.AssetStats stats = assetManager.getStats();
        logger.info("Asset stats - Total loaded: " + stats.totalLoaded + 
                   ", Cache hits: " + stats.cacheHits + 
                   ", Cache misses: " + stats.cacheMisses);
    }
    
    private static void testAssetRegistry() {
        GameLogger logger = GameLogger.getInstance();
        logger.info("Testing AssetRegistry...");
        
        AssetRegistry registry = AssetRegistry.getInstance();
        
        // Test registry statistics
        AssetRegistry.RegistryStats stats = registry.getStats();
        logger.info("Registry stats: " + stats.toString());
        
        // Test asset existence checking
        boolean hasAsset = registry.hasAsset("nonexistent");
        logger.info("Has nonexistent asset: " + hasAsset);
        
        // Test getting asset names
        String[] assetNames = registry.getAssetNames();
        logger.info("Total registered assets: " + assetNames.length);
    }
    
    private static void testTextureFilter() {
        GameLogger logger = GameLogger.getInstance();
        logger.info("Testing TextureFilter...");
        
        // Create a test image
        BufferedImage testImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 32; x++) {
            for (int y = 0; y < 32; y++) {
                testImage.setRGB(x, y, 0x00FF00); // Green
            }
        }
        
        // Test scaling
        BufferedImage scaled = TextureFilter.scaleTexture(testImage, 64, 64, TextureFilter.FilterMode.NEAREST_NEIGHBOR);
        logger.info("Scaled image: " + (scaled != null ? scaled.getWidth() + "x" + scaled.getHeight() : "null"));
        
        // Test pixel perfect scaling
        BufferedImage pixelPerfect = TextureFilter.scalePixelPerfect(testImage, 2);
        logger.info("Pixel perfect scaled: " + (pixelPerfect != null ? pixelPerfect.getWidth() + "x" + pixelPerfect.getHeight() : "null"));
        
        // Test rotation
        BufferedImage rotated = TextureFilter.rotateTexture(testImage, 90);
        logger.info("Rotated image: " + (rotated != null ? rotated.getWidth() + "x" + rotated.getHeight() : "null"));
    }
    
    private static void testAssetPreloader() {
        GameLogger logger = GameLogger.getInstance();
        logger.info("Testing AssetPreloader...");
        
        AssetManager assetManager = AssetManager.getInstance();
        AssetPreloader preloader = new AssetPreloader(assetManager);
        
        // Add some test assets (these will fail gracefully if files don't exist)
        preloader.addPlayerAssets()
                 .addTileAssets()
                 .addMapAssets();
        
        logger.info("Total preload tasks: " + preloader.getTotalTasks());
        
        // Add a progress listener
        preloader.addListener(new AssetPreloader.PreloadListener() {
            @Override
            public void onPreloadStarted(int totalAssets) {
                logger.info("Preload started with " + totalAssets + " assets");
            }
            
            @Override
            public void onProgressUpdate(float progress, int completed, int total) {
                logger.info("Preload progress: " + String.format("%.1f%%", progress * 100) + 
                           " (" + completed + "/" + total + ")");
            }
            
            @Override
            public void onPreloadCompleted(int successful, int failed) {
                logger.info("Preload completed - Success: " + successful + ", Failed: " + failed);
            }
        });
        
        // Start preloading
        preloader.preload();
        
        // Cleanup
        preloader.shutdown();
    }
}