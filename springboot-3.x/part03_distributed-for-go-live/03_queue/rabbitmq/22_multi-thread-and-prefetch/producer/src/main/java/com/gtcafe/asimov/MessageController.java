package com.gtcafe.asimov;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final RabbitTemplate rabbitTemplate;

    @Value("${application.rabbitmq.queue-name}")
    private String queueName;

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody String message) {
        rabbitTemplate.convertAndSend(queueName, message);
        log.info("receive the message: [{}], send to queue: [{}]", message, queueName);
        return ResponseEntity.ok("Message sent: " + message);
    }

    @PostMapping("/send-error")
    public ResponseEntity<String> sendErrorMessage(@RequestBody String message) {
        // 发送一个包含 "error" 的消息，模拟处理失败的场景
        String errorMessage = "error-" + message;
        rabbitTemplate.convertAndSend(queueName, errorMessage);
        return ResponseEntity.ok("Error message sent: " + errorMessage);
    }
}