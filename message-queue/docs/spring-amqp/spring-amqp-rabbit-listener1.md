## Prompt

java 17, springboot 3.2, RabbitMQ 中, 幫我整理使用 springboot RabbitMQ Listener 的幾種方法, 整理他們個別的使用時機以及設計的目的


---

在 Spring Boot 3.2 中，使用 RabbitMQ Listener 有多種實作方式，每種方式都有不同的使用情境和設計目的。以下是幾種常見的 RabbitMQ Listener 實作方法及其使用時機：

---
## 1. @RabbitListener 註解

- 概述：`@RabbitListener` 是 Spring AMQP 提供的最常用的方式，它可以讓我們將某個方法標記為 RabbitMQ 佇列的監聽器。
- 使用場景：適用於 `簡單的訊息處理場景`，例如：
    - 需要對特定隊列的消息進行消費。
    - 消費者的邏輯可以獨立封裝在一個或多個方法中。
    - 不需要複雜的手動訊息確認機制（自動確認模式）。

範例：

```java
@Service
public class SimpleRabbitListener {

    @RabbitListener(queues = "queueName")
    public void listen(String message) {
        System.out.println("Received message: " + message);
    }
}
```

設計目的：

- 簡化訊息監聽器的定義。
- 提供基於註解的監聽器聲明方式，減少 XML 或 Java 配置程式碼。
- 預設使用自動確認機制（可透過 acknowledge 屬性進行手動確認配置）。

使用時機：

- 當訊息處理邏輯簡單，且不涉及複雜的事務控製或並發管理時，@RabbitListener 非常適合。
- 適合 快速開發和集成，只需透過註解即可完成監聽器的配置。


---
## 2. @RabbitListener + 手動確認機制

- 概述：在某些場景下，我們需要手動確認訊息，而不是自動確認（預設情況下 RabbitMQ 使用自動確認）。手動確認可以確保訊息在成功處理後才被確認，並且在處理失敗時可以重試。
- 使用場景：適用於 需要更高可靠性 的場景，例如：
    - 當訊息處理失敗時需要手動拒絕並重試。
    - 需要控制訊息的確認時機，確保訊息只在成功處理後才確認。
    - 訊息處理較為複雜，可能涉及事務控製或外部系統整合。

範例：

```java
@Service
public class ManualAckListener {

    @RabbitListener(queues = "queueName")
    public void listen(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            System.out.println("Processing message: " + message);
            // 業務處理邏輯
            channel.basicAck(tag, false); // 手動確認訊息
        } catch (Exception e) {
            try {
                channel.basicNack(tag, false, true); // 手動拒絕訊息並重新入隊
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
```

設計目的：

- 提供對訊息確認的更精細控制，允許開發者手動確認（ACK）、拒絕（NAK）或重新投遞訊息。
- 更適合需要處理失敗時執行重試機制的應用場景。
- 防止訊息遺失或意外確認，確保處理過程中的資料一致性。

使用時機：

- 當訊息處理失敗時不希望遺失訊息，需要重新處理的場景。
- 當訊息的處理需要與其他系統或資料庫交互，並希望在處理完成後再確認訊息。
- 避免訊息在處理失敗時被 RabbitMQ 自動確認，保證訊息處理的完整性和可靠性。


---
## 3. 並發處理與消費者線程池

- 概述：`@RabbitListener` 預設是單執行緒處理的，但對於高並發、高吞吐量的場景，可以透過設定消費者執行緒池來提升效能。
- 使用場景：適用於 高並發、大量訊息處理 的場景，例如：
    - 需要同時處理大量訊息時，單線程消費無法滿足效能要求。
    - 希望透過增加線程數來提升消費速度和並行處理能力。

範例：

```java
@Configuration
public class RabbitMQConfig {

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
        SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory) {

            SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
            factory.setConcurrentConsumers(5); // 設定初始並發消費者數量
            factory.setMaxConcurrentConsumers(10); // 最大並發消費者數量
            configurer.configure(factory, connectionFactory);
        return factory;
    }
}
```

