package rpg.systems;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

/**
 * Represents a rendering layer with depth sorting and batch management.
 * Layers are rendered in order, with lower layer values rendered first (background).
 */
public class RenderLayer {
    private final int layerIndex;
    private final String name;
    private final List<RenderableEntity> entities;
    private boolean visible;
    private float alpha;
    
    // Sorting options
    private Comparator<RenderableEntity> sortComparator;
    
    public RenderLayer(int layerIndex, String name) {
        this.layerIndex = layerIndex;
        this.name = name;
        this.entities = new ArrayList<>();
        this.visible = true;
        this.alpha = 1.0f;
        
        // Default sorting by Y position (top to bottom)
        this.sortComparator = Comparator.comparing(
            entity -> entity.transformComponent.y
        );
    }
    
    /**
     * Add an entity to this layer
     */
    public void addEntity(RenderableEntity entity) {
        entities.add(entity);
    }
    
    /**
     * Remove an entity from this layer
     */
    public void removeEntity(RenderableEntity entity) {
        entities.remove(entity);
    }
    
    /**
     * Clear all entities from this layer
     */
    public void clear() {
        entities.clear();
    }
    
    /**
     * Sort entities in this layer
     */
    public void sortEntities() {
        if (sortComparator != null) {
            entities.sort(sortComparator);
        }
    }
    
    /**
     * Get all entities in this layer
     */
    public List<RenderableEntity> getEntities() {
        return entities;
    }
    
    /**
     * Get the number of entities in this layer
     */
    public int getEntityCount() {
        return entities.size();
    }
    
    /**
     * Get layer index (rendering order)
     */
    public int getLayerIndex() {
        return layerIndex;
    }
    
    /**
     * Get layer name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Check if layer is visible
     */
    public boolean isVisible() {
        return visible;
    }
    
    /**
     * Set layer visibility
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    /**
     * Get layer alpha (transparency)
     */
    public float getAlpha() {
        return alpha;
    }
    
    /**
     * Set layer alpha (0.0 = transparent, 1.0 = opaque)
     */
    public void setAlpha(float alpha) {
        this.alpha = Math.max(0.0f, Math.min(1.0f, alpha));
    }
    
    /**
     * Set custom sorting comparator for entities in this layer
     */
    public void setSortComparator(Comparator<RenderableEntity> comparator) {
        this.sortComparator = comparator;
    }
    
    /**
     * Get the current sort comparator
     */
    public Comparator<RenderableEntity> getSortComparator() {
        return sortComparator;
    }
    
    @Override
    public String toString() {
        return String.format("RenderLayer{index=%d, name='%s', entities=%d, visible=%s}", 
            layerIndex, name, entities.size(), visible);
    }
}