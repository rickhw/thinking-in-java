從 Spring Boot 3.0 開始，由於底層升級到 Jakarta EE 9，Spring 開始推動使用 **`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`** 替代 `META-INF/spring.factories` 的自動配置機制。這是因為新的配置方式更加靈活且易於管理。

以下是如何將 Spring Boot 2.7 的 `spring.factories` 自動配置更新到 Spring Boot 3.2 的做法：

---

### **舊版 Spring Boot 2.7 的做法 (`spring.factories`)**
在 `src/main/resources/META-INF/spring.factories` 中定義：
```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=com.example.MyAutoConfiguration
```

並在 `MyAutoConfiguration` 中：
```java
@Configuration
public class MyAutoConfiguration {
    @Bean
    public MyService myService() {
        return new MyService();
    }
}
```

這樣，`MyService` 會自動被加載到 Spring 的上下文中。

---

### **新版 Spring Boot 3.2 的做法 (`AutoConfiguration.imports`)**
1. **新增 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`**
在 `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 中定義：
```
com.example.MyAutoConfiguration
```

2. **自動配置類需要加上 `@AutoConfiguration` 註解**
在 `MyAutoConfiguration` 中：
```java
package com.example;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class MyAutoConfiguration {
    @Bean
    public MyService myService() {
        return new MyService();
    }
}
```

---

### **對比新舊方式的優勢**
- **結構更清晰**：`AutoConfiguration.imports` 是針對自動配置而設，與其他功能的配置分離。
- **可讀性更強**：減少了 `spring.factories` 中的混雜配置。
- **性能提升**：新機制在加載時更高效，因為不需要解析整個 `spring.factories` 文件。

---

### **完整專案範例**
假設專案目錄結構如下：
```
src/
├── main/
│   ├── java/com/example/MyAutoConfiguration.java
│   └── resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

#### `MyAutoConfiguration.java`
```java
package com.example;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class MyAutoConfiguration {

    @Bean
    public MyService myService() {
        return new MyService();
    }
}

class MyService {
    public String sayHello() {
        return "Hello from MyService!";
    }
}
```

#### `AutoConfiguration.imports`
```
com.example.MyAutoConfiguration
```

---

### 測試自動配置
1. 創建一個測試應用程式：
```java
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(DemoApplication.class, args);

        // 測試是否自動加載 MyService
        MyService myService = context.getBean(MyService.class);
        System.out.println(myService.sayHello()); // Output: Hello from MyService!
    }
}
```

2. 啟動應用程式，確認輸出。

---

這樣，你的自動配置已從舊的 `spring.factories` 遷移到新的 `AutoConfiguration.imports`，完全符合 Spring Boot 3.2 的最佳實踐！