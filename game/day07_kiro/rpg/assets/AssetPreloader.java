package rpg.assets;

import rpg.exceptions.AssetLoadException;
import rpg.utils.GameLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Asset preloader for loading essential game assets during initialization.
 * Supports progress tracking and asynchronous loading operations.
 */
public class AssetPreloader {
    private static final GameLogger logger = GameLogger.getInstance();
    
    private final AssetManager assetManager;
    private final ExecutorService loadingExecutor;
    private final List<PreloadTask> preloadTasks = new ArrayList<>();
    
    // Progress tracking
    private final AtomicInteger completedTasks = new AtomicInteger(0);
    private final AtomicInteger failedTasks = new AtomicInteger(0);
    private volatile boolean isLoading = false;
    private volatile float progress = 0.0f;
    
    // Preload listeners
    private final List<PreloadListener> listeners = new ArrayList<>();
    
    public AssetPreloader(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.loadingExecutor = Executors.newFixedThreadPool(3);
    }
    
    /**
     * Add an image to preload.
     */
    public AssetPreloader addImage(String path) {
        preloadTasks.add(new PreloadTask(path, AssetType.IMAGE, Priority.NORMAL));
        return this;
    }
    
    /**
     * Add an image to preload with priority.
     */
    public AssetPreloader addImage(String path, Priority priority) {
        preloadTasks.add(new PreloadTask(path, AssetType.IMAGE, priority));
        return this;
    }
    
    /**
     * Add a tileset to preload.
     */
    public AssetPreloader addTileSet(String path) {
        preloadTasks.add(new PreloadTask(path, AssetType.TILESET, Priority.NORMAL));
        return this;
    }
    
    /**
     * Add a tileset to preload with priority.
     */
    public AssetPreloader addTileSet(String path, Priority priority) {
        preloadTasks.add(new PreloadTask(path, AssetType.TILESET, priority));
        return this;
    }
    
    /**
     * Add a map to preload.
     */
    public AssetPreloader addMap(String path) {
        preloadTasks.add(new PreloadTask(path, AssetType.MAP, Priority.NORMAL));
        return this;
    }
    
    /**
     * Add essential player assets.
     */
    public AssetPreloader addPlayerAssets() {
        addImage("/rpg/assets/player/boy_down_1.png", Priority.HIGH);
        addImage("/rpg/assets/player/boy_down_2.png", Priority.HIGH);
        addImage("/rpg/assets/player/boy_up_1.png", Priority.HIGH);
        addImage("/rpg/assets/player/boy_up_2.png", Priority.HIGH);
        addImage("/rpg/assets/player/boy_left_1.png", Priority.HIGH);
        addImage("/rpg/assets/player/boy_left_2.png", Priority.HIGH);
        addImage("/rpg/assets/player/boy_right_1.png", Priority.HIGH);
        addImage("/rpg/assets/player/boy_right_2.png", Priority.HIGH);
        return this;
    }
    
    /**
     * Add essential tile assets.
     */
    public AssetPreloader addTileAssets() {
        addImage("/rpg/assets/tiles/grass.png", Priority.HIGH);
        addImage("/rpg/assets/tiles/wall.png", Priority.HIGH);
        addImage("/rpg/assets/tiles/water.png", Priority.HIGH);
        addImage("/rpg/assets/tiles/earth.png", Priority.NORMAL);
        addImage("/rpg/assets/tiles/tree.png", Priority.NORMAL);
        addImage("/rpg/assets/tiles/sand.png", Priority.NORMAL);
        return this;
    }
    
    /**
     * Add essential map assets.
     */
    public AssetPreloader addMapAssets() {
        addMap("/rpg/assets/maps/world01.txt");
        return this;
    }
    
    /**
     * Start preloading assets synchronously.
     */
    public void preload() {
        if (isLoading) {
            logger.warn("Preloading already in progress");
            return;
        }
        
        isLoading = true;
        progress = 0.0f;
        completedTasks.set(0);
        failedTasks.set(0);
        
        logger.info("Starting asset preloading: " + preloadTasks.size() + " assets");
        notifyPreloadStarted();
        
        // Sort tasks by priority
        preloadTasks.sort((a, b) -> b.priority.ordinal() - a.priority.ordinal());
        
        for (int i = 0; i < preloadTasks.size(); i++) {
            PreloadTask task = preloadTasks.get(i);
            
            try {
                loadAsset(task);
                completedTasks.incrementAndGet();
                
            } catch (Exception e) {
                logger.error("Failed to preload asset: " + task.path, e);
                failedTasks.incrementAndGet();
            }
            
            // Update progress
            progress = (float) (i + 1) / preloadTasks.size();
            notifyProgressUpdate(progress);
        }
        
        isLoading = false;
        
        logger.info("Asset preloading completed. Success: " + completedTasks.get() + 
                   ", Failed: " + failedTasks.get());
        notifyPreloadCompleted();
    }
    
