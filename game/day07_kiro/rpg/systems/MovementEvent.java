package rpg.systems;

/**
 * Event fired for movement-related notifications
 */
public class MovementEvent extends GameEvent {
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
    
    @Override
    public String toString() {
        return String.format("MovementEvent{entityId=%d, from=(%.1f,%.1f), to=(%.1f,%.1f), distance=%.1f}", 
            entityId, oldX, oldY, newX, newY, getDistanceMoved());
    }
}