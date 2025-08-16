import rpg.systems.input.*;
import rpg.systems.commands.*;
import rpg.systems.EventBus;
import rpg.engine.Entity;
import rpg.components.InputComponent;
import rpg.components.MovementComponent;
import rpg.components.TransformComponent;

import java.awt.event.KeyEvent;

/**
 * Test class for the input system and command pattern implementation.
 */
public class TestInputSystem {
    public static void main(String[] args) {
        System.out.println("Testing Input System...");
        
        // Create event bus
        EventBus eventBus = new EventBus();
        
        // Create input manager
        InputManager inputManager = new InputManager(eventBus);
        
        // Test input configuration
        testInputConfiguration(inputManager);
        
        // Test command pattern
        testCommandPattern(inputManager);
        
        // Test device handlers
        testDeviceHandlers(inputManager);
        
        // Test configuration persistence
        testConfigurationPersistence(inputManager);
        
        System.out.println("Input System tests completed!");
    }
    
    private static void testInputConfiguration(InputManager inputManager) {
        System.out.println("\n=== Testing Input Configuration ===");
        
        InputConfiguration config = inputManager.getConfiguration();
        
        // Test default bindings
        System.out.println("Default key bindings:");
        for (var entry : config.getAllKeyBindings().entrySet()) {
            System.out.println("  " + entry.getKey() + " -> " + 
                InputConfiguration.getKeyName(entry.getValue()));
        }
        
        // Test custom binding
        config.bindKey("test_action", KeyEvent.VK_T);
        System.out.println("Added custom binding: test_action -> T");
        
        // Test validation
        var errors = config.validate();
        if (errors.isEmpty()) {
            System.out.println("Configuration validation: PASSED");
        } else {
            System.out.println("Configuration validation errors:");
            for (String error : errors) {
                System.out.println("  " + error);
            }
        }
        
        System.out.println("Configuration summary:");
        System.out.println(config.getSummary());
    }
    
    private static void testCommandPattern(InputManager inputManager) {
        System.out.println("\n=== Testing Command Pattern ===");
        
        // Create test entity
        Entity testEntity = new Entity();
        testEntity.addComponent(new TransformComponent());
        testEntity.addComponent(new MovementComponent());
        testEntity.addComponent(new InputComponent());
        
        // Register command factories
        inputManager.registerCommand("move_up", entity -> new MoveCommand(entity, 0, -1));
        inputManager.registerCommand("move_down", entity -> new MoveCommand(entity, 0, 1));
        inputManager.registerCommand("interact", entity -> new InteractCommand(entity));
        inputManager.registerCommand("attack", entity -> new AttackCommand(entity));
        
        // Add entity to input processing
        inputManager.addInputEntity(testEntity);
        
        System.out.println("Registered commands and test entity");
        
        // Test command queue
        CommandQueue queue = inputManager.getCommandQueue();
        System.out.println("Command queue size: " + queue.getQueueSize());
        System.out.println("Command queue max size: " + queue.getMaxQueueSize());
        
        // Test manual command creation and execution
        Command moveCommand = new MoveCommand(testEntity, 1, 0);
        queue.enqueue(moveCommand);
        System.out.println("Enqueued move command, queue size: " + queue.getQueueSize());
        
        int executed = queue.executeAll();
        System.out.println("Executed " + executed + " commands");
        
        // Test undo functionality
        Command interactCommand = new InteractCommand(testEntity);
        queue.enqueue(interactCommand);
        queue.executeAll();
        
        boolean undone = queue.undoLast();
        System.out.println("Undo last command: " + (undone ? "SUCCESS" : "FAILED"));
    }
    
    private static void testDeviceHandlers(InputManager inputManager) {
        System.out.println("\n=== Testing Device Handlers ===");
        
        // Test keyboard handler
        KeyboardHandler keyboardHandler = new KeyboardHandler();
        keyboardHandler.initialize();
        System.out.println("Keyboard handler: " + keyboardHandler.getStatusString());
        
        // Test mouse handler
        MouseHandler mouseHandler = new MouseHandler();
        mouseHandler.initialize();
        System.out.println("Mouse handler: " + mouseHandler.getStatusString());
        
        // Test device availability
        for (InputDevice device : InputDevice.values()) {
            System.out.println(device.getDisplayName() + ":");
            System.out.println("  Supports analog input: " + device.supportsAnalogInput());
            System.out.println("  Supports multiple inputs: " + device.supportsMultipleInputs());
            System.out.println("  Max button count: " + device.getMaxButtonCount());
        }
    }
    
    private static void testConfigurationPersistence(InputManager inputManager) {
        System.out.println("\n=== Testing Configuration Persistence ===");
        
        InputConfiguration config = inputManager.getConfiguration();
        
        // Modify configuration
        config.bindKey("custom_action_1", KeyEvent.VK_F1);
        config.bindKey("custom_action_2", KeyEvent.VK_F2);
        config.setSetting("test_setting", "test_value");
        
        // Test save
        String testFile = "test_input_config.properties";
        boolean saved = config.saveToFile(testFile);
        System.out.println("Save configuration: " + (saved ? "SUCCESS" : "FAILED"));
        
        // Reset and test load
        config.resetToDefaults();
        System.out.println("Reset to defaults");
        
        boolean loaded = config.loadFromFile(testFile);
        System.out.println("Load configuration: " + (loaded ? "SUCCESS" : "FAILED"));
        
        if (loaded) {
            System.out.println("Loaded custom bindings:");
            Integer f1Binding = config.getKeyBinding("custom_action_1");
            Integer f2Binding = config.getKeyBinding("custom_action_2");
            System.out.println("  custom_action_1: " + (f1Binding != null ? 
                InputConfiguration.getKeyName(f1Binding) : "NOT FOUND"));
            System.out.println("  custom_action_2: " + (f2Binding != null ? 
                InputConfiguration.getKeyName(f2Binding) : "NOT FOUND"));
            
            String testSetting = config.getSetting("test_setting", "default");
            System.out.println("  test_setting: " + testSetting);
        }
        
        // Cleanup test file
        try {
            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(testFile));
            System.out.println("Cleaned up test file");
        } catch (Exception e) {
            System.out.println("Failed to cleanup test file: " + e.getMessage());
        }
    }
}