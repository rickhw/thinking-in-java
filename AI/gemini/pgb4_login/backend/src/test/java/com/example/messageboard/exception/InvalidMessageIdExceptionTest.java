package com.example.messageboard.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.gtcafe.messageboard.exception.InvalidMessageIdException;

/**
 * Unit tests for InvalidMessageIdException
 */
class InvalidMessageIdExceptionTest {

    @Test
    void constructor_WithMessageId_ShouldCreateExceptionWithCorrectMessage() {
        // Given
        String messageId = "invalid-id";
        
        // When
        InvalidMessageIdException exception = new InvalidMessageIdException(messageId);
        
        // Then
        assertEquals("Invalid message ID format: " + messageId, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void constructor_WithMessageIdAndDetails_ShouldCreateExceptionWithCorrectMessage() {
        // Given
        String messageId = "invalid-id";
        String details = "ID must be 36 characters long";
        
        // When
        InvalidMessageIdException exception = new InvalidMessageIdException(messageId, details);
        
        // Then
        assertEquals("Invalid message ID format: " + messageId + ". " + details, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void constructor_WithNullMessageId_ShouldCreateExceptionWithNullInMessage() {
        // Given
        String messageId = null;
        
        // When
        InvalidMessageIdException exception = new InvalidMessageIdException(messageId);
        
        // Then
        assertEquals("Invalid message ID format: null", exception.getMessage());
    }

    @Test
    void constructor_WithEmptyMessageId_ShouldCreateExceptionWithEmptyInMessage() {
        // Given
        String messageId = "";
        
        // When
        InvalidMessageIdException exception = new InvalidMessageIdException(messageId);
        
        // Then
        assertEquals("Invalid message ID format: ", exception.getMessage());
    }

    @Test
    void constructor_WithNullDetails_ShouldCreateExceptionWithNullDetails() {
        // Given
        String messageId = "invalid-id";
        String details = null;
        
        // When
        InvalidMessageIdException exception = new InvalidMessageIdException(messageId, details);
        
        // Then
        assertEquals("Invalid message ID format: " + messageId + ". null", exception.getMessage());
    }

    @Test
    void exception_ShouldBeRuntimeException() {
        // Given
        String messageId = "invalid-id";
        
        // When
        InvalidMessageIdException exception = new InvalidMessageIdException(messageId);
        
        // Then
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void exception_ShouldBeThrowable() {
        // Given
        String messageId = "invalid-id";
        
        // When & Then
        assertThrows(InvalidMessageIdException.class, () -> {
            throw new InvalidMessageIdException(messageId);
        });
    }

    @Test
    void constructor_WithLongMessageId_ShouldHandleCorrectly() {
        // Given
        String longMessageId = "A".repeat(100); // Very long ID
        
        // When
        InvalidMessageIdException exception = new InvalidMessageIdException(longMessageId);
        
        // Then
        assertEquals("Invalid message ID format: " + longMessageId, exception.getMessage());
    }

    @Test
    void constructor_WithSpecialCharacters_ShouldHandleCorrectly() {
        // Given
        String messageIdWithSpecialChars = "invalid@#$%^&*()id";
        
        // When
        InvalidMessageIdException exception = new InvalidMessageIdException(messageIdWithSpecialChars);
        
        // Then
        assertEquals("Invalid message ID format: " + messageIdWithSpecialChars, exception.getMessage());
    }

    @Test
    void constructor_WithDetailsContainingSpecialCharacters_ShouldHandleCorrectly() {
        // Given
        String messageId = "invalid-id";
        String details = "Details with special chars: @#$%^&*()";
        
        // When
        InvalidMessageIdException exception = new InvalidMessageIdException(messageId, details);
        
        // Then
        assertEquals("Invalid message ID format: " + messageId + ". " + details, exception.getMessage());
    }
}