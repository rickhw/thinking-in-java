package rpg.systems;

import rpg.components.InputComponent;
import rpg.components.MovementComponent;
import rpg.engine.Entity;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

/**
 * System responsible for processing input and executing commands.
 * Uses command pattern for decoupled input handling.
 */
public class InputSystem extends GameSystem {
    private Queue<Command> commandQueue;
    private Map<String, CommandFactory> commandFactories;
    private boolean[] keyStates;
    private boolean[] previousKeyStates;
    
    // Input settings
    private boolean acceptInput = true;
    private int maxQueueSize = 100;
    
    @Override
    public void initialize() {
        this.commandQueue = new LinkedList<>();
        this.commandFactories = new HashMap<>();
        this.keyStates = new boolean[256];
        this.previousKeyStates = new boolean[256];
        
        // Register default command factories
        registerDefaultCommands();
    }
    
    private void registerDefaultCommands() {
        // Movement commands
        commandFactories.put("move_up", entity -> new MoveCommand(entity, 0, -1));
        commandFactories.put("move_down", entity -> new MoveCommand(entity, 0, 1));
        commandFactories.put("move_left", entity -> new MoveCommand(entity, -1, 0));
        commandFactories.put("move_right", entity -> new MoveCommand(entity, 1, 0));
        
        // Alternative movement commands
        commandFactories.put("move_up_alt", entity -> new MoveCommand(entity, 0, -1));
        commandFactories.put("move_down_alt", entity -> new MoveCommand(entity, 0, 1));
        commandFactories.put("move_left_alt", entity -> new MoveCommand(entity, -1, 0));
        commandFactories.put("move_right_alt", entity -> new MoveCommand(entity, 1, 0));
        
        // Action commands
        commandFactories.put("interact", entity -> new InteractCommand(entity));
        commandFactories.put("attack", entity -> new AttackCommand(entity));
        commandFactories.put("run", entity -> new RunCommand(entity));
    }
    
    /**
     * Register a command factory for an action
     */
    public void registerCommand(String action, CommandFactory factory) {
        commandFactories.put(action, factory);
    }
    
    /**
     * Process a key press event
     */
    public void processKeyPressed(int keyCode) {
        if (!acceptInput || keyCode < 0 || keyCode >= keyStates.length) return;
        keyStates[keyCode] = true;
    }
    
    /**
     * Process a key release event
     */
    public void processKeyReleased(int keyCode) {
        if (!acceptInput || keyCode < 0 || keyCode >= keyStates.length) return;
        keyStates[keyCode] = false;
    }
    
    /**
     * Set whether the input system accepts input
     */
    public void setAcceptInput(boolean acceptInput) {
        this.acceptInput = acceptInput;
        if (!acceptInput) {
            commandQueue.clear();
        }
    }
    
    @Override
    public void update(float deltaTime) {
        if (!isEnabled() || !acceptInput) return;
        
        // Process input for all entities with input components
        List<Entity> inputEntities = entityManager.getEntitiesWith(InputComponent.class);
        
        for (Entity entity : inputEntities) {
            processEntityInput(entity);
        }
        
        // Execute queued commands
        executeCommands();
        
        // Update previous key states
        System.arraycopy(keyStates, 0, previousKeyStates, 0, keyStates.length);
    }
    
    private void processEntityInput(Entity entity) {
        InputComponent input = entity.getComponent(InputComponent.class);
        if (input == null || !input.isAcceptingInput()) return;
        
        // Update input component with current key states
        for (Map.Entry<String, Integer> binding : input.getAllBindings().entrySet()) {
            String action = binding.getKey();
            int keyCode = binding.getValue();
            
            if (keyCode >= 0 && keyCode < keyStates.length) {
                // Handle key press
                if (keyStates[keyCode] && !previousKeyStates[keyCode]) {
                    input.processKeyPressed(keyCode);
                    queueCommand(action, entity, true);
                }
                
                // Handle key release
                if (!keyStates[keyCode] && previousKeyStates[keyCode]) {
                    input.processKeyReleased(keyCode);
                    queueCommand(action, entity, false);
                }
                
                // Handle continuous press
                if (keyStates[keyCode]) {
                    queueCommand(action, entity, false);
                }
            }
        }
        
        // Update input component
        input.update(0);
    }
    
