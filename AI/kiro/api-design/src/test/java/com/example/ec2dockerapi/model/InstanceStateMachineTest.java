package com.example.ec2dockerapi.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InstanceStateMachine component.
 */
@DisplayName("InstanceStateMachine Tests")
class InstanceStateMachineTest {
    
    private InstanceStateMachine stateMachine;
    
    @BeforeEach
    void setUp() {
        stateMachine = new InstanceStateMachine();
    }
    
    @Test
    @DisplayName("Should allow valid transitions from PENDING state")
    void shouldAllowValidTransitionsFromPending() {
        assertTrue(stateMachine.canTransition(InstanceState.PENDING, InstanceState.RUNNING));
        assertTrue(stateMachine.canTransition(InstanceState.PENDING, InstanceState.ERROR));
    }
    
    @Test
    @DisplayName("Should reject invalid transitions from PENDING state")
    void shouldRejectInvalidTransitionsFromPending() {
        assertFalse(stateMachine.canTransition(InstanceState.PENDING, InstanceState.STOPPED));
        assertFalse(stateMachine.canTransition(InstanceState.PENDING, InstanceState.STOPPING));
        assertFalse(stateMachine.canTransition(InstanceState.PENDING, InstanceState.REBOOTING));
        assertFalse(stateMachine.canTransition(InstanceState.PENDING, InstanceState.TERMINATING));
        assertFalse(stateMachine.canTransition(InstanceState.PENDING, InstanceState.TERMINATED));
    }
    
    @Test
    @DisplayName("Should allow valid transitions from RUNNING state")
    void shouldAllowValidTransitionsFromRunning() {
        assertTrue(stateMachine.canTransition(InstanceState.RUNNING, InstanceState.STOPPING));
        assertTrue(stateMachine.canTransition(InstanceState.RUNNING, InstanceState.REBOOTING));
        assertTrue(stateMachine.canTransition(InstanceState.RUNNING, InstanceState.TERMINATING));
    }
    
    @Test
    @DisplayName("Should reject invalid transitions from RUNNING state")
    void shouldRejectInvalidTransitionsFromRunning() {
        assertFalse(stateMachine.canTransition(InstanceState.RUNNING, InstanceState.PENDING));
        assertFalse(stateMachine.canTransition(InstanceState.RUNNING, InstanceState.STOPPED));
        assertFalse(stateMachine.canTransition(InstanceState.RUNNING, InstanceState.ERROR));
        assertFalse(stateMachine.canTransition(InstanceState.RUNNING, InstanceState.TERMINATED));
    }
    
    @Test
    @DisplayName("Should allow valid transitions from STOPPING state")
    void shouldAllowValidTransitionsFromStopping() {
        assertTrue(stateMachine.canTransition(InstanceState.STOPPING, InstanceState.STOPPED));
        assertTrue(stateMachine.canTransition(InstanceState.STOPPING, InstanceState.ERROR));
    }
    
    @Test
    @DisplayName("Should reject invalid transitions from STOPPING state")
    void shouldRejectInvalidTransitionsFromStopping() {
        assertFalse(stateMachine.canTransition(InstanceState.STOPPING, InstanceState.PENDING));
        assertFalse(stateMachine.canTransition(InstanceState.STOPPING, InstanceState.RUNNING));
        assertFalse(stateMachine.canTransition(InstanceState.STOPPING, InstanceState.REBOOTING));
        assertFalse(stateMachine.canTransition(InstanceState.STOPPING, InstanceState.TERMINATING));
        assertFalse(stateMachine.canTransition(InstanceState.STOPPING, InstanceState.TERMINATED));
    }
    
    @Test
    @DisplayName("Should allow valid transitions from STOPPED state")
    void shouldAllowValidTransitionsFromStopped() {
        assertTrue(stateMachine.canTransition(InstanceState.STOPPED, InstanceState.PENDING));
        assertTrue(stateMachine.canTransition(InstanceState.STOPPED, InstanceState.TERMINATING));
    }
    
    @Test
    @DisplayName("Should reject invalid transitions from STOPPED state")
    void shouldRejectInvalidTransitionsFromStopped() {
        assertFalse(stateMachine.canTransition(InstanceState.STOPPED, InstanceState.RUNNING));
        assertFalse(stateMachine.canTransition(InstanceState.STOPPED, InstanceState.STOPPING));
        assertFalse(stateMachine.canTransition(InstanceState.STOPPED, InstanceState.REBOOTING));
        assertFalse(stateMachine.canTransition(InstanceState.STOPPED, InstanceState.ERROR));
        assertFalse(stateMachine.canTransition(InstanceState.STOPPED, InstanceState.TERMINATED));
    }
    
