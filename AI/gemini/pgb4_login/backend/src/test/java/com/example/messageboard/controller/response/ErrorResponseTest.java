package com.example.messageboard.controller.response;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gtcafe.messageboard.controller.response.ErrorResponse;

/**
 * Unit tests for ErrorResponse
 */
class ErrorResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void constructor_NoArgs_ShouldCreateEmptyErrorResponse() {
        // When
        ErrorResponse errorResponse = new ErrorResponse();

        // Then
        assertNull(errorResponse.getErrorCode());
        assertNull(errorResponse.getMessage());
    }

    @Test
    void constructor_WithArgs_ShouldCreateErrorResponseWithValues() {
        // Given
        String errorCode = "INVALID_MESSAGE_ID";
        String message = "Invalid message ID format";

        // When
        ErrorResponse errorResponse = new ErrorResponse(errorCode, message);

        // Then
        assertEquals(errorCode, errorResponse.getErrorCode());
        assertEquals(message, errorResponse.getMessage());
    }

    @Test
    void setters_ShouldSetValues() {
        // Given
        ErrorResponse errorResponse = new ErrorResponse();
        String errorCode = "INTERNAL_ERROR";
        String message = "An internal error occurred";

        // When
        errorResponse.setErrorCode(errorCode);
        errorResponse.setMessage(message);

        // Then
        assertEquals(errorCode, errorResponse.getErrorCode());
        assertEquals(message, errorResponse.getMessage());
    }

    @Test
    void getters_ShouldReturnSetValues() {
        // Given
        String errorCode = "TEST_ERROR";
        String message = "Test error message";
        ErrorResponse errorResponse = new ErrorResponse(errorCode, message);

        // When & Then
        assertEquals(errorCode, errorResponse.getErrorCode());
        assertEquals(message, errorResponse.getMessage());
    }

    @Test
    void constructor_WithNullValues_ShouldHandleNulls() {
        // When
        ErrorResponse errorResponse = new ErrorResponse(null, null);

        // Then
        assertNull(errorResponse.getErrorCode());
        assertNull(errorResponse.getMessage());
    }

    @Test
    void setters_WithNullValues_ShouldHandleNulls() {
        // Given
        ErrorResponse errorResponse = new ErrorResponse("CODE", "MESSAGE");

        // When
        errorResponse.setErrorCode(null);
        errorResponse.setMessage(null);

        // Then
        assertNull(errorResponse.getErrorCode());
        assertNull(errorResponse.getMessage());
    }

    @Test
    void constructor_WithEmptyValues_ShouldHandleEmptyStrings() {
        // When
        ErrorResponse errorResponse = new ErrorResponse("", "");

        // Then
        assertEquals("", errorResponse.getErrorCode());
        assertEquals("", errorResponse.getMessage());
    }

    @Test
    void serialization_ShouldProduceCorrectJson() throws JsonProcessingException {
        // Given
        String errorCode = "INVALID_MESSAGE_ID";
        String message = "Invalid message ID format: test-id";
        ErrorResponse errorResponse = new ErrorResponse(errorCode, message);

        // When
        String json = objectMapper.writeValueAsString(errorResponse);

        // Then
        assertTrue(json.contains("\"errorCode\":\"" + errorCode + "\""));
        assertTrue(json.contains("\"message\":\"" + message + "\""));
    }

    @Test
    void deserialization_ShouldCreateCorrectObject() throws JsonProcessingException {
        // Given
        String json = "{\"errorCode\":\"TEST_ERROR\",\"message\":\"Test message\"}";

        // When
        ErrorResponse errorResponse = objectMapper.readValue(json, ErrorResponse.class);

        // Then
        assertEquals("TEST_ERROR", errorResponse.getErrorCode());
        assertEquals("Test message", errorResponse.getMessage());
    }

    @Test
    void serialization_WithNullValues_ShouldHandleNulls() throws JsonProcessingException {
        // Given
        ErrorResponse errorResponse = new ErrorResponse(null, null);

        // When
        String json = objectMapper.writeValueAsString(errorResponse);

        // Then
        assertTrue(json.contains("\"errorCode\":null"));
        assertTrue(json.contains("\"message\":null"));
    }

    @Test
    void deserialization_WithMissingFields_ShouldCreateObjectWithNulls() throws JsonProcessingException {
        // Given
        String json = "{}";

        // When
        ErrorResponse errorResponse = objectMapper.readValue(json, ErrorResponse.class);

        // Then
        assertNull(errorResponse.getErrorCode());
        assertNull(errorResponse.getMessage());
    }

    @Test
    void serialization_WithSpecialCharacters_ShouldHandleCorrectly() throws JsonProcessingException {
        // Given
        String errorCode = "SPECIAL_CHARS_ERROR";
        String message = "Error with special chars: @#$%^&*()";
        ErrorResponse errorResponse = new ErrorResponse(errorCode, message);

        // When
        String json = objectMapper.writeValueAsString(errorResponse);

        // Then
        assertTrue(json.contains("\"errorCode\":\"" + errorCode + "\""));
        assertTrue(json.contains("\"message\":\"" + message + "\""));
    }

    @Test
    void serialization_WithUnicodeCharacters_ShouldHandleCorrectly() throws JsonProcessingException {
        // Given
        String errorCode = "UNICODE_ERROR";
        String message = "錯誤訊息包含中文字符";
        ErrorResponse errorResponse = new ErrorResponse(errorCode, message);

        // When
        String json = objectMapper.writeValueAsString(errorResponse);

        // Then
        assertTrue(json.contains("\"errorCode\":\"" + errorCode + "\""));
        assertTrue(json.contains("\"message\":\"" + message + "\""));
    }

    @Test
    void equals_ShouldWorkCorrectly() {
        // Given
        ErrorResponse response1 = new ErrorResponse("CODE", "MESSAGE");
        ErrorResponse response2 = new ErrorResponse("CODE", "MESSAGE");
        ErrorResponse response3 = new ErrorResponse("DIFFERENT", "MESSAGE");

        // Then
        assertEquals(response1, response2);
        assertNotEquals(response1, response3);
    }

    @Test
    void hashCode_ShouldWorkCorrectly() {
        // Given
        ErrorResponse response1 = new ErrorResponse("CODE", "MESSAGE");
        ErrorResponse response2 = new ErrorResponse("CODE", "MESSAGE");

        // Then
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void toString_ShouldReturnReadableString() {
        // Given
        ErrorResponse errorResponse = new ErrorResponse("TEST_CODE", "Test message");

        // When
        String toString = errorResponse.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("TEST_CODE"));
        assertTrue(toString.contains("Test message"));
    }
}