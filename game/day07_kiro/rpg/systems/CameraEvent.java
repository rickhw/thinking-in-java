package rpg.systems;

/**
 * Event fired when camera position or properties change.
 * Used for inter-system communication about camera updates.
 */
public class CameraEvent extends GameEvent {
    private final float cameraX;
    private final float cameraY;
    private final float zoom;
    
    public CameraEvent(float cameraX, float cameraY, float zoom) {
        this.cameraX = cameraX;
        this.cameraY = cameraY;
        this.zoom = zoom;
    }
    
    /**
     * Get camera X position
     */
    public float getCameraX() {
        return cameraX;
    }
    
    /**
     * Get camera Y position
     */
    public float getCameraY() {
        return cameraY;
    }
    
    /**
     * Get camera zoom level
     */
    public float getZoom() {
        return zoom;
    }
    
    @Override
    public String toString() {
        return String.format("CameraEvent{x=%.2f, y=%.2f, zoom=%.2f}", cameraX, cameraY, zoom);
    }
}