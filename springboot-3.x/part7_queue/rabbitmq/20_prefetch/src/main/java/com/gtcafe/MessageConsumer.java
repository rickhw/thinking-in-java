package com.gtcafe;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MessageConsumer {

    private static final int DELAY = 50;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME, ackMode = "MANUAL")
    public void consumeHelloQueue(String eventString, Channel channel, org.springframework.amqp.core.Message message) throws Exception {
        log.info("Dequeue, message: [{}]", eventString);

        try {
            // 處理訊息
            log.info("Processing event, delay: [{}ms]", DELAY);
            Thread.sleep(DELAY);

            // 處理完成，手動 ACK
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        } catch (Exception e) {
            log.error("Failed to process message, will reject: {}", eventString, e);
            // 如果失敗，拒絕訊息，並讓 RabbitMQ 決定是否重新投遞
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }

    }

}
