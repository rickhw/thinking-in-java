package com.example.ec2dockerapi.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA Entity representing an EC2-like instance.
 * This entity stores all the necessary information about an instance including its state,
 * associated Docker container, and metadata.
 */
@Entity
@Table(name = "instances")
public class Instance {
    
    @Id
    @Column(name = "instance_id", nullable = false, length = 50)
    private String instanceId;
    
    @Column(name = "container_id", nullable = false, length = 100)
    private String containerId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    private InstanceState state;
    
    @Column(name = "image_id", nullable = false, length = 100)
    private String imageId;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_state_change")
    private LocalDateTime lastStateChange;
    
    @Column(name = "error_message", length = 500)
    private String errorMessage;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    /**
     * Default constructor for JPA.
     */
    public Instance() {
    }
    
    /**
     * Constructor for creating a new instance.
     * 
     * @param instanceId the unique instance identifier
     * @param containerId the Docker container identifier
     * @param state the initial state of the instance
     * @param imageId the Docker image identifier
     */
    public Instance(String instanceId, String containerId, InstanceState state, String imageId) {
        this.instanceId = instanceId;
        this.containerId = containerId;
        this.state = state;
        this.imageId = imageId;
        this.createdAt = LocalDateTime.now();
        this.lastStateChange = LocalDateTime.now();
    }
    
    // Getters and Setters
    
    public String getInstanceId() {
        return instanceId;
    }
    
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }
    
    public String getContainerId() {
        return containerId;
    }
    
    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }
    
    public InstanceState getState() {
        return state;
    }
    
    public void setState(InstanceState state) {
        this.state = state;
        this.lastStateChange = LocalDateTime.now();
    }
    
    public String getImageId() {
        return imageId;
    }
    
    public void setImageId(String imageId) {
        this.imageId = imageId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastStateChange() {
        return lastStateChange;
    }
    
    public void setLastStateChange(LocalDateTime lastStateChange) {
        this.lastStateChange = lastStateChange;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
    
    /**
     * Updates the state and records the state change timestamp.
     * Also clears error message if transitioning away from ERROR state.
     * 
     * @param newState the new state to transition to
     */
    public void transitionToState(InstanceState newState) {
        this.state = newState;
        this.lastStateChange = LocalDateTime.now();
        
        // Clear error message when transitioning away from ERROR state
        if (newState != InstanceState.ERROR) {
            this.errorMessage = null;
        }
    }
    
    /**
     * Sets the instance to ERROR state with an error message.
     * 
     * @param errorMessage the error message to record
     */
    public void setError(String errorMessage) {
        this.state = InstanceState.ERROR;
        this.errorMessage = errorMessage;
        this.lastStateChange = LocalDateTime.now();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Instance instance = (Instance) o;
        return Objects.equals(instanceId, instance.instanceId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(instanceId);
    }
    
    @Override
    public String toString() {
        return "Instance{" +
                "instanceId='" + instanceId + '\'' +
                ", containerId='" + containerId + '\'' +
                ", state=" + state +
                ", imageId='" + imageId + '\'' +
                ", createdAt=" + createdAt +
                ", lastStateChange=" + lastStateChange +
                ", errorMessage='" + errorMessage + '\'' +
                ", version=" + version +
                '}';
    }
}