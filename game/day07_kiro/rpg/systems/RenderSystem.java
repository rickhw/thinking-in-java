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
import java.util.Comparator;

/**
 * System responsible for rendering entities with render components.
 * Includes frustum culling, layer management, and render optimization.
 */
public class RenderSystem extends GameSystem {
    private Rectangle viewport;
    private List<RenderableEntity> renderQueue;
    
    // Camera offset for world rendering
    private float cameraX = 0;
    private float cameraY = 0;
    
    // Culling settings
    private boolean enableCulling = true;
    private int cullMargin = 64; // Extra pixels around viewport for culling
    
    @Override
    public void initialize() {
        this.renderQueue = new ArrayList<>();
        this.viewport = new Rectangle(0, 0, 800, 600); // Default viewport size
    }
    
    /**
     * Set the viewport for culling calculations
     */
    public void setViewport(Rectangle viewport) {
        this.viewport = new Rectangle(viewport);
    }
    
    /**
     * Set camera position for world-to-screen coordinate conversion
     */
    public void setCameraPosition(float x, float y) {
        this.cameraX = x;
        this.cameraY = y;
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
        List<Entity> renderableEntities = entityManager.getEntitiesWith(RenderComponent.class);
        
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
        
        // Sort by layer (lower layers render first)
        renderQueue.sort(Comparator.comparingInt(re -> re.renderComponent.getLayer()));
        
        // Render each entity
        for (RenderableEntity renderable : renderQueue) {
            renderEntity(g2, renderable);
        }
    }
    
    private void collectRenderableEntities() {
        List<Entity> entities = entityManager.getEntitiesWith(
            TransformComponent.class, RenderComponent.class);
        
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
            
            renderQueue.add(new RenderableEntity(entity, transform, render));
        }
    }
    
    private boolean isInViewport(TransformComponent transform, RenderComponent render) {
        BufferedImage sprite = render.getSprite();
        if (sprite == null) return false;
        
        // Calculate screen position
        float screenX = transform.x - cameraX;
        float screenY = transform.y - cameraY;
        
        // Calculate sprite bounds
        float spriteWidth = sprite.getWidth() * transform.scaleX;
        float spriteHeight = sprite.getHeight() * transform.scaleY;
        
        // Check if sprite intersects with viewport (with margin)
        Rectangle spriteBounds = new Rectangle(
            (int)(screenX - cullMargin),
            (int)(screenY - cullMargin),
            (int)(spriteWidth + cullMargin * 2),
            (int)(spriteHeight + cullMargin * 2)
        );
        
        return viewport.intersects(spriteBounds);
    }
    
    private void renderEntity(Graphics2D g2, RenderableEntity renderable) {
        TransformComponent transform = renderable.transformComponent;
        RenderComponent render = renderable.renderComponent;
        BufferedImage sprite = render.getSprite();
        
        if (sprite == null) return;
        
        // Save original transform
        AffineTransform originalTransform = g2.getTransform();
        
        try {
            // Calculate screen position
            float screenX = transform.x - cameraX;
            float screenY = transform.y - cameraY;
            
            // Apply transformations
            AffineTransform renderTransform = new AffineTransform();
            renderTransform.translate(screenX, screenY);
            
            // Apply rotation if needed
            if (transform.rotation != 0) {
                renderTransform.rotate(Math.toRadians(transform.rotation), 
                    sprite.getWidth() / 2.0, sprite.getHeight() / 2.0);
            }
            
            // Apply scaling
            if (transform.scaleX != 1.0f || transform.scaleY != 1.0f) {
                renderTransform.scale(transform.scaleX, transform.scaleY);
            }
            
            // Apply flipping
            if (render.isFlipX() || render.isFlipY()) {
                float flipX = render.isFlipX() ? -1 : 1;
                float flipY = render.isFlipY() ? -1 : 1;
                renderTransform.scale(flipX, flipY);
                
                if (render.isFlipX()) {
                    renderTransform.translate(-sprite.getWidth(), 0);
                }
                if (render.isFlipY()) {
                    renderTransform.translate(0, -sprite.getHeight());
                }
            }
            
            g2.setTransform(renderTransform);
            
            // Apply alpha if needed
            if (render.getAlpha() < 1.0f) {
                g2.setComposite(java.awt.AlphaComposite.getInstance(
                    java.awt.AlphaComposite.SRC_OVER, render.getAlpha()));
            }
            
            // Draw the sprite
            g2.drawImage(sprite, 0, 0, null);
            
        } finally {
            // Restore original transform and composite
            g2.setTransform(originalTransform);
            g2.setComposite(java.awt.AlphaComposite.getInstance(
                java.awt.AlphaComposite.SRC_OVER, 1.0f));
        }
    }
    
    @Override
    public void cleanup() {
        if (renderQueue != null) {
            renderQueue.clear();
        }
    }
    
    @Override
    public int getPriority() {
        return 1000; // Render system should run last
    }
    
    /**
     * Helper class to hold renderable entity data
     */
    private static class RenderableEntity {
        final Entity entity;
        final TransformComponent transformComponent;
        final RenderComponent renderComponent;
        
        RenderableEntity(Entity entity, TransformComponent transform, RenderComponent render) {
            this.entity = entity;
            this.transformComponent = transform;
            this.renderComponent = render;
        }
    }
}