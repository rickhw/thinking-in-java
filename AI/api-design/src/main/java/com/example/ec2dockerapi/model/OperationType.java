package com.example.ec2dockerapi.model;

/**
 * Enumeration representing the types of operations that can be performed on instances.
 * This enum defines all possible operation types for async operations.
 */
public enum OperationType {
    CREATE("create"),
    START("start"),
    STOP("stop"),
    RESTART("restart"),
    TERMINATE("terminate");
    
    private final String value;
    
    OperationType(String value) {
        this.value = value;
    }
    
    /**
     * Gets the string value of the operation type.
     * 
     * @return the string representation of the operation type
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Creates an OperationType from its string value.
     * 
     * @param value the string value
     * @return the corresponding OperationType
     * @throws IllegalArgumentException if the value doesn't match any operation type
     */
    public static OperationType fromValue(String value) {
        for (OperationType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown operation type: " + value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}