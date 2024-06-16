package com.gtcafe.mq.receiver;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MessageReceiver {

    private final ObjectMapper objectMapper;

    public MessageReceiver(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }




    @RabbitListener(queues = "messageQueue")
    public void receiveMessage(String message) {
        try {
            EventMessage eventMessage = objectMapper.readValue(message, EventMessage.class);
            System.out.println("Received message: " + eventMessage);
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }
}
