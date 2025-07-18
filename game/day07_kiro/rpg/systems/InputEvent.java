package rpg.systems;

/**
 * Event fired when input actions occur
 */
public class InputEvent extends GameEvent {
    private final int entityId;
    private final String action;
    private final InputType type;
    
    public enum InputType {
        PRESSED,
        RELEASED,
        HELD
    }
    
    public InputEvent(int entityId, String action, InputType type) {
        super();
        this.entityId = entityId;
        this.action = action;
        this.type = type;
    }
    
    public int getEntityId() { return entityId; }
    public String getAction() { return action; }
    public InputType getType() { return type; }
    
    @Override
    public String toString() {
        return String.format("InputEvent{entityId=%d, action='%s', type=%s}", 
            entityId, action, type);
    }
}