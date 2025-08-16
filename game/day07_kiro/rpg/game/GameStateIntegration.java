package rpg.game;

import rpg.GamePanel;
import rpg.systems.EventBus;
import rpg.systems.GameStateEvent;
import rpg.systems.EventListener;
import rpg.utils.GameLogger;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

/**
 * Integration class that demonstrates how to use the game state system with the existing GamePanel.
 * This class shows how to integrate the new state management system with the current game architecture.
 */
public class GameStateIntegration {
    
    private final StateManager stateManager;
    private final EventBus eventBus;
    private final GamePanel gamePanel;
    
    public GameStateIntegration(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.eventBus = new EventBus();
        this.stateManager = new StateManager(eventBus);
        
        initializeStates();
        setupEventHandlers();
    }
    
    /**
     * Initialize and register all game states.
     */
    private void initializeStates() {
        GameLogger.info("Initializing game states");
        
        // Create states
        PlayingState playingState = new PlayingState(eventBus, gamePanel);
        PausedState pausedState = new PausedState(eventBus);
        MenuState menuState = new MenuState(eventBus);
        LoadingState loadingState = new LoadingState(eventBus);
        
        // Register states with the state manager
        stateManager.registerState(playingState);
        stateManager.registerState(pausedState);
        stateManager.registerState(menuState);
        stateManager.registerState(loadingState);
        
        // Start with the playing state
        stateManager.pushState("PLAYING");
    }
    
    /**
     * Set up event handlers for state changes.
     */
    private void setupEventHandlers() {
        eventBus.subscribe(GameStateEvent.class, new EventListener<GameStateEvent>() {
            @Override
            public void onEvent(GameStateEvent event) {
                handleStateChange(event);
            }
        });
    }
    
    /**
     * Handle state change events.
     */
    private void handleStateChange(GameStateEvent event) {
        GameLogger.info("State changed from " + event.getPreviousState() + " to " + event.getNewState());
        
        // Handle specific state transitions
        String newState = event.getNewState();
        if ("PLAYING".equals(newState)) {
            // Game resumed or started
            onGameResumed();
        } else if ("PAUSED".equals(newState)) {
            // Game paused
            onGamePaused();
        } else if ("MENU".equals(newState)) {
            // Entered menu
            onMenuEntered();
        }
    }
    
    /**
     * Called when the game is resumed or started.
     */
    private void onGameResumed() {
        GameLogger.info("Game resumed - enabling input and updates");
        // Enable game input and updates
    }
    
    /**
     * Called when the game is paused.
     */
    private void onGamePaused() {
        GameLogger.info("Game paused - disabling input and updates");
        // Disable game input and updates
    }
    
    /**
     * Called when entering a menu.
     */
    private void onMenuEntered() {
        GameLogger.info("Menu entered - switching to menu input mode");
        // Switch to menu input handling
    }
    
    /**
     * Update the state system. Should be called from the main game loop.
     */
    public void update(float deltaTime) {
        stateManager.update(deltaTime);
    }
    
    /**
     * Render the current state. Should be called from the main render loop.
     */
    public void render(Graphics2D g2) {
        stateManager.render(g2);
    }
    
    /**
     * Handle keyboard input. Should be called from the input system.
     */
    public void handleKeyPressed(int keyCode) {
        InputEvent inputEvent = new InputEvent(InputEvent.Type.KEY_PRESSED, keyCode);
        
        // Handle global input first
        if (keyCode == KeyEvent.VK_ESCAPE) {
            handleEscapeKey();
        } else {
            // Pass to current state
            stateManager.handleInput(inputEvent);
        }
    }
    
    /**
     * Handle keyboard input release.
     */
    public void handleKeyReleased(int keyCode) {
        InputEvent inputEvent = new InputEvent(InputEvent.Type.KEY_RELEASED, keyCode);
        stateManager.handleInput(inputEvent);
    }
    
    /**
     * Handle the escape key for pause/unpause functionality.
     */
    private void handleEscapeKey() {
        GameState currentState = stateManager.getCurrentState();
        if (currentState != null) {
            String stateId = currentState.getStateId();
            
            if ("PLAYING".equals(stateId)) {
                // Pause the game
                stateManager.pushState("PAUSED");
            } else if ("PAUSED".equals(stateId)) {
                // Unpause the game
                stateManager.popState();
            } else if ("MENU".equals(stateId)) {
                // Exit menu to playing state
                stateManager.changeState("PLAYING");
            }
        }
    }
    
    /**
     * Show the main menu.
     */
    public void showMenu() {
        stateManager.pushState("MENU");
    }
    
    /**
     * Show the loading screen.
     */
    public void showLoadingScreen() {
        stateManager.pushState("LOADING");
    }
    
    /**
     * Start a new game.
     */
    public void startNewGame() {
        stateManager.changeState("PLAYING");
    }
    
    /**
     * Get the current state ID.
     */
    public String getCurrentStateId() {
        GameState currentState = stateManager.getCurrentState();
        return currentState != null ? currentState.getStateId() : null;
    }
    
    /**
     * Check if the game is currently paused.
     */
    public boolean isPaused() {
        return "PAUSED".equals(getCurrentStateId());
    }
    
    /**
     * Check if the game is currently playing.
     */
    public boolean isPlaying() {
        return "PLAYING".equals(getCurrentStateId());
    }
    
    /**
     * Get the state manager for advanced operations.
     */
    public StateManager getStateManager() {
        return stateManager;
    }
    
    /**
     * Get the event bus for subscribing to events.
     */
    public EventBus getEventBus() {
        return eventBus;
    }
}