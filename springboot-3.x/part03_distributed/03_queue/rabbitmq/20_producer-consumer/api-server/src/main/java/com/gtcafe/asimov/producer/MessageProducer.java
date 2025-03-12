package com.gtcafe.asimov.producer;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gtcafe.asimov.producer.model.Event;

@Service
public class MessageProducer {
    @Autowired
	private AmqpTemplate rabbitTemplate;

	@Value("${app.rabbitmq.exchange}")
	private String exchange;

	@Value("${app.rabbitmq.routingkey}")
	private String routingkey;

	public void send(Event event) {
		rabbitTemplate.convertAndSend(exchange, routingkey, event);
		System.out.println("Enqueue, event: " + event);
	}
}
