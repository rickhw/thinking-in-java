package rpg.systems;

import rpg.components.InputComponent;
import rpg.components.MovementComponent;
import rpg.engine.Entity;
import rpg.systems.commands.*;
import rpg.systems.commands.MenuCommand.MenuAction;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * System responsible for processing input and executing commands.
 * Uses command pattern for decoupled input handling.
 */
public class InputSystem extends GameSystem {
    private CommandQueue commandQueue;
    private Map<String, CommandFactory> commandFactories;
    private boolean[] keyStates;
    private boolean[] previousKeyStates;
    
    // Input settings
    private boolean acceptInput = true;
    private int maxQueueSize = 100;
    
    @Override
    public void initialize() {
        this.commandQueue = new CommandQueue(100, 50);
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
        commandFactories.put("interact", entity -> new InteractCommand(entity, eventBus));
        commandFactories.put("attack", entity -> new AttackCommand(entity, eventBus));
        commandFactories.put("run", entity -> new RunCommand(entity));
        
        // Menu commands
        commandFactories.put("menu", entity -> new MenuCommand(entity, MenuAction.TOGGLE_PAUSE, eventBus));
        commandFactories.put("pause", entity -> new MenuCommand(entity, MenuAction.TOGGLE_PAUSE, eventBus));
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
        List<Entity> inputEntities = entityManager.getEntitiesWithComponent(InputComponent.class);
        
        for (Entity entity : inputEntities) {
            processEntityInput(entity);
        }
        
        // Execute queued commands
        commandQueue.executeAll();
        
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
                    InputCommand inputCommand = (InputCommand) command;
                    inputCommand.setJustPressed(justPressed);
                    inputCommand.setJustReleased(false); // Will be set separately for release events
                }
                
                // Publish input event
                if (eventBus != null) {
                    InputEvent.InputType eventType = justPressed ? 
                        InputEvent.InputType.PRESSED : InputEvent.InputType.HELD;
                    eventBus.publish(new InputEvent(entity.getId(), action, eventType));
                }
                
                // Add to queue if not full
                commandQueue.enqueue(command);
            }
        }
    }
    
    /**
     * Undo the last executed command if possible.
     * @return true if a command was undone
     */
    public boolean undoLastCommand() {
        return commandQueue.undoLast();
    }
    
    /**
     * Get the current command queue size.
     * @return the number of queued commands
     */
    public int getQueuedCommandCount() {
        return commandQueue.getQueueSize();
    }
    
    /**
     * Clear all queued commands.
     */
    public void clearCommandQueue() {
        commandQueue.clearQueue();
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
    
