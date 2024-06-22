package com.gtcafe.mq.receiver;

import java.time.ZonedDateTime;

public class EventMessage {
    private String eventType;
    private ZonedDateTime eventTimestamp;
    private EventData eventData;

    // Getters and Setters
    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public ZonedDateTime getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(ZonedDateTime eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    public EventData getEventData() {
        return eventData;
    }

    public void setEventData(EventData eventData) {
        this.eventData = eventData;
    }

    @Override
    public String toString() {
        return "EventMessage{" +
                "eventType='" + eventType + '\'' +
                ", eventTimestamp=" + eventTimestamp +
                ", eventData=" + eventData +
                '}';
    }
}
