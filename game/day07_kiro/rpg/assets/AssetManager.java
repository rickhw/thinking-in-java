package rpg.assets;

import rpg.exceptions.AssetLoadException;
import rpg.utils.GameLogger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.ArrayList;

/**
 * Centralized asset management system with caching, preloading, and lifecycle management.
 * Supports type-safe asset loading with automatic memory management and fallback assets.
 */
public class AssetManager {
    
    // Asset caches with weak references for automatic cleanup
    private final Map<String, WeakReference<BufferedImage>> imageCache = new ConcurrentHashMap<>();
    private final Map<String, WeakReference<TileSet>> tileSetCache = new ConcurrentHashMap<>();
    private final Map<String, WeakReference<GameMap>> mapCache = new ConcurrentHashMap<>();
    
    // Strong references for preloaded assets
    private final Map<String, BufferedImage> preloadedImages = new HashMap<>();
    private final Map<String, TileSet> preloadedTileSets = new HashMap<>();
    
    // Fallback assets
    private BufferedImage fallbackImage;
    private TileSet fallbackTileSet;
    
    // Asset loading statistics
    private int totalAssetsLoaded = 0;
    private int cacheHits = 0;
    private int cacheMisses = 0;
    
    // Async loading support
    private final ExecutorService loadingExecutor = Executors.newFixedThreadPool(2);
    
    // Singleton instance
    private static AssetManager instance;
    
    private AssetManager() {
        initializeFallbackAssets();
    }
    
    /**
     * Get the singleton instance of AssetManager.
     */
    public static synchronized AssetManager getInstance() {
        if (instance == null) {
            instance = new AssetManager();
        }
        return instance;
    }
    
    /**
     * Load an asset with type safety and caching.
     */
    @SuppressWarnings("unchecked")
    public <T> T loadAsset(String path, Class<T> assetType) throws AssetLoadException {
        if (path == null || path.trim().isEmpty()) {
            throw new AssetLoadException(path, assetType.getSimpleName(), "Asset path cannot be null or empty");
        }
        
        try {
            if (assetType == BufferedImage.class) {
                return (T) loadImage(path);
            } else if (assetType == TileSet.class) {
                return (T) loadTileSet(path);
            } else if (assetType == GameMap.class) {
                return (T) loadMap(path);
            } else {
                throw new AssetLoadException(path, assetType.getSimpleName(), "Unsupported asset type");
            }
        } catch (Exception e) {
            GameLogger.error("Failed to load asset: " + path, e);
            return getFallbackAsset(assetType);
        }
    }
    
    /**
     * Load an image with caching support.
     */
    public BufferedImage loadImage(String path) throws AssetLoadException {
        // Check preloaded assets first
        BufferedImage preloaded = preloadedImages.get(path);
        if (preloaded != null) {
            cacheHits++;
            return preloaded;
        }
        
        // Check weak reference cache
        WeakReference<BufferedImage> ref = imageCache.get(path);
        if (ref != null) {
            BufferedImage cached = ref.get();
            if (cached != null) {
                cacheHits++;
                return cached;
            } else {
                // Reference was garbage collected, remove it
                imageCache.remove(path);
            }
        }
        
        cacheMisses++;
        
        try {
            InputStream stream = getClass().getResourceAsStream(path);
            if (stream == null) {
                throw new AssetLoadException(path, "BufferedImage", "Resource not found");
            }
            
            BufferedImage image = ImageIO.read(stream);
            if (image == null) {
                throw new AssetLoadException(path, "BufferedImage", "Failed to decode image");
            }
            
            // Cache the loaded image
            imageCache.put(path, new WeakReference<>(image));
            totalAssetsLoaded++;
            
            GameLogger.info("Loaded image: " + path);
            return image;
            
        } catch (IOException e) {
            throw new AssetLoadException(path, "BufferedImage", e);
        }
    }
    
    /**
     * Load a tileset with caching support.
     */
    public TileSet loadTileSet(String path) throws AssetLoadException {
        // Check preloaded assets first
        TileSet preloaded = preloadedTileSets.get(path);
        if (preloaded != null) {
            cacheHits++;
            return preloaded;
        }
        
        // Check weak reference cache
        WeakReference<TileSet> ref = tileSetCache.get(path);
        if (ref != null) {
            TileSet cached = ref.get();
            if (cached != null) {
                cacheHits++;
                return cached;
            } else {
                tileSetCache.remove(path);
            }
        }
        
        cacheMisses++;
        
        try {
            TileSet tileSet = new TileSet(path, this);
            tileSetCache.put(path, new WeakReference<>(tileSet));
            totalAssetsLoaded++;
            
            GameLogger.info("Loaded tileset: " + path);
            return tileSet;
            
        } catch (Exception e) {
            throw new AssetLoadException(path, "TileSet", e);
        }
    }
    
