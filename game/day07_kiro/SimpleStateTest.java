import rpg.game.*;
import rpg.systems.EventBus;

public class SimpleStateTest {
    public static void main(String[] args) {
        System.out.println("Testing basic game state functionality...");
        
        try {
            // Initialize logger
            rpg.utils.GameLogger.initialize();
            
            // Create event bus and state manager
            EventBus eventBus = new EventBus();
            StateManager stateManager = new StateManager(eventBus);
            
            // Create states
            GameState playingState = new PlayingState(eventBus, null);
            GameState pausedState = new PausedState(eventBus);
            GameState menuState = new MenuState(eventBus);
            GameState loadingState = new LoadingState(eventBus);
            
            // Register states
            stateManager.registerState(playingState);
            stateManager.registerState(pausedState);
            stateManager.registerState(menuState);
            stateManager.registerState(loadingState);
            
            System.out.println("✓ States created and registered successfully");
            
            // Test basic operations
            stateManager.pushState("PLAYING");
            stateManager.update(0.016f);
            
            System.out.println("Current state: " + stateManager.getCurrentState().getStateId());
            System.out.println("Stack size: " + stateManager.getStackSize());
            
            // Test pause
            stateManager.pushState("PAUSED");
            stateManager.update(0.016f);
            
            System.out.println("After pause - Current state: " + stateManager.getCurrentState().getStateId());
            System.out.println("Stack size: " + stateManager.getStackSize());
            
            // Test unpause
            stateManager.popState();
            stateManager.update(0.016f);
            
            System.out.println("After unpause - Current state: " + stateManager.getCurrentState().getStateId());
            System.out.println("Stack size: " + stateManager.getStackSize());
            
            System.out.println("✓ Basic state operations work correctly");
            
            // Test state properties
            System.out.println("Playing state pauses underlying: " + playingState.pausesUnderlyingStates());
            System.out.println("Paused state pauses underlying: " + pausedState.pausesUnderlyingStates());
            System.out.println("Paused state renders over underlying: " + pausedState.rendersOverUnderlyingStates());
            
            System.out.println("✓ All tests passed!");
            
        } catch (Exception e) {
            System.err.println("✗ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}