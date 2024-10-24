
# Prompt

那請提供 Spring Statemachine 的設計範例

---
# ChatGPT

這是一個基於 Spring Statemachine 的狀態機設計範例，實現了你所描述的四個狀態 (pending, running, completed, failure) 和對應的狀態轉換。

## 項目結構

- 狀態機配置 (StateMachineConfig): 定義狀態和狀態轉換規則。
- 事件處理 (StateMachineListener): 監聽狀態轉換並執行相應的操作。
- 生產者 (Producer): 發送消息觸發狀態變更。
- 消費者 (Consumer): 處理狀態轉移。

## 依賴

首先，在 pom.xml 中加入所需依賴：

```xml
<dependency>
    <groupId>org.springframework.statemachine</groupId>
    <artifactId>spring-statemachine-core</artifactId>
    <version>3.2.0</version>
</dependency>
```

## 1. 狀態機配置 (StateMachineConfig)

```java
package com.example.statemachine.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.state.State;

@Configuration
@EnableStateMachine
public class StateMachineConfig extends StateMachineConfigurerAdapter<States, Events> {

    @Override
    public void configure(StateMachineStateConfigurer<States, Events> states) throws Exception {
        states
            .withStates()
            .initial(States.PENDING)
            .state(States.RUNNING)
            .end(States.COMPLETED)
            .end(States.FAILURE);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<States, Events> transitions) throws Exception {
        transitions
            .withExternal().source(States.PENDING).target(States.RUNNING).event(Events.START)
            .and()
            .withExternal().source(States.RUNNING).target(States.COMPLETED).event(Events.COMPLETE)
            .and()
            .withExternal().source(States.PENDING).target(States.FAILURE).event(Events.FAIL)
            .and()
            .withExternal().source(States.RUNNING).target(States.FAILURE).event(Events.FAIL);
    }
}
```

## 2. 狀態與事件定義 (States, Events)

```java
package com.example.statemachine.config;

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

## 3. 狀態機監聽器 (StateMachineListener)

```java
package com.example.statemachine.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import com.example.statemachine.config.States;
import com.example.statemachine.config.Events;

@Configuration
public class StateMachineListener extends StateMachineListenerAdapter<States, Events> {

    private static final Logger logger = LoggerFactory.getLogger(StateMachineListener.class);

    @Override
    public void stateChanged(State<States, Events> from, State<States, Events> to) {
        logger.info("State changed from {} to {}", from == null ? "none" : from.getId(), to.getId());
    }

    @Bean
    public StateMachineListener<States, Events> listener() {
        return new StateMachineListener();
    }
}
```

## 4. 生產者 (Producer)

在生產者中，我們模擬了發送消息以觸發狀態轉換的操作。這裡會依據不同情境來發送對應事件。

```java
package com.example.statemachine.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;

import com.example.statemachine.config.Events;
import com.example.statemachine.config.States;

@Service
public class Producer {

    private static final Logger logger = LoggerFactory.getLogger(Producer.class);

    @Autowired
    private StateMachine<States, Events> stateMachine;

    public void startProcess() {
        logger.info("Starting process...");
        Message<Events> message = MessageBuilder.withPayload(Events.START).build();
        stateMachine.sendEvent(message);
    }

    public void completeProcess() {
        logger.info("Completing process...");
        Message<Events> message = MessageBuilder.withPayload(Events.COMPLETE).build();
        stateMachine.sendEvent(message);
    }

    public void failProcess() {
        logger.info("Failing process...");
        Message<Events> message = MessageBuilder.withPayload(Events.FAIL).build();
        stateMachine.sendEvent(message);
    }
}
```

## 5. 消費者 (Consumer)
消費者會根據 RabbitMQ 消息來觸發狀態轉移，這裡的消費者處理流程就是將收到的 RabbitMQ 消息轉換為狀態機事件。

```java
package com.example.statemachine.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.statemachine.StateMachine;

import com.example.statemachine.config.Events;
import com.example.statemachine.config.States;

@Service
public class Consumer {

    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    @Autowired
    private StateMachine<States, Events> stateMachine;

    @RabbitListener(queues = "stateQueue")
    public void consumeMessage(String message) {
        logger.info("Received message: {}", message);

        if (message.contains("start")) {
            triggerStateMachine(Events.START);
        } else if (message.contains("complete")) {
            triggerStateMachine(Events.COMPLETE);
        } else if (message.contains("fail")) {
            triggerStateMachine(Events.FAIL);
        }
    }

    private void triggerStateMachine(Events event) {
        Message<Events> msg = MessageBuilder.withPayload(event).build();
        stateMachine.sendEvent(msg);
    }
}
```

## 6. RabbitMQ 配置 (RabbitConfig)

最後是 RabbitMQ 的基本配置，讓消息能夠進出。

```java
package com.example.statemachine.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue stateQueue() {
        return new Queue("stateQueue", true);
    }
}
```

## 總結

這個範例展示了如何使用 Spring Statemachine 來管理和處理複雜的狀態轉換，並通過 RabbitMQ 來觸發狀態機事件。這種設計方式能夠更清晰地管理狀態機的行為，同時使用 RabbitMQ 進行消息傳遞，能夠擴展至分佈式架構。