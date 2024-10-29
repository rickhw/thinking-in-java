package com.example;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    @Value("${custom.rabbitmq.autoInit:false}")
    private boolean autoInit;

    @Bean
    public TopicExchange stateExchange() {
        return new TopicExchange("stateExchange");
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public List<Queue> stateQueues(RabbitAdmin rabbitAdmin) {
        List<Queue> queues = new ArrayList<>();
        if (autoInit) {
            for (String resource : QueueConfiguration.STATE_TRANSITIONS.keySet()) {
                Queue queue = new Queue(QueueConfiguration.getQueueName(resource), true);
                rabbitAdmin.declareQueue(queue);
                queues.add(queue);
            }
        }
        return queues;
    }

    @Bean
    public List<Binding> queueBindings(RabbitAdmin rabbitAdmin, TopicExchange stateExchange) {
        List<Binding> bindings = new ArrayList<>();
        if (autoInit) {
            for (Map.Entry<String, String[]> entry : QueueConfiguration.STATE_TRANSITIONS.entrySet()) {
                String resource = entry.getKey();
                String[] states = entry.getValue();

                for (String currentState : states) {
                    for (String targetState : states) {
                        if (isValidTransition(currentState, targetState)) {
                            String routingKey = QueueConfiguration.getRoutingKey(resource, currentState, targetState);
                            Queue queue = new Queue(QueueConfiguration.getQueueName(resource), true);
                            rabbitAdmin.declareQueue(queue);
                            Binding binding = BindingBuilder.bind(queue).to(stateExchange).with(routingKey);
                            rabbitAdmin.declareBinding(binding);
                            bindings.add(binding);
                        }
                    }
                }
            }
        }
        return bindings;
    }

    private boolean isValidTransition(String currentState, String targetState) {
        return !("completed".equals(currentState) || "failure".equals(currentState)) || currentState.equals(targetState);
    }
}
