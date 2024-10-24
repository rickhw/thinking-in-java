tags:
- RabbitMQ
- Order
- Message Queue
---

## Prompt

如果 想要透過 routing key 提供有序列的應用，請整理應用場景與範例

---

透過 Routing Key 來實現有序的訊息處理，是基於 RabbitMQ 中 Direct Exchange 或 Topic Exchange 的機制，將訊息根據 Routing Key 分發到不同的佇列中，從而確保特定類型的訊息按照特定的順序被處理。

在分散式系統中，訊息的有序處理非常關鍵，尤其是在以下應用場景中，Routing Key 可以幫助實現有序的訊息傳遞和處理。

---
## 1. Routing Key 應用場景

### 1.1 訂單處理系統

在訂單系統中，每個訂單可能包含多個步驟（如建立、付款、出貨），這些步驟必須嚴格依照順序處理。透過 Routing Key 可以將同一訂單的不同步驟訊息傳送到同一個佇列，確保訂單處理的順序性。

場景範例： 訂單首先需要建立（order.create），接著付款（order.pay），最後出貨（order.ship）。透過使用訂單 ID 作為 Routing Key，可以確保同一訂單的所有訊息被依序處理。

### 1.2 使用者活動事件處理

在使用者活動日誌系統中，我們可能需要確保每個使用者的操作（如登入、瀏覽、購買）被依照時間順序處理。這可以透過使用使用者 ID 作為 Routing Key 來保證同一使用者的訊息處理順序。

場景範例： 使用者的登入、瀏覽商品、購買等操作需依序記錄。使用使用者 ID 作為 Routing Key，可以確保每個使用者的操作按時間順序寫入資料庫或日誌系統。

### 1.3 庫存管理系統

在庫存系統中，不同的操作（如進貨、出貨）需要依照順序處理，避免庫存超賣或負庫存的情況。透過 Routing Key 可以確保某個商品的庫存作業按照順序處理。

場景範例： 當某個商品發生進貨或出貨時，透過商品 SKU 作為 Routing Key，將相關訊息傳送到同一個佇列，確保訊息依照正確的順序處理。

---

## 2. 透過 Routing Key 實現有序訊息處理的關鍵點

Routing Key 一致性： 使用相同的 Routing Key 保證特定類型的訊息會被傳送到同一個佇列，從而確保訊息處理的順序性。
訊息分區（Sharding by Key）： 對於大規模的訊息，可以根據 Routing Key 進行分區，將相同類型的訊息路由到不同的佇列（或消費者群組），從而保證每個分區內的訊息有序。
消費者實例： 確保特定佇列只有一個消費者在處理，或透過鎖定機制來確保順序執行。

---
## 3. Routing Key 實作有序訊息的範例

### 3.1 RabbitMQ Exchange 配置

我們使用 Direct Exchange，透過 Routing Key 來將訊息路由到特定的佇列，保證某個佇列內的訊息順序。

```java
package com.example.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQDirectConfig {

    // 定義 Direct Exchange
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange("directOrderExchange");
    }

    // 定義用於處理訂單的佇列
    @Bean
    public Queue orderQueue() {
        return new Queue("orderQueue", true); // 佇列持久化
    }

    // 綁定佇列到 Direct Exchange，並指定 routingKey
    @Bean
    public Binding bindingOrderQueue(Queue orderQueue, DirectExchange directExchange) {
        // Routing Key 可能是訂單號碼或特定的分類，例如 order.create
        return BindingBuilder.bind(orderQueue).to(directExchange).with("order.*");
    }
}
```

### 3.2 Producer 生產者

生產者根據不同的訂單狀態或事件類型產生訊息，並根據訂單 ID 傳送到不同的佇列（使用 Routing Key 來確保同一個訂單的訊息被路由到同一個佇列）。

```java
package com.example.producer;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderProducer {

    private final AmqpTemplate amqpTemplate;

    @Autowired
    public OrderProducer(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    // 發送訂單建立訊息，使用訂單ID作為 Routing Key 保證順序性
    public void sendOrderCreate(String orderId, String message) {
        String routingKey = "order." + orderId; // 使用訂單 ID 作為 routing key
        amqpTemplate.convertAndSend("directOrderExchange", routingKey, message);
        System.out.println("Order Created Message Sent: " + message);
    }

    // 傳送訂單支付訊息
    public void sendOrderPay(String orderId, String message) {
        String routingKey = "order." + orderId;
        amqpTemplate.convertAndSend("directOrderExchange", routingKey, message);
        System.out.println("Order Payment Message Sent: " + message);
    }

    // 傳送訂單出貨訊息
    public void sendOrderShip(String orderId, String message) {
        String routingKey = "order." + orderId;
        amqpTemplate.convertAndSend("directOrderExchange", routingKey, message);
        System.out.println("Order Shipped Message Sent: " + message);
    }
}
```


### 3.3 Consumer 消費者

消費者根據 Routing Key 來從佇列中接收訊息，並依照訊息的順序進行處理，確保訊息是有順序的。

```java
package com.example.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class OrderConsumer {

    // 監聽訂單處理佇列
    @RabbitListener(queues = "orderQueue")
    public void receiveOrderMessage(String message) {
        // 假設訊息體包含了訂單ID和操作類型
        System.out.println("Order Processed: " + message);
        // 在這裡可以根據訊息內容進行進一步處理，例如更新資料庫狀態
    }
}
```


### 3.4 測試調用

```java
package com.example;

import com.example.producer.OrderProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class OrderTestRunner implements CommandLineRunner {

    private final OrderProducer orderProducer;

    @Autowired
    public OrderTestRunner(OrderProducer orderProducer) {
        this.orderProducer = orderProducer;
    }

    @Override
    public void run(String... args) throws Exception {
        // 發送有序的訂單訊息
        String orderId = "12345";
        orderProducer.sendOrderCreate(orderId, "Order Created for " + orderId);
        orderProducer.sendOrderPay(orderId, "Order Paid for " + orderId);
        orderProducer.sendOrderShip(orderId, "Order Shipped for " + orderId);
    }
}
```

### 3.5 Gradle 依賴

```java
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-amqp'
    implementation 'org.springframework.boot:spring-boot-starter'
    implementation 'org.springframework.boot:spring-boot-starter-web'
}
```

---
## 4. 總結

Routing Key 是確保訊息在 RabbitMQ 中依序處理的重要機制。透過使用 Routing Key，可以將相關訊息（如同一訂單的操作）路由到同一個佇列，確保順序處理。

Direct Exchange 和 Topic Exchange 是實作基於 Routing Key 的有序訊息傳遞的常用選擇。 Direct Exchange 可以嚴格符合 Routing Key，Topic Exchange 則支援通配符匹配。

這種機制適合訂單系統、日誌系統、庫存管理等需要保證訊息依序處理的場景。

使用 Routing Key 和 RabbitMQ 的佇列機制，可以有效解決分散式系統中訊息的順序問題，並確保訊息的可靠處理。