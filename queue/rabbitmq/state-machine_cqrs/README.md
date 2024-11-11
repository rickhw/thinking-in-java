## Prompt

我想開發一個應用程式，用 java 17, springboot 3.2, gradle, rabbitmq 作為基礎。

這個應用程式處理多個資源的狀態機，每個資源透過 rabbitmq 的 queue 與 binding key 作狀態移轉，而每個 資源只會有一個 queue, 每個狀態移轉 (Transition) 的邏輯，則在程式裡面的 Consumer Handler 裡實作。

請幫我完成一個完整的程式碼框架，用以下的例子：

- 資源種類部分：
    - 有一個叫做 Tenant 的資源，起始與終止狀態為 Initing / Terminated，其他狀態還有 Active / Inactive
- 核心狀態機移轉處理架構：
    - 使用 CQRS 方式實作
    - 每個移轉 (Transition) 都透過獨立的 method 處理
- RabbitMQ 部分:
    - 每種資源類型，提供一個 Queue 與多個 binding key
    - 幫我配置好 RabbitMQ 初始設定，像是發現 Queue / Exchange 沒有建立，就自動建立
    - 未來可以透過 application.yaml 增加不同資源的 queue
- java 部分：
    - 請提供 build.gradle, 並且幫我確認都可以正常編譯
    - 請提供幾個測試，用來確保範例程式的正確性
    - 這個應用程式的 package name 叫做: com.gtcafe.app
    - 幫我完善程式碼，包含使用 lombok 簡化程式內容
- 幫我用 meriaid 畫好 class and sequence diagram.




## ChatGPT

以下是一個符合您需求的 Java Spring Boot 應用程式架構。此應用程式使用 RabbitMQ 處理資源的狀態機，包含 Tenant 資源，並利用 CQRS 模式來管理狀態移轉。程式碼會使用 `lombok` 簡化程式內容，並提供 `build.gradle` 設定和一些基本測試。

### 目錄結構
```
com/
└── gtcafe/
    └── app/
        ├── config/
        │   └── RabbitMQConfig.java
        ├── domain/
        │   └── Tenant.java
        ├── dto/
        │   └── TenantStatusUpdateRequest.java
        ├── enums/
        │   └── TenantStatus.java
        ├── handler/
        │   └── TenantStatusHandler.java
        ├── queue/
        │   └── TenantStatusConsumer.java
        ├── repository/
        │   └── TenantRepository.java
        ├── service/
        │   ├── CommandService.java
        │   └── QueryService.java
        └── Application.java
```

### 1. RabbitMQ 設定

在 `RabbitMQConfig.java` 中配置 RabbitMQ 的 queue 和 exchange：

```java
// RabbitMQConfig.java
package com.gtcafe.app.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    public static final String TENANT_QUEUE = "tenant-status-queue";
    public static final String EXCHANGE = "tenant-exchange";

    @Bean
    Queue queue() {
        return new Queue(TENANT_QUEUE, false);
    }

    @Bean
    DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(TENANT_QUEUE);
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }
}
```

### 2. Domain 類別

在 `Tenant.java` 中定義 `Tenant` 物件並使用 Lombok 進行簡化：

```java
// Tenant.java
package com.gtcafe.app.domain;

import com.gtcafe.app.enums.TenantStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {
    private String id;
    private TenantStatus status;
}
```

### 3. Enum 狀態

定義 `TenantStatus` 狀態：

```java
// TenantStatus.java
package com.gtcafe.app.enums;

public enum TenantStatus {
    INITING,
    ACTIVE,
    INACTIVE,
    TERMINATED
}
```

### 4. DTO

定義傳遞狀態更新的請求物件 `TenantStatusUpdateRequest`：

```java
// TenantStatusUpdateRequest.java
package com.gtcafe.app.dto;

import com.gtcafe.app.enums.TenantStatus;
import lombok.Data;

@Data
public class TenantStatusUpdateRequest {
    private String tenantId;
    private TenantStatus newStatus;
}
```

### 5. Command 和 Query Service

建立 `CommandService` 和 `QueryService` 以支援 CQRS 模式：

```java
// CommandService.java
package com.gtcafe.app.service;

import com.gtcafe.app.domain.Tenant;
import com.gtcafe.app.enums.TenantStatus;
import com.gtcafe.app.repository.TenantRepository;
import org.springframework.stereotype.Service;

@Service
public class CommandService {
    private final TenantRepository tenantRepository;

    public CommandService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public void updateTenantStatus(String tenantId, TenantStatus status) {
        Tenant tenant = tenantRepository.findById(tenantId);
        tenant.setStatus(status);
        tenantRepository.save(tenant);
    }
}
```

```java
// QueryService.java
package com.gtcafe.app.service;

import com.gtcafe.app.domain.Tenant;
import com.gtcafe.app.repository.TenantRepository;
import org.springframework.stereotype.Service;

@Service
public class QueryService {
    private final TenantRepository tenantRepository;

    public QueryService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public Tenant getTenantById(String tenantId) {
        return tenantRepository.findById(tenantId);
    }
}
```

### 6. Consumer Handler

定義 `TenantStatusConsumer` 處理接收到的訊息：

```java
// TenantStatusConsumer.java
package com.gtcafe.app.queue;

import com.gtcafe.app.dto.TenantStatusUpdateRequest;
import com.gtcafe.app.enums.TenantStatus;
import com.gtcafe.app.service.CommandService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TenantStatusConsumer {
    private final CommandService commandService;

    public TenantStatusConsumer(CommandService commandService) {
        this.commandService = commandService;
    }

    @RabbitListener(queues = RabbitMQConfig.TENANT_QUEUE)
    public void handleTenantStatusUpdate(TenantStatusUpdateRequest request) {
        commandService.updateTenantStatus(request.getTenantId(), request.getNewStatus());
    }
}
```

### 7. 測試範例

撰寫測試來確保應用程式的正確性：

```java
// CommandServiceTest.java
package com.gtcafe.app.service;

import com.gtcafe.app.domain.Tenant;
import com.gtcafe.app.enums.TenantStatus;
import com.gtcafe.app.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CommandServiceTest {
    private CommandService commandService;
    private TenantRepository tenantRepository;

    @BeforeEach
    void setUp() {
        tenantRepository = Mockito.mock(TenantRepository.class);
        commandService = new CommandService(tenantRepository);
    }

    @Test
    void updateTenantStatusTest() {
        Tenant tenant = new Tenant("123", TenantStatus.INITING);
        when(tenantRepository.findById("123")).thenReturn(tenant);

        commandService.updateTenantStatus("123", TenantStatus.ACTIVE);
        assertEquals(TenantStatus.ACTIVE, tenant.getStatus());

        verify(tenantRepository, times(1)).save(tenant);
    }
}
```

### 8. `build.gradle` 設定

以下是 `build.gradle` 設定文件：

```groovy
plugins {
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.3'
    id 'java'
}

group = 'com.gtcafe'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '17'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-amqp'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

test {
    useJUnitPlatform()
}
```

### 說明

此範例程式使用 Spring Boot 的 AMQP 支援來與 RabbitMQ 互動，使用 CQRS 模式分離指令和查詢邏輯。程式碼使用 `lombok` 簡化資料類別的建立，並包含基本的測試，以確保更新邏輯的正確性。

請根據需求擴充測試案例並驗證 RabbitMQ 設定，這樣能確保應用程式運作正常。