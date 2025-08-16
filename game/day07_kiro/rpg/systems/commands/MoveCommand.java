package rpg.systems.commands;

import rpg.engine.Entity;
import rpg.components.MovementComponent;
import rpg.components.InputComponent;
import rpg.components.TransformComponent;

/**
 * Command for handling entity movement based on input.
 * Supports directional movement with speed modifiers.
 */
public class MoveCommand extends InputCommand {
    private final float directionX;
    private final float directionY;
    private TransformComponent previousTransform;
    
    /**
     * Create a movement command.
     * @param entity the entity to move
     * @param directionX the X direction (-1, 0, or 1)
     * @param directionY the Y direction (-1, 0, or 1)
     */
    public MoveCommand(Entity entity, float directionX, float directionY) {
        super(entity);
        this.directionX = directionX;
        this.directionY = directionY;
    }
    
    @Override
    protected boolean shouldExecute() {
        if (!super.shouldExecute()) return false;
        
        MovementComponent movement = entity.getComponent(MovementComponent.class);
        return movement != null && movement.canMove;
    }
    
    @Override
    protected void executeInput() {
        MovementComponent movement = entity.getComponent(MovementComponent.class);
        InputComponent input = entity.getComponent(InputComponent.class);
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        
        if (movement == null || input == null) return;
        
        // Store previous position for undo functionality
        if (transform != null && canUndo()) {
            previousTransform = new TransformComponent();
            previousTransform.x = transform.x;
            previousTransform.y = transform.y;
        }
        
        // Get movement vector from input component (handles multiple input sources)
        float[] moveVector = input.getMovementVector();
        
        // Apply movement based on input
        float speed = input.isRunning() ? movement.maxSpeed * 1.5f : movement.maxSpeed;
        
        // Set velocity based on the movement vector
        movement.setVelocity(moveVector[0] * speed, moveVector[1] * speed);
        
        // Update direction if moving
        if (moveVector[0] != 0 || moveVector[1] != 0) {
            if (moveVector[0] > 0) movement.direction = "right";
            else if (moveVector[0] < 0) movement.direction = "left";
            else if (moveVector[1] > 0) movement.direction = "down";
            else if (moveVector[1] < 0) movement.direction = "up";
        }
    }
    
    @Override
    public boolean canUndo() {
        return previousTransform != null;
    }
    
    @Override
    public void undo() {
        if (!canUndo()) return;
        
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        MovementComponent movement = entity.getComponent(MovementComponent.class);
        
        if (transform != null && previousTransform != null) {
            transform.x = previousTransform.x;
            transform.y = previousTransform.y;
        }
        
        if (movement != null) {
            movement.setVelocity(0, 0);
        }
        
        previousTransform = null;
    }
    
    @Override
    public String getDescription() {
        return String.format("MoveCommand(entity=%d, direction=(%.1f,%.1f), justPressed=%b)", 
            entity != null ? entity.getId() : -1, directionX, directionY, justPressed);
    }
}