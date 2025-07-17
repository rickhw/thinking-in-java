package com.example.ec2dockerapi.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InstanceState enum.
 */
@DisplayName("InstanceState Tests")
class InstanceStateTest {
    
    @Test
    @DisplayName("Should return correct string values for all states")
    void shouldReturnCorrectStringValues() {
        assertEquals("pending", InstanceState.PENDING.getValue());
        assertEquals("running", InstanceState.RUNNING.getValue());
        assertEquals("stopping", InstanceState.STOPPING.getValue());
        assertEquals("stopped", InstanceState.STOPPED.getValue());
        assertEquals("rebooting", InstanceState.REBOOTING.getValue());
        assertEquals("terminating", InstanceState.TERMINATING.getValue());
        assertEquals("terminated", InstanceState.TERMINATED.getValue());
        assertEquals("error", InstanceState.ERROR.getValue());
    }
    
    @Test
    @DisplayName("Should identify TERMINATED as terminal state")
    void shouldIdentifyTerminatedAsTerminalState() {
        assertTrue(InstanceState.TERMINATED.isTerminal());
    }
    
    @ParameterizedTest
    @EnumSource(value = InstanceState.class, names = {"TERMINATED"}, mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("Should identify non-TERMINATED states as non-terminal")
    void shouldIdentifyNonTerminatedStatesAsNonTerminal(InstanceState state) {
        assertFalse(state.isTerminal());
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"pending", "running", "stopping", "stopped", "rebooting", "terminating", "terminated", "error"})
    @DisplayName("Should create InstanceState from valid string values")
    void shouldCreateInstanceStateFromValidStringValues(String value) {
        InstanceState state = InstanceState.fromValue(value);
        assertNotNull(state);
        assertEquals(value, state.getValue());
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid string value")
    void shouldThrowExceptionForInvalidStringValue() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> InstanceState.fromValue("invalid_state")
        );
        assertEquals("Unknown instance state: invalid_state", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException for null value")
    void shouldThrowExceptionForNullValue() {
        assertThrows(
            IllegalArgumentException.class,
            () -> InstanceState.fromValue(null)
        );
    }
    
    @Test
    @DisplayName("Should return string value in toString method")
    void shouldReturnStringValueInToString() {
        assertEquals("pending", InstanceState.PENDING.toString());
        assertEquals("running", InstanceState.RUNNING.toString());
        assertEquals("error", InstanceState.ERROR.toString());
    }
    
    @Test
    @DisplayName("Should have all expected enum values")
    void shouldHaveAllExpectedEnumValues() {
        InstanceState[] states = InstanceState.values();
        assertEquals(8, states.length);
        
        // Verify all expected states exist
        assertNotNull(InstanceState.valueOf("PENDING"));
        assertNotNull(InstanceState.valueOf("RUNNING"));
        assertNotNull(InstanceState.valueOf("STOPPING"));
        assertNotNull(InstanceState.valueOf("STOPPED"));
        assertNotNull(InstanceState.valueOf("REBOOTING"));
        assertNotNull(InstanceState.valueOf("TERMINATING"));
        assertNotNull(InstanceState.valueOf("TERMINATED"));
        assertNotNull(InstanceState.valueOf("ERROR"));
    }
    
    @Test
    @DisplayName("Should be case sensitive for fromValue method")
    void shouldBeCaseSensitiveForFromValue() {
        assertThrows(
            IllegalArgumentException.class,
            () -> InstanceState.fromValue("PENDING")
        );
        
        assertThrows(
            IllegalArgumentException.class,
            () -> InstanceState.fromValue("Running")
        );
    }
}