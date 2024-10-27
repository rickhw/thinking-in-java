package com.example;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public MessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessageToTenant(String currentState, String targetState, String message) {
        String routingKey = QueueConfiguration.getRoutingKey("test.tenant", currentState, targetState);
        rabbitTemplate.convertAndSend("stateExchange", routingKey, message);
        System.out.println("Message sent to tenant queue with routing key: " + routingKey);
    }
}
