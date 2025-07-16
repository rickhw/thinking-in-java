package rpg.utils;

import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Configuration management system with validation and fallback mechanisms.
 * Supports loading from external files with default value fallbacks.
 */
public class ConfigManager {
    private static final Logger logger = Logger.getLogger(ConfigManager.class.getName());
    
    private final Properties config;
    private final Properties defaults;
    private String configFilePath;
    
    public ConfigManager() {
        this.config = new Properties();
        this.defaults = new Properties();
        initializeDefaults();
    }
    
    /**
     * Initialize default configuration values.
     */
    private void initializeDefaults() {
        // Display settings
        defaults.setProperty("display.width", "768");
        defaults.setProperty("display.height", "576");
        defaults.setProperty("display.title", "2D RPG Game");
        defaults.setProperty("display.fullscreen", "false");
        defaults.setProperty("display.vsync", "true");
        
        // Game settings
        defaults.setProperty("game.targetFPS", "60");
        defaults.setProperty("game.tileSize", "48");
        defaults.setProperty("game.maxWorldCol", "50");
        defaults.setProperty("game.maxWorldRow", "50");
        
        // Input settings
        defaults.setProperty("input.moveUp", "W");
        defaults.setProperty("input.moveDown", "S");
        defaults.setProperty("input.moveLeft", "A");
        defaults.setProperty("input.moveRight", "D");
        defaults.setProperty("input.interact", "E");
        defaults.setProperty("input.pause", "ESCAPE");
        
        // Audio settings
        defaults.setProperty("audio.masterVolume", "1.0");
        defaults.setProperty("audio.musicVolume", "0.8");
        defaults.setProperty("audio.sfxVolume", "1.0");
        defaults.setProperty("audio.enabled", "true");
        
        // Debug settings
        defaults.setProperty("debug.showFPS", "false");
        defaults.setProperty("debug.showCollisionBounds", "false");
        defaults.setProperty("debug.logLevel", "INFO");
    }
    
    /**
     * Load configuration from a file.
     */
    public boolean loadFromFile(String filePath) {
        this.configFilePath = filePath;
        File configFile = new File(filePath);
        
        if (!configFile.exists()) {
            logger.info("Configuration file not found: " + filePath + ". Using defaults.");
            config.putAll(defaults);
            return saveToFile(filePath); // Create default config file
        }
        
        try (InputStream input = new FileInputStream(configFile)) {
            config.load(input);
            validateConfiguration();
            logger.info("Configuration loaded from: " + filePath);
            return true;
        } catch (IOException e) {
            logger.severe("Failed to load configuration from: " + filePath + ". Error: " + e.getMessage());
            config.putAll(defaults);
            return false;
        }
    }
    
    /**
     * Save configuration to a file.
     */
    public boolean saveToFile(String filePath) {
        try (OutputStream output = new FileOutputStream(filePath)) {
            config.store(output, "Game Configuration");
            logger.info("Configuration saved to: " + filePath);
            return true;
        } catch (IOException e) {
            logger.severe("Failed to save configuration to: " + filePath + ". Error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get a string value from configuration.
     */
    public String getString(String key) {
        return getString(key, defaults.getProperty(key, ""));
    }
    
    /**
     * Get a string value with a custom default.
     */
    public String getString(String key, String defaultValue) {
        return config.getProperty(key, defaultValue);
    }
    
    /**
     * Get an integer value from configuration.
     */
    public int getInt(String key) {
        return getInt(key, Integer.parseInt(defaults.getProperty(key, "0")));
    }
    
    /**
     * Get an integer value with a custom default.
     */
    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(config.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            logger.warning("Invalid integer value for key '" + key + "': " + config.getProperty(key) + 
                          ". Using default: " + defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * Get a float value from configuration.
     */
    public float getFloat(String key) {
        return getFloat(key, Float.parseFloat(defaults.getProperty(key, "0.0")));
    }
    
    /**
     * Get a float value with a custom default.
     */
    public float getFloat(String key, float defaultValue) {
        try {
            return Float.parseFloat(config.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            logger.warning("Invalid float value for key '" + key + "': " + config.getProperty(key) + 
                          ". Using default: " + defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * Get a boolean value from configuration.
     */
    public boolean getBoolean(String key) {
        return getBoolean(key, Boolean.parseBoolean(defaults.getProperty(key, "false")));
    }
    
    /**
     * Get a boolean value with a custom default.
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = config.getProperty(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }
    
    /**
     * Set a configuration value.
     */
    public void setValue(String key, Object value) {
        config.setProperty(key, String.valueOf(value));
    }
    
    /**
     * Check if a configuration key exists.
     */
    public boolean hasKey(String key) {
        return config.containsKey(key) || defaults.containsKey(key);
    }
    
    /**
     * Validate configuration values and fix invalid ones.
     */
    private void validateConfiguration() {
        // Validate display settings
        validateIntRange("display.width", 320, 3840);
        validateIntRange("display.height", 240, 2160);
        validateIntRange("game.targetFPS", 30, 144);
        validateIntRange("game.tileSize", 16, 128);
        
        // Validate audio settings
        validateFloatRange("audio.masterVolume", 0.0f, 1.0f);
        validateFloatRange("audio.musicVolume", 0.0f, 1.0f);
        validateFloatRange("audio.sfxVolume", 0.0f, 1.0f);
        
        // Add missing keys from defaults
        for (String key : defaults.stringPropertyNames()) {
            if (!config.containsKey(key)) {
                config.setProperty(key, defaults.getProperty(key));
                logger.info("Added missing configuration key: " + key + " = " + defaults.getProperty(key));
            }
        }
    }
    
    /**
     * Validate that an integer value is within a specified range.
     */
    private void validateIntRange(String key, int min, int max) {
        int value = getInt(key, Integer.parseInt(defaults.getProperty(key, "0")));
        if (value < min || value > max) {
            String defaultValue = defaults.getProperty(key);
            config.setProperty(key, defaultValue);
            logger.warning("Invalid value for '" + key + "': " + value + 
                          ". Must be between " + min + " and " + max + ". Reset to default: " + defaultValue);
        }
    }
    
    /**
     * Validate that a float value is within a specified range.
     */
    private void validateFloatRange(String key, float min, float max) {
        float value = getFloat(key, Float.parseFloat(defaults.getProperty(key, "0.0")));
        if (value < min || value > max) {
            String defaultValue = defaults.getProperty(key);
            config.setProperty(key, defaultValue);
            logger.warning("Invalid value for '" + key + "': " + value + 
                          ". Must be between " + min + " and " + max + ". Reset to default: " + defaultValue);
        }
    }
    
    /**
     * Reset all configuration to defaults.
     */
    public void resetToDefaults() {
        config.clear();
        config.putAll(defaults);
        logger.info("Configuration reset to defaults");
    }
    
    /**
     * Save current configuration to the loaded file.
     */
    public boolean save() {
        if (configFilePath != null) {
            return saveToFile(configFilePath);
        }
        return false;
    }
}