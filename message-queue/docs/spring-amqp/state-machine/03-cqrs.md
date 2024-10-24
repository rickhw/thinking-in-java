請繼續提供 Command Query Responsibility Segregation 的設計範例

---
# ChatGPT

Command Query Responsibility Segregation (CQRS) 是一種架構設計模式，它將「查詢」與「命令」的責任區分開來，這樣可以分離處理數據的方式，提高系統的擴展性和性能。尤其是在高並發系統中，CQRS 非常適合處理大量的讀寫操作。

在這個設計範例中，CQRS 將用於處理狀態機的狀態變更操作，並且這裡會整合 Spring Statemachine 和 RabbitMQ，從而應用到多個不同的服務。

CQRS 的架構原則：

- Command: 負責執行修改操作（例如，狀態轉移）。
- Query: 負責查詢數據（例如，查詢狀態的歷史記錄或當前狀態）。
- Event Store: 保存所有命令操作的事件，使得讀操作可以基於事件進行查詢（可以用 Redis 或 Kafka 作為事件存儲的後端）。

## 系統結構

- Command Service：接收並執行命令，處理狀態變更邏輯。
- Query Service：處理查詢邏輯，根據事件存儲查詢狀態。
- Event Publisher：將狀態變更事件發布到消息隊列（RabbitMQ）。
- Event Consumer：消費狀態變更事件，記錄狀態變更的歷史記錄。

### 1. 命令服務 (CommandService)

```java
package com.example.cqrs.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;

import com.example.cqrs.config.Events;
import com.example.cqrs.config.States;

@Service
public class CommandService {

    private static final Logger logger = LoggerFactory.getLogger(CommandService.class);

    @Autowired
    private StateMachine<States, Events> stateMachine;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void changeState(String processId, Events event) {
        logger.info("Changing state for process: {}", processId);
        Message<Events> message = MessageBuilder.withPayload(event).build();
        boolean accepted = stateMachine.sendEvent(message);
        if (accepted) {
            publishEvent(processId, event);
        } else {
            logger.error("Failed to change state for process: {}", processId);
        }
    }

    private void publishEvent(String processId, Events event) {
        logger.info("Publishing event to RabbitMQ: {}", event);
        rabbitTemplate.convertAndSend("stateExchange", "state.routingKey", new StateChangeEvent(processId, event));
    }
}
```

### 2. 狀態事件 (StateChangeEvent)

這是一個用來封裝狀態變更的事件對象，會被發送到 RabbitMQ 中。

```java
package com.example.cqrs.command;

import com.example.cqrs.config.Events;

public class StateChangeEvent {
    private String processId;
    private Events event;

    public StateChangeEvent(String processId, Events event) {
        this.processId = processId;
        this.event = event;
    }

    // Getters and setters...
}
```

### 3. 查詢服務 (QueryService)

查詢服務負責處理讀取邏輯，例如查詢當前狀態或歷史狀態變更記錄。這裡可以用 Redis 或其他資料庫來查詢。

```java
package com.example.cqrs.query;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QueryService {

    private Map<String, String> processStateCache = new ConcurrentHashMap<>();

    // 查詢當前狀態
    public String getCurrentState(String processId) {
        return processStateCache.getOrDefault(processId, "Unknown");
    }

    // 更新狀態
    public void updateState(String processId, String newState) {
        processStateCache.put(processId, newState);
    }
}
```

### 4. RabbitMQ 消費者 (EventConsumer)

事件消費者負責從 RabbitMQ 接收狀態變更事件，並根據事件更新查詢數據（如 Redis 或資料庫）。

```java
package com.example.cqrs.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.cqrs.command.StateChangeEvent;
import com.example.cqrs.query.QueryService;

@Service
public class EventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private QueryService queryService;

    @RabbitListener(queues = "stateQueue")
    public void consumeStateChange(StateChangeEvent event) {
        logger.info("Consumed event: {}", event.getEvent());
        queryService.updateState(event.getProcessId(), event.getEvent().name());
    }
}
```

### 5. RabbitMQ 配置 (RabbitMQConfig)

這是 RabbitMQ 的配置，用於創建 stateExchange 和 stateQueue，並且設置路由鍵。

```java
package com.example.cqrs.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange stateExchange() {
        return new DirectExchange("stateExchange");
    }

    @Bean
    public Queue stateQueue() {
        return new Queue("stateQueue", true);
    }

    @Bean
    public Binding binding(DirectExchange stateExchange, Queue stateQueue) {
        return BindingBuilder.bind(stateQueue).to(stateExchange).with("state.routingKey");
    }
}
```

### 6. 狀態和事件定義 (States, Events)

這部分和前面範例一樣，定義狀態和事件。

```java
package com.example.cqrs.config;

public enum States {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILURE
}

public enum Events {
    START,
    COMPLETE,
    FAIL
}
```

## 總結

這個 CQRS 的設計範例展示了如何將「命令」（狀態變更）和「查詢」（查詢當前狀態或歷史記錄）責任分離。系統通過 CommandService 處理狀態變更，並將狀態變更事件發布到 RabbitMQ。消費者通過事件來更新查詢層的數據，從而讓系統能夠在高並發場景下有效應對讀寫操作。

這個架構可以應用於分佈式系統中，尤其適合需要大規模讀寫分離的場景。