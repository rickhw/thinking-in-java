package rpg.systems;

import rpg.Config;
import rpg.components.*;
import rpg.engine.Entity;
import rpg.entity.Direction;
import rpg.game.EntityManager;

/**
 * System that handles player-specific logic and behavior.
 * Processes player input, movement, animation, and interactions.
 */
public class PlayerController extends GameSystem {
    private Entity playerEntity;
    
    public PlayerController() {
        super();
    }
    
    public void setPlayerEntity(Entity playerEntity) {
        this.playerEntity = playerEntity;
    }
    
    public Entity getPlayerEntity() {
        return playerEntity;
    }
    
    @Override
    public void initialize() {
        // System initialization if needed
    }
    
    @Override
    public void update(float deltaTime) {
        if (playerEntity == null || !playerEntity.isActive()) {
            return;
        }
        
        // Get required components
        PlayerInputComponent input = playerEntity.getComponent(PlayerInputComponent.class);
        TransformComponent transform = playerEntity.getComponent(TransformComponent.class);
        MovementComponent movement = playerEntity.getComponent(MovementComponent.class);
        AnimationComponent animation = playerEntity.getComponent(AnimationComponent.class);
        CollisionComponent collision = playerEntity.getComponent(CollisionComponent.class);
        
        if (input == null || transform == null || movement == null) {
            return;
        }
        
        // Process player input and movement
        processPlayerInput(input, movement, animation, deltaTime);
        
        // Handle interactions
        if (input.isInteractPressed()) {
            handleInteraction();
        }
    }
    
    private void processPlayerInput(PlayerInputComponent input, MovementComponent movement, 
                                  AnimationComponent animation, float deltaTime) {
        // Get movement vector from input
        float[] moveVector = input.getMovementVector();
        float moveX = moveVector[0];
        float moveY = moveVector[1];
        
        // Calculate movement speed
        float baseSpeed = 5.0f; // Base player speed (matching original)
        float currentSpeed = baseSpeed;
        
        // Apply run modifier
        if (input.isRunPressed()) {
            currentSpeed *= 1.5f; // 50% speed boost when running
        }
        
        // Set movement velocity
        if (input.isMoving()) {
            movement.setVelocity(moveX * currentSpeed, moveY * currentSpeed);
            
            // Update animation to walking
            if (animation != null) {
                animation.playAnimation("walk", input.getDirection());
            }
        } else {
            // Stop movement
            movement.setVelocity(0, 0);
            
            // Update animation to idle
            if (animation != null) {
                animation.playAnimation("idle", input.getLastDirection());
            }
        }
    }
    
    private void handleInteraction() {
        // TODO: Implement interaction logic
        // This could involve checking for nearby interactable objects
        // and triggering interaction events
        
        // For now, just publish an interaction event
        if (eventBus != null) {
            // eventBus.publish(new InteractionEvent(playerEntity));
        }
    }
    
    @Override
    public void cleanup() {
        playerEntity = null;
    }
    
    // Utility methods for player management
    public void teleportPlayer(float x, float y) {
        if (playerEntity != null) {
            TransformComponent transform = playerEntity.getComponent(TransformComponent.class);
            if (transform != null) {
                transform.setPosition(x, y);
            }
        }
    }
    
    public void setPlayerDirection(Direction direction) {
        if (playerEntity != null) {
            PlayerInputComponent input = playerEntity.getComponent(PlayerInputComponent.class);
            if (input != null) {
                input.setDirection(direction);
            }
        }
    }
    
    public Direction getPlayerDirection() {
        if (playerEntity != null) {
            PlayerInputComponent input = playerEntity.getComponent(PlayerInputComponent.class);
            if (input != null) {
                return input.getDirection();
            }
        }
        return Direction.DOWN;
    }
    
    public float[] getPlayerPosition() {
        if (playerEntity != null) {
            TransformComponent transform = playerEntity.getComponent(TransformComponent.class);
            if (transform != null) {
                return new float[]{transform.x, transform.y};
            }
        }
        return new float[]{0, 0};
    }
}