    /**
     * Load a game map with caching support.
     */
    public GameMap loadMap(String path) throws AssetLoadException {
        // Check weak reference cache
        WeakReference<GameMap> ref = mapCache.get(path);
        if (ref != null) {
            GameMap cached = ref.get();
            if (cached != null) {
                cacheHits++;
                return cached;
            } else {
                mapCache.remove(path);
            }
        }
        
        cacheMisses++;
        
        try {
            GameMap map = new GameMap(path, this);
            mapCache.put(path, new WeakReference<>(map));
            totalAssetsLoaded++;
            
            GameLogger.info("Loaded map: " + path);
            return map;
            
        } catch (Exception e) {
            throw new AssetLoadException(path, "GameMap", e);
        }
    }
    
    /**
     * Preload essential assets to prevent loading delays during gameplay.
     */
    public void preloadAssets(List<String> imagePaths, List<String> tileSetPaths) {
        GameLogger.info("Starting asset preloading...");
        
        for (String path : imagePaths) {
            try {
                BufferedImage image = loadImage(path);
                preloadedImages.put(path, image);
                GameLogger.debug("Preloaded image: " + path);
            } catch (AssetLoadException e) {
                GameLogger.warn("Failed to preload image: " + path, e);
            }
        }
        
        for (String path : tileSetPaths) {
            try {
                TileSet tileSet = loadTileSet(path);
                preloadedTileSets.put(path, tileSet);
                GameLogger.debug("Preloaded tileset: " + path);
            } catch (AssetLoadException e) {
                GameLogger.warn("Failed to preload tileset: " + path, e);
            }
        }
        
        GameLogger.info("Asset preloading completed. Images: " + preloadedImages.size() + 
                   ", TileSets: " + preloadedTileSets.size());
    }
    
    /**
     * Asynchronously preload assets.
     */
    public CompletableFuture<Void> preloadAssetsAsync(List<String> imagePaths, List<String> tileSetPaths) {
        return CompletableFuture.runAsync(() -> preloadAssets(imagePaths, tileSetPaths), loadingExecutor);
    }
    
    /**
     * Unload a specific asset from cache.
     */
    public void unloadAsset(String path) {
        imageCache.remove(path);
        tileSetCache.remove(path);
        mapCache.remove(path);
        preloadedImages.remove(path);
        preloadedTileSets.remove(path);
        
        GameLogger.debug("Unloaded asset: " + path);
    }
    
    /**
     * Clear all cached assets and force garbage collection.
     */
    public void cleanup() {
        imageCache.clear();
        tileSetCache.clear();
        mapCache.clear();
        preloadedImages.clear();
        preloadedTileSets.clear();
        
        // Suggest garbage collection
        System.gc();
        
        GameLogger.info("Asset manager cleanup completed");
    }
    
    /**
     * Get fallback asset for unsupported or failed loads.
     */
    @SuppressWarnings("unchecked")
    private <T> T getFallbackAsset(Class<T> assetType) {
        if (assetType == BufferedImage.class) {
            return (T) fallbackImage;
        } else if (assetType == TileSet.class) {
            return (T) fallbackTileSet;
        }
        return null;
    }
    
    /**
     * Initialize fallback assets for error recovery.
     */
    private void initializeFallbackAssets() {
        // Create a simple fallback image (magenta square)
        fallbackImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 32; x++) {
            for (int y = 0; y < 32; y++) {
                fallbackImage.setRGB(x, y, 0xFF00FF); // Magenta
            }
        }
        
        // Create fallback tileset
        try {
            fallbackTileSet = new TileSet();
            fallbackTileSet.addTile(0, fallbackImage, false);
        } catch (Exception e) {
            GameLogger.error("Failed to create fallback tileset", e);
        }
        
        GameLogger.debug("Fallback assets initialized");
    }
    
    /**
     * Get asset loading statistics.
     */
    public AssetStats getStats() {
        return new AssetStats(totalAssetsLoaded, cacheHits, cacheMisses, 
                             imageCache.size(), preloadedImages.size());
    }
    
    /**
     * Check if an asset exists in the resources.
     */
    public boolean assetExists(String path) {
        InputStream stream = getClass().getResourceAsStream(path);
        if (stream != null) {
            try {
                stream.close();
                return true;
            } catch (IOException e) {
                GameLogger.warn("Error checking asset existence: " + path, e);
            }
        }
        return false;
    }
    
    /**
     * Shutdown the asset manager and cleanup resources.
     */
    public void shutdown() {
        cleanup();
        loadingExecutor.shutdown();
        GameLogger.info("Asset manager shutdown completed");
    }
    
    /**
     * Asset loading statistics.
     */
    public static class AssetStats {
        public final int totalLoaded;
        public final int cacheHits;
        public final int cacheMisses;
        public final int cachedAssets;
        public final int preloadedAssets;
        
        public AssetStats(int totalLoaded, int cacheHits, int cacheMisses, 
                         int cachedAssets, int preloadedAssets) {
            this.totalLoaded = totalLoaded;
            this.cacheHits = cacheHits;
            this.cacheMisses = cacheMisses;
            this.cachedAssets = cachedAssets;
            this.preloadedAssets = preloadedAssets;
        }
        
        public double getCacheHitRatio() {
            int total = cacheHits + cacheMisses;
            return total > 0 ? (double) cacheHits / total : 0.0;
        }
    }
}