    @Test
    @DisplayName("Should allow valid transitions from REBOOTING state")
    void shouldAllowValidTransitionsFromRebooting() {
        assertTrue(stateMachine.canTransition(InstanceState.REBOOTING, InstanceState.RUNNING));
        assertTrue(stateMachine.canTransition(InstanceState.REBOOTING, InstanceState.ERROR));
    }
    
    @Test
    @DisplayName("Should reject invalid transitions from REBOOTING state")
    void shouldRejectInvalidTransitionsFromRebooting() {
        assertFalse(stateMachine.canTransition(InstanceState.REBOOTING, InstanceState.PENDING));
        assertFalse(stateMachine.canTransition(InstanceState.REBOOTING, InstanceState.STOPPING));
        assertFalse(stateMachine.canTransition(InstanceState.REBOOTING, InstanceState.STOPPED));
        assertFalse(stateMachine.canTransition(InstanceState.REBOOTING, InstanceState.TERMINATING));
        assertFalse(stateMachine.canTransition(InstanceState.REBOOTING, InstanceState.TERMINATED));
    }
    
    @Test
    @DisplayName("Should allow valid transitions from ERROR state")
    void shouldAllowValidTransitionsFromError() {
        assertTrue(stateMachine.canTransition(InstanceState.ERROR, InstanceState.TERMINATING));
    }
    
    @Test
    @DisplayName("Should reject invalid transitions from ERROR state")
    void shouldRejectInvalidTransitionsFromError() {
        assertFalse(stateMachine.canTransition(InstanceState.ERROR, InstanceState.PENDING));
        assertFalse(stateMachine.canTransition(InstanceState.ERROR, InstanceState.RUNNING));
        assertFalse(stateMachine.canTransition(InstanceState.ERROR, InstanceState.STOPPING));
        assertFalse(stateMachine.canTransition(InstanceState.ERROR, InstanceState.STOPPED));
        assertFalse(stateMachine.canTransition(InstanceState.ERROR, InstanceState.REBOOTING));
        assertFalse(stateMachine.canTransition(InstanceState.ERROR, InstanceState.TERMINATED));
    }
    
    @Test
    @DisplayName("Should allow valid transitions from TERMINATING state")
    void shouldAllowValidTransitionsFromTerminating() {
        assertTrue(stateMachine.canTransition(InstanceState.TERMINATING, InstanceState.TERMINATED));
        assertTrue(stateMachine.canTransition(InstanceState.TERMINATING, InstanceState.ERROR));
    }
    
    @Test
    @DisplayName("Should reject invalid transitions from TERMINATING state")
    void shouldRejectInvalidTransitionsFromTerminating() {
        assertFalse(stateMachine.canTransition(InstanceState.TERMINATING, InstanceState.PENDING));
        assertFalse(stateMachine.canTransition(InstanceState.TERMINATING, InstanceState.RUNNING));
        assertFalse(stateMachine.canTransition(InstanceState.TERMINATING, InstanceState.STOPPING));
        assertFalse(stateMachine.canTransition(InstanceState.TERMINATING, InstanceState.STOPPED));
        assertFalse(stateMachine.canTransition(InstanceState.TERMINATING, InstanceState.REBOOTING));
    }
    
    @Test
    @DisplayName("Should reject all transitions from TERMINATED state")
    void shouldRejectAllTransitionsFromTerminated() {
        for (InstanceState targetState : InstanceState.values()) {
            assertFalse(stateMachine.canTransition(InstanceState.TERMINATED, targetState),
                "TERMINATED should not transition to " + targetState);
        }
    }
    
    @Test
    @DisplayName("Should handle null states in canTransition")
    void shouldHandleNullStatesInCanTransition() {
        assertFalse(stateMachine.canTransition(null, InstanceState.RUNNING));
        assertFalse(stateMachine.canTransition(InstanceState.PENDING, null));
        assertFalse(stateMachine.canTransition(null, null));
    }
    
