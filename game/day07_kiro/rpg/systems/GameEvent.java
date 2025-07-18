package rpg.systems;

/**
 * Base class for all game events
 */
public abstract class GameEvent {
    private final long timestamp;
    private boolean consumed = false;
    
    public GameEvent() {
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Get the timestamp when this event was created
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Mark this event as consumed (prevents further processing)
     */
    public void consume() {
        this.consumed = true;
    }
    
    /**
     * Check if this event has been consumed
     */
    public boolean isConsumed() {
        return consumed;
    }
}