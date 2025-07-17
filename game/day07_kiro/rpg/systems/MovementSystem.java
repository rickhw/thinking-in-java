package rpg.systems;

import rpg.components.MovementComponent;
import rpg.components.TransformComponent;
import rpg.engine.Entity;
import java.util.List;

/**
 * System responsible for updating entity positions based on their movement components.
 * Handles velocity, acceleration, friction, and movement constraints.
 */
public class MovementSystem extends GameSystem {
    
    @Override
    public void initialize() {
        // No initialization needed for movement system
    }
    
    @Override
    public void update(float deltaTime) {
        if (!isEnabled()) return;
        
        // Get all entities with both Transform and Movement components
        List<Entity> movableEntities = entityManager.getEntitiesWith(
            TransformComponent.class, MovementComponent.class);
        
        for (Entity entity : movableEntities) {
            updateEntityMovement(entity, deltaTime);
        }
    }
    
    private void updateEntityMovement(Entity entity, float deltaTime) {
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        MovementComponent movement = entity.getComponent(MovementComponent.class);
        
        if (transform == null || movement == null || !movement.canMove) {
            return;
        }
        
        // Update movement component (applies acceleration, friction, etc.)
        movement.update(deltaTime);
        
        // Calculate new position
        float newX = transform.x + movement.velocityX * deltaTime;
        float newY = transform.y + movement.velocityY * deltaTime;
        
        // Check movement bounds
        if (movement.hasBounds) {
            if (!movement.isWithinBounds(newX, newY)) {
                // Clamp to bounds and stop velocity in that direction
                if (newX < movement.minX || newX > movement.maxX) {
                    newX = Math.max(movement.minX, Math.min(movement.maxX, newX));
                    movement.velocityX = 0;
                }
                if (newY < movement.minY || newY > movement.maxY) {
                    newY = Math.max(movement.minY, Math.min(movement.maxY, newY));
                    movement.velocityY = 0;
                }
            }
        }
        
        // Update transform position
        transform.setPosition(newX, newY);
    }
    
    @Override
    public void cleanup() {
        // No cleanup needed for movement system
    }
    
    @Override
    public int getPriority() {
        return 100; // Movement should happen before collision detection
    }
}