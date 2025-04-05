這裡是一個完整的 Spring Boot 3.4 範例，示範 `@Transactional` 及 `@TransactionalEventListener` 的使用方式。這個範例包括以下部分：

1. **`@Transactional`**：確保資料庫操作是在同一個交易（Transaction）內執行，若發生錯誤則回滾。
2. **`@TransactionalEventListener`**：監聽交易提交成功後發生的事件，並執行對應的邏輯。

---

### **專案結構**
```
transaction-demo/
│── src/main/java/com/example/transactiondemo
│   ├── TransactionDemoApplication.java   # Spring Boot 啟動類
│   ├── config
│   │   ├── DatabaseConfig.java           # Spring Data JPA 設定 (可選)
│   ├── entity
│   │   ├── User.java                     # 使用者實體
│   ├── repository
│   │   ├── UserRepository.java           # Spring Data JPA Repository
│   ├── event
│   │   ├── UserCreatedEvent.java         # 自定義事件
│   ├── listener
│   │   ├── UserEventListener.java        # 事件監聽器
│   ├── service
│   │   ├── UserService.java              # 服務層
│   ├── controller
│   │   ├── UserController.java           # REST 控制器
│── build.gradle                           # Gradle 設定
│── application.yml                        # Spring Boot 設定檔
```

---

### **完整程式碼**
#### **1. Spring Boot 啟動類**
```java
package com.example.transactiondemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TransactionDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(TransactionDemoApplication.class, args);
    }
}
```

---

#### **2. `User` 實體類**
```java
package com.example.transactiondemo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    public User(String name) {
        this.name = name;
    }
}
```

---

#### **3. `UserRepository` (Spring Data JPA)**
```java
package com.example.transactiondemo.repository;

import com.example.transactiondemo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
```

---

#### **4. `UserCreatedEvent` (事件)**
```java
package com.example.transactiondemo.event;

import com.example.transactiondemo.entity.User;

public record UserCreatedEvent(User user) {
}
```

---

#### **5. `UserEventListener` (事件監聽器)**
```java
package com.example.transactiondemo.listener;

import com.example.transactiondemo.event.UserCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class UserEventListener {

    @TransactionalEventListener
    public void handleUserCreated(UserCreatedEvent event) {
        System.out.println("✅ Transaction committed! User created: " + event.user().getName());
    }

    @EventListener
    @Async
    public void handleUserCreatedAsync(UserCreatedEvent event) {
        System.out.println("📢 Async Event Triggered: " + event.user().getName());
    }
}
```

**說明：**
- `@TransactionalEventListener`：當交易成功提交 (`COMMIT`) 後才會執行監聽邏輯。
- `@EventListener` + `@Async`：用於異步監聽，不受交易影響，可能會即時執行。

---

#### **6. `UserService` (服務層)**
```java
package com.example.transactiondemo.service;

import com.example.transactiondemo.entity.User;
import com.example.transactiondemo.event.UserCreatedEvent;
import com.example.transactiondemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void createUser(String name) {
        User user = new User(name);
        userRepository.save(user);

        // 只有當 Transaction 提交成功後，事件才會觸發
        eventPublisher.publishEvent(new UserCreatedEvent(user));

        System.out.println("📌 User saved in DB: " + name);
    }

    @Transactional
    public void createUserWithRollback(String name) {
        User user = new User(name);
        userRepository.save(user);
        eventPublisher.publishEvent(new UserCreatedEvent(user));

        // 人為製造異常，測試 Transaction Rollback
        throw new RuntimeException("❌ Simulating an error to rollback transaction");
    }
}
```

---

#### **7. `UserController` (REST API)**
```java
package com.example.transactiondemo.controller;

import com.example.transactiondemo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/create")
    public String createUser(@RequestParam String name) {
        userService.createUser(name);
        return "User created: " + name;
    }

    @PostMapping("/create-fail")
    public String createUserWithRollback(@RequestParam String name) {
        try {
            userService.createUserWithRollback(name);
        } catch (Exception e) {
            return "User creation failed: " + e.getMessage();
        }
        return "User created: " + name;
    }
}
```

---

### **8. `application.yml` (資料庫設定)**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/transaction_demo?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
    show-sql: true
  application:
    name: transaction-demo
```

---

### **測試 API**
啟動 Spring Boot 應用程式後，透過 Postman 或 CURL 測試：

✅ **成功的交易**
```sh
curl -X POST "http://localhost:8080/users/create?name=John"
```
📌 預期輸出：
```sh
📌 User saved in DB: John
✅ Transaction committed! User created: John
📢 Async Event Triggered: John
```

❌ **交易回滾**
```sh
curl -X POST "http://localhost:8080/users/create-fail?name=Jane"
```
📌 預期輸出：
```sh
📌 User saved in DB: Jane
❌ Simulating an error to rollback transaction
```
(資料庫 **不會** 存在 `Jane`，因為交易回滾)

---

### **結論**
1. `@Transactional` 確保方法在同一個交易中執行，發生錯誤則回滾。
2. `@TransactionalEventListener` 只有當交易成功後才會觸發，確保事件是可靠的。
3. `@EventListener` 可搭配 `@Async` 來非同步執行，適用於即時通知等場景。

這樣的設計確保了 **數據一致性**，並且支援事件驅動架構，適用於需要事件通知的應用場景。🚀