package rpg.systems;

import rpg.Config;
import rpg.engine.Entity;
import rpg.components.TransformComponent;
import java.awt.Rectangle;

/**
 * System for managing the game camera within the ECS architecture.
 * Handles camera updates, following, and viewport management.
 */
public class CameraSystem extends GameSystem {
    private Camera camera;
    private Entity followTarget;
    
    @Override
    public void initialize() {
        // Initialize camera with screen dimensions
        this.camera = new Camera(Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
        
        // Set up world boundaries based on world size
        Rectangle worldBounds = new Rectangle(0, 0, Config.WORLD_WIDTH, Config.WORLD_HEIGHT);
        camera.setWorldBounds(worldBounds);
        camera.setBoundaryEnabled(true);
        
        // Configure camera settings
        camera.setFollowSpeed(5.0f);
        camera.setFollowDeadZone(32.0f);
    }
    
    @Override
    public void update(float deltaTime) {
        if (!isEnabled()) return;
        
        // Update camera
        camera.update(deltaTime);
        
        // Publish camera events if needed
        if (eventBus != null) {
            CameraEvent event = new CameraEvent(camera.getX(), camera.getY(), camera.getZoom());
            eventBus.publish(event);
        }
    }
    
    @Override
    public void cleanup() {
        followTarget = null;
    }
    
    @Override
    public int getPriority() {
        return 100; // Update before render system
    }
    
    /**
     * Get the camera instance
     */
    public Camera getCamera() {
        return camera;
    }
    
    /**
     * Set the entity for the camera to follow
     */
    public void setFollowTarget(Entity target) {
        this.followTarget = target;
        camera.followTarget(target);
    }
    
    /**
     * Get the current follow target
     */
    public Entity getFollowTarget() {
        return followTarget;
    }
    
    /**
     * Set camera position directly
     */
    public void setCameraPosition(float x, float y) {
        camera.setPosition(x, y);
    }
    
    /**
     * Start camera shake effect
     */
    public void shakeCamera(float intensity, float duration) {
        camera.shake(intensity, duration);
    }
    
    /**
     * Transition camera to target position
     */
    public void transitionTo(float x, float y, float duration) {
        camera.transitionTo(x, y, duration);
    }
    
    /**
     * Set camera zoom level
     */
    public void setZoom(float zoom) {
        camera.setZoom(zoom);
    }
    
    /**
     * Get current camera zoom
     */
    public float getZoom() {
        return camera.getZoom();
    }
    
    /**
     * Update viewport size (e.g., when window is resized)
     */
    public void updateViewportSize(int width, int height) {
        camera.setViewportSize(width, height);
    }
    
    /**
     * Check if a world position is visible
     */
    public boolean isVisible(float worldX, float worldY) {
        return camera.isVisible(worldX, worldY);
    }
    
    /**
     * Check if a rectangle is visible
     */
    public boolean isVisible(Rectangle worldRect) {
        return camera.isVisible(worldRect);
    }
}