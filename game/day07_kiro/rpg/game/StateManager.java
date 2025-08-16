package rpg.game;

import rpg.systems.EventBus;
import rpg.systems.GameStateEvent;
import rpg.utils.GameLogger;

import java.awt.Graphics2D;
import java.util.*;

/**
 * Manages game states using a stack-based approach.
 * Supports state transitions, nested states (like pause over gameplay), and state persistence.
 */
public class StateManager {
    
    private final Stack<GameState> stateStack;
    private final Map<String, GameState> registeredStates;
    private final EventBus eventBus;
    private final StateTransitionManager transitionManager;
    private GameState pendingState;
    private boolean pendingPop;
    
    public StateManager(EventBus eventBus) {
        this.stateStack = new Stack<>();
        this.registeredStates = new HashMap<>();
        this.eventBus = eventBus;
        this.transitionManager = new StateTransitionManager();
        this.pendingState = null;
        this.pendingPop = false;
    }
    
    /**
     * Register a state with the manager.
     * @param state the state to register
     */
    public void registerState(GameState state) {
        if (state == null) {
            throw new IllegalArgumentException("State cannot be null");
        }
        
        String stateId = state.getStateId();
        if (registeredStates.containsKey(stateId)) {
            GameLogger.warn("State with ID '" + stateId + "' is already registered. Overwriting.");
        }
        
        registeredStates.put(stateId, state);
        GameLogger.info("Registered state: " + stateId);
    }
    
    /**
     * Push a state onto the stack by ID.
     * @param stateId the ID of the state to push
     */
    public void pushState(String stateId) {
        GameState state = registeredStates.get(stateId);
        if (state == null) {
            GameLogger.error("Cannot push unregistered state: " + stateId);
            return;
        }
        
        pushState(state);
    }
    
    /**
     * Push a state onto the stack.
     * @param state the state to push
     */
    public void pushState(GameState state) {
        if (state == null) {
            GameLogger.error("Cannot push null state");
            return;
        }
        
        // Store the pending state to be processed in the next update
        pendingState = state;
        GameLogger.info("Queued state push: " + state.getStateId());
    }
    
    /**
     * Pop the current state from the stack.
     */
    public void popState() {
        if (stateStack.isEmpty()) {
            GameLogger.warn("Cannot pop state: stack is empty");
            return;
        }
        
        pendingPop = true;
        GameLogger.info("Queued state pop");
    }
    
    /**
     * Change to a different state (pop current and push new).
     * @param stateId the ID of the new state
     */
    public void changeState(String stateId) {
        GameState state = registeredStates.get(stateId);
        if (state == null) {
            GameLogger.error("Cannot change to unregistered state: " + stateId);
            return;
        }
        
        changeState(state);
    }
    
    /**
     * Change to a different state (pop current and push new).
     * @param state the new state
     */
    public void changeState(GameState state) {
        if (state == null) {
            GameLogger.error("Cannot change to null state");
            return;
        }
        
        // Pop current state and push new one
        if (!stateStack.isEmpty()) {
            popState();
        }
        pushState(state);
    }
    
    /**
     * Get the current active state.
     * @return the current state, or null if stack is empty
     */
    public GameState getCurrentState() {
        return stateStack.isEmpty() ? null : stateStack.peek();
    }
    
    /**
     * Get all states in the stack (bottom to top).
     * @return list of states in the stack
     */
    public List<GameState> getStateStack() {
        return new ArrayList<>(stateStack);
    }
    
    /**
     * Check if the state stack is empty.
     * @return true if no states are active
     */
    public boolean isEmpty() {
        return stateStack.isEmpty();
    }
    
    /**
     * Get the number of states in the stack.
     * @return stack size
     */
    public int getStackSize() {
        return stateStack.size();
    }
    