    private void queueCommand(String action, Entity entity, boolean justPressed) {
        CommandFactory factory = commandFactories.get(action);
        if (factory != null) {
            Command command = factory.createCommand(entity);
            if (command != null) {
                // Set command properties
                if (command instanceof InputCommand) {
                    ((InputCommand) command).setJustPressed(justPressed);
                }
                
                // Add to queue if not full
                if (commandQueue.size() < maxQueueSize) {
                    commandQueue.offer(command);
                }
            }
        }
    }
    
    private void executeCommands() {
        while (!commandQueue.isEmpty()) {
            Command command = commandQueue.poll();
            if (command != null) {
                command.execute();
            }
        }
    }
    
    @Override
    public void cleanup() {
        if (commandQueue != null) {
            commandQueue.clear();
        }
        if (commandFactories != null) {
            commandFactories.clear();
        }
    }
    
    @Override
    public int getPriority() {
        return 50; // Input should be processed early
    }
    
    // Command interfaces and implementations
    
    /**
     * Base interface for all commands
     */
    public interface Command {
        void execute();
        void undo();
    }
    
    /**
     * Factory interface for creating commands
     */
    public interface CommandFactory {
        Command createCommand(Entity entity);
    }
    
    /**
     * Base class for input-related commands
     */
    public abstract static class InputCommand implements Command {
        protected Entity entity;
        protected boolean justPressed;
        
        public InputCommand(Entity entity) {
            this.entity = entity;
            this.justPressed = false;
        }
        
        public void setJustPressed(boolean justPressed) {
            this.justPressed = justPressed;
        }
        
        @Override
        public void undo() {
            // Default implementation - most input commands can't be undone
        }
    }
    
    /**
     * Movement command implementation
     */
    public static class MoveCommand extends InputCommand {
        private final float directionX;
        private final float directionY;
        
        public MoveCommand(Entity entity, float directionX, float directionY) {
            super(entity);
            this.directionX = directionX;
            this.directionY = directionY;
        }
        
        @Override
        public void execute() {
            MovementComponent movement = entity.getComponent(MovementComponent.class);
            InputComponent input = entity.getComponent(InputComponent.class);
            
            if (movement == null || input == null || !movement.canMove) return;
            
            // Get movement vector from input component
            float[] moveVector = input.getMovementVector();
            
            // Apply movement based on input
            float speed = input.isRunning() ? movement.maxSpeed * 1.5f : movement.maxSpeed;
            movement.setVelocity(moveVector[0] * speed, moveVector[1] * speed);
        }
    }
    
    /**
     * Interact command implementation
     */
    public static class InteractCommand extends InputCommand {
        public InteractCommand(Entity entity) {
            super(entity);
        }
        
        @Override
        public void execute() {
            if (!justPressed) return; // Only execute on key press, not hold
            
            // Interaction logic will be implemented when needed
            // For now, just log the interaction
            System.out.println("Entity " + entity.getId() + " interacted");
        }
    }
    
    /**
     * Attack command implementation
     */
    public static class AttackCommand extends InputCommand {
        public AttackCommand(Entity entity) {
            super(entity);
        }
        
        @Override
        public void execute() {
            if (!justPressed) return; // Only execute on key press, not hold
            
            // Attack logic will be implemented when needed
            // For now, just log the attack
            System.out.println("Entity " + entity.getId() + " attacked");
        }
    }
    
    /**
     * Run command implementation
     */
    public static class RunCommand extends InputCommand {
        public RunCommand(Entity entity) {
            super(entity);
        }
        
        @Override
        public void execute() {
            // Running is handled in MoveCommand by checking input.isRunning()
            // This command exists for consistency but doesn't need to do anything
        }
    }
}