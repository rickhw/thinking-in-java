## Prompt

同樣的條件，針對 springboot 3.2, 請整理 rabbitMQ 的 Producer 的用法與情境，像是我有多個 queue 的 producer  應該要怎麼用？

---
在 Spring Boot 3.2 中，RabbitMQ 的 Producer 用法非常靈活，支援將訊息傳送到多個佇列。 Producer 主要透過 RabbitTemplate 來傳送訊息，而不同的佇列可以透過交換器（Exchange）和路由鍵（Routing Key）來動態選擇。以下是幾種常見的 Producer 用法以及使用多個佇列的場景整理：

## 1. 基本的 RabbitTemplate 使用

- 概述：RabbitTemplate 是 Spring AMQP 提供的核心元件，用於傳送訊息到 RabbitMQ 佇列。你可以指定佇列的名稱，或透過路由鍵將訊息傳送到綁定的交換器。
- 使用場景：單一佇列的訊息發送。適合簡單場景，需要將訊息傳送到指定的佇列。

範例：

```java
@Service
public class SimpleProducer {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public SimpleProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend("queueName", message);
        System.out.println("Message sent to queue: " + message);
    }
}
```

使用時機：

- 當訊息只需要傳送到某個指定佇列時，適用於簡單的生產者需求。
- 當系統只有單一佇列，且沒有複雜的路由需求。


## 2. 使用 Exchange 和 Routing Key 傳送訊息

- 概述：透過將訊息傳送到交換器（Exchange），生產者可以透過 路由鍵（Routing Key） 動態將訊息路由到不同的佇列。常見的交換器類型有：Direct Exchange、Topic Exchange、Fanout Exchange 和 Headers Exchange。
- 使用場景：
    - 動態路由訊息到不同的佇列，根據路由鍵相符的規則來決定訊息的去向。
    - 多個佇列場景下，生產者需要傳送訊息到不同佇列，適合複雜的訊息流。

範例：

```java
@Service
public class RoutingProducer {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public RoutingProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessageToQueue1(String message) {
        rabbitTemplate.convertAndSend("directExchange", "queue1RoutingKey", message);
        System.out.println("Message sent to queue1: " + message);
    }

    public void sendMessageToQueue2(String message) {
        rabbitTemplate.convertAndSend("directExchange", "queue2RoutingKey", message);
        System.out.println("Message sent to queue2: " + message);
    }
}
```

使用時機：

- 多個佇列 場景下，生產者需要根據不同的邏輯將訊息傳送到不同的佇列。
- 使用交換器來實現更靈活的訊息路由，適合 多對多 訊息傳遞需求。


---

## 3. 傳送訊息到多個佇列（Fanout Exchange）

- 概述：Fanout Exchange 是一種將訊息廣播到所有綁定佇列的交換器類型。無論路由鍵是什麼，綁定到 Fanout Exchange 的所有佇列都會收到訊息。
- 使用場景：廣播訊息 場景下，生產者需要將同一則訊息傳送到多個佇列，所有消費者都會收到訊息。

範例：

```java
@Service
public class BroadcastProducer {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public BroadcastProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendBroadcastMessage(String message) {
        rabbitTemplate.convertAndSend("fanoutExchange", "", message);
        System.out.println("Broadcast message sent: " + message);
    }
}
```

使用時機：

- 當你希望訊息被 所有綁定的隊列 接收到。
- 適合需要廣播訊息的場景，例如：日誌收集、全域事件通知。

---

## 4. Topic Exchange（基於通配符的路由）

- 概述：Topic Exchange 允許使用通配符（* 和 #）來匹配路由鍵，並根據規則將訊息傳送到不同的佇列。 * 配對一個單詞，# 匹配零個或多個單字。
- 使用場景：
    - 模式比對路由 場景，生產者可以根據複雜的規則將訊息路由到多個佇列。
    - 適合 主題訂閱 場景，訊息只會傳送到符合路由鍵規則的佇列。

範例：

```java
@Service
public class TopicProducer {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public TopicProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessageToTopic1(String message) {
        rabbitTemplate.convertAndSend("topicExchange", "topic.queue1", message);
        System.out.println("Message sent to topic.queue1: " + message);
    }

    public void sendMessageToTopic2(String message) {
        rabbitTemplate.convertAndSend("topicExchange", "topic.queue2.sub", message);
        System.out.println("Message sent to topic.queue2.sub: " + message);
    }
}
```

使用時機：

- 適合需要基於 主題匹配 來動態路由訊息的場景。
- 當有多個子系統訂閱某些特定主題，生產者可以發送不同類型的訊息到符合規則的佇列。

---

## 5. 非同步傳送訊息並確認（Publisher Confirms）

- 概述：RabbitMQ 支援 非同步發送訊息並確認，即生產者可以發送訊息後，非同步接收 RabbitMQ 的確認訊息。這種方式適合確保訊息成功投遞到佇列。
- 使用場景：
    - 高可靠性 場景，確保訊息成功到達 RabbitMQ 佇列。
    - 適合需要對訊息投遞進行確認的場景，如 金融、訂單處理 等對訊息可靠性要求較高的場景。

範例：

```java
@Configuration
public class RabbitMQConfig {

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {

    RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setConfirmCallback((correlationData, ack, cause) -> {
                if (ack) {
                    System.out.println("Message successfully sent to RabbitMQ");
                } else {
                    System.out.println("Failed to send message: " + cause);
                }
            }
        );
        return template;
    }
}
```

```java
@Service
public class ConfirmProducer {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public ConfirmProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessageWithConfirm(String message) {
        rabbitTemplate.convertAndSend("directExchange", "queueRoutingKey", message);
        System.out.println("Message sent with confirm: " + message);
    }
}
```

使用時機：

- 需要高可靠性和投遞確認 的場景。
- 當系統無法接受訊息遺失時，非同步確認可以確保訊息正確投遞到佇列。

---

## 6. 事務性訊息傳送

- 概述：RabbitMQ 支援透過 事務 來傳送訊息。透過開啟事務，可以確保訊息在事務成功提交後才會被發送。如果事務回滾，訊息也不會被傳送。
- 使用場景：
 - 需要強力事務保障 的場景，例如：金融交易、訂單處理等需要確保訊息與資料庫操作一致性。

範例：

```java
@Service
public class TransactionalProducer {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public TransactionalProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public void sendTransactionalMessage(String message) {
        try {
            rabbitTemplate.convertAndSend("queueName", message);
            // 模擬其他業務操作，例如資料庫操作
            System.out.println("Transactional message sent: " + message);
        } catch (Exception e) {
            // 回滾事務
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }
}
```

使用時機：

- 需要確保訊息傳送與業務邏輯一致 的場景，確保訊息和資料庫等作業在相同事務中處理。

---

## 總結

Producer | 實作方式 | 使用場景 | 設計目的
---------|--------|---------|---------
RabbitTemplate 單一佇列發送 | 簡單訊息發送，單一佇列 | 簡化訊息傳送邏輯，快速開發
使用 Exchange 和 Routing Key 傳送訊息 | 動態路由訊息到不同佇列，多佇列場景 | 靈活的訊息路由，根據路由鍵動態比對佇列
Fanout Exchange 廣播訊息 | 廣播訊息到所​​有佇列 | 適合需要訊息廣播的場景，如日誌收集、全域事件通知
Topic Exchange 基於通配符路由 | 主題匹配的訊息路由，多隊列場景 | 根據複雜的主題規則將訊息路由到符合規則