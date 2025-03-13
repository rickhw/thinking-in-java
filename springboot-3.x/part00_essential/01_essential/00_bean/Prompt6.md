### **Bean 概念與重點摘要**

#### **1. Bean 是什麼？**
- **定義：**  
  Bean 是 Spring 容器管理的對象，由框架負責其生命週期和依賴注入。
- **創建方式：**
  - 使用 `@Component` 或其衍生註解（如 `@Service`, `@Controller`）。
  - 使用 Java 配置類中的 `@Bean` 方法。

---

#### **2. Bean 的 Scope（範圍）**
- **常見範圍：**
  - **singleton**（預設）：整個應用中只創建一個實例。
  - **prototype**：每次請求都創建一個新實例。
  - 其他：`request`, `session`（主要用於 Web 應用）。

- **設置方法：**
  - 在 `@Bean` 或 `@Component` 上配合 `@Scope` 設置。
    ```java
    @Bean
    @Scope("prototype")
    public MyBean myBean() {
        return new MyBean();
    }
    ```
  - 透過 XML 或 `BeanDefinition` 配置。

---

#### **3. @Component 與 @Bean 的比較**
| 特性            | @Component                      | @Bean                              |
|-----------------|----------------------------------|------------------------------------|
| **用法**        | 自動掃描（自動註冊 Bean）。       | 手動在配置類中定義。               |
| **靈活性**      | 限於類本身的定義。                | 可以在方法中自定義邏輯創建 Bean。 |
| **適用場景**    | 預設情況下註冊簡單類別。           | 創建需要額外邏輯或第三方類別的 Bean。 |

---

#### **4. 如何檢查與操作 Spring 容器中的 Bean**
- **列出所有 Bean：**
  - 使用 `ApplicationContext#getBeanDefinitionNames` 獲取所有 Bean 的名稱。
  - 使用 `ConfigurableApplicationContext#getBeanFactory` 進一步檢查 `scope` 等資訊。
  
- **按範圍篩選：**
  - 使用 `BeanDefinition#getScope` 檢查是否為 `singleton`、`prototype` 等。
  - 篩選出需要的 Bean 清單。

- **獲取指定 Bean：**
  - 透過名稱：
    ```java
    Object bean = applicationContext.getBean("beanName");
    ```
  - 透過名稱和類型：
    ```java
    MyBean bean = applicationContext.getBean("beanName", MyBean.class);
    ```

---

#### **5. 常見問題與解決**
- **Bean 名稱衝突：**
  - 問題：多個 Bean 使用相同名稱，導致初始化失敗。
  - 解決：給予明確名稱或使用 `@Qualifier` 指定依賴。
    ```java
    @Bean("customName")
    public MyBean myBean() {
        return new MyBean();
    }
    ```

- **Scope 不匹配：**
  - 問題：`singleton` 注入 `prototype` 時，`prototype` 的多例性失效。
  - 解決：使用 `ObjectFactory` 或 `@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)`。

- **除錯工具：**
  - 啟用 Actuator 的 `/actuator/beans` 查看詳細資訊。
  - 使用 `ScopedBeanInspector` 和 `BeanRetriever` 自行列舉和檢查。

---

#### **6. 最佳實踐**
1. **簡單類型使用 `@Component`，複雜邏輯使用 `@Bean`：**  
   根據需求選擇適合的註解，確保可讀性與靈活性。
2. **清晰的命名規範：**  
   避免名稱衝突，為每個 Bean 設定描述性名稱。
3. **範圍選擇：**  
   - 預設使用 `singleton` 範圍。
   - 僅在需要多例時使用 `prototype`，並搭配代理處理。
4. **啟用 Actuator：**  
   利用 Spring 提供的監控工具輕鬆查看 Bean 狀態。

---

透過掌握以上概念與工具，能夠有效地管理 Spring 容器中的 Bean，解決範圍衝突與初始化問題，並進一步優化應用程式結構與運行效能。