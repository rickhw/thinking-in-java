
# prompt

我有個狀態機，有四個狀態：pending, running, completed, failure.
其中 completed and failure 為結束狀態, 而且無法返回.

這四個狀態的轉換表如下

from \ to  | pending | running | completed | failure
-----|-----|-----|-----|-----
pending | x |  v | x | v
running | x | x | v | v
completed | x | x | x | x
failure |  x | x | x | x

- x: 代表不能轉換
- v: 代表可以轉換
- 縱向代表原始狀態
- 橫向代表目標狀態

依照個狀態機，請幫我用 java 17, springboot 3.2, rabbitmq 寫一個範例框架，包涵 producer & consumer. 其中 consumer 要能夠處理狀態轉移. rabbitmq 幫我用多個 queue 規劃


---

# ChatGPT

為了滿足你的狀態機需求並結合 RabbitMQ，我們可以設計一個多個 RabbitMQ queues 的範例框架，其中 Producer 用來發送狀態轉換的消息，而 Consumer 負責處理這些狀態轉換。每個狀態可以有對應的 Queue 和 Exchange 來進行消息傳遞。讓我們來一步步構建這個系統。

狀態機邏輯
我們將四個狀態分別定義為：

- PENDING
- RUNNING
- COMPLETED
- FAILURE

Consumer 會依據當前狀態決定是否允許轉移到新狀態，並將結果發送至下一個狀態的 Queue。


## RabbitMQ 設置 (多個 queues)

我們將使用四個 queues，分別對應每個狀態。這樣，Producer 可以向不同的 queue 發送狀態消息，Consumer 則負責根據當前狀態處理轉換邏輯。

### 1. 狀態機邏輯

```java
public enum Status {
    PENDING, RUNNING, COMPLETED, FAILURE;

    public boolean canTransitionTo(Status targetStatus) {
        switch (this) {
            case PENDING:
                return targetStatus == RUNNING || targetStatus == FAILURE;
            case RUNNING:
                return targetStatus == COMPLETED || targetStatus == FAILURE;
            case COMPLETED:
            case FAILURE:
                return false; // These are terminal states
            default:
                throw new IllegalArgumentException("Invalid status transition");
        }
    }
}
```

### 2. RabbitMQ 多個 Queue 的配置

我們將設置四個 queues 對應狀態：pending, running, completed, failure，並配置好對應的 exchanges 和 routing keys。

```java
@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue pendingQueue() {
        return new Queue("pending.queue", true);
    }

    @Bean
    public Queue runningQueue() {
        return new Queue("running.queue", true);
    }

    @Bean
    public Queue completedQueue() {
        return new Queue("completed.queue", true);
    }

    @Bean
    public Queue failureQueue() {
        return new Queue("failure.queue", true);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange("status.exchange");
    }

    @Bean
    public Binding pendingBinding(Queue pendingQueue, DirectExchange exchange) {
        return BindingBuilder.bind(pendingQueue).to(exchange).with("pending");
    }

    @Bean
    public Binding runningBinding(Queue runningQueue, DirectExchange exchange) {
        return BindingBuilder.bind(runningQueue).to(exchange).with("running");
    }

    @Bean
    public Binding completedBinding(Queue completedQueue, DirectExchange exchange) {
        return BindingBuilder.bind(completedQueue).to(exchange).with("completed");
    }

    @Bean
    public Binding failureBinding(Queue failureQueue, DirectExchange exchange) {
        return BindingBuilder.bind(failureQueue).to(exchange).with("failure");
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
```

### 3. Producer 範例

Producer 負責根據狀態轉換向對應的 queue 發送消息。

```java
@Service
public class StatusProducer {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public StatusProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendStatusUpdate(Status currentStatus, Status newStatus) {
        if (currentStatus.canTransitionTo(newStatus)) {
            rabbitTemplate.convertAndSend("status.exchange", newStatus.name().toLowerCase(), newStatus);
            System.out.println("Sent status update: " + newStatus);
        } else {
            System.out.println("Invalid status transition from " + currentStatus + " to " + newStatus);
        }
    }
}
```

### 4. Consumer 範例

Consumer 負責監聽不同的狀態 queues，並處理狀態轉換邏輯。

```java
@Service
public class StatusConsumer {

    @RabbitListener(queues = "pending.queue")
    public void handlePendingStatus(Status newStatus) {
        processStatus(Status.PENDING, newStatus);
    }

    @RabbitListener(queues = "running.queue")
    public void handleRunningStatus(Status newStatus) {
        processStatus(Status.RUNNING, newStatus);
    }

    @RabbitListener(queues = "completed.queue")
    public void handleCompletedStatus(Status newStatus) {
        processStatus(Status.COMPLETED, newStatus);
    }

    @RabbitListener(queues = "failure.queue")
    public void handleFailureStatus(Status newStatus) {
        processStatus(Status.FAILURE, newStatus);
    }

    private void processStatus(Status currentStatus, Status newStatus) {
        if (currentStatus.canTransitionTo(newStatus)) {
            System.out.println("Transitioning from " + currentStatus + " to " + newStatus);
            // 處理狀態轉換的邏輯
        } else {
            System.out.println("Cannot transition from " + currentStatus + " to " + newStatus);
        }
    }
}
```

### 5. application.yaml

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

app:
  rabbitmq:
    queues:
      - pending.queue
      - running.queue
      - completed.queue
      - failure.queue
```

### 6. 測試案例

你可以使用 StatusProducer 來發送狀態轉換請求，測試 consumer 如何處理這些轉換。

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class StatusMachineTest {

    @Autowired
    private StatusProducer producer;

    @Test
    public void testStatusTransition() {
        producer.sendStatusUpdate(Status.PENDING, Status.RUNNING); // valid transition
        producer.sendStatusUpdate(Status.PENDING, Status.COMPLETED); // invalid transition
        producer.sendStatusUpdate(Status.RUNNING, Status.COMPLETED); // valid transition
    }
}
```

## 結論

這樣的設計框架使用多個 RabbitMQ queues 並處理狀態轉換。每個狀態都有相應的 consumer 和 queue，並且 Producer 根據狀態邏輯來決定是否發送到對應的 queue。


---