    /**
     * Start preloading assets asynchronously.
     */
    public CompletableFuture<Void> preloadAsync() {
        return CompletableFuture.runAsync(this::preload, loadingExecutor);
    }
    
    /**
     * Load a single asset based on its type.
     */
    private void loadAsset(PreloadTask task) throws AssetLoadException {
        switch (task.type) {
            case IMAGE:
                assetManager.loadImage(task.path);
                break;
            case TILESET:
                assetManager.loadTileSet(task.path);
                break;
            case MAP:
                assetManager.loadMap(task.path);
                break;
            default:
                throw new AssetLoadException(task.path, task.type.name(), "Unsupported asset type");
        }
        
        logger.debug("Preloaded " + task.type.name().toLowerCase() + ": " + task.path);
    }
    
    /**
     * Get current loading progress (0.0 to 1.0).
     */
    public float getProgress() {
        return progress;
    }
    
    /**
     * Check if preloading is currently in progress.
     */
    public boolean isLoading() {
        return isLoading;
    }
    
    /**
     * Get number of completed tasks.
     */
    public int getCompletedTasks() {
        return completedTasks.get();
    }
    
    /**
     * Get number of failed tasks.
     */
    public int getFailedTasks() {
        return failedTasks.get();
    }
    
    /**
     * Get total number of tasks.
     */
    public int getTotalTasks() {
        return preloadTasks.size();
    }
    
    /**
     * Clear all preload tasks.
     */
    public void clear() {
        if (isLoading) {
            logger.warn("Cannot clear tasks while preloading is in progress");
            return;
        }
        
        preloadTasks.clear();
        completedTasks.set(0);
        failedTasks.set(0);
        progress = 0.0f;
    }
    
    /**
     * Add a preload listener.
     */
    public void addListener(PreloadListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Remove a preload listener.
     */
    public void removeListener(PreloadListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notify listeners that preloading started.
     */
    private void notifyPreloadStarted() {
        for (PreloadListener listener : listeners) {
            try {
                listener.onPreloadStarted(preloadTasks.size());
            } catch (Exception e) {
                logger.error("Error in preload listener", e);
            }
        }
    }
    
    /**
     * Notify listeners of progress update.
     */
    private void notifyProgressUpdate(float progress) {
        for (PreloadListener listener : listeners) {
            try {
                listener.onProgressUpdate(progress, completedTasks.get(), preloadTasks.size());
            } catch (Exception e) {
                logger.error("Error in preload listener", e);
            }
        }
    }
    
    /**
     * Notify listeners that preloading completed.
     */
    private void notifyPreloadCompleted() {
        for (PreloadListener listener : listeners) {
            try {
                listener.onPreloadCompleted(completedTasks.get(), failedTasks.get());
            } catch (Exception e) {
                logger.error("Error in preload listener", e);
            }
        }
    }
    
    /**
     * Shutdown the preloader.
     */
    public void shutdown() {
        loadingExecutor.shutdown();
        clear();
        listeners.clear();
    }
    
    /**
     * Preload task definition.
     */
    private static class PreloadTask {
        final String path;
        final AssetType type;
        final Priority priority;
        
        PreloadTask(String path, AssetType type, Priority priority) {
            this.path = path;
            this.type = type;
            this.priority = priority;
        }
    }
    
    /**
     * Asset types for preloading.
     */
    public enum AssetType {
        IMAGE,
        TILESET,
        MAP,
        SOUND,
        MUSIC
    }
    
    /**
     * Loading priority levels.
     */
    public enum Priority {
        LOW,
        NORMAL,
        HIGH,
        CRITICAL
    }
    
    /**
     * Preload progress listener interface.
     */
    public interface PreloadListener {
        default void onPreloadStarted(int totalAssets) {}
        default void onProgressUpdate(float progress, int completed, int total) {}
        default void onPreloadCompleted(int successful, int failed) {}
    }
}