    @Test
    @DisplayName("Should validate valid transitions without throwing exception")
    void shouldValidateValidTransitionsWithoutException() {
        assertDoesNotThrow(() -> stateMachine.validateTransition(InstanceState.PENDING, InstanceState.RUNNING));
        assertDoesNotThrow(() -> stateMachine.validateTransition(InstanceState.RUNNING, InstanceState.STOPPING));
        assertDoesNotThrow(() -> stateMachine.validateTransition(InstanceState.TERMINATING, InstanceState.TERMINATED));
    }
    
    @Test
    @DisplayName("Should throw exception for invalid transitions")
    void shouldThrowExceptionForInvalidTransitions() {
        IllegalStateException exception1 = assertThrows(IllegalStateException.class,
            () -> stateMachine.validateTransition(InstanceState.PENDING, InstanceState.STOPPED));
        assertEquals("Invalid state transition from PENDING to STOPPED", exception1.getMessage());
        
        IllegalStateException exception2 = assertThrows(IllegalStateException.class,
            () -> stateMachine.validateTransition(InstanceState.TERMINATED, InstanceState.RUNNING));
        assertEquals("Invalid state transition from TERMINATED to RUNNING", exception2.getMessage());
    }
    
    @Test
    @DisplayName("Should return correct valid next states for each state")
    void shouldReturnCorrectValidNextStates() {
        assertEquals(Set.of(InstanceState.RUNNING, InstanceState.ERROR), 
            stateMachine.getValidNextStates(InstanceState.PENDING));
        
        assertEquals(Set.of(InstanceState.STOPPING, InstanceState.REBOOTING, InstanceState.TERMINATING), 
            stateMachine.getValidNextStates(InstanceState.RUNNING));
        
        assertEquals(Set.of(InstanceState.STOPPED, InstanceState.ERROR), 
            stateMachine.getValidNextStates(InstanceState.STOPPING));
        
        assertEquals(Set.of(InstanceState.PENDING, InstanceState.TERMINATING), 
            stateMachine.getValidNextStates(InstanceState.STOPPED));
        
        assertEquals(Set.of(InstanceState.RUNNING, InstanceState.ERROR), 
            stateMachine.getValidNextStates(InstanceState.REBOOTING));
        
        assertEquals(Set.of(InstanceState.TERMINATING), 
            stateMachine.getValidNextStates(InstanceState.ERROR));
        
        assertEquals(Set.of(InstanceState.TERMINATED, InstanceState.ERROR), 
            stateMachine.getValidNextStates(InstanceState.TERMINATING));
        
        assertEquals(Set.of(), 
            stateMachine.getValidNextStates(InstanceState.TERMINATED));
    }
    
    @Test
    @DisplayName("Should return empty set for null state in getValidNextStates")
    void shouldReturnEmptySetForNullStateInGetValidNextStates() {
        assertEquals(Set.of(), stateMachine.getValidNextStates(null));
    }
    
    @Test
    @DisplayName("Should identify TERMINATED as terminal state")
    void shouldIdentifyTerminatedAsTerminalState() {
        assertTrue(stateMachine.isTerminalState(InstanceState.TERMINATED));
    }
    
