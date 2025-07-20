package com.example.ec2dockerapi.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AsyncOperation entity.
 */
@DisplayName("AsyncOperation Entity Tests")
class AsyncOperationTest {
    
    private AsyncOperation operation;
    private final String operationId = "op-1234567890abcdef0";
    private final String instanceId = "i-1234567890abcdef0";
    
    @BeforeEach
    void setUp() {
        operation = new AsyncOperation(operationId, instanceId, OperationType.CREATE);
    }
    
    @Test
    @DisplayName("Should create operation with constructor parameters")
    void shouldCreateOperationWithConstructorParameters() {
        assertNotNull(operation);
        assertEquals(operationId, operation.getOperationId());
        assertEquals(instanceId, operation.getInstanceId());
        assertEquals(OperationType.CREATE, operation.getType());
        assertEquals(OperationStatus.IN_PROGRESS, operation.getStatus());
        assertNotNull(operation.getCreatedAt());
        assertNull(operation.getCompletedAt());
        assertNull(operation.getErrorMessage());
        assertNull(operation.getResult());
    }
    
    @Test
    @DisplayName("Should set createdAt on construction")
    void shouldSetCreatedAtOnConstruction() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        AsyncOperation newOperation = new AsyncOperation("test-op", "test-instance", OperationType.START);
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        
        assertTrue(newOperation.getCreatedAt().isAfter(before));
        assertTrue(newOperation.getCreatedAt().isBefore(after));
    }
    
    @Test
    @DisplayName("Should set completedAt when status changes to completed")
    void shouldSetCompletedAtWhenStatusChangesToCompleted() {
        assertNull(operation.getCompletedAt());
        
        operation.setStatus(OperationStatus.COMPLETED);
        
        assertNotNull(operation.getCompletedAt());
        assertEquals(OperationStatus.COMPLETED, operation.getStatus());
    }
    
    @Test
    @DisplayName("Should set completedAt when status changes to failed")
    void shouldSetCompletedAtWhenStatusChangesToFailed() {
        assertNull(operation.getCompletedAt());
        
        operation.setStatus(OperationStatus.FAILED);
        
        assertNotNull(operation.getCompletedAt());
        assertEquals(OperationStatus.FAILED, operation.getStatus());
    }
    
    @Test
    @DisplayName("Should not overwrite completedAt if already set")
    void shouldNotOverwriteCompletedAtIfAlreadySet() {
        LocalDateTime originalTime = LocalDateTime.now().minusMinutes(5);
        operation.setCompletedAt(originalTime);
        operation.setStatus(OperationStatus.COMPLETED);
        
        assertEquals(originalTime, operation.getCompletedAt());
    }
    
    @Test
    @DisplayName("Should complete operation successfully")
    void shouldCompleteOperationSuccessfully() {
        String result = "Operation completed successfully";
        
        operation.complete(result);
        
        assertEquals(OperationStatus.COMPLETED, operation.getStatus());
        assertEquals(result, operation.getResult());
        assertNotNull(operation.getCompletedAt());
        assertNull(operation.getErrorMessage());
    }
    
    @Test
    @DisplayName("Should fail operation with error message")
    void shouldFailOperationWithErrorMessage() {
        String errorMessage = "Docker container failed to start";
        
        operation.fail(errorMessage);
        
        assertEquals(OperationStatus.FAILED, operation.getStatus());
        assertEquals(errorMessage, operation.getErrorMessage());
        assertNotNull(operation.getCompletedAt());
        assertNull(operation.getResult());
    }
    
    @Test
    @DisplayName("Should update status with message for completed operation")
    void shouldUpdateStatusWithMessageForCompletedOperation() {
        String result = "Success result";
        
        operation.updateStatus(OperationStatus.COMPLETED, result);
        
        assertEquals(OperationStatus.COMPLETED, operation.getStatus());
        assertEquals(result, operation.getResult());
        assertNotNull(operation.getCompletedAt());
        assertNull(operation.getErrorMessage());
    }
    
    @Test
    @DisplayName("Should update status with message for failed operation")
    void shouldUpdateStatusWithMessageForFailedOperation() {
        String errorMessage = "Failure message";
        
        operation.updateStatus(OperationStatus.FAILED, errorMessage);
        
        assertEquals(OperationStatus.FAILED, operation.getStatus());
        assertEquals(errorMessage, operation.getErrorMessage());
        assertNotNull(operation.getCompletedAt());
        assertNull(operation.getResult());
    }
    
    @Test
    @DisplayName("Should clear previous result when failing")
    void shouldClearPreviousResultWhenFailing() {
        operation.setResult("Previous result");
        
        operation.fail("Error occurred");
        
        assertEquals(OperationStatus.FAILED, operation.getStatus());
        assertEquals("Error occurred", operation.getErrorMessage());
        assertNull(operation.getResult());
    }
    
    @Test
    @DisplayName("Should clear previous error when completing")
    void shouldClearPreviousErrorWhenCompleting() {
        operation.setErrorMessage("Previous error");
        
        operation.complete("Success");
        
        assertEquals(OperationStatus.COMPLETED, operation.getStatus());
        assertEquals("Success", operation.getResult());
        assertNull(operation.getErrorMessage());
    }
    
    @Test
    @DisplayName("Should correctly identify in progress status")
    void shouldCorrectlyIdentifyInProgressStatus() {
        assertTrue(operation.isInProgress());
        assertFalse(operation.isCompleted());
        assertFalse(operation.isSuccess());
        assertFalse(operation.isFailed());
    }
    
    @Test
    @DisplayName("Should correctly identify completed status")
    void shouldCorrectlyIdentifyCompletedStatus() {
        operation.setStatus(OperationStatus.COMPLETED);
        
        assertFalse(operation.isInProgress());
        assertTrue(operation.isCompleted());
        assertTrue(operation.isSuccess());
        assertFalse(operation.isFailed());
    }
    
    @Test
    @DisplayName("Should correctly identify failed status")
    void shouldCorrectlyIdentifyFailedStatus() {
        operation.setStatus(OperationStatus.FAILED);
        
        assertFalse(operation.isInProgress());
        assertTrue(operation.isCompleted());
        assertFalse(operation.isSuccess());
        assertTrue(operation.isFailed());
    }
    
    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        AsyncOperation operation1 = new AsyncOperation("same-id", "instance1", OperationType.CREATE);
        AsyncOperation operation2 = new AsyncOperation("same-id", "instance2", OperationType.START);
        AsyncOperation operation3 = new AsyncOperation("different-id", "instance1", OperationType.CREATE);
        
        // Same operation ID should be equal
        assertEquals(operation1, operation2);
        
        // Different operation ID should not be equal
        assertNotEquals(operation1, operation3);
        
        // Should not be equal to null
        assertNotEquals(operation1, null);
        
        // Should not be equal to different class
        assertNotEquals(operation1, "string");
        
        // Should be equal to itself
        assertEquals(operation1, operation1);
    }
    
    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        AsyncOperation operation1 = new AsyncOperation("same-id", "instance1", OperationType.CREATE);
        AsyncOperation operation2 = new AsyncOperation("same-id", "instance2", OperationType.START);
        AsyncOperation operation3 = new AsyncOperation("different-id", "instance1", OperationType.CREATE);
        
        // Same operation ID should have same hash code
        assertEquals(operation1.hashCode(), operation2.hashCode());
        
        // Different operation ID should likely have different hash code
        assertNotEquals(operation1.hashCode(), operation3.hashCode());
    }
    
    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        String toString = operation.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("AsyncOperation{"));
        assertTrue(toString.contains("operationId='" + operationId + "'"));
        assertTrue(toString.contains("instanceId='" + instanceId + "'"));
        assertTrue(toString.contains("type=" + OperationType.CREATE));
        assertTrue(toString.contains("status=" + OperationStatus.IN_PROGRESS));
    }
    
    @Test
    @DisplayName("Should handle null values in toString")
    void shouldHandleNullValuesInToString() {
        operation.setErrorMessage(null);
        operation.setResult(null);
        
        String toString = operation.toString();
        assertTrue(toString.contains("errorMessage='null'"));
        assertTrue(toString.contains("result='null'"));
    }
    
    @Test
    @DisplayName("Should create operation with default constructor")
    void shouldCreateOperationWithDefaultConstructor() {
        AsyncOperation emptyOperation = new AsyncOperation();
        assertNotNull(emptyOperation);
        assertNull(emptyOperation.getOperationId());
        assertNull(emptyOperation.getInstanceId());
        assertNull(emptyOperation.getType());
        assertNull(emptyOperation.getStatus());
    }
    
    @Test
    @DisplayName("Should handle all getters and setters")
    void shouldHandleAllGettersAndSetters() {
        AsyncOperation testOperation = new AsyncOperation();
        LocalDateTime now = LocalDateTime.now();
        
        testOperation.setOperationId("test-op");
        testOperation.setInstanceId("test-instance");
        testOperation.setType(OperationType.RESTART);
        testOperation.setStatus(OperationStatus.COMPLETED);
        testOperation.setCreatedAt(now);
        testOperation.setCompletedAt(now);
        testOperation.setErrorMessage("test error");
        testOperation.setResult("test result");
        testOperation.setVersion(1L);
        
        assertEquals("test-op", testOperation.getOperationId());
        assertEquals("test-instance", testOperation.getInstanceId());
        assertEquals(OperationType.RESTART, testOperation.getType());
        assertEquals(OperationStatus.COMPLETED, testOperation.getStatus());
        assertEquals(now, testOperation.getCreatedAt());
        assertEquals(now, testOperation.getCompletedAt());
        assertEquals("test error", testOperation.getErrorMessage());
        assertEquals("test result", testOperation.getResult());
        assertEquals(1L, testOperation.getVersion());
    }
    
    @Test
    @DisplayName("Should handle operation lifecycle correctly")
    void shouldHandleOperationLifecycleCorrectly() {
        // Start with in progress
        assertTrue(operation.isInProgress());
        assertFalse(operation.isCompleted());
        
        // Complete successfully
        operation.complete("Success");
        assertFalse(operation.isInProgress());
        assertTrue(operation.isCompleted());
        assertTrue(operation.isSuccess());
        assertFalse(operation.isFailed());
        
        // Create new operation and fail it
        AsyncOperation failedOp = new AsyncOperation("op2", "instance2", OperationType.STOP);
        failedOp.fail("Error occurred");
        assertFalse(failedOp.isInProgress());
        assertTrue(failedOp.isCompleted());
        assertFalse(failedOp.isSuccess());
        assertTrue(failedOp.isFailed());
    }
}