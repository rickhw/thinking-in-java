package rpg.systems;

import rpg.game.EntityManager;
import rpg.utils.ServiceLocator;

/**
 * Base class for all game systems in the Entity-Component-System architecture.
 * Systems contain the logic that operates on entities with specific components.
 */
public abstract class GameSystem {
    protected EntityManager entityManager;
    protected EventBus eventBus;
    protected boolean enabled;
    
    public GameSystem() {
        this.entityManager = ServiceLocator.getService(EntityManager.class);
        this.eventBus = ServiceLocator.getService(EventBus.class);
        this.enabled = true;
    }
    
    /**
     * Initialize the system. Called once when the system is created.
     */
    public abstract void initialize();
    
    /**
     * Update the system. Called every frame.
     */
    public abstract void update(float deltaTime);
    
    /**
     * Clean up the system. Called when the system is destroyed.
     */
    public abstract void cleanup();
    
    /**
     * Check if this system is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Enable or disable this system.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * Get the priority of this system for update ordering.
     * Lower values are updated first.
     */
    public int getPriority() {
        return 0;
    }
}