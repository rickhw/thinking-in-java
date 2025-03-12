如果 RabbitMQ 的 Initizlier 和 Consumer 在同一個 application 裡，開發過程中，我會重新初始化 RabbitMQ (透過 docker-compose) ，那麼啟動 springboot application 的時候會遇到 conumser 因為已經註冊 listener, 而初始 Queue 的 RabbitMQConfig 還沒透過 RabbitAdmin 初始 RabbitMQ 的 Queue / Exchange 等，因此造成相互等待的狀況，

有沒有什麼方法可以讓 RabbitAdmin 初始建立 Queue 的部分先執行，然後 Consumer 的 RabbitListener 再去監聽？


幾個相關的 Class 如下：

```java
/// RabbitInitializer.java
@Configuration
@ConfigurationProperties(prefix = "app.rabbitmq")
public class RabbitInitializer {

    @Value("${app.rabbitmq.autoInit:true}")
    private boolean autoInit;

    @Value("${app.rabbitmq.reset:false}")
    private boolean reset;

    // @Value("${app.rabbitmq.prefixName:asimov}")
    // private String prefixName;
    

    private List<QueueConfig> queues;
    private Map<String, QueueConfig> queueMap = new HashMap<>();

    public List<QueueConfig> getQueues() { return queues; }
    public void setQueues(List<QueueConfig> queues) { this.queues = queues; }

    public QueueConfig getQueueConfig(String queueName) {
		return queueMap.get(queueName);
	}

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    @DependsOn("rabbitAdmin")
    public CommandLineRunner initRabbitMQ(RabbitAdmin rabbitAdmin) {
        return args -> {
            System.out.println("Initiate the Queue ... ");
            if (reset) {
                System.out.println("Reset flag is true, deleting existing queues...");
                for (QueueConfig config : queues) {
                    rabbitAdmin.deleteQueue(config.getName());
                    System.out.printf("  - Queue [%s] deleted.\n", config.getName());
                }
            }

            if (autoInit) {
                System.out.println("AutoInit is true, initializing queues...");
                for (QueueConfig config : queues) {
                    Queue queue = new Queue(config.getName(), true);
                    rabbitAdmin.declareQueue(queue);

                    System.out.printf("  - Set Queue: [%s], exchange: [%s].\n", config.getName(), config.getExchange());

                    if (QueueName.FANOUT_EXCHANGE.equals(config.getExchange())) {
                        FanoutExchange exchange = new FanoutExchange(config.getExchange());
                        rabbitAdmin.declareExchange(exchange);
                        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange));
                    } else {
                        DirectExchange exchange = new DirectExchange(config.getExchange());
                        rabbitAdmin.declareExchange(exchange);
                        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(config.getRoutingKey()));
                    }

                    queueMap.put(config.getName(), config);
                }
                System.out.println("RabbitMQ Queues, Exchanges, and Bindings initialized.");
            }
        };
    }
}

/// HelloConsumer.java
package com.gtcafe.asimov.platform.hello.consumer;

@Service
@Slf4j
public class HelloConsumer {

    @Autowired
    private JsonUtils jsonUtils;

    @Autowired
    private CacheRepository cacheRepos;

    @Autowired
    private HelloEventHandler eventHandler;

    @RabbitListener(queues = QueueName.HELLO_QUEUE)
    public void consumeHelloQueue(String eventString) {
        // log.info("Received message: [{}]", eventString);

        // convert json string to model
        HelloEvent event = jsonUtils.jsonStringToModel(eventString, HelloEvent.class);
        String cachedKey = String.format("%s:%s", KindConstants.PLATFORM_HELLO, event.getId());

        log.info("start the conusmer, cachedKey: [{}], state: [{}]", cachedKey, event.getState());

        event.setState(TaskState.RUNNING);

        // update state from pending to running
        String afterEventString = jsonUtils.modelToJsonString(event);
        cacheRepos.saveOrUpdateObject(cachedKey, afterEventString);

        // start processing (running)
        eventHandler.handleEvent(event);
        
    }
}

```

底下是 application.yaml

```yaml

app:
  rabbitmq:
    autoInit: true
    reset: true
    queues:
      - name: asimov.platform.task
        exchange: asimov.directExchange
        routingKey: platform-task

      - name: asimov.platform.hello
        exchange: asimov.directExchange
        routingKey: platform-hello

      - name: asimov.platform.tenant
        exchange: asimov.directExchange
        routingKey: platform-tenant
```



啟動時, 完整的 Log 如下：

