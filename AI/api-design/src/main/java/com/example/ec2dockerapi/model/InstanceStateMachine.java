package com.example.ec2dockerapi.model;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * State machine component that manages valid state transitions for EC2-like instances.
 * This component defines the complete state transition rules including the TERMINATED state
 * and provides validation methods for state transitions.
 */
@Component
public class InstanceStateMachine {
    
    /**
     * Map defining valid state transitions.
     * Each state maps to a set of states it can transition to.
     */
    private static final Map<InstanceState, Set<InstanceState>> VALID_TRANSITIONS = Map.of(
        InstanceState.PENDING, Set.of(InstanceState.RUNNING, InstanceState.ERROR),
        InstanceState.RUNNING, Set.of(InstanceState.STOPPING, InstanceState.REBOOTING, InstanceState.TERMINATING),
        InstanceState.STOPPING, Set.of(InstanceState.STOPPED, InstanceState.ERROR),
        InstanceState.STOPPED, Set.of(InstanceState.PENDING, InstanceState.TERMINATING),
        InstanceState.REBOOTING, Set.of(InstanceState.RUNNING, InstanceState.ERROR),
        InstanceState.ERROR, Set.of(InstanceState.TERMINATING),
        InstanceState.TERMINATING, Set.of(InstanceState.TERMINATED, InstanceState.ERROR),
        InstanceState.TERMINATED, Set.of() // Terminal state - no transitions allowed
    );
    
    /**
     * Checks if a state transition is valid.
     * 
     * @param from the current state
     * @param to the target state
     * @return true if the transition is valid, false otherwise
     */
    public boolean canTransition(InstanceState from, InstanceState to) {
        if (from == null || to == null) {
            return false;
        }
        
        Set<InstanceState> validNextStates = VALID_TRANSITIONS.get(from);
        return validNextStates != null && validNextStates.contains(to);
    }
    
    /**
     * Validates a state transition and throws an exception if invalid.
     * 
     * @param from the current state
     * @param to the target state
     * @throws IllegalStateException if the transition is not valid
     */
    public void validateTransition(InstanceState from, InstanceState to) {
        if (!canTransition(from, to)) {
            throw new IllegalStateException(
                String.format("Invalid state transition from %s to %s", from.name(), to.name())
            );
        }
    }
    
    /**
     * Gets all valid next states for a given current state.
     * 
     * @param currentState the current state
     * @return set of valid next states, or empty set if no transitions are possible
     */
    public Set<InstanceState> getValidNextStates(InstanceState currentState) {
        if (currentState == null) {
            return Set.of();
        }
        
        Set<InstanceState> validStates = VALID_TRANSITIONS.get(currentState);
        return validStates != null ? Set.copyOf(validStates) : Set.of();
    }
    
    /**
     * Checks if a state is a terminal state (no further transitions possible).
     * 
     * @param state the state to check
     * @return true if the state is terminal, false otherwise
     */
    public boolean isTerminalState(InstanceState state) {
        if (state == null) {
            return false;
        }
        
        Set<InstanceState> validNextStates = VALID_TRANSITIONS.get(state);
        return validNextStates != null && validNextStates.isEmpty();
    }
    
    /**
     * Gets all possible states that can transition to the given target state.
     * 
     * @param targetState the target state
     * @return set of states that can transition to the target state
     */
    public Set<InstanceState> getStatesTransitioningTo(InstanceState targetState) {
        if (targetState == null) {
            return Set.of();
        }
        
        return VALID_TRANSITIONS.entrySet().stream()
            .filter(entry -> entry.getValue().contains(targetState))
            .map(Map.Entry::getKey)
            .collect(java.util.stream.Collectors.toSet());
    }
    
    /**
     * Checks if a state allows any outgoing transitions.
     * 
     * @param state the state to check
     * @return true if the state has valid outgoing transitions, false otherwise
     */
    public boolean hasOutgoingTransitions(InstanceState state) {
        if (state == null) {
            return false;
        }
        
        Set<InstanceState> validNextStates = VALID_TRANSITIONS.get(state);
        return validNextStates != null && !validNextStates.isEmpty();
    }
    
    /**
     * Gets the initial state for new instances.
     * 
     * @return the initial state (PENDING)
     */
    public InstanceState getInitialState() {
        return InstanceState.PENDING;
    }
}