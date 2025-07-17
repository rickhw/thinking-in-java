package com.example.ec2dockerapi.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA Entity representing an asynchronous operation.
 * This entity tracks the progress and status of long-running operations
 * performed on instances.
 */
@Entity
@Table(name = "operations")
public class AsyncOperation {
    
    @Id
    @Column(name = "operation_id", nullable = false, length = 50)
    private String operationId;
    
    @Column(name = "instance_id", nullable = false, length = 50)
    private String instanceId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private OperationType type;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OperationStatus status;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "error_message", length = 500)
    private String errorMessage;
    
    @Column(name = "result", length = 1000)
    private String result;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    /**
     * Default constructor for JPA.
     */
    public AsyncOperation() {
    }
    
    /**
     * Constructor for creating a new operation.
     * 
     * @param operationId the unique operation identifier
     * @param instanceId the instance identifier this operation is for
     * @param type the type of operation
     */
    public AsyncOperation(String operationId, String instanceId, OperationType type) {
        this.operationId = operationId;
        this.instanceId = instanceId;
        this.type = type;
        this.status = OperationStatus.IN_PROGRESS;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    
    public String getOperationId() {
        return operationId;
    }
    
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }
    
    public String getInstanceId() {
        return instanceId;
    }
    
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }
    
    public OperationType getType() {
        return type;
    }
    
    public void setType(OperationType type) {
        this.type = type;
    }
    
    public OperationStatus getStatus() {
        return status;
    }
    
    public void setStatus(OperationStatus status) {
        this.status = status;
        
        // Set completion timestamp when operation completes
        if (status.isCompleted() && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getResult() {
        return result;
    }
    
    public void setResult(String result) {
        this.result = result;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
    
    /**
     * Completes the operation successfully with a result.
     * 
     * @param result the operation result
     */
    public void complete(String result) {
        this.status = OperationStatus.COMPLETED;
        this.result = result;
        this.completedAt = LocalDateTime.now();
        this.errorMessage = null; // Clear any previous error message
    }
    
    /**
     * Marks the operation as failed with an error message.
     * 
     * @param errorMessage the error message
     */
    public void fail(String errorMessage) {
        this.status = OperationStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
        this.result = null; // Clear any previous result
    }
    
    /**
     * Updates the operation status and sets completion timestamp if needed.
     * 
     * @param newStatus the new status
     * @param message optional message (error message for failed operations, result for completed)
     */
    public void updateStatus(OperationStatus newStatus, String message) {
        this.status = newStatus;
        
        if (newStatus.isCompleted() && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
        
        if (newStatus == OperationStatus.FAILED) {
            this.errorMessage = message;
            this.result = null;
        } else if (newStatus == OperationStatus.COMPLETED) {
            this.result = message;
            this.errorMessage = null;
        }
    }
    
    /**
     * Checks if the operation is still in progress.
     * 
     * @return true if the operation is in progress, false otherwise
     */
    public boolean isInProgress() {
        return status == OperationStatus.IN_PROGRESS;
    }
    
    /**
     * Checks if the operation has completed (either successfully or with failure).
     * 
     * @return true if the operation is completed, false otherwise
     */
    public boolean isCompleted() {
        return status.isCompleted();
    }
    
    /**
     * Checks if the operation completed successfully.
     * 
     * @return true if the operation completed successfully, false otherwise
     */
    public boolean isSuccess() {
        return status == OperationStatus.COMPLETED;
    }
    
    /**
     * Checks if the operation failed.
     * 
     * @return true if the operation failed, false otherwise
     */
    public boolean isFailed() {
        return status == OperationStatus.FAILED;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AsyncOperation that = (AsyncOperation) o;
        return Objects.equals(operationId, that.operationId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(operationId);
    }
    
    @Override
    public String toString() {
        return "AsyncOperation{" +
                "operationId='" + operationId + '\'' +
                ", instanceId='" + instanceId + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", completedAt=" + completedAt +
                ", errorMessage='" + errorMessage + '\'' +
                ", result='" + result + '\'' +
                ", version=" + version +
                '}';
    }
}