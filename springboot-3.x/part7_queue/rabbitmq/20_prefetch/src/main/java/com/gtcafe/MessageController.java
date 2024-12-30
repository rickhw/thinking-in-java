package com.gtcafe;

import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/messages")
@Slf4j
public class MessageController {

    private final RabbitTemplate rabbitTemplate;

    public MessageController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @GetMapping
    public ResponseEntity<String> sendMessage() {
        String uuid = UUID.randomUUID().toString();

        log.info("Enqueue: [{}]", uuid);
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, uuid);

        return ResponseEntity.ok("Message sent: [" + uuid + "]");
    }
}