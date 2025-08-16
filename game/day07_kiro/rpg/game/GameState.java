package rpg.game;

import java.awt.Graphics2D;

/**
 * Interface for game states in the state machine.
 * Each state represents a different mode of the game (playing, paused, menu, etc.)
 */
public interface GameState {
    
    /**
     * Called when entering this state.
     * Use this to initialize state-specific resources and setup.
     */
    void enter();
    
    /**
     * Called when exiting this state.
     * Use this to cleanup state-specific resources.
     */
    void exit();
    
    /**
     * Update the state logic.
     * @param deltaTime time elapsed since last update in seconds
     */
    void update(float deltaTime);
    
    /**
     * Render the state.
     * @param g2 graphics context for rendering
     */
    void render(Graphics2D g2);
    
    /**
     * Handle input events for this state.
     * @param inputEvent the input event to handle
     */
    void handleInput(InputEvent inputEvent);
    
    /**
     * Get the unique identifier for this state.
     * @return state identifier
     */
    String getStateId();
    
    /**
     * Check if this state should pause the states below it in the stack.
     * @return true if this state pauses underlying states
     */
    boolean pausesUnderlyingStates();
    
    /**
     * Check if this state should render over the states below it in the stack.
     * @return true if this state renders over underlying states
     */
    boolean rendersOverUnderlyingStates();
}