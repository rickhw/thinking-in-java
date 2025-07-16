package rpg.exceptions;

/**
 * Exception thrown when configuration loading or validation fails.
 */
public class ConfigurationException extends GameException {
    private final String configKey;
    private final String configValue;
    
    public ConfigurationException(String message) {
        super(message, "CONFIG_ERROR");
        this.configKey = null;
        this.configValue = null;
    }
    
    public ConfigurationException(String configKey, String configValue, String message) {
        super("Configuration error for key '" + configKey + "' with value '" + configValue + "': " + message, "CONFIG_VALIDATION_FAILED");
        this.configKey = configKey;
        this.configValue = configValue;
    }
    
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause, "CONFIG_ERROR", true);
        this.configKey = null;
        this.configValue = null;
    }
    
    /**
     * Get the configuration key that caused the error.
     */
    public String getConfigKey() {
        return configKey;
    }
    
    /**
     * Get the configuration value that caused the error.
     */
    public String getConfigValue() {
        return configValue;
    }
}