```bash
❯ gradle :asimov-webapi:bootRun

> Task :asimov-webapi:bootRun
2025-01-02T09:52:29.181 INFO  [background-preinit] o.h.validator.internal.util.Version#<clinit>(21) HV000001: Hibernate Validator 8.0.1.Final
2025-01-02T09:52:29.204 INFO  [main] com.gtcafe.asimov.Main#logStarting(50) Starting Main using Java 17.0.13 with PID 86674 (/Users/rickhwang/Repos/rickhwang/github/asimov/asimov-webapi/build/classes/java/main started by rickhwang in /Users/rickhwang/Repos/rickhwang/github/asimov/asimov-webapi)
2025-01-02T09:52:29.205 INFO  [main] com.gtcafe.asimov.Main#logStartupProfileInfo(653) No active profile set, falling back to 1 default profile: "default"

... 略 ...

2025-01-02T09:52:32.056 WARN  [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-1] o.s.a.r.l.BlockingQueueConsumer#attemptPassiveDeclarations(753) Failed to declare queue: asimov.platform.hello
2025-01-02T09:52:32.057 WARN  [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-1] o.s.a.r.l.BlockingQueueConsumer#handleDeclarationException(687) Queue declaration failed; retries left=3
org.springframework.amqp.rabbit.listener.BlockingQueueConsumer$DeclarationException: Failed to declare queue(s):[asimov.platform.hello]
        at org.springframework.amqp.rabbit.listener.BlockingQueueConsumer.attemptPassiveDeclarations(BlockingQueueConsumer.java:760)
        at org.springframework.amqp.rabbit.listener.BlockingQueueConsumer.passiveDeclarations(BlockingQueueConsumer.java:637)
        at org.springframework.amqp.rabbit.listener.BlockingQueueConsumer.start(BlockingQueueConsumer.java:624)
        at org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer$AsyncMessageProcessingConsumer.initialize(SimpleMessageListenerContainer.java:1384)
        at org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer$AsyncMessageProcessingConsumer.run(SimpleMessageListenerContainer.java:1225)
        at java.base/java.lang.Thread.run(Thread.java:840)
Caused by: java.io.IOException: null
        at com.rabbitmq.client.impl.AMQChannel.wrap(AMQChannel.java:140)
        at com.rabbitmq.client.impl.AMQChannel.wrap(AMQChannel.java:136)
        at com.rabbitmq.client.impl.AMQChannel.exnWrappingRpc(AMQChannel.java:158)
        at com.rabbitmq.client.impl.ChannelN.queueDeclarePassive(ChannelN.java:1033)
        at com.rabbitmq.client.impl.ChannelN.queueDeclarePassive(ChannelN.java:47)
        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
        at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.base/java.lang.reflect.Method.invoke(Method.java:569)
        at org.springframework.amqp.rabbit.connection.CachingConnectionFactory$CachedChannelInvocationHandler.invoke(CachingConnectionFactory.java:1167)
        at jdk.proxy2/jdk.proxy2.$Proxy181.queueDeclarePassive(Unknown Source)
        at org.springframework.amqp.rabbit.listener.BlockingQueueConsumer.attemptPassiveDeclarations(BlockingQueueConsumer.java:738)
        ... 5 common frames omitted
Caused by: com.rabbitmq.client.ShutdownSignalException: channel error; protocol method: #method<channel.close>(reply-code=404, reply-text=NOT_FOUND - no queue 'asimov.platform.hello' in vhost '/', class-id=50, method-id=10)
        at com.rabbitmq.utility.ValueOrException.getValue(ValueOrException.java:66)
        at com.rabbitmq.utility.BlockingValueOrException.uninterruptibleGetValue(BlockingValueOrException.java:36)
        at com.rabbitmq.client.impl.AMQChannel$BlockingRpcContinuation.getReply(AMQChannel.java:552)
        at com.rabbitmq.client.impl.AMQChannel.privateRpc(AMQChannel.java:316)
        at com.rabbitmq.client.impl.AMQChannel.exnWrappingRpc(AMQChannel.java:152)
        ... 14 common frames omitted
Caused by: com.rabbitmq.client.ShutdownSignalException: channel error; protocol method: #method<channel.close>(reply-code=404, reply-text=NOT_FOUND - no queue 'asimov.platform.hello' in vhost '/', class-id=50, method-id=10)
        at com.rabbitmq.client.impl.ChannelN.asyncShutdown(ChannelN.java:528)
        at com.rabbitmq.client.impl.ChannelN.processAsync(ChannelN.java:349)
        at com.rabbitmq.client.impl.AMQChannel.handleCompleteInboundCommand(AMQChannel.java:193)
        at com.rabbitmq.client.impl.AMQChannel.handleFrame(AMQChannel.java:125)
        at com.rabbitmq.client.impl.AMQConnection.readFrame(AMQConnection.java:761)
        at com.rabbitmq.client.impl.AMQConnection.access$400(AMQConnection.java:48)
        at com.rabbitmq.client.impl.AMQConnection$MainLoop.run(AMQConnection.java:688)
        ... 1 common frames omitted

... 略 ...

Caused by: com.rabbitmq.client.ShutdownSignalException: channel error; protocol method: #method<channel.close>(reply-code=404, reply-text=NOT_FOUND - no queue 'asimov.platform.hello' in vhost '/', class-id=50, method-id=10)
        at com.rabbitmq.client.impl.ChannelN.asyncShutdown(ChannelN.java:528)
        at com.rabbitmq.client.impl.ChannelN.processAsync(ChannelN.java:349)
        at com.rabbitmq.client.impl.AMQChannel.handleCompleteInboundCommand(AMQChannel.java:193)
        at com.rabbitmq.client.impl.AMQChannel.handleFrame(AMQChannel.java:125)
        at com.rabbitmq.client.impl.AMQConnection.readFrame(AMQConnection.java:761)
        at com.rabbitmq.client.impl.AMQConnection.access$400(AMQConnection.java:48)
        at com.rabbitmq.client.impl.AMQConnection$MainLoop.run(AMQConnection.java:688)
        ... 1 common frames omitted

> Task :asimov-webapi:bootRun FAILED
[Incubating] Problems report is available at: file:///Users/rickhwang/Repos/rickhwang/github/asimov/build/reports/problems/problems-report.html
```


