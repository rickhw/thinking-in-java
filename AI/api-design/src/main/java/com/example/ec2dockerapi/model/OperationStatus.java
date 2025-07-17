package com.example.ec2dockerapi.model;

/**
 * Enumeration representing the status of async operations.
 * This enum defines all possible states an operation can be in during its lifecycle.
 */
public enum OperationStatus {
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    FAILED("failed");
    
    private final String value;
    
    OperationStatus(String value) {
        this.value = value;
    }
    
    /**
     * Gets the string value of the operation status.
     * 
     * @return the string representation of the operation status
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Checks if this status represents a completed operation (either success or failure).
     * 
     * @return true if the operation is completed (success or failure), false if still in progress
     */
    public boolean isCompleted() {
        return this == COMPLETED || this == FAILED;
    }
    
    /**
     * Checks if this status represents a successful completion.
     * 
     * @return true if the operation completed successfully, false otherwise
     */
    public boolean isSuccess() {
        return this == COMPLETED;
    }
    
    /**
     * Checks if this status represents a failed operation.
     * 
     * @return true if the operation failed, false otherwise
     */
    public boolean isFailed() {
        return this == FAILED;
    }
    
    /**
     * Creates an OperationStatus from its string value.
     * 
     * @param value the string value
     * @return the corresponding OperationStatus
     * @throws IllegalArgumentException if the value doesn't match any operation status
     */
    public static OperationStatus fromValue(String value) {
        for (OperationStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown operation status: " + value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}