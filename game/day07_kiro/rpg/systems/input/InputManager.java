package rpg.systems.input;

import rpg.systems.commands.Command;
import rpg.systems.commands.CommandFactory;
import rpg.systems.commands.CommandQueue;
import rpg.engine.Entity;
import rpg.systems.EventBus;
import rpg.systems.InputEvent;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;

/**
 * Manages input devices and key bindings with configurable mappings.
 * Supports multiple input devices and runtime key binding modification.
 */
public class InputManager implements KeyListener, MouseListener {
    private final Map<String, CommandFactory> commandFactories;
    private final CommandQueue commandQueue;
    private final EventBus eventBus;
    private final InputConfiguration inputConfig;
    private final Map<InputDevice, InputDeviceHandler> deviceHandlers;
    
    // Input state tracking
    private final boolean[] keyStates;
    private final boolean[] previousKeyStates;
    private final boolean[] mouseButtonStates;
    private final boolean[] previousMouseButtonStates;
    
    // Input processing settings
    private boolean acceptInput;
    private int inputPriority;
    private final Set<Entity> inputEntities;
    
    /**
     * Create a new InputManager.
     * @param eventBus the event bus for publishing input events
     */
    public InputManager(EventBus eventBus) {
        this.commandFactories = new HashMap<>();
        this.commandQueue = new CommandQueue();
        this.eventBus = eventBus;
        this.inputConfig = new InputConfiguration();
        this.deviceHandlers = new HashMap<>();
        
        this.keyStates = new boolean[256];
        this.previousKeyStates = new boolean[256];
        this.mouseButtonStates = new boolean[8];
        this.previousMouseButtonStates = new boolean[8];
        
        this.acceptInput = true;
        this.inputPriority = 0;
        this.inputEntities = new HashSet<>();
        
        // Initialize default device handlers
        initializeDeviceHandlers();
    }
    
    /**
     * Initialize default input device handlers.
     */
    private void initializeDeviceHandlers() {
        deviceHandlers.put(InputDevice.KEYBOARD, new KeyboardHandler());
        deviceHandlers.put(InputDevice.MOUSE, new MouseHandler());
        // Additional device handlers can be added here (gamepad, touch, etc.)
    }
    
    /**
     * Register a command factory for an action.
     * @param action the action name
     * @param factory the command factory
     */
    public void registerCommand(String action, CommandFactory factory) {
        commandFactories.put(action, factory);
    }
    
    /**
     * Unregister a command factory.
     * @param action the action name to unregister
     */
    public void unregisterCommand(String action) {
        commandFactories.remove(action);
    }
    
    /**
     * Bind a key to an action.
     * @param action the action name
     * @param keyCode the key code
     */
    public void bindKey(String action, int keyCode) {
        inputConfig.bindKey(action, keyCode);
    }
    
    /**
     * Bind a mouse button to an action.
     * @param action the action name
     * @param mouseButton the mouse button code
     */
    public void bindMouseButton(String action, int mouseButton) {
        inputConfig.bindMouseButton(action, mouseButton);
    }
    
    /**
     * Unbind an action from all inputs.
     * @param action the action name to unbind
     */
    public void unbindAction(String action) {
        inputConfig.unbindAction(action);
    }
    
    /**
     * Get the current key binding for an action.
     * @param action the action name
     * @return the key code, or null if not bound
     */
    public Integer getKeyBinding(String action) {
        return inputConfig.getKeyBinding(action);
    }
    
    /**
     * Get all current key bindings.
     * @return a map of action names to key codes
     */
    public Map<String, Integer> getAllKeyBindings() {
        return inputConfig.getAllKeyBindings();
    }
    
    /**
     * Load input configuration from file.
     * @param filename the configuration file path
     * @return true if loaded successfully
     */
    public boolean loadConfiguration(String filename) {
        return inputConfig.loadFromFile(filename);
    }
    
    /**
     * Save input configuration to file.
     * @param filename the configuration file path
     * @return true if saved successfully
     */
    public boolean saveConfiguration(String filename) {
        return inputConfig.saveToFile(filename);
    }
    
    /**
     * Reset to default key bindings.
     */
    public void resetToDefaults() {
        inputConfig.resetToDefaults();
    }
    
    /**
     * Add an entity to receive input processing.
     * @param entity the entity to add
     */
    public void addInputEntity(Entity entity) {
        inputEntities.add(entity);
    }
    
    /**
     * Remove an entity from input processing.
     * @param entity the entity to remove
     */
    public void removeInputEntity(Entity entity) {
        inputEntities.remove(entity);
    }
    
    /**
     * Process input for all registered entities.
     * Should be called once per frame.
     */
    public void processInput() {
        if (!acceptInput) return;
        
        // Process input for each registered entity
        for (Entity entity : inputEntities) {
            processEntityInput(entity);
        }
        
        // Execute queued commands
        commandQueue.executeAll();
        
        // Update previous input states
        System.arraycopy(keyStates, 0, previousKeyStates, 0, keyStates.length);
        System.arraycopy(mouseButtonStates, 0, previousMouseButtonStates, 0, mouseButtonStates.length);
    }
    