    /**
     * Update all active states.
     * States that are paused by overlying states will not be updated.
     * @param deltaTime time elapsed since last update
     */
    public void update(float deltaTime) {
        // Process pending state changes first
        processPendingStateChanges();
        
        if (stateStack.isEmpty()) {
            return;
        }
        
        // Update transition effects
        transitionManager.update(deltaTime);
        
        // Find which states should be updated (not paused by overlying states)
        List<GameState> statesToUpdate = new ArrayList<>();
        
        for (int i = stateStack.size() - 1; i >= 0; i--) {
            GameState state = stateStack.get(i);
            statesToUpdate.add(0, state); // Add to front to maintain order
            
            // If this state pauses underlying states, stop here
            if (state.pausesUnderlyingStates()) {
                break;
            }
        }
        
        // Update states from bottom to top
        for (GameState state : statesToUpdate) {
            try {
                state.update(deltaTime);
            } catch (Exception e) {
                GameLogger.error("Error updating state " + state.getStateId() + ": " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Render all active states.
     * @param g2 graphics context
     */
    public void render(Graphics2D g2) {
        if (stateStack.isEmpty()) {
            return;
        }
        
        // Find which states should be rendered
        List<GameState> statesToRender = new ArrayList<>();
        
        for (int i = stateStack.size() - 1; i >= 0; i--) {
            GameState state = stateStack.get(i);
            statesToRender.add(0, state); // Add to front to maintain order
            
            // If this state doesn't render over underlying states, stop here
            if (!state.rendersOverUnderlyingStates()) {
                break;
            }
        }
        
        // Render states from bottom to top
        for (GameState state : statesToRender) {
            try {
                state.render(g2);
            } catch (Exception e) {
                GameLogger.error("Error rendering state " + state.getStateId() + ": " + e.getMessage(), e);
            }
        }
        
        // Render transition effects on top
        transitionManager.render(g2);
    }
    
    /**
     * Handle input for the current active state.
     * @param inputEvent the input event
     */
    public void handleInput(InputEvent inputEvent) {
        if (stateStack.isEmpty()) {
            return;
        }
        
        // Only the top state handles input
        GameState currentState = stateStack.peek();
        try {
            currentState.handleInput(inputEvent);
        } catch (Exception e) {
            GameLogger.error("Error handling input in state " + currentState.getStateId() + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Process pending state changes.
     * This is done at the beginning of update to ensure clean state transitions.
     */
    private void processPendingStateChanges() {
        // Handle pending pop first
        if (pendingPop) {
            if (!stateStack.isEmpty()) {
                GameState exitingState = stateStack.pop();
                String previousStateId = exitingState.getStateId();
                
                try {
                    exitingState.exit();
                } catch (Exception e) {
                    GameLogger.error("Error exiting state " + previousStateId + ": " + e.getMessage(), e);
                }
                
                String newStateId = stateStack.isEmpty() ? null : stateStack.peek().getStateId();
                publishStateChangeEvent(previousStateId, newStateId);
                
                GameLogger.info("Popped state: " + previousStateId);
            }
            pendingPop = false;
        }
        
        // Handle pending push
        if (pendingState != null) {
            String previousStateId = stateStack.isEmpty() ? null : stateStack.peek().getStateId();
            String newStateId = pendingState.getStateId();
            
            stateStack.push(pendingState);
            
            try {
                pendingState.enter();
            } catch (Exception e) {
                GameLogger.error("Error entering state " + newStateId + ": " + e.getMessage(), e);
            }
            
            publishStateChangeEvent(previousStateId, newStateId);
            
            GameLogger.info("Pushed state: " + newStateId);
            pendingState = null;
        }
    }
    
    /**
     * Publish a state change event.
     */
    private void publishStateChangeEvent(String previousStateId, String newStateId) {
        if (eventBus != null) {
            GameStateEvent event = new GameStateEvent(previousStateId, newStateId);
            eventBus.publish(event);
        }
    }
    
    /**
     * Clear all states from the stack.
     */
    public void clearStates() {
        while (!stateStack.isEmpty()) {
            popState();
        }
        // Process the pops immediately
        processPendingStateChanges();
    }
    
    /**
     * Get state persistence data for save/load functionality.
     * @return map of state data that can be serialized
     */
    public Map<String, Object> getStateData() {
        Map<String, Object> data = new HashMap<>();
        
        // Save current stack state IDs
        List<String> stackStateIds = new ArrayList<>();
        for (GameState state : stateStack) {
            stackStateIds.add(state.getStateId());
        }
        data.put("stateStack", stackStateIds);
        
        return data;
    }
    
    /**
     * Restore state from persistence data.
     * @param data the state data to restore
     */
    public void restoreStateData(Map<String, Object> data) {
        if (data == null) {
            return;
        }
        
        // Clear current states
        clearStates();
        
        // Restore state stack
        @SuppressWarnings("unchecked")
        List<String> stackStateIds = (List<String>) data.get("stateStack");
        if (stackStateIds != null) {
            for (String stateId : stackStateIds) {
                pushState(stateId);
            }
            // Process all pushes immediately
            while (pendingState != null) {
                processPendingStateChanges();
            }
        }
    }
}