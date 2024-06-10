package com.gtcafe.mqlab01.producer;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gtcafe.mqlab01.producer.model.MessagePayload;

@Service
public class MessageProducer {
    @Autowired
	private AmqpTemplate rabbitTemplate;

	@Value("${app.rabbitmq.exchange}")
	private String exchange;

	@Value("${app.rabbitmq.routingkey}")
	private String routingkey;

	public void send(MessagePayload payload) {
		rabbitTemplate.convertAndSend(exchange, routingkey, payload);
		System.out.println("Send msg = " + payload);
	}
}
