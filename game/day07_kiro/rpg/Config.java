package rpg;

import rpg.utils.ConfigManager;

/**
 * Centralized configuration class that provides easy access to game settings.
 * This class acts as a facade over the ConfigManager for type-safe configuration access.
 */
public class Config {
    private static ConfigManager configManager;
    
    // Display settings
    public static final int SCREEN_WIDTH;
    public static final int SCREEN_HEIGHT;
    public static final String WINDOW_TITLE;
    public static final boolean FULLSCREEN;
    public static final boolean VSYNC;
    
    // Game settings
    public static final int TARGET_FPS;
    public static final int TILE_SIZE;
    public static final int MAX_WORLD_COL;
    public static final int MAX_WORLD_ROW;
    public static final int WORLD_WIDTH;
    public static final int WORLD_HEIGHT;
    
    // Input settings
    public static final String MOVE_UP_KEY;
    public static final String MOVE_DOWN_KEY;
    public static final String MOVE_LEFT_KEY;
    public static final String MOVE_RIGHT_KEY;
    public static final String INTERACT_KEY;
    public static final String PAUSE_KEY;
    
    // Audio settings
    public static final float MASTER_VOLUME;
    public static final float MUSIC_VOLUME;
    public static final float SFX_VOLUME;
    public static final boolean AUDIO_ENABLED;
    
    // Debug settings
    public static final boolean SHOW_FPS;
    public static final boolean SHOW_COLLISION_BOUNDS;
    public static final String LOG_LEVEL;
    
    static {
        // Initialize configuration manager
        configManager = new ConfigManager();
        configManager.loadFromFile("config.properties");
        
        // Load display settings
        SCREEN_WIDTH = configManager.getInt("display.width");
        SCREEN_HEIGHT = configManager.getInt("display.height");
        WINDOW_TITLE = configManager.getString("display.title");
        FULLSCREEN = configManager.getBoolean("display.fullscreen");
        VSYNC = configManager.getBoolean("display.vsync");
        
        // Load game settings
        TARGET_FPS = configManager.getInt("game.targetFPS");
        TILE_SIZE = configManager.getInt("game.tileSize");
        MAX_WORLD_COL = configManager.getInt("game.maxWorldCol");
        MAX_WORLD_ROW = configManager.getInt("game.maxWorldRow");
        WORLD_WIDTH = TILE_SIZE * MAX_WORLD_COL;
        WORLD_HEIGHT = TILE_SIZE * MAX_WORLD_ROW;
        
        // Load input settings
        MOVE_UP_KEY = configManager.getString("input.moveUp");
        MOVE_DOWN_KEY = configManager.getString("input.moveDown");
        MOVE_LEFT_KEY = configManager.getString("input.moveLeft");
        MOVE_RIGHT_KEY = configManager.getString("input.moveRight");
        INTERACT_KEY = configManager.getString("input.interact");
        PAUSE_KEY = configManager.getString("input.pause");
        
        // Load audio settings
        MASTER_VOLUME = configManager.getFloat("audio.masterVolume");
        MUSIC_VOLUME = configManager.getFloat("audio.musicVolume");
        SFX_VOLUME = configManager.getFloat("audio.sfxVolume");
        AUDIO_ENABLED = configManager.getBoolean("audio.enabled");
        
        // Load debug settings
        SHOW_FPS = configManager.getBoolean("debug.showFPS");
        SHOW_COLLISION_BOUNDS = configManager.getBoolean("debug.showCollisionBounds");
        LOG_LEVEL = configManager.getString("debug.logLevel");
    }
    
    /**
     * Get the underlying configuration manager for dynamic access.
     */
    public static ConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * Save current configuration to file.
     */
    public static boolean saveConfig() {
        return configManager.save();
    }
    
    /**
     * Reload configuration from file.
     */
    public static void reloadConfig() {
        configManager.loadFromFile("config.properties");
    }
}