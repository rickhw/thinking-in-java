package com.gtcafe.asimov;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${application.rabbitmq.queue-name}")
    private String queueName;

    @Value("${application.rabbitmq.max-concurrent-consumers}")
    private int maxConcurrentConsumers;

    @Bean
    public Queue queue() {
        return QueueBuilder.durable(queueName)
            .withArgument("x-dead-letter-exchange", "") // 設定死信隊列
            .withArgument("x-dead-letter-routing-key", queueName + ".dlq")
            .build();
    }

    @Bean
    public RabbitListenerContainerFactory<?> rabbitListenerContainerFactory(
        ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(maxConcurrentConsumers);
        factory.setPrefetchCount(1); // 每個 consumer 一次只處理一個 message
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL); // 手動 ACK
        return factory;
    }
}