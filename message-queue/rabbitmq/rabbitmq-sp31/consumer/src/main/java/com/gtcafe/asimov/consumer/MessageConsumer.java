package com.gtcafe.asimov.consumer;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.stereotype.Service;

// https://www.javainuse.com/messaging/rabbitmq/listeners
@Service
public class MessageConsumer implements MessageListener {

	public void onMessage(Message message) {

		// if ( message.getBody() instanceof MessagePayload) {
		// 	System.out.println("Consuming Message - " + new String(message.getBody()));
		// } else {

		// System.out.println("Consuming Message - " + message);
		// (Body:'[B@2af96eb5(byte[145])' MessageProperties [headers={__TypeId__=com.gtcafe.springbootlab.day00.model.MessagePayload}, contentType=application/json, contentEncoding=UTF-8, contentLength=0, receivedDeliveryMode=PERSISTENT, priority=0, redelivered=false, receivedExchange=app.exchange1.topic1, receivedRoutingKey=app.routingkey, deliveryTag=1, consumerTag=amq.ctag-ZzZwtyzHXoN5eKEuvvcmaQ, consumerQueue=app.queue])

		// message.getBody()
		System.out.println("Consuming Message - " + new String(message.getBody()));
		// }
	}

}
