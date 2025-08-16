package rpg.systems;

import rpg.Config;
import rpg.components.CollisionComponent;
import rpg.components.TransformComponent;
import rpg.engine.Entity;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * Debug rendering system for development purposes.
 * Provides visual debugging information like collision bounds, entity positions, etc.
 */
public class DebugRenderer {
    private boolean enabled;
    private boolean showCollisionBounds;
    private boolean showEntityPositions;
    private boolean showCameraInfo;
    private boolean showPerformanceInfo;
    private boolean showGrid;
    
    // Colors for debug rendering
    private static final Color COLLISION_COLOR = new Color(255, 0, 0, 128);
    private static final Color POSITION_COLOR = Color.GREEN;
    private static final Color GRID_COLOR = new Color(128, 128, 128, 64);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color TEXT_BACKGROUND = new Color(0, 0, 0, 128);
    
    // Font for debug text
    private Font debugFont;
    
    public DebugRenderer() {
        this.enabled = Config.SHOW_COLLISION_BOUNDS; // Use config setting
        this.showCollisionBounds = true;
        this.showEntityPositions = false;
        this.showCameraInfo = true;
        this.showPerformanceInfo = Config.SHOW_FPS;
        this.showGrid = false;
        
        this.debugFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    }
    
    /**
     * Render debug information
     */
    public void render(Graphics2D g2, Camera camera, List<Entity> entities, RenderQueue.RenderStats stats) {
        if (!enabled) return;
        
        // Save original rendering state
        Color originalColor = g2.getColor();
        Font originalFont = g2.getFont();
        Stroke originalStroke = g2.getStroke();
        
        try {
            g2.setFont(debugFont);
            
            if (showGrid) {
                renderGrid(g2, camera);
            }
            
            if (showCollisionBounds || showEntityPositions) {
                renderEntityDebugInfo(g2, camera, entities);
            }
            
            if (showCameraInfo) {
                renderCameraInfo(g2, camera);
            }
            
            if (showPerformanceInfo) {
                renderPerformanceInfo(g2, stats);
            }
            
        } finally {
            // Restore original rendering state
            g2.setColor(originalColor);
            g2.setFont(originalFont);
            g2.setStroke(originalStroke);
        }
    }
    
    private void renderGrid(Graphics2D g2, Camera camera) {
        if (camera == null) return;
        
        g2.setColor(GRID_COLOR);
        g2.setStroke(new BasicStroke(1));
        
        Rectangle viewBounds = camera.getViewBounds();
        int tileSize = Config.TILE_SIZE;
        
        // Calculate grid lines within viewport
        int startX = (viewBounds.x / tileSize) * tileSize;
        int startY = (viewBounds.y / tileSize) * tileSize;
        int endX = viewBounds.x + viewBounds.width;
        int endY = viewBounds.y + viewBounds.height;
        
        // Draw vertical lines
        for (int x = startX; x <= endX; x += tileSize) {
            Point2D.Float screenStart = camera.worldToScreen(x, viewBounds.y);
            Point2D.Float screenEnd = camera.worldToScreen(x, viewBounds.y + viewBounds.height);
            g2.drawLine((int)screenStart.x, (int)screenStart.y, (int)screenEnd.x, (int)screenEnd.y);
        }
        
        // Draw horizontal lines
        for (int y = startY; y <= endY; y += tileSize) {
            Point2D.Float screenStart = camera.worldToScreen(viewBounds.x, y);
            Point2D.Float screenEnd = camera.worldToScreen(viewBounds.x + viewBounds.width, y);
            g2.drawLine((int)screenStart.x, (int)screenStart.y, (int)screenEnd.x, (int)screenEnd.y);
        }
    }
    
    private void renderEntityDebugInfo(Graphics2D g2, Camera camera, List<Entity> entities) {
        for (Entity entity : entities) {
            TransformComponent transform = entity.getComponent(TransformComponent.class);
            if (transform == null) continue;
            
            // Only render debug info for visible entities
            if (camera != null && !camera.isVisible(transform.x, transform.y)) {
                continue;
            }
            
            Point2D.Float screenPos = camera != null ? 
                camera.worldToScreen(transform.x, transform.y) : 
                new Point2D.Float(transform.x, transform.y);
            
            if (showEntityPositions) {
                renderEntityPosition(g2, screenPos, entity);
            }
            
            if (showCollisionBounds) {
                renderCollisionBounds(g2, camera, entity, transform);
            }
        }
    }
    
