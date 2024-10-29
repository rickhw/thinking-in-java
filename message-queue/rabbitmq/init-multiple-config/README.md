
## Prompt

我有一個 java 17, springboot 3.2 的 application.yml 如下:

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: root
    password: password
app:
  rabbitmq:
    autoInit: true
    queues:
    - name: queue1
      exchange: directExchange
      routingKey: rk1
    - name: queue2
      exchange: directExchange
      routingKey: rk2
    - name: queue3
      exchange: fanoutExchange
      routingKey: ""
```

請提供完整的程式，滿足以下需求：

1. 這段範例，請用 com.gtcafe 當作 package，起動為 Main.java, 使用 gradle
2. 在啟動的時候 Main 的時候，請同時在最後顯示 對 RabbitMQ 的連線狀況，如果 RabbitMQ 連線失敗，就馬上停止應用程式，並且顯示訊息。
3. Queue 的配置從 application.yml 讀取.
2. 當 `app.rabbitmq.autoInit` 是 `true` 的時候，application 啟動時就主動去 RabbitMQ 建立 Queue, Exchange, 做好 Key Binding; 如果是 `false` 就建立連線就好





---
## ChatGPT


以下是完整的 Java Spring Boot 程式碼，滿足您提到的需求。該程式碼會從 `application.yml` 中讀取 RabbitMQ 的配置，並根據 `autoInit` 的設置，動態初始化 Queue、Exchange 和 Key Binding。

### 專案結構
```plaintext
src
└── main
    ├── java
    │   └── com
    │       └── gtcafe
    │           ├── Main.java
    │           ├── config
    │           │   └── RabbitMQConfig.java
    │           └── model
    │               └── QueueConfig.java
    └── resources
        └── application.yml
```

### 1. `application.yml`
確保此檔案位於 `src/main/resources` 下：

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: root
    password: password
app:
  rabbitmq:
    autoInit: true
    queues:
      - name: queue1
        exchange: directExchange
        routingKey: rk1
      - name: queue2
        exchange: directExchange
        routingKey: rk2
      - name: queue3
        exchange: fanoutExchange
        routingKey: ""
```

### 2. `QueueConfig.java`
`QueueConfig` 類別代表每個 Queue 的配置信息。

```java
package com.gtcafe.model;

public class QueueConfig {
    private String name;
    private String exchange;
    private String routingKey;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }

    public String getRoutingKey() { return routingKey; }
    public void setRoutingKey(String routingKey) { this.routingKey = routingKey; }
}
```

### 3. `RabbitMQConfig.java`
`RabbitMQConfig` 負責從配置文件中讀取 Queue 列表並初始化 `RabbitAdmin`。

```java
package com.gtcafe.config;

import com.gtcafe.model.QueueConfig;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app.rabbitmq")
public class RabbitMQConfig {

    @Value("${app.rabbitmq.autoInit}")
    private boolean autoInit;

    private List<QueueConfig> queues;

    public List<QueueConfig> getQueues() { return queues; }
    public void setQueues(List<QueueConfig> queues) { this.queues = queues; }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    @DependsOn("rabbitAdmin")
    public CommandLineRunner initRabbitMQ(RabbitAdmin rabbitAdmin) {
        return args -> {
            if (autoInit) {
                for (QueueConfig config : queues) {
                    Queue queue = new Queue(config.getName(), true);
                    rabbitAdmin.declareQueue(queue);

                    if (config.getExchange().equals("fanoutExchange")) {
                        FanoutExchange exchange = new FanoutExchange(config.getExchange());
                        rabbitAdmin.declareExchange(exchange);
                        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange));
                    } else {
                        DirectExchange exchange = new DirectExchange(config.getExchange());
                        rabbitAdmin.declareExchange(exchange);
                        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(config.getRoutingKey()));
                    }
                }
                System.out.println("RabbitMQ Queues, Exchanges, and Bindings initialized.");
            }
        };
    }
}
```

### 4. `Main.java`
主程式負責啟動應用程式，檢查 RabbitMQ 的連線狀態，並根據 `autoInit` 配置初始化 RabbitMQ。

```java
package com.gtcafe;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main implements CommandLineRunner {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            rabbitTemplate.execute(channel -> true); // 檢查連線
            System.out.println("RabbitMQ connection successful.");
        } catch (Exception e) {
            System.err.println("Failed to connect to RabbitMQ: " + e.getMessage());
            System.exit(1); // 停止應用程式
        }
    }
}
```

### 5. `build.gradle`
使用 Gradle 構建專案，確保 Spring Boot 與 AMQP 相依性。

```gradle
plugins {
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.0.15.RELEASE'
    id 'java'
}

group = 'com.gtcafe'
version = '1.0.0'
sourceCompatibility = '17'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-amqp'
    implementation 'org.springframework.boot:spring-boot-starter'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

### 說明
1. `Main.java` 檔案負責在應用程式啟動時檢查 RabbitMQ 的連線狀況。如果無法連線，會停止應用程式並顯示錯誤訊息。
2. `RabbitMQConfig.java` 根據 `application.yml` 中的設定自動初始化 Queue、Exchange 和 Key Binding，這些配置會在 `app.rabbitmq.autoInit` 為 `true` 時啟動。
3. `QueueConfig` 類別用於代表 `application.yml` 中的每個 Queue 配置。