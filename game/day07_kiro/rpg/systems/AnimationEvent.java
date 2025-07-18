package rpg.systems;

/**
 * Event fired when animations complete
 */
public class AnimationEvent extends GameEvent {
    private final int entityId;
    private final String animationName;
    private final AnimationEventType type;
    
    public enum AnimationEventType {
        STARTED,
        COMPLETED,
        LOOPED
    }
    
    public AnimationEvent(int entityId, String animationName, AnimationEventType type) {
        super();
        this.entityId = entityId;
        this.animationName = animationName;
        this.type = type;
    }
    
    public int getEntityId() { return entityId; }
    public String getAnimationName() { return animationName; }
    public AnimationEventType getType() { return type; }
    
    @Override
    public String toString() {
        return String.format("AnimationEvent{entityId=%d, animationName='%s', type=%s}", 
            entityId, animationName, type);
    }
}