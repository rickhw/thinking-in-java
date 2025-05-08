package com.gtcafe.asimov;


import java.io.IOException;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessageConsumer {

    private final MessageProcessor messageProcessor;
    // private final ThreadPoolTaskExecutor asyncExecutor;

    @Async
    @RabbitListener(
        queues = "${application.rabbitmq.queue-name}",
        containerFactory = "rabbitListenerContainerFactory"
    )
    public void receiveMessage(
        Message message, 
        Channel channel, 
        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {
        String messageBody = new String(message.getBody());
        
        try {
            boolean processed = messageProcessor.processMessage(messageBody);
            
            if (processed) {
                // 手動確認消息
                channel.basicAck(deliveryTag, false);
                log.info("Message processed successfully: {}", messageBody);
            } else {
                // 處理失敗，重新入隊
                channel.basicNack(deliveryTag, false, true);
                log.warn("Message processing failed, will be requeued: {}", messageBody);
            }
        } catch (Exception e) {
            // 發生異常，重新入隊
            channel.basicNack(deliveryTag, false, true);
            log.error("Unexpected error processing message", e);
        }
    }
}