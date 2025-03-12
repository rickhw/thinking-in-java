## Prmopt

除了 @RabbitListener 之外，springboot 3.2 還有沒有其他方法可以用的？

---

除了 @RabbitListener，Spring Boot 3.2 还提供了其他方法来实现 RabbitMQ 消息的监听和处理，通常是在需要更灵活、复杂或低级别的消息处理场景下使用。以下是几种常见的替代方法及其使用场景：

## 1. SimpleMessageListenerContainer

- 概述：SimpleMessageListenerContainer 是 Spring AMQP 提供的低级别消息监听容器，它允许你手动配置消息监听器并控制消息的并发性、事务性和其他高级功能。相比 @RabbitListener，它提供了更多的灵活性和控制能力。
- 使用场景：
    - 高级场景，例如：自定义消费者线程池、消息事务管理、自定义消息确认等。
    - 不想使用注解的场景，或者需要对队列监听的管理有更细粒度的控制。

示例：

```java
@Configuration
public class RabbitMQConfig {

    @Bean
    public SimpleMessageListenerContainer messageListenerContainer(
            ConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter) {

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames("queueName");
        container.setMessageListener(listenerAdapter);
        container.setConcurrentConsumers(3); // 并发消费者数量
        container.setMaxConcurrentConsumers(10); // 最大并发消费者数量
        return container;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(MessageHandler handler) {
        return new MessageListenerAdapter(handler, "handleMessage");
    }
}

@Service
public class MessageHandler {

    public void handleMessage(String message) {
        System.out.println("Message received: " + message);
    }
}
```

设计目的：

- 提供对消息监听的更多手动控制，包括并发管理、消费者控制、事务处理等。
- 适合需要定制化控制的应用，允许直接操作 RabbitMQ 的监听容器。

使用时机：

- 当需要对监听器进行复杂配置时，SimpleMessageListenerContainer 是很好的选择。
- 在需要细粒度控制并发和事务处理的场景下使用。


---
## 2. DirectMessageListenerContainer

- 概述：DirectMessageListenerContainer 是 Spring AMQP 提供的另一种监听容器，它与 SimpleMessageListenerContainer 类似，但与后者相比，它不使用 RabbitMQ 的消费池，并且更适合处理短连接和大量消费者的场景。
- 使用场景：
    - 需要更高的消息处理性能和更快速的消费启动/关闭时间。
    - 高并发或大规模消费的场景，适合需要管理大量短暂消费者的系统。

示例：

```java
@Configuration
public class RabbitMQDirectConfig {

    @Bean
    public DirectMessageListenerContainer directMessageListenerContainer(
            ConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter) {

        DirectMessageListenerContainer container = new DirectMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames("queueName");
        container.setMessageListener(listenerAdapter);
        container.setConsumersPerQueue(5); // 每个队列的消费者数量
        return container;
    }
}

@Service
public class DirectMessageHandler {

    public void handleMessage(String message) {
        System.out.println("Direct Message received: " + message);
    }
}
```

设计目的：

- 提供与 RabbitMQ 连接更加灵活和直接的消息监听容器，不使用 RabbitMQ 消费者线程池。
- 比 SimpleMessageListenerContainer 提供更快的响应速度和更灵活的消费者管理。

使用时机：

- 当应用程序需要 更高的性能，尤其是在短连接和快速消费启动/关闭场景下。
- 需要对消费者实例和队列绑定进行高效管理的场景。


---

## 3. 使用 RabbitTemplate 接收消息

- 概述：RabbitTemplate 是 Spring AMQP 提供的核心组件之一，它通常用于发送消息，但它也可以用于同步或异步地接收消息。相比 @RabbitListener，RabbitTemplate 更适合简单的、点对点的消息消费场景。
- 使用场景：
    - 需要 同步接收消息 的场景，或者希望程序通过轮询主动获取消息而不是监听。
    - 请求/响应模型下，消费者希望在特定时机拉取消息而不是持续监听。

示例：

