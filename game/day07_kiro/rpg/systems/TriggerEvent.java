package rpg.systems;

/**
 * Event fired when an entity enters, stays in, or exits a trigger zone
 */
public class TriggerEvent extends GameEvent {
    private final int triggerEntity;
    private final int otherEntity;
    private final TriggerType type;
    
    public enum TriggerType {
        ENTER,  // Entity entered trigger zone
        STAY,   // Entity staying in trigger zone
        EXIT    // Entity exited trigger zone
    }
    
    public TriggerEvent(int triggerEntity, int otherEntity, TriggerType type) {
        super();
        this.triggerEntity = triggerEntity;
        this.otherEntity = otherEntity;
        this.type = type;
    }
    
    public int getTriggerEntity() { return triggerEntity; }
    public int getOtherEntity() { return otherEntity; }
    public TriggerType getType() { return type; }
    
    @Override
    public String toString() {
        return String.format("TriggerEvent{trigger=%d, other=%d, type=%s}", 
            triggerEntity, otherEntity, type);
    }
}