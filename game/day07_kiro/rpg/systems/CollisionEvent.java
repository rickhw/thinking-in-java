package rpg.systems;

/**
 * Event fired when entities collide
 */
public class CollisionEvent extends GameEvent {
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
    
    @Override
    public String toString() {
        return String.format("CollisionEvent{entityA=%d, entityB=%d, type=%s}", 
            entityA, entityB, type);
    }
}