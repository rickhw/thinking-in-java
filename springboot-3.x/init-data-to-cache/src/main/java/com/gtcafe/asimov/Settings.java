package com.gtcafe.asimov;

public class Settings {
    private int maxUsers;
    private int maxConnections;
    private int timeoutSeconds;

    // Getters and setters
    public int getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(int maxUsers) {
        this.maxUsers = maxUsers;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}