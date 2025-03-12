


以下是使用 Java 17 和 Spring Boot 3.4 的一個完整範例，展示了 `@Component` 和 `@Bean` 的用法及其差異。 

---

### 1. 範例程式碼

#### **專案目錄結構**
```
src
└── main
    ├── java
    │   └── com.example.demo
    │       ├── DemoApplication.java
    │       ├── MyService.java
    │       ├── CustomConfig.java
    │       └── CustomBean.java
    └── resources
        └── application.yml
```

---

#### **1.1 DemoApplication.java**
```java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

---

#### **1.2 MyService.java**
`@Component` 用於將類別自動註冊為 Spring 容器中的一個 Bean。
```java
package com.example.demo;

import org.springframework.stereotype.Component;

@Component
public class MyService {
    public String getMessage() {
        return "Hello from MyService!";
    }
}
```

---

#### **1.3 CustomConfig.java**
`@Bean` 是用於方法層級的註解，適合需要更精確控制或建構自定義邏輯的 Bean。
```java
package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomConfig {

    @Bean
    public CustomBean customBean() {
        // 你可以在這裡添加自定義邏輯來初始化 Bean
        return new CustomBean("CustomBean Initialized!");
    }
}
```

---

#### **1.4 CustomBean.java**
一個普通的 Java 類別。
```java
package com.example.demo;

public class CustomBean {
    private final String message;

    public CustomBean(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
```

---

#### **1.5 application.yml**
Spring Boot 配置文件，可留空或用於定義環境變數。
```yaml
server:
  port: 8080
```

---

### 2. 測試程式

#### **2.1 使用 `@Component` 註冊的 Bean**
```java
package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ComponentRunner implements CommandLineRunner {

    private final MyService myService;

    public ComponentRunner(MyService myService) {
        this.myService = myService;
    }

    @Override
    public void run(String... args) {
        System.out.println(myService.getMessage());
    }
}
```

---

#### **2.2 使用 `@Bean` 註冊的 Bean**
```java
package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BeanRunner implements CommandLineRunner {

    private final CustomBean customBean;

    public BeanRunner(CustomBean customBean) {
        this.customBean = customBean;
    }

    @Override
    public void run(String... args) {
        System.out.println(customBean.getMessage());
    }
}
```

---

### 3. 執行結果

啟動應用程式後，會在控制台輸出：
```
Hello from MyService!
CustomBean Initialized!
```

---

### 4. 注意事項

1. **`@Component` vs `@Bean`**
   - **`@Component`**：適合用於簡單的類別，只需將其註冊為 Spring Bean 並由容器管理。
   - **`@Bean`**：適合需要更細緻的控制，例如初始化邏輯、依賴注入時的條件判斷。

2. **生命週期管理**
   - Spring 管理的 Bean 默認是單例 (`singleton`)，可以透過 `@Scope` 改變作用域。

3. **依賴注入**
   - Spring 使用構造函數或 `@Autowired` 進行依賴注入，建議優先使用構造函數注入。

4. **日誌與除錯**
   - 使用 `@PostConstruct` 來驗證 Bean 是否被正確初始化。

---

有了這些範例和注意事項，你應該能夠靈活使用 `@Component` 和 `@Bean`！需要進一步擴展嗎？
