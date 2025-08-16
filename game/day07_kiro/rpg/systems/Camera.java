package rpg.systems;

import rpg.engine.Entity;
import rpg.components.TransformComponent;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

/**
 * Camera class for managing viewport and world-to-screen coordinate conversion.
 * Supports smooth following, boundaries, camera shake, and transition effects.
 */
public class Camera {
    // Camera position in world coordinates
    private float x;
    private float y;
    
    // Target position for smooth following
    private float targetX;
    private float targetY;
    
    // Viewport dimensions
    private int viewportWidth;
    private int viewportHeight;
    
    // Camera following settings
    private Entity followTarget;
    private float followSpeed = 5.0f;
    private float followDeadZone = 32.0f;
    
    // Camera boundaries
    private Rectangle worldBounds;
    private boolean boundaryEnabled = false;
    
    // Camera shake effect
    private float shakeIntensity = 0.0f;
    private float shakeDuration = 0.0f;
    private float shakeTimer = 0.0f;
    private float shakeOffsetX = 0.0f;
    private float shakeOffsetY = 0.0f;
    
    // Transition effects
    private boolean inTransition = false;
    private float transitionDuration = 0.0f;
    private float transitionTimer = 0.0f;
    private float transitionStartX;
    private float transitionStartY;
    private float transitionEndX;
    private float transitionEndY;
    
    // Zoom functionality
    private float zoom = 1.0f;
    private float targetZoom = 1.0f;
    private float zoomSpeed = 2.0f;
    
    public Camera(int viewportWidth, int viewportHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.x = 0;
        this.y = 0;
        this.targetX = 0;
        this.targetY = 0;
    }
    
    /**
     * Update camera position and effects
     */
    public void update(float deltaTime) {
        updateTransition(deltaTime);
        updateFollowing(deltaTime);
        updateShake(deltaTime);
        updateZoom(deltaTime);
        applyBoundaries();
    }
    
