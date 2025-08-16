package rpg.systems;

import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Manages render queue with batching and state management to minimize state changes.
 * Groups similar rendering operations together for better performance.
 */
public class RenderQueue {
    private final Map<Integer, RenderLayer> layers;
    private final List<RenderBatch> batches;
    private final RenderStateManager stateManager;
    
    // Performance tracking
    private int totalEntities;
    private int totalBatches;
    private int stateChanges;
    
    public RenderQueue() {
        this.layers = new TreeMap<>(); // Sorted by layer index
        this.batches = new ArrayList<>();
        this.stateManager = new RenderStateManager();
    }
    
    /**
     * Create or get a render layer
     */
    public RenderLayer getOrCreateLayer(int layerIndex, String name) {
        return layers.computeIfAbsent(layerIndex, k -> new RenderLayer(layerIndex, name));
    }
    
    /**
     * Get a specific layer
     */
    public RenderLayer getLayer(int layerIndex) {
        return layers.get(layerIndex);
    }
    
    /**
     * Get all layers sorted by index
     */
    public Collection<RenderLayer> getLayers() {
        return layers.values();
    }
    
    /**
     * Add entity to appropriate layer
     */
    public void addEntity(RenderableEntity entity) {
        int layerIndex = entity.renderComponent.getLayer();
        RenderLayer layer = getOrCreateLayer(layerIndex, "Layer_" + layerIndex);
        layer.addEntity(entity);
    }
    
    /**
     * Clear all layers and batches
     */
    public void clear() {
        for (RenderLayer layer : layers.values()) {
            layer.clear();
        }
        batches.clear();
        totalEntities = 0;
        totalBatches = 0;
        stateChanges = 0;
    }
    
    /**
     * Prepare render queue by sorting layers and creating batches
     */
    public void prepare() {
        batches.clear();
        totalEntities = 0;
        
        // Sort entities within each layer
        for (RenderLayer layer : layers.values()) {
            if (layer.isVisible() && layer.getEntityCount() > 0) {
                layer.sortEntities();
                createBatchesForLayer(layer);
                totalEntities += layer.getEntityCount();
            }
        }
        
        totalBatches = batches.size();
    }
    
    /**
     * Render all batches
     */
    public void render(Graphics2D g2) {
        stateManager.reset();
        stateChanges = 0;
        
        for (RenderBatch batch : batches) {
            renderBatch(g2, batch);
        }
    }
    
    /**
     * Get performance statistics
     */
    public RenderStats getStats() {
        return new RenderStats(totalEntities, totalBatches, stateChanges, layers.size());
    }
    
    private void createBatchesForLayer(RenderLayer layer) {
        List<RenderableEntity> entities = layer.getEntities();
        if (entities.isEmpty()) return;
        
        RenderBatch currentBatch = null;
        RenderState currentState = null;
        
        for (RenderableEntity entity : entities) {
            RenderState entityState = createRenderState(entity, layer);
            
            // Start new batch if state changed or batch is full
            if (currentBatch == null || 
                !entityState.equals(currentState) || 
                currentBatch.isFull()) {
                
                currentBatch = new RenderBatch(entityState);
                batches.add(currentBatch);
                currentState = entityState;
            }
            
            currentBatch.addEntity(entity);
        }
    }
    
    private RenderState createRenderState(RenderableEntity entity, RenderLayer layer) {
        return new RenderState(
            entity.renderComponent.getSprite(),
            entity.renderComponent.getAlpha() * layer.getAlpha(),
            entity.renderComponent.isFlipX(),
            entity.renderComponent.isFlipY()
        );
    }
    
    private void renderBatch(Graphics2D g2, RenderBatch batch) {
        RenderState state = batch.getState();
        
        // Apply state changes only if needed
        if (stateManager.applyState(g2, state)) {
            stateChanges++;
        }
        
        // Render all entities in the batch
        for (RenderableEntity entity : batch.getEntities()) {
            renderEntity(g2, entity);
        }
    }
    
