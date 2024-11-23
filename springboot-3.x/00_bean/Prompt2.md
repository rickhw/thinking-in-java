以下是關於 `@Component` 和 `@Bean` 進一步的擴展知識，包括它們的進階用法、相關設計模式以及實際應用中的注意事項。

---

### 1. **Spring 中的 `@Component` 衍生註解**
`@Component` 是 Spring 核心的一部分，但實際開發中，可能會用到它的衍生註解，根據不同場景進行分類：

#### **1.1 @Service**
- 主要用於標記業務層邏輯。
- 表明該類主要負責業務操作。
```java
@Service
public class UserService {
    public String findUser(String userId) {
        return "User: " + userId;
    }
}
```

#### **1.2 @Repository**
- 用於標記數據訪問層（如 DAO）。
- 自動處理與數據庫交互的異常（例如將 `SQLException` 轉為 `DataAccessException`）。
```java
@Repository
public class UserRepository {
    public String findById(String id) {
        return "User from DB: " + id;
    }
}
```

#### **1.3 @Controller**
- 用於標記控制層，處理 HTTP 請求。
- 通常與 `@RequestMapping` 或其衍生註解（如 `@GetMapping`）一起使用。
```java
@RestController // 等價於 @Controller + @ResponseBody
public class UserController {
    @GetMapping("/users/{id}")
    public String getUser(@PathVariable String id) {
        return "User: " + id;
    }
}
```

---

### 2. **`@Bean` 的高級用法**

#### **2.1 設定初始化與銷毀方法**
透過 `@Bean`，可以指定 Bean 初始化和銷毀時執行的方法：
```java
@Bean(initMethod = "init", destroyMethod = "cleanup")
public CustomBean customBean() {
    return new CustomBean("Managed Bean");
}

// CustomBean.java
public class CustomBean {
    private final String message;
    public CustomBean(String message) { this.message = message; }
    public void init() { System.out.println("Bean initialized!"); }
    public void cleanup() { System.out.println("Bean destroyed!"); }
}
```

#### **2.2 使用條件註解**
可以結合 `@Conditional`，在特定條件下創建 Bean。例如：只在某個屬性存在時註冊：
```java
@Bean
@ConditionalOnProperty(name = "app.feature.enabled", havingValue = "true")
public FeatureService featureService() {
    return new FeatureService();
}
```

#### **2.3 配置多個 Bean**
一個配置類中可以定義多個 `@Bean`，並且可以互相依賴：
```java
@Bean
public CustomBean customBean() {
    return new CustomBean("Bean 1");
}

@Bean
public CustomBean dependentBean(CustomBean customBean) {
    return new CustomBean("Dependent on: " + customBean.getMessage());
}
```

---

### 3. **設計模式與 Spring Bean**

#### **3.1 單例模式 (Singleton)**
Spring 默認的 Bean 範疇是單例 (`@Scope("singleton")`)。
```java
@Component
@Scope("singleton")
public class SingletonBean {
    public SingletonBean() { System.out.println("SingletonBean Created"); }
}
```

#### **3.2 工廠模式 (Factory)**
`@Bean` 本質上是工廠方法的實現，使用者可以靈活控制如何創建 Bean。
```java
@Configuration
public class FactoryConfig {
    @Bean
    public MyService myService() {
        return new MyService("Created via Factory");
    }
}
```

#### **3.3 依賴注入 (Dependency Injection)**
Spring 的依賴注入支援多種方式：
1. **構造函數注入**（推薦）
2. **Setter 方法注入**
3. **Field 注入**（需要 `@Autowired`）

---

### 4. **Scope 的使用**

#### **4.1 Prototype**
每次請求都會創建一個新的實例：
```java
@Component
@Scope("prototype")
public class PrototypeBean {
    public PrototypeBean() { System.out.println("PrototypeBean Created"); }
}
```

#### **4.2 Request / Session**
適用於 Web 應用，Bean 在請求或會話的範疇內有效。
```java
@Component
@Scope("request")
public class RequestScopedBean {
    public RequestScopedBean() { System.out.println("RequestScopedBean Created"); }
}
```

---

### 5. **實際應用中的注意事項**

#### **5.1 Bean 循環依賴**
- 如果兩個 Bean 相互依賴，可能導致 `CircularDependencyException`。
- 解決方法：使用 `@Lazy` 或重構代碼避免相互依賴。
```java
@Component
public class A {
    private final B b;
    public A(@Lazy B b) { this.b = b; }
}

@Component
public class B {
    private final A a;
    public B(A a) { this.a = a; }
}
```

#### **5.2 @Bean 與 XML 配置共存**
即使現代應用幾乎完全使用 Java Config，但 XML 配置依然可以和 `@Bean` 共存：
```xml
<bean id="xmlBean" class="com.example.XmlBean"/>
```
可以從 Java 中引用：
```java
@ImportResource("classpath:application-context.xml")
@Configuration
public class XmlConfig {
}
```

#### **5.3 測試中的 Bean 註冊**
在測試環境中，通常會使用 `@MockBean` 或 `@TestConfiguration` 來創建 Mock 或 Test-specific Bean：
```java
@TestConfiguration
public class TestConfig {
    @Bean
    public MyService testService() {
        return new MyService("Test Service");
    }
}
```

---

### 6. 總結

1. **`@Component` 系列適合用於一般的類別註冊**，如業務邏輯、控制器、DAO。
2. **`@Bean` 提供更靈活的方式來定義和管理 Bean**，尤其是當需要自定義初始化邏輯或條件化配置時。
3. 了解 **Bean 的作用域與生命週期**，在不同應用場景（如單例、請求範疇）靈活選擇。
4. 注意避免 **循環依賴** 和 **過多的 Bean 配置**，保持代碼簡潔。

希望這些擴展知識能幫助你深入了解 Spring 的 Bean 管理！如果需要更多專題範例或進一步的討論，可以隨時提問！