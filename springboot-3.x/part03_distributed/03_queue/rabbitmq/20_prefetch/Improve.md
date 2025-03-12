
這幾段 code 是用來測試 rabbitmq 的 prefetch 效果, 這邊有幾個問題想請教一下:

1. 如何知道正在處理的 message count
2. 有沒有方法可以動態調整 prefetch 的數量？

程式如下:

// RabbitMQConfig.java
package com.gtcafe;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NAME = "example-queue";

    @Bean
    public Queue exampleQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);

        // 設置 prefetch 為 3
        factory.setPrefetchCount(50);

        // 啟用手動確認模式
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return factory;
    }
}

// MessageController.java
package com.gtcafe;

import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/messages")
@Slf4j
public class MessageController {

    private final RabbitTemplate rabbitTemplate;

    public MessageController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @GetMapping
    public ResponseEntity<String> sendMessage() {
        String uuid = UUID.randomUUID().toString();

        log.info("Enqueue: [{}]", uuid);
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, uuid);

        return ResponseEntity.ok("Message sent: [" + uuid + "]");
    }
}

// MessageConsumer.java
package com.gtcafe;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MessageConsumer {

    private static final int DELAY = 50;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME, ackMode = "MANUAL")
    public void consumeHelloQueue(String eventString, Channel channel, org.springframework.amqp.core.Message message) throws Exception {
        log.info("Dequeue, message: [{}]", eventString);

        try {
            // 處理訊息
            log.info("Processing event, delay: [{}ms]", DELAY);
            Thread.sleep(DELAY);

            // 處理完成，手動 ACK
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        } catch (Exception e) {
            log.error("Failed to process message, will reject: {}", eventString, e);
            // 如果失敗，拒絕訊息，並讓 RabbitMQ 決定是否重新投遞
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }

    }

}


----


我來回答你的問題：

1. 關於如何知道正在處理的 message count，有幾種方法：



```java
@Service
@Slf4j
public class MessageConsumer {
    private final AtomicInteger processingCount = new AtomicInteger(0);
    
    // 新增一個方法來獲取當前處理中的消息數量
    public int getProcessingMessageCount() {
        return processingCount.get();
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME, ackMode = "MANUAL")
    public void consumeHelloQueue(String eventString, Channel channel, Message message) throws Exception {
        processingCount.incrementAndGet();
        try {
            log.info("Currently processing messages: {}", processingCount.get());
            log.info("Dequeue, message: [{}]", eventString);
            
            // 處理訊息
            log.info("Processing event, delay: [{}ms]", DELAY);
            Thread.sleep(DELAY);
            
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("Failed to process message, will reject: {}", eventString, e);
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        } finally {
            processingCount.decrementAndGet();
        }
    }
    
    // 新增一個 REST endpoint 來查詢處理中的消息數量
    @RestController
    @RequestMapping("/api/queue-stats")
    public static class QueueStatsController {
        private final MessageConsumer messageConsumer;
        
        public QueueStatsController(MessageConsumer messageConsumer) {
            this.messageConsumer = messageConsumer;
        }
        
        @GetMapping("/processing-count")
        public ResponseEntity<Map<String, Integer>> getProcessingCount() {
            return ResponseEntity.ok(Map.of(
                "processingCount", messageConsumer.getProcessingMessageCount()
            ));
        }
    }
}

```

2. 關於動態調整 prefetch 數量，可以透過以下方式實現：

```java
@Configuration
public class RabbitMQConfig {
    private final SimpleRabbitListenerContainerFactory factory;
    
    public RabbitMQConfig(SimpleRabbitListenerContainerFactory factory) {
        this.factory = factory;
    }
    
    @RestController
    @RequestMapping("/api/rabbitmq-config")
    public class RabbitMQConfigController {
        @PutMapping("/prefetch")
        public ResponseEntity<String> updatePrefetchCount(@RequestParam int prefetchCount) {
            try {
                // 獲取所有的消費者容器
                for (SimpleMessageListenerContainer container : 
                        factory.getListenerContainer().getApplicationContext()
                        .getBeansOfType(SimpleMessageListenerContainer.class)
                        .values()) {
                    // 更新 prefetch count
                    container.setPrefetchCount(prefetchCount);
                }
                return ResponseEntity.ok("Prefetch count updated to: " + prefetchCount);
            } catch (Exception e) {
                return ResponseEntity.internalServerError()
                    .body("Failed to update prefetch count: " + e.getMessage());
            }
        }
    }
}

```

使用說明：

1. 對於監控處理中的消息數量：
   - 使用 `AtomicInteger` 來追蹤正在處理的消息數量
   - 在處理消息前增加計數，完成後減少計數
   - 提供了一個 REST endpoint `/api/queue-stats/processing-count` 來查詢當前處理中的消息數量

2. 對於動態調整 prefetch：
   - 提供了一個 REST endpoint `/api/rabbitmq-config/prefetch` 來動態調整 prefetch 數量
   - 可以通過 PUT 請求來更新 prefetch 值
   - 這個改變會影響到所有現有的消費者容器

使用範例：

```bash
# 查詢當前處理中的消息數量
curl http://localhost:8080/api/queue-stats/processing-count

# 動態調整 prefetch 數量
curl -X PUT "http://localhost:8080/api/rabbitmq-config/prefetch?prefetchCount=100"
```

需要注意的是，動態調整 prefetch 數量可能會對系統性能產生影響，建議在進行調整時要謹慎，最好在非峰值時段進行測試。同時，也要考慮到系統的資源限制，不要設置過大的 prefetch 值。
