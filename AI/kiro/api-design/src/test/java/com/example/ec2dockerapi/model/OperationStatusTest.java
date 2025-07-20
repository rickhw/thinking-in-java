package com.example.ec2dockerapi.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OperationStatus enum.
 */
@DisplayName("OperationStatus Tests")
class OperationStatusTest {
    
    @Test
    @DisplayName("Should return correct string values for all operation statuses")
    void shouldReturnCorrectStringValues() {
        assertEquals("in_progress", OperationStatus.IN_PROGRESS.getValue());
        assertEquals("completed", OperationStatus.COMPLETED.getValue());
        assertEquals("failed", OperationStatus.FAILED.getValue());
    }
    
    @Test
    @DisplayName("Should identify completed statuses correctly")
    void shouldIdentifyCompletedStatusesCorrectly() {
        assertTrue(OperationStatus.COMPLETED.isCompleted());
        assertTrue(OperationStatus.FAILED.isCompleted());
        assertFalse(OperationStatus.IN_PROGRESS.isCompleted());
    }
    
    @Test
    @DisplayName("Should identify successful status correctly")
    void shouldIdentifySuccessfulStatusCorrectly() {
        assertTrue(OperationStatus.COMPLETED.isSuccess());
        assertFalse(OperationStatus.FAILED.isSuccess());
        assertFalse(OperationStatus.IN_PROGRESS.isSuccess());
    }
    
    @Test
    @DisplayName("Should identify failed status correctly")
    void shouldIdentifyFailedStatusCorrectly() {
        assertTrue(OperationStatus.FAILED.isFailed());
        assertFalse(OperationStatus.COMPLETED.isFailed());
        assertFalse(OperationStatus.IN_PROGRESS.isFailed());
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"in_progress", "completed", "failed"})
    @DisplayName("Should create OperationStatus from valid string values")
    void shouldCreateOperationStatusFromValidStringValues(String value) {
        OperationStatus status = OperationStatus.fromValue(value);
        assertNotNull(status);
        assertEquals(value, status.getValue());
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid string value")
    void shouldThrowExceptionForInvalidStringValue() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> OperationStatus.fromValue("invalid_status")
        );
        assertEquals("Unknown operation status: invalid_status", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException for null value")
    void shouldThrowExceptionForNullValue() {
        assertThrows(
            IllegalArgumentException.class,
            () -> OperationStatus.fromValue(null)
        );
    }
    
    @Test
    @DisplayName("Should return string value in toString method")
    void shouldReturnStringValueInToString() {
        assertEquals("in_progress", OperationStatus.IN_PROGRESS.toString());
        assertEquals("completed", OperationStatus.COMPLETED.toString());
        assertEquals("failed", OperationStatus.FAILED.toString());
    }
    
    @Test
    @DisplayName("Should have all expected enum values")
    void shouldHaveAllExpectedEnumValues() {
        OperationStatus[] statuses = OperationStatus.values();
        assertEquals(3, statuses.length);
        
        // Verify all expected statuses exist
        assertNotNull(OperationStatus.valueOf("IN_PROGRESS"));
        assertNotNull(OperationStatus.valueOf("COMPLETED"));
        assertNotNull(OperationStatus.valueOf("FAILED"));
    }
    
    @Test
    @DisplayName("Should be case sensitive for fromValue method")
    void shouldBeCaseSensitiveForFromValue() {
        assertThrows(
            IllegalArgumentException.class,
            () -> OperationStatus.fromValue("IN_PROGRESS")
        );
        
        assertThrows(
            IllegalArgumentException.class,
            () -> OperationStatus.fromValue("Completed")
        );
    }
    
    @Test
    @DisplayName("Should handle specific operation status mappings")
    void shouldHandleSpecificOperationStatusMappings() {
        assertEquals(OperationStatus.IN_PROGRESS, OperationStatus.fromValue("in_progress"));
        assertEquals(OperationStatus.COMPLETED, OperationStatus.fromValue("completed"));
        assertEquals(OperationStatus.FAILED, OperationStatus.fromValue("failed"));
    }
    
    @ParameterizedTest
    @EnumSource(OperationStatus.class)
    @DisplayName("Should have consistent behavior between status methods")
    void shouldHaveConsistentBehaviorBetweenStatusMethods(OperationStatus status) {
        // If a status is completed, it should be either success or failed (but not both)
        if (status.isCompleted()) {
            assertTrue(status.isSuccess() ^ status.isFailed(), 
                "Completed status should be either success or failed, but not both");
        }
        
        // If a status is not completed, it should not be success or failed
        if (!status.isCompleted()) {
            assertFalse(status.isSuccess(), "Non-completed status should not be success");
            assertFalse(status.isFailed(), "Non-completed status should not be failed");
        }
    }
}