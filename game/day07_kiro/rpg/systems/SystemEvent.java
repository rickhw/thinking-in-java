package rpg.systems;

/**
 * Event fired for system lifecycle events
 */
public class SystemEvent extends GameEvent {
    private final String systemName;
    private final SystemEventType type;
    
    /**
     * Types of system events
     */
    public enum SystemEventType {
        // System registration events
        REGISTERED,
        UNREGISTERED,
        
        // System lifecycle events
        INITIALIZED,
        ENABLED,
        DISABLED,
        ERROR,
        
        // System update events
        PRE_UPDATE,
        POST_UPDATE,
        PRE_SYSTEM_UPDATE,
        POST_SYSTEM_UPDATE,
        
        // System cleanup events
        PRE_CLEANUP,
        POST_CLEANUP,
        PRE_SYSTEM_CLEANUP,
        POST_SYSTEM_CLEANUP
    }
    
    /**
     * Create a new system event
     * 
     * @param systemName The name of the system this event relates to
     * @param type The type of system event
     */
    public SystemEvent(String systemName, SystemEventType type) {
        super();
        this.systemName = systemName;
        this.type = type;
        
        // System events are high priority
        setPriority(10);
    }
    
    /**
     * Get the name of the system this event relates to
     * 
     * @return The system name
     */
    public String getSystemName() {
        return systemName;
    }
    
    /**
     * Get the type of system event
     * 
     * @return The event type
     */
    public SystemEventType getType() {
        return type;
    }
    
    @Override
    public String toString() {
        return String.format("SystemEvent{systemName='%s', type=%s}", 
            systemName, type);
    }
}