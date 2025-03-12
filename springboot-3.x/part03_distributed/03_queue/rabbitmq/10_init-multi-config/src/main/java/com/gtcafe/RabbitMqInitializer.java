package com.gtcafe;

import java.util.List;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@ConfigurationProperties(prefix = "app.rabbitmq")
public class RabbitMqInitializer {

    @Value("${app.rabbitmq.autoInit:true}")
    private boolean autoInit;

    @Value("${app.rabbitmq.reset:false}")
    private boolean reset;

    private List<QueueConfig> queues;

    public List<QueueConfig> getQueues() { return queues; }
    public void setQueues(List<QueueConfig> queues) { this.queues = queues; }

    // public QueueConfig getQueueConfig(String queueName) {
	// 	return queueMap.get(queueName);
	// }


    @Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		return rabbitTemplate;
	}

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    @DependsOn("rabbitAdmin")
    public CommandLineRunner initRabbitMQ(RabbitAdmin rabbitAdmin) {
        return args -> {
            System.out.println("Initiate the Queue ... ");
            if (reset) {
                System.out.println("Reset flag is true, deleting existing queues...");
                for (QueueConfig config : queues) {
                    rabbitAdmin.deleteQueue(config.getName());
                    System.out.printf("  - Queue [%s] deleted.\n", config.getName());
                }
            }

            if (autoInit) {
                System.out.println("AutoInit is true, initializing queues...");
                for (QueueConfig config : queues) {
                    Queue queue = new Queue(config.getName(), true);
                    rabbitAdmin.declareQueue(queue);

                    System.out.printf("  - Set Queue: [%s], exchange: [%s].\n", config.getName(), config.getExchange());

                    if ("fanoutExchange".equals(config.getExchange())) {
                        FanoutExchange exchange = new FanoutExchange(config.getExchange());
                        rabbitAdmin.declareExchange(exchange);
                        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange));
                    } else {
                        DirectExchange exchange = new DirectExchange(config.getExchange());
                        rabbitAdmin.declareExchange(exchange);
                        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(config.getRoutingKey()));
                    }
                }
                System.out.println("RabbitMQ Queues, Exchanges, and Bindings initialized.");
            }
        };
    }
}