```java
@Service
public class RabbitTemplateReceiver {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public RabbitTemplateReceiver(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void receiveMessage() {
        String message = (String) rabbitTemplate.receiveAndConvert("queueName");
        System.out.println("Received message using RabbitTemplate: " + message);
    }
}
```

设计目的：

- 提供主动拉取（Pull）的消息接收方式，而不是依赖监听器的推送（Push）方式。
- 适合需要在特定时间或事件点手动获取消息的应用。

使用时机：

- 当消息消费不是持续进行时，可以使用 RabbitTemplate 来主动拉取消息。
- 适合需要点对点拉取消息或处理单个消息的场景。

---


## 4. 使用 Channel（低级别 API）

- 概述：通过使用 RabbitMQ 的原生 Channel API，可以获得对 RabbitMQ 更细粒度的控制。通过 Channel，开发者可以手动创建连接、声明队列、消费消息，并且可以完全控制消息的消费确认、事务处理等。
- 使用场景：
    - 需要直接控制 RabbitMQ 的低级别功能，例如：手动管理连接、消息确认、重试、事务控制等。
    - 需要绕过 Spring 提供的高级封装，直接操作 RabbitMQ 原生 API。

示例：

```java
@Service
public class ChannelReceiver {

    private final ConnectionFactory connectionFactory;

    @Autowired
    public ChannelReceiver(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void receiveMessage() throws Exception {
        try (Connection connection = connectionFactory.createConnection();
             Channel channel = connection.createChannel(false)) {
            channel.queueDeclare("queueName", true, false, false, null);
            GetResponse response = channel.basicGet("queueName", false);
            if (response != null) {
                String message = new String(response.getBody(), "UTF-8");
                System.out.println("Received message: " + message);
                channel.basicAck(response.getEnvelope().getDeliveryTag(), false); // 手动确认消息
            }
        }
    }
}
```

设计目的：

- 提供对 RabbitMQ 的 完全控制权，允许开发者在底层层次上操作队列、通道、连接、事务等。
- 适合高级场景下需要完全控制消费行为或实现非常定制化的处理逻辑。

使用时机：

- 当需要完全控制消息确认和事务时。
- 当需要与 RabbitMQ 进行直接通信而绕过 Spring AMQP 的封装时。


---

## 5. 使用 Reactive 编程模型

- 概述：Spring AMQP 也支持响应式编程模型，通过集成 Project Reactor 或者其他响应式框架，可以在 Spring Boot 中使用 非阻塞 的方式来处理 RabbitMQ 消息。
- 使用场景：
    - 高并发、非阻塞的应用场景，尤其是在需要响应式编程模式的系统中。
    - 适合需要处理大量请求并希望尽可能高效地利用系统资源的场景。

示例：

```java
@Service
public class ReactiveRabbitListener {

    private final ReactiveMessageListenerContainer reactiveContainer;

    @Autowired
    public ReactiveRabbitListener(ReactiveMessageListenerContainer reactiveContainer) {
        this.reactiveContainer = reactiveContainer;
    }

    @PostConstruct
    public void startListening() {
        reactiveContainer.receive("queueName")
            .subscribe(message -> {
                String body = new String(message.getBody());
                System.out.println("Reactive message received: " + body);
            });
    }
}
```

设计目的：

- 提供非阻塞的消息消费方式，适合高并发、低延迟的场景。
- 利用响应式编程模型优化资源使用，处理大量请求而不会阻塞线程。

使用时机：

- 当系统需要利用响应式架构来处理 RabbitMQ 消息时。
- 适合需要大规模并发处理并且避免阻塞线程的场景，例如微服务架构中的非阻塞通信。

## 总结

方法 | 使用场景 | 设计目的
@RabbitListener | 简单的消息处理，自动或手动确认 | 快速集成，简化开发过程
SimpleMessageListenerContainer | 需要更多控制消息处理的场景，如并发、事务和复杂的确认机制 | 提供更灵活和自定义的控制
DirectMessageListenerContainer | 高性能消息处理，短连接或高并发场景 | 提供快速的消费启动和关闭，适合大规模短暂消费者的场景