package com.gtcafe.rws.booter.config;


import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gtcafe.rws.booter.consumer.RabbitMQConsumerListner;


@Configuration
public class RabbitMQConsumerConfig {


	@Value("${app.rabbitmq.queue}")
	String queueName;

	// @Value("${spring.rabbitmq.username}")
	// String username;

	// @Value("${spring.rabbitmq.password}")
	// String password;


	@Bean
	Queue consumeQueueName() {
		return new Queue(queueName, false);
	}

	// @Bean
	// public MessageConverter jsonMessageConverter() {
    // 	return new Jackson2JsonMessageConverter();
	// }

	@Bean
	MessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory ) {
		SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer();
		simpleMessageListenerContainer.setConnectionFactory(connectionFactory);
		simpleMessageListenerContainer.setQueues(consumeQueueName());
		simpleMessageListenerContainer.setMessageListener(new RabbitMQConsumerListner());
		// connectionFactory.setMessageConverter(new Jackson2JsonMessageConverter());
		return simpleMessageListenerContainer;
	}

}
