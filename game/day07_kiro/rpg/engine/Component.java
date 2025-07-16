package rpg.engine;

/**
 * Base Component interface for the Entity-Component-System architecture.
 * Components hold data and state for entities.
 */
public abstract class Component {
    private Entity entity;
    
    /**
     * Get the entity this component is attached to.
     */
    public Entity getEntity() {
        return entity;
    }
    
    /**
     * Set the entity this component is attached to.
     * This is called automatically by Entity.addComponent().
     */
    void setEntity(Entity entity) {
        this.entity = entity;
    }
    
    /**
     * Called when the component is added to an entity.
     * Override this method to perform initialization.
     */
    public void onAttach() {
        // Default implementation does nothing
    }
    
    /**
     * Called when the component is removed from an entity.
     * Override this method to perform cleanup.
     */
    public void onDetach() {
        // Default implementation does nothing
    }
    
    /**
     * Called every frame to update the component.
     * Override this method to implement component-specific update logic.
     */
    public void update(float deltaTime) {
        // Default implementation does nothing
    }
}