    private void renderEntityPosition(Graphics2D g2, Point2D.Float screenPos, Entity entity) {
        g2.setColor(POSITION_COLOR);
        g2.setStroke(new BasicStroke(2));
        
        // Draw crosshair at entity position
        int size = 8;
        g2.drawLine((int)screenPos.x - size, (int)screenPos.y, (int)screenPos.x + size, (int)screenPos.y);
        g2.drawLine((int)screenPos.x, (int)screenPos.y - size, (int)screenPos.x, (int)screenPos.y + size);
        
        // Draw entity ID
        g2.setColor(TEXT_COLOR);
        String idText = "ID:" + entity.getId();
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(idText);
        int textHeight = fm.getHeight();
        
        // Draw text background
        g2.setColor(TEXT_BACKGROUND);
        g2.fillRect((int)screenPos.x + size + 2, (int)screenPos.y - textHeight/2, textWidth + 4, textHeight);
        
        // Draw text
        g2.setColor(TEXT_COLOR);
        g2.drawString(idText, (int)screenPos.x + size + 4, (int)screenPos.y + textHeight/4);
    }
    
    private void renderCollisionBounds(Graphics2D g2, Camera camera, Entity entity, TransformComponent transform) {
        CollisionComponent collision = entity.getComponent(CollisionComponent.class);
        if (collision == null) return;
        
        g2.setColor(COLLISION_COLOR);
        g2.setStroke(new BasicStroke(2));
        
        // Get collision bounds
        Rectangle bounds = collision.getBounds();
        if (bounds == null) return;
        
        // Convert to screen coordinates
        if (camera != null) {
            Point2D.Float topLeft = camera.worldToScreen(bounds.x, bounds.y);
            Point2D.Float bottomRight = camera.worldToScreen(bounds.x + bounds.width, bounds.y + bounds.height);
            
            g2.drawRect(
                (int)topLeft.x, 
                (int)topLeft.y, 
                (int)(bottomRight.x - topLeft.x), 
                (int)(bottomRight.y - topLeft.y)
            );
        } else {
            g2.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }
    
    private void renderCameraInfo(Graphics2D g2, Camera camera) {
        if (camera == null) return;
        
        String[] info = {
            String.format("Camera: (%.1f, %.1f)", camera.getX(), camera.getY()),
            String.format("Zoom: %.2f", camera.getZoom()),
            String.format("Viewport: %dx%d", camera.getViewportWidth(), camera.getViewportHeight())
        };
        
        renderTextBox(g2, info, 10, 10);
    }
    
    private void renderPerformanceInfo(Graphics2D g2, RenderQueue.RenderStats stats) {
        String[] info = {
            String.format("Entities: %d", stats.totalEntities),
            String.format("Batches: %d", stats.totalBatches),
            String.format("State Changes: %d", stats.stateChanges),
            String.format("Layers: %d", stats.layerCount)
        };
        
        renderTextBox(g2, info, 10, 120);
    }
    
    private void renderTextBox(Graphics2D g2, String[] lines, int x, int y) {
        FontMetrics fm = g2.getFontMetrics();
        int lineHeight = fm.getHeight();
        int maxWidth = 0;
        
        // Calculate box dimensions
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, fm.stringWidth(line));
        }
        
        int boxWidth = maxWidth + 8;
        int boxHeight = lines.length * lineHeight + 8;
        
        // Draw background
        g2.setColor(TEXT_BACKGROUND);
        g2.fillRect(x, y, boxWidth, boxHeight);
        
        // Draw border
        g2.setColor(TEXT_COLOR);
        g2.setStroke(new BasicStroke(1));
        g2.drawRect(x, y, boxWidth, boxHeight);
        
        // Draw text
        for (int i = 0; i < lines.length; i++) {
            g2.drawString(lines[i], x + 4, y + 4 + (i + 1) * lineHeight - 4);
        }
    }
    
    // Getters and setters for debug options
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isShowCollisionBounds() {
        return showCollisionBounds;
    }
    
    public void setShowCollisionBounds(boolean showCollisionBounds) {
        this.showCollisionBounds = showCollisionBounds;
    }
    
    public boolean isShowEntityPositions() {
        return showEntityPositions;
    }
    
    public void setShowEntityPositions(boolean showEntityPositions) {
        this.showEntityPositions = showEntityPositions;
    }
    
    public boolean isShowCameraInfo() {
        return showCameraInfo;
    }
    
    public void setShowCameraInfo(boolean showCameraInfo) {
        this.showCameraInfo = showCameraInfo;
    }
    
    public boolean isShowPerformanceInfo() {
        return showPerformanceInfo;
    }
    
    public void setShowPerformanceInfo(boolean showPerformanceInfo) {
        this.showPerformanceInfo = showPerformanceInfo;
    }
    
    public boolean isShowGrid() {
        return showGrid;
    }
    
    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }
}