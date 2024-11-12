package com.gtcafe.app.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Value("${rabbitmq.exchanges.state-machine}")
    private String exchange;
    
    @Value("${rabbitmq.queues.tenant}")
    private String tenantQueue;
    
    @Value("${rabbitmq.routing-keys.tenant}")
    private String tenantRoutingKey;
    
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(exchange, true, false);
    }
    
    @Bean
    public Queue tenantQueue() {
        return QueueBuilder.durable(tenantQueue).build();
    }
    
    @Bean
    public Binding tenantBinding(Queue tenantQueue, DirectExchange exchange) {
        return BindingBuilder.bind(tenantQueue)
            .to(exchange)
            .with(tenantRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}