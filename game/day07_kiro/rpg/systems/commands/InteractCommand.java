package rpg.systems.commands;

import rpg.engine.Entity;
import rpg.components.TransformComponent;
import rpg.systems.EventBus;
import rpg.systems.TriggerEvent;

/**
 * Command for handling entity interactions with objects in the world.
 * Triggers interaction events and handles interaction logic.
 */
public class InteractCommand extends InputCommand {
    private final EventBus eventBus;
    private Entity interactedEntity;
    
    /**
     * Create an interaction command.
     * @param entity the entity performing the interaction
     * @param eventBus the event bus for publishing interaction events
     */
    public InteractCommand(Entity entity, EventBus eventBus) {
        super(entity);
        this.eventBus = eventBus;
    }
    
    /**
     * Create an interaction command without event bus.
     * @param entity the entity performing the interaction
     */
    public InteractCommand(Entity entity) {
        this(entity, null);
    }
    
    @Override
    protected boolean shouldExecute() {
        // Only execute on key press, not on key hold
        return super.shouldExecute() && justPressed;
    }
    
    @Override
    protected void executeInput() {
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        if (transform == null) return;
        
        // Find nearby interactable objects
        Entity nearbyEntity = findNearbyInteractable(transform);
        
        if (nearbyEntity != null) {
            performInteraction(nearbyEntity);
            interactedEntity = nearbyEntity;
        } else {
            // No nearby interactable found
            System.out.println("Entity " + entity.getId() + " tried to interact but found nothing nearby");
        }
    }
    
    /**
     * Find an interactable entity near the given transform.
     * @param transform the transform to search around
     * @return the nearest interactable entity, or null if none found
     */
    private Entity findNearbyInteractable(TransformComponent transform) {
        // This is a simplified implementation
        // In a real game, this would query the spatial partitioning system
        // or use a more sophisticated interaction detection system
        
        // For now, just log that we're looking for interactables
        System.out.println("Looking for interactables near position (" + 
            transform.x + ", " + transform.y + ")");
        
        // TODO: Implement proper interaction detection
        // This would typically involve:
        // 1. Getting entities within interaction range
        // 2. Filtering for entities with InteractableComponent
        // 3. Checking line of sight or facing direction
        // 4. Returning the closest valid target
        
        return null; // No interactables found for now
    }
    
    /**
     * Perform the interaction with the target entity.
     * @param target the entity to interact with
     */
    private void performInteraction(Entity target) {
        System.out.println("Entity " + entity.getId() + " interacted with entity " + target.getId());
        
        // Publish interaction event if event bus is available
        if (eventBus != null) {
            TriggerEvent interactionEvent = new TriggerEvent(
                entity.getId(), 
                target.getId(), 
                TriggerEvent.TriggerType.INTERACTION
            );
            eventBus.publish(interactionEvent);
        }
        
        // TODO: Implement specific interaction logic based on target type
        // This could involve:
        // - Opening doors
        // - Picking up items
        // - Starting conversations
        // - Activating switches
        // - etc.
    }
    
    @Override
    public boolean canUndo() {
        // Most interactions can't be undone, but some might be reversible
        return false;
    }
    
    @Override
    public void undo() {
        // Default implementation - most interactions can't be undone
        if (interactedEntity != null) {
            System.out.println("Attempting to undo interaction with entity " + interactedEntity.getId());
            // Specific undo logic would go here for reversible interactions
        }
    }
    
    @Override
    public String getDescription() {
        return String.format("InteractCommand(entity=%d, target=%s)", 
            entity != null ? entity.getId() : -1,
            interactedEntity != null ? interactedEntity.getId() : "none");
    }
}