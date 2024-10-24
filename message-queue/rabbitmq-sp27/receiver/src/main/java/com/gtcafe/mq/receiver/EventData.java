package com.gtcafe.mq.receiver;

public class EventData {
    private String kind;
    private String resourceId;
    private String state;

    // Getters and Setters
    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "EventData{" +
                "kind='" + kind + '\'' +
                ", resourceId='" + resourceId + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}
