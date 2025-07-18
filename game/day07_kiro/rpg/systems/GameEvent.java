package rpg.systems;

/**
 * Base class for all game events.
 * Game events are used for communication between systems in a decoupled way.
 * Events can be published to the EventBus and received by any system that has
 * subscribed to that event type.
 */
public abstract class GameEvent {
    private final long timestamp;
    private boolean consumed = false;
    private int priority = 0;
    private String source = null;
    
    /**
     * Create a new game event with the current timestamp
     */
    public GameEvent() {
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Get the timestamp when this event was created
     * 
     * @return The timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Mark this event as consumed (prevents further processing)
     * Once an event is consumed, it will not be delivered to any more listeners
     */
    public void consume() {
        this.consumed = true;
    }
    
    /**
     * Check if this event has been consumed
     * 
     * @return True if the event has been consumed, false otherwise
     */
    public boolean isConsumed() {
        return consumed;
    }
    
    /**
     * Set the priority of this event
     * Higher priority events are processed before lower priority events
     * 
     * @param priority The priority value (default is 0)
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    /**
     * Get the priority of this event
     * 
     * @return The priority value
     */
    public int getPriority() {
        return priority;
    }
    
    /**
     * Set the source of this event (e.g., the system that published it)
     * 
     * @param source A string identifying the source of the event
     */
    public void setSource(String source) {
        this.source = source;
    }
    
    /**
     * Get the source of this event
     * 
     * @return The source identifier, or null if not set
     */
    public String getSource() {
        return source;
    }
    
    /**
     * Get the age of this event in milliseconds
     * 
     * @return The time elapsed since this event was created
     */
    public long getAge() {
        return System.currentTimeMillis() - timestamp;
    }
    
    /**
     * Check if this event is older than the specified time
     * 
     * @param milliseconds The time threshold in milliseconds
     * @return True if the event is older than the specified time
     */
    public boolean isOlderThan(long milliseconds) {
        return getAge() > milliseconds;
    }
}