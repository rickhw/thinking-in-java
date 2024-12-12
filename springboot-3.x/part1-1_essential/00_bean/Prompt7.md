### **Spring Bean 的預設名稱規則**

當使用 Spring 的註解（如 `@Component` 或 `@Bean`）定義 Bean 時，如果沒有顯式指定名稱，Spring 會根據以下規則生成預設名稱：

---

#### **1. 使用 `@Component` 的預設名稱**
- **規則：**  
  預設名稱為類名的首字母小寫形式。
  - 如果類名的第一個字母是大寫，Spring 會將其轉為小寫。
  - 如果類名本身是以多個大寫字母開頭（如 `HTMLParser`），則名稱不會完全小寫化，而是直接保留原始類名的形式。

**範例：**
```java
@Component
public class MyService {
}
```
預設名稱：`myService`

```java
@Component
public class HTMLParser {
}
```
預設名稱：`HTMLParser`

---

#### **2. 使用 `@Bean` 的預設名稱**
- **規則：**  
  預設名稱為定義該方法的方法名。

**範例：**
```java
@Configuration
public class AppConfig {

    @Bean
    public MyService myService() {
        return new MyService();
    }
}
```
預設名稱：`myService`（取自方法名）

---

#### **3. 自定義名稱**
無論是 `@Component` 還是 `@Bean`，都可以使用自定義名稱來覆蓋預設名稱：
- **`@Component` 示例：**
  ```java
  @Component("customServiceName")
  public class MyService {
  }
  ```
  Bean 名稱：`customServiceName`

- **`@Bean` 示例：**
  ```java
  @Configuration
  public class AppConfig {
      
      @Bean("customBeanName")
      public MyService myService() {
          return new MyService();
      }
  }
  ```
  Bean 名稱：`customBeanName`

---

### **為什麼關注 Bean 名稱？**
1. **避免名稱衝突：**  
   當多個 Bean 名稱相同時，Spring 容器會報錯。
2. **方便依賴注入：**  
   使用 `@Qualifier` 注入特定名稱的 Bean，需要明確知道名稱。
   ```java
   @Autowired
   @Qualifier("customServiceName")
   private MyService myService;
   ```
3. **除錯：**  
   Bean 名稱是定位問題的重要依據。

---

### **最佳實踐**
- **命名規範化：**  
  使用描述性的名稱，尤其在多模組或多環境項目中，避免模糊名稱。
- **顯式定義名稱：**  
  當存在可能混淆時，使用自定義名稱清楚標識每個 Bean 的用途和上下文。
- **配合 Actuator 使用：**  
  透過 `/actuator/beans` 列出所有 Bean 的名稱和類型，以檢查名稱是否符合預期。