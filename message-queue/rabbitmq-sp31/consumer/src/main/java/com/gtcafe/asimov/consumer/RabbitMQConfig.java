package com.gtcafe.asimov.consumer;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {


	@Value("${app.rabbitmq.queue}")
	String queueName;

	// @Value("${app.rabbitmq.exchange}")
	// String exchange;

	// @Value("${app.rabbitmq.routingkey}")
	// private String routingkey;

	@Value("${spring.rabbitmq.username}")
	String username;

	@Value("${spring.rabbitmq.password}")
	String password;

	@Bean
	Queue queue() {
		return new Queue(queueName, false);
	}

    //create MessageListenerContainer using default connection factory
	@Bean
	MessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory ) {
		SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer();
		simpleMessageListenerContainer.setConnectionFactory(connectionFactory);
		simpleMessageListenerContainer.setQueues(queue());
		simpleMessageListenerContainer.setMessageListener(new MessageConsumer());
		return simpleMessageListenerContainer;
	}

}
