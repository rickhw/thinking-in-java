package com.gtcafe.messageboard.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.gtcafe.messageboard.controller.response.ErrorResponse;
import com.gtcafe.messageboard.exception.InvalidMessageIdException;

/**
 * Global exception handler for all controllers
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles InvalidMessageIdException and returns a standardized error response
     */
    @ExceptionHandler(InvalidMessageIdException.class)
    public ResponseEntity<ErrorResponse> handleInvalidMessageId(InvalidMessageIdException e) {
        ErrorResponse errorResponse = new ErrorResponse("INVALID_MESSAGE_ID", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles general exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        ErrorResponse errorResponse = new ErrorResponse("INTERNAL_ERROR", "An internal error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}