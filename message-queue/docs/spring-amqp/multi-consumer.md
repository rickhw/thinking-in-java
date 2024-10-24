## Prompt

關於 producer / consumer 的運作, 如果 consumer 有多個 instance 在執行，而 message 只能被執行一次的狀況之下，請整理摘要以及實作方式

---


為了確保在有多個 Consumer 實例同時運作的情況下，RabbitMQ 中的訊息 只能被處理一次（即訊息不會被重複消費），我們需要考慮以下幾個要點：

## 1. Queue 的特性

RabbitMQ 預設的行為是將訊息分發給消費者時，使用輪詢調度（Round-Robin Dispatching），也就是說，對於同一個佇列中的訊息，每個消費者實例只會獲得一部分訊息，並且一個訊息只能被一個消費者處理。因此，RabbitMQ 本身已經具備了讓訊息在多個消費者實例中只被消費一次的能力。

## 2. 訊息的 Acknowledgment (確認機制)

為了確保訊息 成功處理一次，RabbitMQ 提供了訊息確認機制。每個訊息在消費後，消費者都需要 手動確認（acknowledgment） 訊息已經成功處理。沒有被確認的訊息會被重新投遞給其他消費者。你可以透過以下方式啟用訊息確認機制：

- 手動 ack 機制：消費完成訊息後，呼叫 channel.basicAck() 手動確認。
- 消費者實例宕機或處理失敗時，RabbitMQ 會將未確認的訊息重新放回佇列中，確保它被其他消費者接收。

## 3. 消費者公平調度 (Fair Dispatch)

預設情況下，RabbitMQ 使用的輪詢調度不考慮每個消費者處理訊息的速度。為了避免某些消費者積壓訊息，可以啟用 公平調度（Fair Dispatch），讓消費者只有在處理完之前的訊息並發送 ack 之後，才會接收到新的訊息。

## 4. 設定 prefetchCount

透過設定 prefetchCount，你可以讓每個消費者一次只消費一定數量的訊息，在訊息被 ack 之前，不會再發送新的訊息。這樣可以實現更公平的消息分發，避免某位消費者積壓過多訊息。


---

# 實作細節

## 1. 手動 ack 機制

在 Spring Boot 中，預設使用自動 ack。若要啟用手動 ack，需要在 RabbitListener 中明確處理訊息確認。

```java
package com.example.consumer;

import com.example.common.event.*;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;

@Service
public class EventConsumer implements ChannelAwareMessageListener {

    private final IEventHandler<TypeAMessage> typeAHandler;
    private final IEventHandler<TypeBMessage> typeBHandler;

    public EventConsumer(IEventHandler<TypeAMessage> typeAHandler, IEventHandler<TypeBMessage> typeBHandler) {
        this.typeAHandler = typeAHandler;
        this.typeBHandler = typeBHandler;
    }

    // 手動 ack 模式
    @Override
    @RabbitListener(queues = "eventQueue")
    public void onMessage(Message message, Channel channel) throws Exception {
        try {
            // 從訊息中解析 Event
            Event<? extends com.example.common.event.Message> event = (Event<? extends com.example.common.event.Message>) SerializationUtils.deserialize(message.getBody());

            switch (event.getEventType()) {
                case TYPE_A -> typeAHandler.handle((TypeAMessage) event.getData());
                case TYPE_B -> typeBHandler.handle((TypeBMessage) event.getData());
                default -> throw new IllegalArgumentException("Unknown event type: " + event.getEventType());
            }

            // 手動確認訊息已經處理
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // 處理失敗時，拒絕訊息並返回佇列重新投遞
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }
}
```
在這個例子中：

- basicAck() 用於確認訊息已成功處理。
- basicNack() 用於在處理失敗時拒絕訊息，並選擇是否重新放回佇列。

## 2. 設定 prefetchCount 實現公平調度

你可以透過設定 RabbitMQ 的 prefetchCount 來控制每個消費者一次能接收多少未確認的訊息。在 Spring Boot 中，可以透過設定 SimpleMessageListenerContainer 來設定。

```java
package com.example.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public SimpleMessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory,
            ChannelAwareMessageListener listener) {

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames("eventQueue");
        container.setMessageListener(listener);

        // 設定 prefetchCount，控制每個消費者最多可以取得多少個未確認的訊息
        container.setPrefetchCount(1);
        return container;
    }
}
```

## 3. 訊息持久化與佇列持久化

為了確保訊息不會在 RabbitMQ 宕機或異常時遺失，我們還可以設定訊息和佇列的 持久化。在佇列和訊息的定義中，需要設定 durable=true。

```java
@Bean
public Queue eventQueue() {
    return QueueBuilder.durable("eventQueue").build();
}

@Bean
public Exchange eventExchange() {
    return ExchangeBuilder.directExchange("eventExchange").durable(true).build();
}
```

在生產者中，可以在發送訊息時設定訊息為持久化：

```java
public void sendEvent(Event<? extends Message> event) {
    MessageProperties messageProperties = new MessageProperties();
    messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT); // 訊息持久化
    Message rabbitMessage = new Message(SerializationUtils.serialize(event), messageProperties);

    amqpTemplate.convertAndSend("eventExchange", "eventRoutingKey", rabbitMessage);
    System.out.println("Event Sent: " + event.getEventType());
}
```

總結：

- RabbitMQ 預設的訊息調度機制 保證每個訊息只會被一個消費者實例消費。
- 手動確認機制（Manual Ack） 確保訊息成功處理後才會從佇列中刪除，失敗時可以重試。
- 公平調度機制（Fair Dispatch） 可以透過 prefetchCount 來防止某些消費者被過多訊息淹沒。
- 訊息持久化 和 佇列持久化 可以保證在意外宕機情況下不會遺失訊息。

透過這些配置，您可以確保在多個 Consumer 實例同時運作的情況下，訊息只會被處理一次且處理可靠。