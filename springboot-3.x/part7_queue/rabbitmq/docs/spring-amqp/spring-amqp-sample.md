

## Prompt

用 java 17, springboot 3.2, gradle ,rabbitmq ，幫我提供以下 producer / consumer 的範例程式：

1. producer / consumer 兩者共用同一個 event  的資料結構, 放在同一個 package
2. event 本身有一個 eventType, 與實際的 message (或者稱為 data)
3. message 是個抽象介面, 依據不同的 eventType, 在 consumer 收到 event 之後，交由不同的 handler 處理
4. 在 consumer 端，有一個抽象介面 IEventHandler

先基於這樣的條件，提供 Producer / Consumer / 以及 common event package 範例程式

---

下面是基于您提供的条件，使用 Java 17、Spring Boot 3.2、Gradle 和 RabbitMQ 的 Producer/Consumer 以及共享事件包的示例代码结构。我们将定义一个共享事件包 common-event，包含 Event 和 Message 接口。同时，Consumer 端会使用抽象 IEventHandler 来根据 EventType 处理不同的消息。

## 1. 共享的 common-event 包

```java
package com.example.common.event;

public class Event<T extends Message> {
    private final EventType eventType;
    private final T data;

    public Event(EventType eventType, T data) {
        this.eventType = eventType;
        this.data = data;
    }

    public EventType getEventType() {
        return eventType;
    }

    public T getData() {
        return data;
    }
}

// 定义事件类型的枚举
public enum EventType {
    TYPE_A,
    TYPE_B
}

// Message 是一个抽象接口
public interface Message {
    String getMessageContent();
}

// TypeAMessage 实现 Message 接口
public class TypeAMessage implements Message {
    private final String messageContent;

    public TypeAMessage(String messageContent) {
        this.messageContent = messageContent;
    }

    @Override
    public String getMessageContent() {
        return messageContent;
    }
}

// TypeBMessage 实现 Message 接口
public class TypeBMessage implements Message {
    private final String messageContent;

    public TypeBMessage(String messageContent) {
        this.messageContent = messageContent;
    }

    @Override
    public String getMessageContent() {
        return messageContent;
    }
}
```

## 2. Producer 示例代码

```java
package com.example.producer;

import com.example.common.event.*;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventProducer {
    private final AmqpTemplate amqpTemplate;

    @Autowired
    public EventProducer(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    // 发送 Event 消息
    public void sendEvent(Event<? extends Message> event) {
        amqpTemplate.convertAndSend("eventExchange", "eventRoutingKey", event);
        System.out.println("Event Sent: " + event.getEventType());
    }

    // 示例：发送 TypeAMessage
    public void sendTypeAEvent() {
        TypeAMessage message = new TypeAMessage("This is a Type A Message");
        Event<TypeAMessage> event = new Event<>(EventType.TYPE_A, message);
        sendEvent(event);
    }

    // 示例：发送 TypeBMessage
    public void sendTypeBEvent() {
        TypeBMessage message = new TypeBMessage("This is a Type B Message");
        Event<TypeBMessage> event = new Event<>(EventType.TYPE_B, message);
        sendEvent(event);
    }
}
```

## 3. Consumer 示例代码

```java
package com.example.consumer;

import com.example.common.event.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class EventConsumer {

    private final IEventHandler<TypeAMessage> typeAHandler;
    private final IEventHandler<TypeBMessage> typeBHandler;

    public EventConsumer(IEventHandler<TypeAMessage> typeAHandler, IEventHandler<TypeBMessage> typeBHandler) {
        this.typeAHandler = typeAHandler;
        this.typeBHandler = typeBHandler;
    }

    // RabbitMQ 消费者
    @RabbitListener(queues = "eventQueue")
    public void receiveEvent(Event<? extends Message> event) {
        switch (event.getEventType()) {
            case TYPE_A -> typeAHandler.handle((TypeAMessage) event.getData());
            case TYPE_B -> typeBHandler.handle((TypeBMessage) event.getData());
            default -> throw new IllegalArgumentException("Unknown event type: " + event.getEventType());
        }
    }
}
```

## 4. IEventHandler 接口及其实现

```java
package com.example.consumer;

import com.example.common.event.Message;

// 定义 IEventHandler 接口
public interface IEventHandler<T extends Message> {
    void handle(T message);
}

// TypeAEventHandler 处理 TypeAMessage
@Service
public class TypeAEventHandler implements IEventHandler<TypeAMessage> {
    @Override
    public void handle(TypeAMessage message) {
        System.out.println("Handling Type A Message: " + message.getMessageContent());
    }
}

// TypeBEventHandler 处理 TypeBMessage
@Service
public class TypeBEventHandler implements IEventHandler<TypeBMessage> {
    @Override
    public void handle(TypeBMessage message) {
        System.out.println("Handling Type B Message: " + message.getMessageContent());
    }
}
```


## 5. 配置 RabbitMQ

在 Spring Boot 中，我们需要配置 RabbitMQ 的 exchange、queue 和 routing key。

```java
package com.example.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 定义 Exchange
    @Bean
    public Exchange eventExchange() {
        return ExchangeBuilder.directExchange("eventExchange").durable(true).build();
    }

    // 定义 Queue
    @Bean
    public Queue eventQueue() {
        return QueueBuilder.durable("eventQueue").build();
    }

    // 将 Queue 和 Exchange 绑定
    @Bean
    public Binding eventBinding(Queue eventQueue, Exchange eventExchange) {
        return BindingBuilder.bind(eventQueue).to(eventExchange).with("eventRoutingKey").noargs();
    }
}
```

## 6. Gradle 依赖

确保在 build.gradle 文件中添加以下依赖：

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-amqp'
    implementation 'org.springframework.boot:spring-boot-starter'
    implementation 'org.springframework.boot:spring-boot-starter-web'

    // 其他依赖
}
```

## 7. 总结

事件结构 (Event, Message, EventType) 放在 common.event 包下，供 Producer 和 Consumer 共同使用。

- Producer 根据不同类型的消息发送事件。
- Consumer 使用抽象的 IEventHandler 根据 EventType 处理不同的消息类型。

这样，您可以根据实际业务需求扩展 EventType、Message 实现类和 IEventHandler 实现类。