    /**
     * Process input for a specific entity.
     * @param entity the entity to process input for
     */
    private void processEntityInput(Entity entity) {
        // Check all bound actions for this entity
        for (Map.Entry<String, Integer> binding : inputConfig.getAllKeyBindings().entrySet()) {
            String action = binding.getKey();
            int keyCode = binding.getValue();
            
            if (keyCode >= 0 && keyCode < keyStates.length) {
                // Handle key press
                if (keyStates[keyCode] && !previousKeyStates[keyCode]) {
                    queueCommand(action, entity, true, false);
                }
                
                // Handle key release
                if (!keyStates[keyCode] && previousKeyStates[keyCode]) {
                    queueCommand(action, entity, false, true);
                }
                
                // Handle continuous press
                if (keyStates[keyCode]) {
                    queueCommand(action, entity, false, false);
                }
            }
        }
        
        // Process mouse button bindings
        for (Map.Entry<String, Integer> binding : inputConfig.getAllMouseBindings().entrySet()) {
            String action = binding.getKey();
            int mouseButton = binding.getValue();
            
            if (mouseButton >= 0 && mouseButton < mouseButtonStates.length) {
                // Handle mouse button press
                if (mouseButtonStates[mouseButton] && !previousMouseButtonStates[mouseButton]) {
                    queueCommand(action, entity, true, false);
                }
                
                // Handle mouse button release
                if (!mouseButtonStates[mouseButton] && previousMouseButtonStates[mouseButton]) {
                    queueCommand(action, entity, false, true);
                }
                
                // Handle continuous press
                if (mouseButtonStates[mouseButton]) {
                    queueCommand(action, entity, false, false);
                }
            }
        }
    }
    
    /**
     * Queue a command for execution.
     * @param action the action name
     * @param entity the target entity
     * @param justPressed true if this is a press event
     * @param justReleased true if this is a release event
     */
    private void queueCommand(String action, Entity entity, boolean justPressed, boolean justReleased) {
        CommandFactory factory = commandFactories.get(action);
        if (factory != null) {
            Command command = factory.createCommand(entity);
            if (command != null) {
                // Set command properties
                if (command instanceof rpg.systems.commands.InputCommand) {
                    rpg.systems.commands.InputCommand inputCommand = (rpg.systems.commands.InputCommand) command;
                    inputCommand.setJustPressed(justPressed);
                    inputCommand.setJustReleased(justReleased);
                }
                
                // Publish input event
                if (eventBus != null) {
                    InputEvent.InputType eventType;
                    if (justPressed) {
                        eventType = InputEvent.InputType.PRESSED;
                    } else if (justReleased) {
                        eventType = InputEvent.InputType.RELEASED;
                    } else {
                        eventType = InputEvent.InputType.HELD;
                    }
                    eventBus.publish(new InputEvent(entity.getId(), action, eventType));
                }
                
                // Add to queue
                commandQueue.enqueue(command);
            }
        }
    }
    
    /**
     * Set whether input should be accepted.
     * @param acceptInput true to accept input
     */
    public void setAcceptInput(boolean acceptInput) {
        this.acceptInput = acceptInput;
        if (!acceptInput) {
            commandQueue.clearQueue();
        }
    }
    
    /**
     * Check if input is currently being accepted.
     * @return true if input is accepted
     */
    public boolean isAcceptingInput() {
        return acceptInput;
    }
    
    /**
     * Get the input configuration.
     * @return the input configuration
     */
    public InputConfiguration getConfiguration() {
        return inputConfig;
    }
    
    /**
     * Get the command queue.
     * @return the command queue
     */
    public CommandQueue getCommandQueue() {
        return commandQueue;
    }
    
    // KeyListener implementation
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode >= 0 && keyCode < keyStates.length) {
            keyStates[keyCode] = true;
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode >= 0 && keyCode < keyStates.length) {
            keyStates[keyCode] = false;
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        // Not used for game input
    }
    
    // MouseListener implementation
    @Override
    public void mousePressed(MouseEvent e) {
        int button = e.getButton();
        if (button >= 0 && button < mouseButtonStates.length) {
            mouseButtonStates[button] = true;
        }
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        int button = e.getButton();
        if (button >= 0 && button < mouseButtonStates.length) {
            mouseButtonStates[button] = false;
        }
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        // Handled by pressed/released events
    }
    
    @Override
    public void mouseEntered(MouseEvent e) {
        // Not used for game input
    }
    
    @Override
    public void mouseExited(MouseEvent e) {
        // Not used for game input
    }
    
    /**
     * Cleanup resources.
     */
    public void cleanup() {
        commandQueue.clear();
        commandFactories.clear();
        inputEntities.clear();
        deviceHandlers.clear();
    }
}