---
# ChatGPT

上述問題主要是由於 Spring Boot 的 `@RabbitListener` 嘗試在 RabbitMQ 初始化完成之前監聽尚未建立的 Queue，導致 Queue 的被動宣告（`Passive Declaration`）失敗。為了解決這個問題，可以考慮以下策略：

---

### 解決方案 1：分離 RabbitMQ 初始化與 Listener 啟動

利用 Spring 的 `SmartLifecycle` 接口控制 Bean 的啟動順序，確保 `RabbitAdmin` 的初始化邏輯先於 `@RabbitListener` 的啟動。

#### 實現步驟：
1. **自定義 RabbitListenerContainerFactory**  
   使用 `SimpleRabbitListenerContainerFactory` 並延遲其啟動。

2. **確保 RabbitMQ 配置優先執行**  
   使用 `SmartLifecycle` 並設置 `getPhase()` 方法的返回值為較小的數字，使其優先啟動。

#### 修改代碼：

##### RabbitInitializer.java
```java
@Configuration
@ConfigurationProperties(prefix = "app.rabbitmq")
public class RabbitInitializer implements SmartLifecycle {

    private boolean isRunning = false;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Override
    public void start() {
        System.out.println("RabbitMQ initialization started...");
        if (reset) {
            System.out.println("Resetting queues...");
            queues.forEach(config -> rabbitAdmin.deleteQueue(config.getName()));
        }
        if (autoInit) {
            System.out.println("Auto-initializing queues...");
            queues.forEach(config -> {
                Queue queue = new Queue(config.getName(), true);
                rabbitAdmin.declareQueue(queue);
                DirectExchange exchange = new DirectExchange(config.getExchange());
                rabbitAdmin.declareExchange(exchange);
                rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(config.getRoutingKey()));
            });
        }
        isRunning = true;
        System.out.println("RabbitMQ initialization completed.");
    }

    @Override
    public void stop() {
        isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public int getPhase() {
        return Integer.MIN_VALUE; // 確保優先啟動
    }
}
```

##### RabbitMQ Listener 配置
```java
@Configuration
public class RabbitListenerConfig {

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAutoStartup(false); // 延遲啟動
        return factory;
    }
}
```

##### HelloConsumer.java
```java
@Service
@Slf4j
public class HelloConsumer implements SmartLifecycle {

    private boolean isRunning = false;

    @Autowired
    private SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory;

    @Autowired
    private RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;

    @Override
    public void start() {
        System.out.println("Starting RabbitListener...");
        rabbitListenerEndpointRegistry.start();
        isRunning = true;
    }

    @Override
    public void stop() {
        rabbitListenerEndpointRegistry.stop();
        isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE; // 確保最後啟動
    }
}
```

---

### 解決方案 2：延遲 Consumer 的啟動

在 `@RabbitListener` 方法上使用 Spring 的條件延遲啟動，確保所有 Queue 已完成初始化。

#### 修改代碼：
在 `@RabbitListener` 中設定 `autoStartup` 為 `false`，並在 `CommandLineRunner` 的最後手動啟動 Listener。

```java
@Configuration
public class RabbitListenerConfig {

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        return factory;
    }

    @Bean
    public CommandLineRunner startListeners(RabbitListenerEndpointRegistry registry) {
        return args -> {
            System.out.println("Starting Rabbit Listeners...");
            registry.start();
        };
    }
}

@Service
@Slf4j
public class HelloConsumer {

    @RabbitListener(queues = QueueName.HELLO_QUEUE, autoStartup = "false")
    public void consumeHelloQueue(String eventString) {
        log.info("Processing message: [{}]", eventString);
    }
}
```

---

### 解決方案 3：分離 Producer 與 Consumer 應用

如果開發環境中經常重啟 RabbitMQ 並重新初始化，考慮將 Queue 的初始化邏輯移到單獨的應用，避免與 Consumer 相互干擾。這樣每次 RabbitMQ 重啟後，先執行 Queue 初始化應用，然後再啟動 Consumer 應用。

---

這些方法可以有效避免初始化與監聽之間的競態條件，根據你的需求選擇合適的解法即可！