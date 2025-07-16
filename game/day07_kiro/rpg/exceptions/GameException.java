package rpg.exceptions;

/**
 * Base exception class for all game-related exceptions.
 * Provides a common base for game-specific error handling.
 */
public class GameException extends Exception {
    private final String errorCode;
    private final boolean recoverable;
    
    public GameException(String message) {
        this(message, null, null, true);
    }
    
    public GameException(String message, Throwable cause) {
        this(message, cause, null, true);
    }
    
    public GameException(String message, String errorCode) {
        this(message, null, errorCode, true);
    }
    
    public GameException(String message, Throwable cause, String errorCode, boolean recoverable) {
        super(message, cause);
        this.errorCode = errorCode;
        this.recoverable = recoverable;
    }
    
    /**
     * Get the error code associated with this exception.
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Check if this exception represents a recoverable error.
     */
    public boolean isRecoverable() {
        return recoverable;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        if (errorCode != null) {
            sb.append(" [").append(errorCode).append("]");
        }
        sb.append(": ").append(getMessage());
        return sb.toString();
    }
}