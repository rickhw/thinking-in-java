package com.example;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class MessageConsumer {

    @RabbitListener(queues = "#{QueueConfiguration.getQueueName('test.tenant')}")
    public void receiveMessage(@Payload String message) {
        System.out.println("Received message from test.tenant queue: " + message);
    }
}
