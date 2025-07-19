package com.example.gbook;

public class TaskResponse {
    private String taskId;
    private String status;
    private String message;

    public TaskResponse(String taskId, String status, String message) {
        this.taskId = taskId;
        this.status = status;
        this.message = message;
    }

    // Getters and Setters
}
