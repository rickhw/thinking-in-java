package rpg.systems;

/**
 * Event fired when entities are created or destroyed
 */
public class EntityEvent extends GameEvent {
    private final int entityId;
    private final EntityEventType type;
    private final String componentType; // Optional, used for component events
    
    public enum EntityEventType {
        CREATED,
        DESTROYED,
        COMPONENT_ADDED,
        COMPONENT_REMOVED
    }
    
    public EntityEvent(int entityId, EntityEventType type) {
        this(entityId, type, null);
    }
    
    public EntityEvent(int entityId, EntityEventType type, String componentType) {
        super();
        this.entityId = entityId;
        this.type = type;
        this.componentType = componentType;
    }
    
    public int getEntityId() { return entityId; }
    public EntityEventType getType() { return type; }
    public String getComponentType() { return componentType; }
    
    @Override
    public String toString() {
        if (componentType != null) {
            return String.format("EntityEvent{entityId=%d, type=%s, componentType='%s'}", 
                entityId, type, componentType);
        } else {
            return String.format("EntityEvent{entityId=%d, type=%s}", 
                entityId, type);
        }
    }
}