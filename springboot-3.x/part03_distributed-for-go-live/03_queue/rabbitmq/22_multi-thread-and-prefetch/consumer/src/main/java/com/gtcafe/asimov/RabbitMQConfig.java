package com.gtcafe.asimov;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration
public class RabbitMQConfig {

    @Value("${application.rabbitmq.queue-name}")
    private String queueName;

    @Value("${application.rabbitmq.client.thread-name-prefix}")
    private String threadNamePrefix;

    @Value("${application.rabbitmq.client.concurrent-consumers}")
    private int concurrentConsumers;

    @Value("${application.rabbitmq.client.max-concurrent-consumers}")
    private int maxConcurrentConsumers;

    @Value("${application.rabbitmq.client.prefetch-count}")
    private int prefetchCount;

    @Bean
    public Queue queue() {
        return QueueBuilder.durable(queueName)
            .withArgument("x-dead-letter-exchange", "") // 設定死信隊列
            .withArgument("x-dead-letter-routing-key", queueName + ".dlq")
            .build();
    }

    @Bean
    public RabbitListenerContainerFactory<?> rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) 
    {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setTaskExecutor(new SimpleAsyncTaskExecutor(threadNamePrefix));
        factory.setConnectionFactory(connectionFactory);

        // 正在處理中
        factory.setConcurrentConsumers(concurrentConsumers); // concurrent consumer
        factory.setMaxConcurrentConsumers(maxConcurrentConsumers);

        // 每個 consumer 一次處理多少個
        factory.setPrefetchCount(prefetchCount); // 每個 consumer 一次只處理一個 message
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL); // 手動 ACK
        return factory;
    }
}