package com.gtcafe.messageboard.exception;

/**
 * Exception thrown when an invalid message ID format is encountered
 */
public class InvalidMessageIdException extends RuntimeException {
    
    public InvalidMessageIdException(String messageId) {
        super("Invalid message ID format: " + messageId);
    }
    
    public InvalidMessageIdException(String messageId, String details) {
        super("Invalid message ID format: " + messageId + ". " + details);
    }
}