    /**
     * Set camera position directly
     */
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
    }
    
    /**
     * Get camera X position
     */
    public float getX() {
        return x + shakeOffsetX;
    }
    
    /**
     * Get camera Y position
     */
    public float getY() {
        return y + shakeOffsetY;
    }
    
    /**
     * Get viewport width
     */
    public int getViewportWidth() {
        return viewportWidth;
    }
    
    /**
     * Get viewport height
     */
    public int getViewportHeight() {
        return viewportHeight;
    }
    
    /**
     * Set viewport dimensions
     */
    public void setViewportSize(int width, int height) {
        this.viewportWidth = width;
        this.viewportHeight = height;
    }
    
    /**
     * Set entity for camera to follow
     */
    public void followTarget(Entity target) {
        this.followTarget = target;
        if (target != null) {
            TransformComponent transform = target.getComponent(TransformComponent.class);
            if (transform != null) {
                this.targetX = transform.x;
                this.targetY = transform.y;
            }
        }
    }
    
    /**
     * Set follow speed (higher = faster following)
     */
    public void setFollowSpeed(float speed) {
        this.followSpeed = Math.max(0.1f, speed);
    }
    
    /**
     * Set follow dead zone (area where camera doesn't move)
     */
    public void setFollowDeadZone(float deadZone) {
        this.followDeadZone = Math.max(0, deadZone);
    }
    
    /**
     * Set world boundaries for camera movement
     */
    public void setWorldBounds(Rectangle bounds) {
        this.worldBounds = new Rectangle(bounds);
        this.boundaryEnabled = true;
    }
    
    /**
     * Enable or disable world boundaries
     */
    public void setBoundaryEnabled(boolean enabled) {
        this.boundaryEnabled = enabled;
    }
    
    /**
     * Start camera shake effect
     */
    public void shake(float intensity, float duration) {
        this.shakeIntensity = intensity;
        this.shakeDuration = duration;
        this.shakeTimer = 0.0f;
    }
    
    /**
     * Start camera transition to target position
     */
    public void transitionTo(float targetX, float targetY, float duration) {
        this.transitionStartX = this.x;
        this.transitionStartY = this.y;
        this.transitionEndX = targetX;
        this.transitionEndY = targetY;
        this.transitionDuration = duration;
        this.transitionTimer = 0.0f;
        this.inTransition = true;
    }
    
    /**
     * Set zoom level
     */
    public void setZoom(float zoom) {
        this.targetZoom = Math.max(0.1f, Math.min(5.0f, zoom));
    }
    
    /**
     * Get current zoom level
     */
    public float getZoom() {
        return zoom;
    }
    
    /**
     * Set zoom speed for smooth zooming
     */
    public void setZoomSpeed(float speed) {
        this.zoomSpeed = Math.max(0.1f, speed);
    }
    
    /**
     * Convert world coordinates to screen coordinates
     */
    public Point2D.Float worldToScreen(float worldX, float worldY) {
        float screenX = (worldX - getX()) * zoom + viewportWidth / 2.0f;
        float screenY = (worldY - getY()) * zoom + viewportHeight / 2.0f;
        return new Point2D.Float(screenX, screenY);
    }
    
    /**
     * Convert screen coordinates to world coordinates
     */
    public Point2D.Float screenToWorld(float screenX, float screenY) {
        float worldX = (screenX - viewportWidth / 2.0f) / zoom + getX();
        float worldY = (screenY - viewportHeight / 2.0f) / zoom + getY();
        return new Point2D.Float(worldX, worldY);
    }
    
    /**
     * Get viewport bounds in world coordinates
     */
    public Rectangle getViewBounds() {
        float halfWidth = (viewportWidth / 2.0f) / zoom;
        float halfHeight = (viewportHeight / 2.0f) / zoom;
        
        return new Rectangle(
            (int)(getX() - halfWidth),
            (int)(getY() - halfHeight),
            (int)(halfWidth * 2),
            (int)(halfHeight * 2)
        );
    }
    
    /**
     * Check if a point is visible in the viewport
     */
    public boolean isVisible(float worldX, float worldY) {
        Rectangle bounds = getViewBounds();
        return bounds.contains(worldX, worldY);
    }
    
    /**
     * Check if a rectangle is visible in the viewport
     */
    public boolean isVisible(Rectangle worldRect) {
        Rectangle bounds = getViewBounds();
        return bounds.intersects(worldRect);
    }
    
    private void updateTransition(float deltaTime) {
        if (!inTransition) return;
        
        transitionTimer += deltaTime;
        float progress = Math.min(1.0f, transitionTimer / transitionDuration);
        
        // Use smooth interpolation (ease-in-out)
        progress = smoothStep(progress);
        
        this.x = lerp(transitionStartX, transitionEndX, progress);
        this.y = lerp(transitionStartY, transitionEndY, progress);
        this.targetX = this.x;
        this.targetY = this.y;
        
        if (progress >= 1.0f) {
            inTransition = false;
        }
    }
    
    private void updateFollowing(float deltaTime) {
        if (followTarget == null || inTransition) return;
        
        TransformComponent transform = followTarget.getComponent(TransformComponent.class);
        if (transform == null) return;
        
        // Calculate distance to target
        float dx = transform.x - targetX;
        float dy = transform.y - targetY;
        float distance = (float)Math.sqrt(dx * dx + dy * dy);
        
        // Only move if outside dead zone
        if (distance > followDeadZone) {
            // Smooth following with lerp
            targetX = lerp(targetX, transform.x, followSpeed * deltaTime);
            targetY = lerp(targetY, transform.y, followSpeed * deltaTime);
        }
        
        // Smooth camera movement to target
        this.x = lerp(this.x, targetX, followSpeed * deltaTime);
        this.y = lerp(this.y, targetY, followSpeed * deltaTime);
    }
    
    private void updateShake(float deltaTime) {
        if (shakeDuration <= 0) {
            shakeOffsetX = 0;
            shakeOffsetY = 0;
            return;
        }
        
        shakeTimer += deltaTime;
        if (shakeTimer >= shakeDuration) {
            shakeDuration = 0;
            shakeOffsetX = 0;
            shakeOffsetY = 0;
            return;
        }
        
        // Calculate shake intensity (decreases over time)
        float intensity = shakeIntensity * (1.0f - shakeTimer / shakeDuration);
        
        // Generate random shake offset
        shakeOffsetX = (float)(Math.random() - 0.5) * 2 * intensity;
        shakeOffsetY = (float)(Math.random() - 0.5) * 2 * intensity;
    }
    
    private void updateZoom(float deltaTime) {
        if (Math.abs(zoom - targetZoom) > 0.01f) {
            zoom = lerp(zoom, targetZoom, zoomSpeed * deltaTime);
        }
    }
    
    private void applyBoundaries() {
        if (!boundaryEnabled || worldBounds == null) return;
        
        float halfWidth = (viewportWidth / 2.0f) / zoom;
        float halfHeight = (viewportHeight / 2.0f) / zoom;
        
        // Clamp camera position to world bounds
        this.x = Math.max(worldBounds.x + halfWidth, 
                 Math.min(worldBounds.x + worldBounds.width - halfWidth, this.x));
        this.y = Math.max(worldBounds.y + halfHeight, 
                 Math.min(worldBounds.y + worldBounds.height - halfHeight, this.y));
        
        // Update target position to match clamped position
        this.targetX = this.x;
        this.targetY = this.y;
    }
    
    private float lerp(float start, float end, float t) {
        return start + (end - start) * Math.max(0, Math.min(1, t));
    }
    
    private float smoothStep(float t) {
        return t * t * (3.0f - 2.0f * t);
    }
}