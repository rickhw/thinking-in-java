
## Prompt



---
## ChatGPT

在 RabbitMQ 與 Spring Boot 整合中，當 `@RabbitListener` 方法執行時，代表 RabbitMQ 已經將訊息交付到這個方法（Consumer）。**此時訊息已從 RabbitMQ 的 queue 中移出並由 Spring AMQP 負責傳遞給 `consumeHelloQueue` 方法。**

如果方法執行失敗且未設置手動回應（ACK），訊息會根據 RabbitMQ 的配置進行重新投遞或被丟棄。因此，控制訊息處理速度是 RabbitMQ 和 Consumer 之間的一個重要設計點。

---

### 如何控制 `dequeue` 的速度？

1. **手動 ACK 模式**  
   透過手動確認（ACK），你可以在訊息處理完成後告訴 RabbitMQ 繼續投遞訊息，否則訊息會留在 queue 中。

2. **設置 Prefetch Count**  
   RabbitMQ 支援設定 Prefetch Count，限制一次傳遞到 Consumer 的訊息數量。這樣可以控制 Consumer 每次最多處理的訊息數。

3. **批次處理**  
   配合 Prefetch Count，可以讓 Consumer 每次接收固定數量的訊息，處理完成後再繼續接收。

---

### 範例：控制一次只處理 3 個訊息

這段範例展示如何在 Spring Boot 中使用 Prefetch Count 和手動 ACK 來控制訊息消費的速度。

```java
package com.gtcafe.asimov.platform.hello.consumer;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HelloConsumer {

    @RabbitListener(queues = QueueName.HELLO_QUEUE, ackMode = "MANUAL")
    public void consumeHelloQueue(String eventString, Channel channel, org.springframework.amqp.core.Message message) throws Exception {
        log.info("Received message: [{}]", eventString);

        try {
            // 處理訊息
            log.info("Processing event: {}", eventString);

            // 處理完成，手動 ACK
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("Failed to process message, will reject: {}", eventString, e);

            // 如果失敗，拒絕訊息，並讓 RabbitMQ 決定是否重新投遞
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);

        // 設置 prefetch 為 3
        factory.setPrefetchCount(3);

        // 啟用手動確認模式
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return factory;
    }
}
```

---

### 配置重點解釋

1. **`@RabbitListener`**  
   - 使用 `ackMode = "MANUAL"` 開啟手動 ACK 模式。
   - RabbitMQ 將訊息傳遞到方法時，Spring AMQP 自動提供 `Channel` 和原始訊息。

2. **手動 ACK**  
   - `channel.basicAck` 確認訊息已成功處理。
   - `channel.basicNack` 可選擇重新投遞或直接丟棄訊息。

3. **設置 Prefetch Count**  
   - 設置 `prefetchCount = 3` 限制 Consumer 每次最多接收 3 個未完成的訊息。
   - RabbitMQ 只有在收到 ACK 後才會投遞新訊息。

---

### 運作流程
1. RabbitMQ 將最多 3 條訊息傳遞給 Consumer。
2. Consumer 處理訊息並手動確認（ACK）。
3. 當某條訊息處理完成後，RabbitMQ 再傳遞新訊息給 Consumer。
4. 如果訊息處理失敗，根據 `basicNack` 的配置，決定是否重新投遞。

這樣可以有效控制 Consumer 的負載，避免因訊息過多導致資源耗盡或系統不穩定。