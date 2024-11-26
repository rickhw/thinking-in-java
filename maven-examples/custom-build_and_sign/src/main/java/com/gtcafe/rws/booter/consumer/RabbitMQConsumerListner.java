package com.gtcafe.rws.booter.consumer;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;

import com.gtcafe.rws.booter.repository.TaskRepository;

// @Service
public class RabbitMQConsumerListner implements MessageListener {

	// @Value("${app.rabbitmq.queue}")
	// String queueName;

	@Autowired
	private TaskRepository repos;

	// @RabbitListener(queues = "app.queue", containerFactory ="customListenerContainerFactory")
	// public void onMessage(@Valid CreateTaskResponse event){
	// 	System.out.println("Consuming Message1 - " + event.getId());
	// }

	public void onMessage(Message message) {
		// CreateTaskResponse res = (CreateTaskResponse)message;
		System.out.println("MessageListener: Consuming  - " + new String(message.getBody()));
	}

}
