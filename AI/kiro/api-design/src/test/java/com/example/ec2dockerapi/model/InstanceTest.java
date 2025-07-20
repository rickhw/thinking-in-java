package com.example.ec2dockerapi.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Instance entity.
 */
@DisplayName("Instance Entity Tests")
class InstanceTest {
    
    private Instance instance;
    private final String instanceId = "i-1234567890abcdef0";
    private final String containerId = "container123";
    private final String imageId = "nginx:latest";
    
    @BeforeEach
    void setUp() {
        instance = new Instance(instanceId, containerId, InstanceState.PENDING, imageId);
    }
    
    @Test
    @DisplayName("Should create instance with constructor parameters")
    void shouldCreateInstanceWithConstructorParameters() {
        assertNotNull(instance);
        assertEquals(instanceId, instance.getInstanceId());
        assertEquals(containerId, instance.getContainerId());
        assertEquals(InstanceState.PENDING, instance.getState());
        assertEquals(imageId, instance.getImageId());
        assertNotNull(instance.getCreatedAt());
        assertNotNull(instance.getLastStateChange());
    }
    
    @Test
    @DisplayName("Should set createdAt and lastStateChange on construction")
    void shouldSetTimestampsOnConstruction() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        Instance newInstance = new Instance("test-id", "test-container", InstanceState.PENDING, "test-image");
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        
        assertTrue(newInstance.getCreatedAt().isAfter(before));
        assertTrue(newInstance.getCreatedAt().isBefore(after));
        assertTrue(newInstance.getLastStateChange().isAfter(before));
        assertTrue(newInstance.getLastStateChange().isBefore(after));
    }
    
    @Test
    @DisplayName("Should update lastStateChange when setting state")
    void shouldUpdateLastStateChangeWhenSettingState() {
        LocalDateTime originalStateChange = instance.getLastStateChange();
        
        // Wait a small amount to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        instance.setState(InstanceState.RUNNING);
        
        assertEquals(InstanceState.RUNNING, instance.getState());
        assertTrue(instance.getLastStateChange().isAfter(originalStateChange));
    }
    
    @Test
    @DisplayName("Should transition to new state and update timestamp")
    void shouldTransitionToNewStateAndUpdateTimestamp() {
        LocalDateTime originalStateChange = instance.getLastStateChange();
        
        // Wait a small amount to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        instance.transitionToState(InstanceState.RUNNING);
        
        assertEquals(InstanceState.RUNNING, instance.getState());
        assertTrue(instance.getLastStateChange().isAfter(originalStateChange));
    }
    
    @Test
    @DisplayName("Should clear error message when transitioning away from ERROR state")
    void shouldClearErrorMessageWhenTransitioningAwayFromError() {
        instance.setError("Test error message");
        assertEquals(InstanceState.ERROR, instance.getState());
        assertEquals("Test error message", instance.getErrorMessage());
        
        instance.transitionToState(InstanceState.RUNNING);
        
        assertEquals(InstanceState.RUNNING, instance.getState());
        assertNull(instance.getErrorMessage());
    }
    
    @Test
    @DisplayName("Should not clear error message when transitioning to ERROR state")
    void shouldNotClearErrorMessageWhenTransitioningToError() {
        instance.setError("First error");
        instance.transitionToState(InstanceState.ERROR);
        
        assertEquals(InstanceState.ERROR, instance.getState());
        assertEquals("First error", instance.getErrorMessage());
    }
    
    @Test
    @DisplayName("Should set error state and message")
    void shouldSetErrorStateAndMessage() {
        String errorMessage = "Docker container failed to start";
        LocalDateTime originalStateChange = instance.getLastStateChange();
        
        // Wait a small amount to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        instance.setError(errorMessage);
        
        assertEquals(InstanceState.ERROR, instance.getState());
        assertEquals(errorMessage, instance.getErrorMessage());
        assertTrue(instance.getLastStateChange().isAfter(originalStateChange));
    }
    
    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        Instance instance1 = new Instance("same-id", "container1", InstanceState.PENDING, "image1");
        Instance instance2 = new Instance("same-id", "container2", InstanceState.RUNNING, "image2");
        Instance instance3 = new Instance("different-id", "container1", InstanceState.PENDING, "image1");
        
        // Same instance ID should be equal
        assertEquals(instance1, instance2);
        
        // Different instance ID should not be equal
        assertNotEquals(instance1, instance3);
        
        // Should not be equal to null
        assertNotEquals(instance1, null);
        
        // Should not be equal to different class
        assertNotEquals(instance1, "string");
        
        // Should be equal to itself
        assertEquals(instance1, instance1);
    }
    
    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        Instance instance1 = new Instance("same-id", "container1", InstanceState.PENDING, "image1");
        Instance instance2 = new Instance("same-id", "container2", InstanceState.RUNNING, "image2");
        Instance instance3 = new Instance("different-id", "container1", InstanceState.PENDING, "image1");
        
        // Same instance ID should have same hash code
        assertEquals(instance1.hashCode(), instance2.hashCode());
        
        // Different instance ID should likely have different hash code
        assertNotEquals(instance1.hashCode(), instance3.hashCode());
    }
    
    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        String toString = instance.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("Instance{"));
        assertTrue(toString.contains("instanceId='" + instanceId + "'"));
        assertTrue(toString.contains("containerId='" + containerId + "'"));
        assertTrue(toString.contains("state=" + InstanceState.PENDING));
        assertTrue(toString.contains("imageId='" + imageId + "'"));
    }
    
    @Test
    @DisplayName("Should handle null error message")
    void shouldHandleNullErrorMessage() {
        instance.setErrorMessage(null);
        assertNull(instance.getErrorMessage());
        
        String toString = instance.toString();
        assertTrue(toString.contains("errorMessage='null'"));
    }
    
    @Test
    @DisplayName("Should create instance with default constructor")
    void shouldCreateInstanceWithDefaultConstructor() {
        Instance emptyInstance = new Instance();
        assertNotNull(emptyInstance);
        assertNull(emptyInstance.getInstanceId());
        assertNull(emptyInstance.getContainerId());
        assertNull(emptyInstance.getState());
        assertNull(emptyInstance.getImageId());
    }
    
    @Test
    @DisplayName("Should handle all getters and setters")
    void shouldHandleAllGettersAndSetters() {
        Instance testInstance = new Instance();
        LocalDateTime now = LocalDateTime.now();
        
        testInstance.setInstanceId("test-id");
        testInstance.setContainerId("test-container");
        testInstance.setState(InstanceState.RUNNING);
        testInstance.setImageId("test-image");
        testInstance.setCreatedAt(now);
        testInstance.setLastStateChange(now);
        testInstance.setErrorMessage("test error");
        testInstance.setVersion(1L);
        
        assertEquals("test-id", testInstance.getInstanceId());
        assertEquals("test-container", testInstance.getContainerId());
        assertEquals(InstanceState.RUNNING, testInstance.getState());
        assertEquals("test-image", testInstance.getImageId());
        assertEquals(now, testInstance.getCreatedAt());
        assertEquals("test error", testInstance.getErrorMessage());
        assertEquals(1L, testInstance.getVersion());
    }
}