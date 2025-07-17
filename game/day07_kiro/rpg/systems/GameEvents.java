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

/**
 * Interface for event listeners
 */
@FunctionalInterface
public interface EventListener<T extends GameEvent> {
    void onEvent(T event);
}

/**
 * Event fired when entities collide
 */
class CollisionEvent extends GameEvent {
    private final int entityA;
    private final int entityB;
    private final CollisionType type;
    
    public enum CollisionType {
        ENTER,  // Collision started
        STAY,   // Collision continuing
        EXIT    // Collision ended
    }
    
    public CollisionEvent(int entityA, int entityB, CollisionType type) {
        super();
        this.entityA = entityA;
        this.entityB = entityB;
        this.type = type;
    }
    
    public int getEntityA() { return entityA; }
    public int getEntityB() { return entityB; }
    public CollisionType getType() { return type; }
}

/**
 * Event fired when input actions occur
 */
class InputEvent extends GameEvent {
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
}

/**
 * Event fired when entities are created or destroyed
 */
class EntityEvent extends GameEvent {
    private final int entityId;
    private final EntityEventType type;
    
    public enum EntityEventType {
        CREATED,
        DESTROYED,
        COMPONENT_ADDED,
        COMPONENT_REMOVED
    }
    
    public EntityEvent(int entityId, EntityEventType type) {
        super();
        this.entityId = entityId;
        this.type = type;
    }
    
    public int getEntityId() { return entityId; }
    public EntityEventType getType() { return type; }
}

/**
 * Event fired when game state changes
 */
class GameStateEvent extends GameEvent {
    private final String previousState;
    private final String newState;
    
    public GameStateEvent(String previousState, String newState) {
        super();
        this.previousState = previousState;
        this.newState = newState;
    }
    
    public String getPreviousState() { return previousState; }
    public String getNewState() { return newState; }
}

/**
 * Event fired when animations complete
 */
class AnimationEvent extends GameEvent {
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
}

/**
 * Event fired for movement-related notifications
 */
class MovementEvent extends GameEvent {
    private final int entityId;
    private final float oldX, oldY;
    private final float newX, newY;
    
    public MovementEvent(int entityId, float oldX, float oldY, float newX, float newY) {
        super();
        this.entityId = entityId;
        this.oldX = oldX;
        this.oldY = oldY;
        this.newX = newX;
        this.newY = newY;
    }
    
    public int getEntityId() { return entityId; }
    public float getOldX() { return oldX; }
    public float getOldY() { return oldY; }
    public float getNewX() { return newX; }
    public float getNewY() { return newY; }
    
    public float getDistanceMoved() {
        float dx = newX - oldX;
        float dy = newY - oldY;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}