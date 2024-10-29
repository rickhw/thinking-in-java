package com.gtcafe.asimov.producer.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(
	generator = ObjectIdGenerators.IntSequenceGenerator.class,
	property = "@id",
	scope = Event.class)
public class Event {

	private String eventId;
	private String eventType;
    private Message message;

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return String.format("Event: [eventId=%s], [eventType=%s], [message=%s]", eventId, eventType, message);
	}
}
