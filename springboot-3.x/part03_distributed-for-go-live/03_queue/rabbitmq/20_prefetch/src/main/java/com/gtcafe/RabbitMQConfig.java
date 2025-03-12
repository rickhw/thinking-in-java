package com.gtcafe;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NAME = "example-queue";

    @Bean
    public Queue exampleQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);

        // 設置 prefetch count 為 3
        factory.setPrefetchCount(3);

        // 啟用手動確認模式
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return factory;
    }
}