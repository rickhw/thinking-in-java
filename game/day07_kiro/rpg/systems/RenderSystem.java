package rpg.systems;

import rpg.components.RenderComponent;
import rpg.components.TransformComponent;
import rpg.engine.Entity;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

/**
 * System responsible for rendering entities with render components.
 * Includes frustum culling, layer management, and render optimization.
 * Integrates with CameraSystem for viewport management.
 */
public class RenderSystem extends GameSystem implements EventListener<CameraEvent> {
    private Rectangle viewport;
    private RenderQueue renderQueue;
    private Camera camera;
    private DebugRenderer debugRenderer;
    
    // Culling settings
    private boolean enableCulling = true;
    private int cullMargin = 64; // Extra pixels around viewport for culling
    
    @Override
    public void initialize() {
        this.renderQueue = new RenderQueue();
        this.debugRenderer = new DebugRenderer();
        this.viewport = new Rectangle(0, 0, 800, 600); // Default viewport size
        
        // Subscribe to camera events
        if (eventBus != null) {
            eventBus.subscribe(CameraEvent.class, this);
        }
    }
    
    /**
     * Set the viewport for culling calculations
     */
    public void setViewport(Rectangle viewport) {
        this.viewport = new Rectangle(viewport);
    }
    
    /**
     * Set the camera for rendering
     */
    public void setCamera(Camera camera) {
        this.camera = camera;
        if (camera != null) {
            this.viewport = new Rectangle(0, 0, camera.getViewportWidth(), camera.getViewportHeight());
        }
    }
    
    /**
     * Get the current camera
     */
    public Camera getCamera() {
        return camera;
    }
    
    /**
     * Enable or disable frustum culling
     */
    public void setCullingEnabled(boolean enabled) {
        this.enableCulling = enabled;
    }
    
    /**
     * Set the culling margin (extra pixels around viewport)
     */
    public void setCullMargin(int margin) {
        this.cullMargin = margin;
    }
    
    @Override
    public void update(float deltaTime) {
        if (!isEnabled()) return;
        
        // Update render components (animations, etc.)
        List<Entity> renderableEntities = entityManager.getEntitiesWithComponent(RenderComponent.class);
        
        for (Entity entity : renderableEntities) {
            RenderComponent render = entity.getComponent(RenderComponent.class);
            if (render != null) {
                render.update(deltaTime);
            }
        }
    }
    
    /**
     * Render all visible entities to the graphics context
     */
    public void render(Graphics2D g2) {
        if (!isEnabled()) return;
        
        // Clear render queue
        renderQueue.clear();
        
        // Collect renderable entities
        collectRenderableEntities();
        
        // Prepare render queue (sort layers, create batches)
        renderQueue.prepare();
        
        // Render all batches
        renderQueue.render(g2);
        
        // Render debug information if enabled
        if (debugRenderer.isEnabled()) {
            Collection<Entity> entityCollection = entityManager.getAllEntities();
            List<Entity> allEntities = new ArrayList<>(entityCollection);
            RenderQueue.RenderStats stats = renderQueue.getStats();
            debugRenderer.render(g2, camera, allEntities, stats);
        }
    }
    
    private void collectRenderableEntities() {
        Collection<Entity> entityCollection = entityManager.getAllEntities();
        List<Entity> entities = new ArrayList<>(entityCollection);
        
        for (Entity entity : entities) {
            TransformComponent transform = entity.getComponent(TransformComponent.class);
            RenderComponent render = entity.getComponent(RenderComponent.class);
            
            if (transform == null || render == null || !render.isVisible()) {
                continue;
            }
            
            // Frustum culling
            if (enableCulling && !isInViewport(transform, render)) {
                continue;
            }
            
            renderQueue.addEntity(new RenderableEntity(entity, transform, render));
        }
    }
    
    private boolean isInViewport(TransformComponent transform, RenderComponent render) {
        if (camera == null) return true; // Render everything if no camera
        
        BufferedImage sprite = render.getSprite();
        if (sprite == null) return false;
        
        // Calculate sprite bounds in world coordinates
        float spriteWidth = sprite.getWidth() * transform.scaleX;
        float spriteHeight = sprite.getHeight() * transform.scaleY;
        
        Rectangle spriteBounds = new Rectangle(
            (int)(transform.x - cullMargin),
            (int)(transform.y - cullMargin),
            (int)(spriteWidth + cullMargin * 2),
            (int)(spriteHeight + cullMargin * 2)
        );
        
        // Use camera's visibility check
        return camera.isVisible(spriteBounds);
    }
    

    
    /**
     * Get the render queue for external access
     */
    public RenderQueue getRenderQueue() {
        return renderQueue;
    }
    
    /**
     * Get the debug renderer
     */
    public DebugRenderer getDebugRenderer() {
        return debugRenderer;
    }
    
    /**
     * Get render statistics
     */
    public RenderQueue.RenderStats getRenderStats() {
        return renderQueue.getStats();
    }
    
    /**
     * Create or get a render layer
     */
    public RenderLayer getOrCreateLayer(int layerIndex, String name) {
        return renderQueue.getOrCreateLayer(layerIndex, name);
    }
    
    @Override
    public void cleanup() {
        if (renderQueue != null) {
            renderQueue.clear();
        }
        
        // Unsubscribe from events
        if (eventBus != null) {
            eventBus.unsubscribe(CameraEvent.class, this);
        }
    }
    
    @Override
    public void onEvent(CameraEvent event) {
        // Camera events are handled automatically through the camera reference
        // This method can be used for additional camera-related rendering updates
    }
    
    @Override
    public int getPriority() {
        return 1000; // Render system should run last
    }
    

}