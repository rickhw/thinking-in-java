package com.example.ec2dockerapi.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OperationType enum.
 */
@DisplayName("OperationType Tests")
class OperationTypeTest {
    
    @Test
    @DisplayName("Should return correct string values for all operation types")
    void shouldReturnCorrectStringValues() {
        assertEquals("create", OperationType.CREATE.getValue());
        assertEquals("start", OperationType.START.getValue());
        assertEquals("stop", OperationType.STOP.getValue());
        assertEquals("restart", OperationType.RESTART.getValue());
        assertEquals("terminate", OperationType.TERMINATE.getValue());
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"create", "start", "stop", "restart", "terminate"})
    @DisplayName("Should create OperationType from valid string values")
    void shouldCreateOperationTypeFromValidStringValues(String value) {
        OperationType type = OperationType.fromValue(value);
        assertNotNull(type);
        assertEquals(value, type.getValue());
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid string value")
    void shouldThrowExceptionForInvalidStringValue() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> OperationType.fromValue("invalid_operation")
        );
        assertEquals("Unknown operation type: invalid_operation", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException for null value")
    void shouldThrowExceptionForNullValue() {
        assertThrows(
            IllegalArgumentException.class,
            () -> OperationType.fromValue(null)
        );
    }
    
    @Test
    @DisplayName("Should return string value in toString method")
    void shouldReturnStringValueInToString() {
        assertEquals("create", OperationType.CREATE.toString());
        assertEquals("start", OperationType.START.toString());
        assertEquals("terminate", OperationType.TERMINATE.toString());
    }
    
    @Test
    @DisplayName("Should have all expected enum values")
    void shouldHaveAllExpectedEnumValues() {
        OperationType[] types = OperationType.values();
        assertEquals(5, types.length);
        
        // Verify all expected types exist
        assertNotNull(OperationType.valueOf("CREATE"));
        assertNotNull(OperationType.valueOf("START"));
        assertNotNull(OperationType.valueOf("STOP"));
        assertNotNull(OperationType.valueOf("RESTART"));
        assertNotNull(OperationType.valueOf("TERMINATE"));
    }
    
    @Test
    @DisplayName("Should be case sensitive for fromValue method")
    void shouldBeCaseSensitiveForFromValue() {
        assertThrows(
            IllegalArgumentException.class,
            () -> OperationType.fromValue("CREATE")
        );
        
        assertThrows(
            IllegalArgumentException.class,
            () -> OperationType.fromValue("Start")
        );
    }
    
    @Test
    @DisplayName("Should handle specific operation type mappings")
    void shouldHandleSpecificOperationTypeMappings() {
        assertEquals(OperationType.CREATE, OperationType.fromValue("create"));
        assertEquals(OperationType.START, OperationType.fromValue("start"));
        assertEquals(OperationType.STOP, OperationType.fromValue("stop"));
        assertEquals(OperationType.RESTART, OperationType.fromValue("restart"));
        assertEquals(OperationType.TERMINATE, OperationType.fromValue("terminate"));
    }
}