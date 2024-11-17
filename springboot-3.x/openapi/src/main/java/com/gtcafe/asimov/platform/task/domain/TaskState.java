package com.gtcafe.asimov.platform.task.domain;

public enum TaskState {
    Pending("pending", "Task is pending for running"),
    Running("running", "Task is running"),
    Completed("completed", "Task is completed"),
    ;

    private String label;
    private String description;

    private TaskState(String label, String desc) {
        this.label = label;
        this.description = desc;
    }

    public String toString() {
        return this.label;
    }
}