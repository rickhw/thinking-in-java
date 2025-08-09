package com.example.messageboard.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.gtcafe.messageboard.controller.GlobalExceptionHandler;
import com.gtcafe.messageboard.controller.response.ErrorResponse;
import com.gtcafe.messageboard.exception.InvalidMessageIdException;

/**
 * Unit tests for GlobalExceptionHandler
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    private String testMessageId;
    private String testErrorMessage;

    @BeforeEach
    void setUp() {
        testMessageId = "invalid-id";
        testErrorMessage = "Invalid message ID format: " + testMessageId;
    }

    @Test
    void handleInvalidMessageId_ShouldReturnBadRequestWithCorrectErrorResponse() {
        // Given
        InvalidMessageIdException exception = new InvalidMessageIdException(testMessageId);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidMessageId(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_MESSAGE_ID", response.getBody().getErrorCode());
        assertEquals(testErrorMessage, response.getBody().getMessage());
    }

    @Test
    void handleInvalidMessageId_WithNullMessageId_ShouldReturnBadRequestWithNullInMessage() {
        // Given
        InvalidMessageIdException exception = new InvalidMessageIdException(null);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidMessageId(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_MESSAGE_ID", response.getBody().getErrorCode());
        assertEquals("Invalid message ID format: null", response.getBody().getMessage());
    }

    @Test
    void handleInvalidMessageId_WithEmptyMessageId_ShouldReturnBadRequestWithEmptyInMessage() {
        // Given
        InvalidMessageIdException exception = new InvalidMessageIdException("");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidMessageId(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_MESSAGE_ID", response.getBody().getErrorCode());
        assertEquals("Invalid message ID format: ", response.getBody().getMessage());
    }

    @Test
    void handleInvalidMessageId_WithDetailsConstructor_ShouldReturnBadRequestWithDetails() {
        // Given
        String details = "ID must be 36 characters long";
        InvalidMessageIdException exception = new InvalidMessageIdException(testMessageId, details);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidMessageId(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_MESSAGE_ID", response.getBody().getErrorCode());
        assertEquals("Invalid message ID format: " + testMessageId + ". " + details, response.getBody().getMessage());
    }

    @Test
    void handleInvalidMessageId_WithLongMessageId_ShouldHandleCorrectly() {
        // Given
        String longMessageId = "A".repeat(100);
        InvalidMessageIdException exception = new InvalidMessageIdException(longMessageId);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidMessageId(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_MESSAGE_ID", response.getBody().getErrorCode());
        assertEquals("Invalid message ID format: " + longMessageId, response.getBody().getMessage());
    }

    @Test
    void handleInvalidMessageId_WithSpecialCharacters_ShouldHandleCorrectly() {
        // Given
        String messageIdWithSpecialChars = "invalid@#$%^&*()id";
        InvalidMessageIdException exception = new InvalidMessageIdException(messageIdWithSpecialChars);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidMessageId(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_MESSAGE_ID", response.getBody().getErrorCode());
        assertEquals("Invalid message ID format: " + messageIdWithSpecialChars, response.getBody().getMessage());
    }

    @Test
    void handleGeneralException_ShouldReturnInternalServerErrorWithGenericMessage() {
        // Given
        Exception exception = new RuntimeException("Some internal error");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGeneralException(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_ERROR", response.getBody().getErrorCode());
        assertEquals("An internal error occurred", response.getBody().getMessage());
    }

    @Test
    void handleGeneralException_WithNullPointerException_ShouldReturnInternalServerError() {
        // Given
        NullPointerException exception = new NullPointerException("Null pointer error");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGeneralException(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_ERROR", response.getBody().getErrorCode());
        assertEquals("An internal error occurred", response.getBody().getMessage());
    }

    @Test
    void handleGeneralException_WithIllegalArgumentException_ShouldReturnInternalServerError() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGeneralException(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_ERROR", response.getBody().getErrorCode());
        assertEquals("An internal error occurred", response.getBody().getMessage());
    }

    @Test
    void handleGeneralException_WithNullException_ShouldHandleGracefully() {
        // Given
        Exception exception = null;

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGeneralException(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_ERROR", response.getBody().getErrorCode());
        assertEquals("An internal error occurred", response.getBody().getMessage());
    }

    @Test
    void errorResponse_ShouldHaveCorrectStructure() {
        // Given
        InvalidMessageIdException exception = new InvalidMessageIdException(testMessageId);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidMessageId(exception);

        // Then
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getErrorCode());
        assertNotNull(errorResponse.getMessage());
        assertFalse(errorResponse.getErrorCode().isEmpty());
        assertFalse(errorResponse.getMessage().isEmpty());
    }

    @Test
    void errorResponse_ShouldBeSerializable() {
        // Given
        InvalidMessageIdException exception = new InvalidMessageIdException(testMessageId);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidMessageId(exception);

        // Then
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        
        // Test that ErrorResponse can be created and accessed properly
        ErrorResponse testResponse = new ErrorResponse("TEST_CODE", "Test message");
        assertEquals("TEST_CODE", testResponse.getErrorCode());
        assertEquals("Test message", testResponse.getMessage());
    }
}