package com.example.ec2dockerapi.model;

/**
 * Enumeration representing the various states of an EC2-like instance.
 * This enum defines all possible states that an instance can be in during its lifecycle.
 */
public enum InstanceState {
    PENDING("pending"),
    RUNNING("running"),
    STOPPING("stopping"),
    STOPPED("stopped"),
    REBOOTING("rebooting"),
    TERMINATING("terminating"),
    TERMINATED("terminated"),
    ERROR("error");
    
    private final String value;
    
    InstanceState(String value) {
        this.value = value;
    }
    
    /**
     * Gets the string value of the instance state.
     * 
     * @return the string representation of the state
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Checks if this state is a terminal state (no further transitions possible).
     * 
     * @return true if this is a terminal state, false otherwise
     */
    public boolean isTerminal() {
        return this == TERMINATED;
    }
    
    /**
     * Creates an InstanceState from its string value.
     * 
     * @param value the string value
     * @return the corresponding InstanceState
     * @throws IllegalArgumentException if the value doesn't match any state
     */
    public static InstanceState fromValue(String value) {
        for (InstanceState state : values()) {
            if (state.value.equals(value)) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown instance state: " + value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}