package rpg.utils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

/**
 * Custom logging system for the game with different log levels and file output.
 * Provides centralized logging with proper formatting and error handling.
 */
public class GameLogger {
    private static final Logger logger = Logger.getLogger("GameLogger");
    private static boolean initialized = false;
    
    public enum LogLevel {
        DEBUG(Level.FINE),
        INFO(Level.INFO),
        WARN(Level.WARNING),
        ERROR(Level.SEVERE);
        
        private final Level javaLevel;
        
        LogLevel(Level javaLevel) {
            this.javaLevel = javaLevel;
        }
        
        public Level getJavaLevel() {
            return javaLevel;
        }
    }
    
    /**
     * Initialize the logging system.
     */
    public static void initialize() {
        if (initialized) {
            return;
        }
        
        try {
            // Remove default handlers
            Logger rootLogger = Logger.getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                rootLogger.removeHandler(handler);
            }
            
            // Create console handler with custom formatter
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new GameLogFormatter());
            consoleHandler.setLevel(Level.ALL);
            logger.addHandler(consoleHandler);
            
            // Create file handler for persistent logging
            try {
                FileHandler fileHandler = new FileHandler("game.log", true);
                fileHandler.setFormatter(new GameLogFormatter());
                fileHandler.setLevel(Level.ALL);
                logger.addHandler(fileHandler);
            } catch (IOException e) {
                logger.warning("Could not create log file handler: " + e.getMessage());
            }
            
            // Set logger level
            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(false);
            
            initialized = true;
            info("Logging system initialized");
            
        } catch (Exception e) {
            System.err.println("Failed to initialize logging system: " + e.getMessage());
        }
    }
    
    /**
     * Log a debug message.
     */
    public static void debug(String message) {
        if (initialized) {
            logger.fine("[DEBUG] " + message);
        }
    }
    
    /**
     * Log an info message.
     */
    public static void info(String message) {
        if (initialized) {
            logger.info("[INFO] " + message);
        }
    }
    
    /**
     * Log a warning message.
     */
    public static void warn(String message) {
        if (initialized) {
            logger.warning("[WARN] " + message);
        }
    }
    
    /**
     * Log a warning message with exception.
     */
    public static void warn(String message, Throwable throwable) {
        if (initialized) {
            logger.log(Level.WARNING, "[WARN] " + message, throwable);
        }
    }
    
    /**
     * Log an error message.
     */
    public static void error(String message) {
        if (initialized) {
            logger.severe("[ERROR] " + message);
        }
    }
    
    /**
     * Log an error message with exception.
     */
    public static void error(String message, Throwable throwable) {
        if (initialized) {
            logger.log(Level.SEVERE, "[ERROR] " + message, throwable);
        }
    }
    
    /**
     * Set the logging level.
     */
    public static void setLogLevel(LogLevel level) {
        if (initialized) {
            logger.setLevel(level.getJavaLevel());
            for (Handler handler : logger.getHandlers()) {
                handler.setLevel(level.getJavaLevel());
            }
        }
    }
    
    /**
     * Custom formatter for game logs.
     */
    private static class GameLogFormatter extends Formatter {
        private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();
            
            // Add timestamp
            sb.append(LocalDateTime.now().format(timeFormatter));
            sb.append(" ");
            
            // Add message
            sb.append(record.getMessage());
            sb.append(System.lineSeparator());
            
            // Add exception if present
            if (record.getThrown() != null) {
                Throwable throwable = record.getThrown();
                sb.append("Exception: ").append(throwable.getClass().getSimpleName());
                sb.append(": ").append(throwable.getMessage());
                sb.append(System.lineSeparator());
                
                // Add stack trace for severe errors
                if (record.getLevel() == Level.SEVERE) {
                    for (StackTraceElement element : throwable.getStackTrace()) {
                        sb.append("    at ").append(element.toString());
                        sb.append(System.lineSeparator());
                    }
                }
            }
            
            return sb.toString();
        }
    }
}