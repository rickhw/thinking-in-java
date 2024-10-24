package com.gtcafe.asimov.producer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "app.rabbitmq")
public class RabbitMQMultipleConfig {

	private static final Logger logger = LoggerFactory.getLogger(RabbitMQMultipleConfig.class);
	private List<RabbitQueueConfig> queues = new ArrayList<>();

	public List<RabbitQueueConfig> getQueues() {
		return queues;
	}

	public void setQueues(List<RabbitQueueConfig> queues) {
		this.queues = queues;
	}

	@Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }


    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

	
    @Bean
    public Declarables rabbitMQDeclarables() {
        List<Declarable> declarablesList = new ArrayList<>();

        for (RabbitQueueConfig queueConfig : queues) {
            logger.info("Init RabbitMQ config: queue: [{}], exchange: [{}], routingKey: [{}]", queueConfig.name, queueConfig.exchange, queueConfig.routingKey);

            // 1. init queue
            Queue queue = new Queue(queueConfig.getName(), true); // durable queue

            // 2. init exchange
            Exchange exchange;
            if ("fanoutExchange".equals(queueConfig.getExchange())) {
                exchange = new FanoutExchange(queueConfig.getExchange());
            } else {
                exchange = new DirectExchange(queueConfig.getExchange());
            }

            // 3. init binding
            Binding binding;
            if (exchange instanceof FanoutExchange) {
                binding = BindingBuilder.bind(queue).to((FanoutExchange) exchange);
            } else {
                binding = BindingBuilder.bind(queue).to((DirectExchange) exchange).with(queueConfig.getRoutingKey());
            }

            // Add queue, exchange, and binding to declarables
            declarablesList.add(queue);
            declarablesList.add(exchange);
            declarablesList.add(binding);
        }

        return new Declarables(declarablesList);
    }

	public static class RabbitQueueConfig {
		private String name;
		private String exchange;
		private String routingKey;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getExchange() {
			return exchange;
		}

		public void setExchange(String exchange) {
			this.exchange = exchange;
		}

		public String getRoutingKey() {
			return routingKey;
		}

		public void setRoutingKey(String routingKey) {
			this.routingKey = routingKey;
		}
	}
}
