package rpg.game;

import rpg.systems.EventBus;
import rpg.utils.GameLogger;

import java.awt.Graphics2D;

/**
 * Abstract base class for game states providing common functionality.
 */
public abstract class AbstractGameState implements GameState {
    
    protected final String stateId;
    protected final EventBus eventBus;
    protected boolean initialized;
    
    public AbstractGameState(String stateId, EventBus eventBus) {
        this.stateId = stateId;
        this.eventBus = eventBus;
        this.initialized = false;
    }
    
    @Override
    public String getStateId() {
        return stateId;
    }
    
    @Override
    public void enter() {
        if (!initialized) {
            initialize();
            initialized = true;
        }
        
        onEnter();
        GameLogger.info("Entered state: " + stateId);
    }
    
    @Override
    public void exit() {
        onExit();
        GameLogger.info("Exited state: " + stateId);
    }
    
    @Override
    public void update(float deltaTime) {
        if (!initialized) {
            GameLogger.warn("State " + stateId + " is being updated before initialization");
            return;
        }
        
        onUpdate(deltaTime);
    }
    
    @Override
    public void render(Graphics2D g2) {
        if (!initialized) {
            return;
        }
        
        onRender(g2);
    }
    
    @Override
    public void handleInput(InputEvent inputEvent) {
        if (!initialized) {
            return;
        }
        
        onHandleInput(inputEvent);
    }
    
    @Override
    public boolean pausesUnderlyingStates() {
        return false; // Default: don't pause underlying states
    }
    
    @Override
    public boolean rendersOverUnderlyingStates() {
        return false; // Default: don't render over underlying states
    }
    
    /**
     * Initialize the state. Called once when the state is first entered.
     */
    protected abstract void initialize();
    
    /**
     * Called when entering the state (after initialization if needed).
     */
    protected abstract void onEnter();
    
    /**
     * Called when exiting the state.
     */
    protected abstract void onExit();
    
    /**
     * Update the state logic.
     * @param deltaTime time elapsed since last update
     */
    protected abstract void onUpdate(float deltaTime);
    
    /**
     * Render the state.
     * @param g2 graphics context
     */
    protected abstract void onRender(Graphics2D g2);
    
    /**
     * Handle input for the state.
     * @param inputEvent the input event
     */
    protected abstract void onHandleInput(InputEvent inputEvent);
    
    /**
     * Check if the state is initialized.
     * @return true if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
}