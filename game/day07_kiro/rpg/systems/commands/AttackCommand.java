package rpg.systems.commands;

import rpg.engine.Entity;
import rpg.components.TransformComponent;
import rpg.systems.EventBus;
import rpg.systems.TriggerEvent;

/**
 * Command for handling entity attack actions.
 * Manages attack timing, targeting, and damage dealing.
 */
public class AttackCommand extends InputCommand {
    private final EventBus eventBus;
    private Entity targetEntity;
    private long lastAttackTime;
    private static final long ATTACK_COOLDOWN = 500; // 500ms cooldown between attacks
    
    /**
     * Create an attack command.
     * @param entity the entity performing the attack
     * @param eventBus the event bus for publishing attack events
     */
    public AttackCommand(Entity entity, EventBus eventBus) {
        super(entity);
        this.eventBus = eventBus;
        this.lastAttackTime = 0;
    }
    
    /**
     * Create an attack command without event bus.
     * @param entity the entity performing the attack
     */
    public AttackCommand(Entity entity) {
        this(entity, null);
    }
    
    @Override
    protected boolean shouldExecute() {
        // Only execute on key press, not on key hold
        if (!super.shouldExecute() || !justPressed) {
            return false;
        }
        
        // Check attack cooldown
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAttackTime < ATTACK_COOLDOWN) {
            return false;
        }
        
        return true;
    }
    
    @Override
    protected void executeInput() {
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        if (transform == null) return;
        
        // Update last attack time
        lastAttackTime = System.currentTimeMillis();
        
        // Find target in attack range
        Entity target = findAttackTarget(transform);
        
        if (target != null) {
            performAttack(target);
            targetEntity = target;
        } else {
            // No target found, perform attack animation anyway
            performAttackAnimation();
        }
    }
    
    /**
     * Find a valid attack target near the entity.
     * @param transform the attacker's transform
     * @return the target entity, or null if no valid target
     */
    private Entity findAttackTarget(TransformComponent transform) {
        // This is a simplified implementation
        // In a real game, this would use the collision system or spatial partitioning
        
        System.out.println("Looking for attack targets near position (" + 
            transform.x + ", " + transform.y + ")");
        
        // TODO: Implement proper target detection
        // This would typically involve:
        // 1. Getting entities within attack range
        // 2. Filtering for entities that can be attacked (enemies, destructibles)
        // 3. Checking line of sight or facing direction
        // 4. Returning the closest valid target
        
        return null; // No targets found for now
    }
    
    /**
     * Perform an attack against the target entity.
     * @param target the entity being attacked
     */
    private void performAttack(Entity target) {
        System.out.println("Entity " + entity.getId() + " attacked entity " + target.getId());
        
        // Publish attack event if event bus is available
        if (eventBus != null) {
            TriggerEvent attackEvent = new TriggerEvent(
                entity.getId(), 
                target.getId(), 
                TriggerEvent.TriggerType.ATTACK
            );
            eventBus.publish(attackEvent);
        }
        
        // TODO: Implement attack logic
        // This would typically involve:
        // - Calculating damage based on attacker's stats
        // - Applying damage to target's health component
        // - Playing attack animations and sounds
        // - Handling critical hits, status effects, etc.
        // - Checking if target is defeated
    }
    
    /**
     * Perform attack animation when no target is present.
     */
    private void performAttackAnimation() {
        System.out.println("Entity " + entity.getId() + " performed attack animation (no target)");
        
        // TODO: Implement attack animation
        // This would typically involve:
        // - Playing attack animation
        // - Playing attack sound effects
        // - Creating visual effects (weapon trails, etc.)
    }
    
    /**
     * Get the remaining cooldown time in milliseconds.
     * @return the remaining cooldown time, or 0 if ready to attack
     */
    public long getRemainingCooldown() {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastAttackTime;
        return Math.max(0, ATTACK_COOLDOWN - elapsed);
    }
    
    /**
     * Check if the entity can attack (cooldown has expired).
     * @return true if the entity can attack
     */
    public boolean canAttack() {
        return getRemainingCooldown() == 0;
    }
    
    @Override
    public boolean canUndo() {
        // Attacks generally can't be undone
        return false;
    }
    
    @Override
    public void undo() {
        // Attacks can't be undone in most cases
        System.out.println("Cannot undo attack command");
    }
    
    @Override
    public String getDescription() {
        return String.format("AttackCommand(entity=%d, target=%s, cooldown=%dms)", 
            entity != null ? entity.getId() : -1,
            targetEntity != null ? targetEntity.getId() : "none",
            getRemainingCooldown());
    }
}