```java
@Service
public class ConcurrentListener {

    @RabbitListener(queues = "concurrentQueue", containerFactory = "rabbitListenerContainerFactory")
    public void listen(String message) {
        System.out.println("Received message: " + message);
    }
}
```

設計目的：

- 提供並發處理能力，允許在多執行緒環境下處理 RabbitMQ 訊息。
- 透過配置 SimpleRabbitListenerContainerFactory 來控制消費者的並發數，適應高吞吐量的場景。
- 提高訊息的處理效能和系統的吞吐量。

使用時機：

- 當需要處理大量訊息且希望提高消費效率時，配置消費者線程池是必要的。
- 當單執行緒消費無法滿足業務需求，需要透過多執行緒並行處理來提高回應速度。
- 當需要根據系統負載動態調整並發消費者數量時。


---
## 4. 訊息批次處理

- 概述：`@RabbitListener` 支援訊息的大量消費，這意味著一次可以處理多個訊息。這種方式可以提升系統效能，因為每次拉取多個訊息而不是每次拉取一條。
- 使用場景：適用於 批次訊息處理 場景，例如：
    - 系統需要同時處理多個訊息並且可以在一次事務中處理這些訊息。
    - 批次處理訊息有助於減少網路開銷和系統的資源使用。
    - 在資料處理上有大量優化需求。

範例：

```java
@Service
public class BatchListener {

    @RabbitListener(queues = "batchQueue", containerFactory = "batchContainerFactory")
    public void listen(List<String> messages) {
        System.out.println("Received batch of messages: " + messages);
    }
}
```


```java
@Configuration
public class RabbitMQBatchConfig {

    @Bean
    public SimpleRabbitListenerContainerFactory batchContainerFactory(
        SimpleRabbitListenerContainerFactoryConfigurer configurer,
        ConnectionFactory connectionFactory) {

            SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
            factory.setBatchListener(true); // 開啟批次監聽
            factory.setConsumerBatchEnabled(true); // 消費者批次處理
            factory.setBatchSize(10); // 每次處理10個訊息
            configurer.configure(factory, connectionFactory);
        return factory;
    }
}
```

設計目的：

- 提供批次處理能力，允許每次從佇列中獲取多個訊息進行處理，從而減少 I/O 和提高處理效率。
- 更適合需要在一次事務中處理多個訊息的場景，減少網路開銷和事務開銷。

使用時機：

- 當需要批次處理數據，或希望一次處理多個訊息時。
- 當網路資源有限，批量處理有助於降低網路流量。
- 適合場景：日誌處理、資料整合、大量資料更新。

---

## 5. 訊息過濾

- 概述：Spring AMQP 提供了 `MessagePostProcessor`，可以在訊息到達消費者之前對訊息進行過濾或修改。
- 使用場景：適用於 需要對訊息進行預處理 或 過濾 的場景，例如：
    - 需要對訊息進行某種條件過濾，只有滿足條件的訊息才會被處理。
    - 在訊息到達消費者之前需要對其內容進行修改或增強。

範例：

```java
@Service
public class FilteredListener {

    @RabbitListener(queues = "filteredQueue")
    public void listen(String message) {
        System.out.println("Received filtered message: " + message);
    }
}
```


```java
@Configuration
public class RabbitMQFilterConfig {

    @Bean
    public SimpleRabbitListenerContainerFactory filteredListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setAfterReceivePostProcessors((MessagePostProcessor) message -> {
            // 過濾條件
            if (message.getBody().length > 100) {
                return message;
            }
                return null; // 過濾掉不符合條件的訊息
            });
        configurer.configure(factory, connectionFactory);
        return factory;
    }
}
```

設計目的：

- 提供對訊息內容進行預處理和過濾的能力。
- 可以減少無用或不符合條件的訊息傳遞到消費者，提高系統的處理效率。

使用時機：

- 當需要過濾某些無效或不需要處理的訊息時。
- 當希望對訊息進行某種預處理或增強時。


---
## 總結

實現方式 | 使用場景 | 設計目的
--------|--------|---------
@RabbitListener | 簡單的訊息處理場景，自動確認模式下適用。 | 簡化配置，快速開發。
@RabbitListener + 手動確認 | 需要手動控制 |