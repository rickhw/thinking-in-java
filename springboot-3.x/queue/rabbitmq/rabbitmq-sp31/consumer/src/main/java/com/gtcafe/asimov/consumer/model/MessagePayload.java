package com.gtcafe.asimov.consumer.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(
	generator = ObjectIdGenerators.IntSequenceGenerator.class,
	property = "@id",
	scope = MessagePayload.class)
public class MessagePayload {

	private String messageId;
    private MessageEvent event;

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public MessageEvent getEvent() {
		return event;
	}

	public void setEvent(MessageEvent event) {
		this.event = event;
	}

	@Override
	public String toString() {
		return String.format("MessagePayload [messageId=%s], [event=%s]", messageId, event);
	}
}