    @ParameterizedTest
    @EnumSource(value = InstanceState.class, names = {"TERMINATED"}, mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("Should identify non-TERMINATED states as non-terminal")
    void shouldIdentifyNonTerminatedStatesAsNonTerminal(InstanceState state) {
        assertFalse(stateMachine.isTerminalState(state));
    }
    
    @Test
    @DisplayName("Should return false for null state in isTerminalState")
    void shouldReturnFalseForNullStateInIsTerminalState() {
        assertFalse(stateMachine.isTerminalState(null));
    }
    
    @Test
    @DisplayName("Should return states that can transition to target state")
    void shouldReturnStatesTransitioningToTargetState() {
        assertEquals(Set.of(InstanceState.PENDING, InstanceState.REBOOTING), 
            stateMachine.getStatesTransitioningTo(InstanceState.RUNNING));
        
        assertEquals(Set.of(InstanceState.PENDING, InstanceState.STOPPING, InstanceState.REBOOTING, InstanceState.TERMINATING), 
            stateMachine.getStatesTransitioningTo(InstanceState.ERROR));
        
        assertEquals(Set.of(InstanceState.TERMINATING), 
            stateMachine.getStatesTransitioningTo(InstanceState.TERMINATED));
        
        assertEquals(Set.of(InstanceState.STOPPED), 
            stateMachine.getStatesTransitioningTo(InstanceState.PENDING));
    }
    
    @Test
    @DisplayName("Should return empty set for null target state in getStatesTransitioningTo")
    void shouldReturnEmptySetForNullTargetStateInGetStatesTransitioningTo() {
        assertEquals(Set.of(), stateMachine.getStatesTransitioningTo(null));
    }
    
    @Test
    @DisplayName("Should correctly identify states with outgoing transitions")
    void shouldCorrectlyIdentifyStatesWithOutgoingTransitions() {
        assertTrue(stateMachine.hasOutgoingTransitions(InstanceState.PENDING));
        assertTrue(stateMachine.hasOutgoingTransitions(InstanceState.RUNNING));
        assertTrue(stateMachine.hasOutgoingTransitions(InstanceState.STOPPING));
        assertTrue(stateMachine.hasOutgoingTransitions(InstanceState.STOPPED));
        assertTrue(stateMachine.hasOutgoingTransitions(InstanceState.REBOOTING));
        assertTrue(stateMachine.hasOutgoingTransitions(InstanceState.ERROR));
        assertTrue(stateMachine.hasOutgoingTransitions(InstanceState.TERMINATING));
        
        assertFalse(stateMachine.hasOutgoingTransitions(InstanceState.TERMINATED));
    }
    
    @Test
    @DisplayName("Should return false for null state in hasOutgoingTransitions")
    void shouldReturnFalseForNullStateInHasOutgoingTransitions() {
        assertFalse(stateMachine.hasOutgoingTransitions(null));
    }
    
    @Test
    @DisplayName("Should return PENDING as initial state")
    void shouldReturnPendingAsInitialState() {
        assertEquals(InstanceState.PENDING, stateMachine.getInitialState());
    }
    
    @Test
    @DisplayName("Should validate state machine graph completeness")
    void shouldValidateStateMachineGraphCompleteness() {
        // Verify all states are covered in the transition map
        for (InstanceState state : InstanceState.values()) {
            Set<InstanceState> validNextStates = stateMachine.getValidNextStates(state);
            assertNotNull(validNextStates, "State " + state + " should have valid next states defined");
        }
        
        // Verify the state machine graph matches the design document requirements
        // PENDING can go to RUNNING or ERROR
        assertTrue(stateMachine.canTransition(InstanceState.PENDING, InstanceState.RUNNING));
        assertTrue(stateMachine.canTransition(InstanceState.PENDING, InstanceState.ERROR));
        
        // RUNNING can go to STOPPING, REBOOTING, or TERMINATING
        assertTrue(stateMachine.canTransition(InstanceState.RUNNING, InstanceState.STOPPING));
        assertTrue(stateMachine.canTransition(InstanceState.RUNNING, InstanceState.REBOOTING));
        assertTrue(stateMachine.canTransition(InstanceState.RUNNING, InstanceState.TERMINATING));
        
        // STOPPING can go to STOPPED or ERROR
        assertTrue(stateMachine.canTransition(InstanceState.STOPPING, InstanceState.STOPPED));
        assertTrue(stateMachine.canTransition(InstanceState.STOPPING, InstanceState.ERROR));
        
        // STOPPED can go to PENDING or TERMINATING
        assertTrue(stateMachine.canTransition(InstanceState.STOPPED, InstanceState.PENDING));
        assertTrue(stateMachine.canTransition(InstanceState.STOPPED, InstanceState.TERMINATING));
        
        // REBOOTING can go to RUNNING or ERROR
        assertTrue(stateMachine.canTransition(InstanceState.REBOOTING, InstanceState.RUNNING));
        assertTrue(stateMachine.canTransition(InstanceState.REBOOTING, InstanceState.ERROR));
        
        // ERROR can only go to TERMINATING
        assertTrue(stateMachine.canTransition(InstanceState.ERROR, InstanceState.TERMINATING));
        
        // TERMINATING can go to TERMINATED or ERROR
        assertTrue(stateMachine.canTransition(InstanceState.TERMINATING, InstanceState.TERMINATED));
        assertTrue(stateMachine.canTransition(InstanceState.TERMINATING, InstanceState.ERROR));
        
        // TERMINATED is terminal - no outgoing transitions
        assertFalse(stateMachine.hasOutgoingTransitions(InstanceState.TERMINATED));
    }
}