    private void renderEntity(Graphics2D g2, RenderableEntity entity) {
        BufferedImage sprite = entity.renderComponent.getSprite();
        if (sprite == null) return;
        
        // Save original transform
        AffineTransform originalTransform = g2.getTransform();
        
        try {
            // Calculate screen position (assuming camera transformation is already applied)
            float screenX = entity.transformComponent.x;
            float screenY = entity.transformComponent.y;
            
            // Apply transformations
            AffineTransform renderTransform = new AffineTransform();
            renderTransform.translate(screenX, screenY);
            
            // Apply rotation if needed
            if (entity.transformComponent.rotation != 0) {
                renderTransform.rotate(Math.toRadians(entity.transformComponent.rotation), 
                    sprite.getWidth() / 2.0, sprite.getHeight() / 2.0);
            }
            
            // Apply scaling
            if (entity.transformComponent.scaleX != 1.0f || entity.transformComponent.scaleY != 1.0f) {
                renderTransform.scale(entity.transformComponent.scaleX, entity.transformComponent.scaleY);
            }
            
            g2.setTransform(renderTransform);
            
            // Draw the sprite
            g2.drawImage(sprite, 0, 0, null);
            
        } finally {
            // Restore original transform
            g2.setTransform(originalTransform);
        }
    }
    
    /**
     * Render batch for similar entities
     */
    private static class RenderBatch {
        private static final int MAX_BATCH_SIZE = 100;
        
        private final RenderState state;
        private final List<RenderableEntity> entities;
        
        public RenderBatch(RenderState state) {
            this.state = state;
            this.entities = new ArrayList<>();
        }
        
        public void addEntity(RenderableEntity entity) {
            entities.add(entity);
        }
        
        public boolean isFull() {
            return entities.size() >= MAX_BATCH_SIZE;
        }
        
        public RenderState getState() {
            return state;
        }
        
        public List<RenderableEntity> getEntities() {
            return entities;
        }
    }
    
    /**
     * Represents render state for batching
     */
    private static class RenderState {
        private final BufferedImage texture;
        private final float alpha;
        private final boolean flipX;
        private final boolean flipY;
        
        public RenderState(BufferedImage texture, float alpha, boolean flipX, boolean flipY) {
            this.texture = texture;
            this.alpha = alpha;
            this.flipX = flipX;
            this.flipY = flipY;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            
            RenderState that = (RenderState) obj;
            return Float.compare(that.alpha, alpha) == 0 &&
                   flipX == that.flipX &&
                   flipY == that.flipY &&
                   Objects.equals(texture, that.texture);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(texture, alpha, flipX, flipY);
        }
    }
    
    /**
     * Manages render state changes to minimize GPU state switches
     */
    private static class RenderStateManager {
        private RenderState currentState;
        
        public void reset() {
            currentState = null;
        }
        
        public boolean applyState(Graphics2D g2, RenderState newState) {
            if (Objects.equals(currentState, newState)) {
                return false; // No state change needed
            }
            
            // Apply alpha composite
            if (newState.alpha < 1.0f) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, newState.alpha));
            } else {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
            
            currentState = newState;
            return true; // State was changed
        }
    }
    
    /**
     * Performance statistics for the render queue
     */
    public static class RenderStats {
        public final int totalEntities;
        public final int totalBatches;
        public final int stateChanges;
        public final int layerCount;
        
        public RenderStats(int totalEntities, int totalBatches, int stateChanges, int layerCount) {
            this.totalEntities = totalEntities;
            this.totalBatches = totalBatches;
            this.stateChanges = stateChanges;
            this.layerCount = layerCount;
        }
        
        @Override
        public String toString() {
            return String.format("RenderStats{entities=%d, batches=%d, stateChanges=%d, layers=%d}", 
                totalEntities, totalBatches, stateChanges, layerCount);
        }
    }
}