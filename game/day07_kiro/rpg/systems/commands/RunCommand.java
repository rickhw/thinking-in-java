package rpg.systems.commands;

import rpg.engine.Entity;
import rpg.components.MovementComponent;
import rpg.components.InputComponent;

/**
 * Command for handling entity running/sprinting actions.
 * Modifies movement speed and manages stamina if applicable.
 */
public class RunCommand extends InputCommand {
    private boolean wasRunning;
    private float previousMaxSpeed;
    
    /**
     * Create a run command.
     * @param entity the entity that should run
     */
    public RunCommand(Entity entity) {
        super(entity);
    }
    
    @Override
    protected boolean shouldExecute() {
        if (!super.shouldExecute()) return false;
        
        MovementComponent movement = entity.getComponent(MovementComponent.class);
        InputComponent input = entity.getComponent(InputComponent.class);
        
        return movement != null && input != null && movement.canMove;
    }
    
    @Override
    protected void executeInput() {
        MovementComponent movement = entity.getComponent(MovementComponent.class);
        InputComponent input = entity.getComponent(InputComponent.class);
        
        if (movement == null || input == null) return;
        
        // Store previous state for undo
        wasRunning = input.isRunning();
        previousMaxSpeed = movement.maxSpeed;
        
        // Running is handled by checking input.isRunning() in MoveCommand
        // This command exists primarily for consistency and potential future features
        // like stamina management, run animations, or sound effects
        
        if (input.isRunning()) {
            handleRunning(movement, input);
        } else {
            handleWalking(movement, input);
        }
    }
    
    /**
     * Handle the running state.
     * @param movement the movement component
     * @param input the input component
     */
    private void handleRunning(MovementComponent movement, InputComponent input) {
        // TODO: Implement running-specific logic
        // This could include:
        // - Stamina consumption
        // - Different animation states
        // - Sound effects (footsteps)
        // - Particle effects (dust clouds)
        // - Movement restrictions (can't run while carrying heavy items)
        
        System.out.println("Entity " + entity.getId() + " is running");
    }
    
    /**
     * Handle the walking state.
     * @param movement the movement component
     * @param input the input component
     */
    private void handleWalking(MovementComponent movement, InputComponent input) {
        // TODO: Implement walking-specific logic
        // This could include:
        // - Stamina regeneration
        // - Different animation states
        // - Sound effects (different footsteps)
        
        if (wasRunning) {
            System.out.println("Entity " + entity.getId() + " stopped running");
        }
    }
    
    @Override
    public boolean canUndo() {
        // Running state changes can potentially be undone
        return true;
    }
    
    @Override
    public void undo() {
        MovementComponent movement = entity.getComponent(MovementComponent.class);
        if (movement != null && previousMaxSpeed > 0) {
            movement.maxSpeed = previousMaxSpeed;
        }
        
        // Note: We can't directly undo the input state since that's controlled
        // by actual key presses, but we can undo movement-related changes
    }
    
    @Override
    public String getDescription() {
        InputComponent input = entity != null ? entity.getComponent(InputComponent.class) : null;
        boolean isCurrentlyRunning = input != null && input.isRunning();
        
        return String.format("RunCommand(entity=%d, running=%b, wasRunning=%b)", 
            entity != null ? entity.getId() : -1, isCurrentlyRunning, wasRunning);
    }
}