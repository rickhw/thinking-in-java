package rpg.utils;

import rpg.exceptions.AssetLoadException;
import rpg.exceptions.ConfigurationException;
import rpg.exceptions.GameException;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Color;
import java.util.function.Supplier;

/**
 * Utility class for error recovery and graceful degradation strategies.
 * Provides fallback mechanisms when game systems encounter errors.
 */
public class ErrorRecovery {
    
    /**
     * Execute a risky operation with automatic error recovery.
     */
    public static <T> T executeWithRecovery(Supplier<T> operation, Supplier<T> fallback, String operationName) {
        try {
            return operation.get();
        } catch (Exception e) {
            GameLogger.warn("Operation '" + operationName + "' failed, using fallback", e);
            try {
                return fallback.get();
            } catch (Exception fallbackException) {
                GameLogger.error("Fallback for '" + operationName + "' also failed", fallbackException);
                throw new RuntimeException("Both operation and fallback failed for: " + operationName, fallbackException);
            }
        }
    }
    
    /**
     * Execute a risky operation with automatic error recovery, returning null on failure.
     */
    public static <T> T executeWithRecoveryOrNull(Supplier<T> operation, String operationName) {
        try {
            return operation.get();
        } catch (Exception e) {
            GameLogger.warn("Operation '" + operationName + "' failed, returning null", e);
            return null;
        }
    }
    
    /**
     * Create a placeholder image when asset loading fails.
     */
    public static BufferedImage createPlaceholderImage(int width, int height, String text) {
        BufferedImage placeholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = placeholder.createGraphics();
        
        // Fill with magenta background to make it obvious it's a placeholder
        g2.setColor(Color.MAGENTA);
        g2.fillRect(0, 0, width, height);
        
        // Draw border
        g2.setColor(Color.BLACK);
        g2.drawRect(0, 0, width - 1, height - 1);
        
        // Draw text if provided
        if (text != null && !text.isEmpty()) {
            g2.setColor(Color.WHITE);
            int textX = Math.max(5, (width - text.length() * 6) / 2);
            int textY = height / 2;
            g2.drawString(text, textX, textY);
        }
        
        g2.dispose();
        return placeholder;
    }
    
    /**
     * Handle asset loading exceptions with appropriate recovery.
     */
    public static BufferedImage handleAssetLoadException(AssetLoadException e, int fallbackWidth, int fallbackHeight) {
        GameLogger.error("Asset loading failed", e);
        
        // Create a placeholder image
        String placeholderText = "MISSING: " + e.getAssetType();
        return createPlaceholderImage(fallbackWidth, fallbackHeight, placeholderText);
    }
    
    /**
     * Handle configuration exceptions with appropriate recovery.
     */
    public static void handleConfigurationException(ConfigurationException e) {
        GameLogger.error("Configuration error", e);
        
        if (e.isRecoverable()) {
            GameLogger.info("Attempting to recover from configuration error by using defaults");
            // Configuration manager should handle fallback to defaults
        } else {
            GameLogger.error("Configuration error is not recoverable, game may not function properly");
        }
    }
    
    /**
     * Handle general game exceptions with appropriate logging and recovery.
     */
    public static void handleGameException(GameException e, String context) {
        if (e.isRecoverable()) {
            GameLogger.warn("Recoverable game exception in " + context, e);
        } else {
            GameLogger.error("Non-recoverable game exception in " + context, e);
        }
    }
    
    /**
     * Safely execute a void operation with error logging.
     */
    public static void safeExecute(Runnable operation, String operationName) {
        try {
            operation.run();
        } catch (Exception e) {
            GameLogger.error("Failed to execute operation: " + operationName, e);
        }
    }
    
    /**
     * Validate that a value is within acceptable bounds, providing a fallback if not.
     */
    public static int validateIntRange(int value, int min, int max, int fallback, String valueName) {
        if (value < min || value > max) {
            GameLogger.warn("Value '" + valueName + "' (" + value + ") is out of range [" + min + ", " + max + "]. Using fallback: " + fallback);
            return fallback;
        }
        return value;
    }
    
    /**
     * Validate that a value is within acceptable bounds, providing a fallback if not.
     */
    public static float validateFloatRange(float value, float min, float max, float fallback, String valueName) {
        if (value < min || value > max || Float.isNaN(value) || Float.isInfinite(value)) {
            GameLogger.warn("Value '" + valueName + "' (" + value + ") is out of range [" + min + ", " + max + "] or invalid. Using fallback: " + fallback);
            return fallback;
        }
        return value;
    }
}