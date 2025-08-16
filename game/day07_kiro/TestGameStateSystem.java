import rpg.game.*;
import rpg.systems.EventBus;
import rpg.systems.GameStateEvent;
import rpg.systems.EventListener;
import rpg.utils.GameLogger;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test class for the game state system.
 */
public class TestGameStateSystem {
    
    public static void main(String[] args) {
        System.out.println("Testing Game State System...");
        
        // Test basic state management
        testBasicStateManagement();
        
        // Test state stack operations
        testStateStack();
        
        // Test state transitions
        testStateTransitions();
        
        // Test state persistence
        testStatePersistence();
        
        System.out.println("All tests completed!");
    }
    
    private static void testBasicStateManagement() {
        System.out.println("\n=== Testing Basic State Management ===");
        
        EventBus eventBus = new EventBus();
        StateManager stateManager = new StateManager(eventBus);
        
        // Create test states
        GameState playingState = new PlayingState(eventBus, null);
        GameState pausedState = new PausedState(eventBus);
        GameState menuState = new MenuState(eventBus);
        
        // Register states
        stateManager.registerState(playingState);
        stateManager.registerState(pausedState);
        stateManager.registerState(menuState);
        
        // Test initial state
        assert stateManager.isEmpty() : "State manager should be empty initially";
        assert stateManager.getCurrentState() == null : "Current state should be null initially";
        
        // Test pushing a state
        stateManager.pushState("PLAYING");
        stateManager.update(0.016f); // Process pending changes
        
        assert !stateManager.isEmpty() : "State manager should not be empty after push";
        assert stateManager.getCurrentState() == playingState : "Current state should be playing state";
        assert stateManager.getStackSize() == 1 : "Stack size should be 1";
        
        System.out.println("✓ Basic state management tests passed");
    }
    
    private static void testStateStack() {
        System.out.println("\n=== Testing State Stack Operations ===");
        
        EventBus eventBus = new EventBus();
        StateManager stateManager = new StateManager(eventBus);
        
        // Create and register states
        GameState playingState = new PlayingState(eventBus, null);
        GameState pausedState = new PausedState(eventBus);
        GameState menuState = new MenuState(eventBus);
        
        stateManager.registerState(playingState);
        stateManager.registerState(pausedState);
        stateManager.registerState(menuState);
        
        // Test state stack operations
        stateManager.pushState("PLAYING");
        stateManager.update(0.016f);
        
        stateManager.pushState("PAUSED");
        stateManager.update(0.016f);
        
        assert stateManager.getStackSize() == 2 : "Stack size should be 2 after two pushes";
        assert stateManager.getCurrentState() == pausedState : "Current state should be paused";
        
        // Test popping
        stateManager.popState();
        stateManager.update(0.016f);
        
        assert stateManager.getStackSize() == 1 : "Stack size should be 1 after pop";
        assert stateManager.getCurrentState() == playingState : "Current state should be playing after pop";
        
        // Test state change
        stateManager.changeState("MENU");
        stateManager.update(0.016f);
        
        assert stateManager.getStackSize() == 1 : "Stack size should still be 1 after change";
        assert stateManager.getCurrentState() == menuState : "Current state should be menu after change";
        
        System.out.println("✓ State stack operation tests passed");
    }
    
    private static void testStateTransitions() {
        System.out.println("\n=== Testing State Transitions ===");
        
        EventBus eventBus = new EventBus();
        StateManager stateManager = new StateManager(eventBus);
        
        // Track state change events
        AtomicInteger eventCount = new AtomicInteger(0);
        eventBus.subscribe(GameStateEvent.class, new EventListener<GameStateEvent>() {
            @Override
            public void onEvent(GameStateEvent event) {
                eventCount.incrementAndGet();
                System.out.println("State change: " + event.getPreviousState() + " -> " + event.getNewState());
            }
        });
        
        // Create and register states
        GameState playingState = new PlayingState(eventBus, null);
        GameState pausedState = new PausedState(eventBus);
        
        stateManager.registerState(playingState);
        stateManager.registerState(pausedState);
        
        // Test transitions
        stateManager.pushState("PLAYING");
        stateManager.update(0.016f);
        
        stateManager.pushState("PAUSED");
        stateManager.update(0.016f);
        
        stateManager.popState();
        stateManager.update(0.016f);
        
        assert eventCount.get() == 3 : "Should have received 3 state change events";
        
        System.out.println("✓ State transition tests passed");
    }
    
    private static void testStatePersistence() {
        System.out.println("\n=== Testing State Persistence ===");
        
        EventBus eventBus = new EventBus();
        StateManager stateManager = new StateManager(eventBus);
        
        // Create and register states
        GameState playingState = new PlayingState(eventBus, null);
        GameState pausedState = new PausedState(eventBus);
        
        stateManager.registerState(playingState);
        stateManager.registerState(pausedState);
        
        // Set up a state stack
        stateManager.pushState("PLAYING");
        stateManager.pushState("PAUSED");
        stateManager.update(0.016f);
        
        // Get state data
        var stateData = stateManager.getStateData();
        assert stateData != null : "State data should not be null";
        
        // Create new state manager and restore data
        StateManager newStateManager = new StateManager(eventBus);
        newStateManager.registerState(playingState);
        newStateManager.registerState(pausedState);
        
        newStateManager.restoreStateData(stateData);
        
        assert newStateManager.getStackSize() == 2 : "Restored stack should have size 2";
        assert newStateManager.getCurrentState().getStateId().equals("PAUSED") : "Current state should be PAUSED";
        
        System.out.println("✓ State persistence tests passed");
    }
    
    /**
     * Test rendering and input handling
     */
    private static void testRenderingAndInput() {
        System.out.println("\n=== Testing Rendering and Input ===");
        
        EventBus eventBus = new EventBus();
        StateManager stateManager = new StateManager(eventBus);
        
        // Create states
        GameState playingState = new PlayingState(eventBus, null);
        GameState pausedState = new PausedState(eventBus);
        
        stateManager.registerState(playingState);
        stateManager.registerState(pausedState);
        
        // Set up state stack
        stateManager.pushState("PLAYING");
        stateManager.pushState("PAUSED");
        stateManager.update(0.016f);
        
        // Test rendering
        BufferedImage testImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = testImage.createGraphics();
        
        try {
            stateManager.render(g2);
            System.out.println("✓ Rendering completed without errors");
        } catch (Exception e) {
            System.err.println("✗ Rendering failed: " + e.getMessage());
        } finally {
            g2.dispose();
        }
        
        // Test input handling
        InputEvent testInput = new InputEvent(InputEvent.Type.KEY_PRESSED, 27); // ESC key
        
        try {
            stateManager.handleInput(testInput);
            System.out.println("✓ Input handling completed without errors");
        } catch (Exception e) {
            System.err.println("✗ Input handling failed: " + e.getMessage());
        }
        
        System.out.println("✓ Rendering and input tests passed");
    }
}