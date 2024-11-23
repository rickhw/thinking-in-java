
BeanInspector 範例中可以列舉現在容器裡的 Bean：
1. 有沒有方法可以用 scope 個別列出，例如我想取得 所有 singleton 的 bean
2. 取得 beanName 之後，我要用這個名稱取得實際 Bean Object 的方法


---

## ChatGPT

### **1. 按 `@Scope` 顯示容器內的 Beans**

可以利用 Spring 的 `ConfigurableListableBeanFactory` 查詢 Bean 的範圍，進一步過濾出特定範圍的 Bean，例如 Singleton 或 Prototype：

#### 實作範例
```java
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ScopedBeanInspector {

    private final ApplicationContext context;

    public ScopedBeanInspector(ApplicationContext context) {
        this.context = context;
    }

    public void listBeansByScope(String scope) {
        ConfigurableApplicationContext configurableContext = (ConfigurableApplicationContext) context;
        String[] beanNames = configurableContext.getBeanFactory().getBeanDefinitionNames();

        System.out.println("Beans with scope: " + scope);
        for (String name : beanNames) {
            BeanDefinition beanDefinition = configurableContext.getBeanFactory().getBeanDefinition(name);
            if (scope.equals(beanDefinition.getScope()) || 
                (scope.equals("singleton") && beanDefinition.getScope().isEmpty())) {
                System.out.println("Bean name: " + name + ", Bean class: " + beanDefinition.getBeanClassName());
            }
        }
    }
}
```

#### **用法**
在應用啟動後，調用方法列出指定範圍的 Bean，例如所有 `singleton`：
```java
@Bean
public CommandLineRunner runner(ScopedBeanInspector inspector) {
    return args -> inspector.listBeansByScope("singleton");
}
```

---

### **2. 使用名稱獲取實際的 Bean**

一旦獲得 Bean 的名稱，可以透過 `ApplicationContext#getBean(String name)` 方法獲取實例：

#### 基本範例
```java
@Component
public class BeanRetriever {

    private final ApplicationContext context;

    public BeanRetriever(ApplicationContext context) {
        this.context = context;
    }

    public Object getBeanByName(String beanName) {
        if (context.containsBean(beanName)) {
            return context.getBean(beanName);
        } else {
            throw new IllegalArgumentException("Bean with name '" + beanName + "' not found in context.");
        }
    }
}
```

#### 獲取特定類型的 Bean
如果知道 Bean 的類型，可以指定類型進行獲取：
```java
public <T> T getBeanByNameAndType(String beanName, Class<T> type) {
    if (context.containsBean(beanName)) {
        return context.getBean(beanName, type);
    } else {
        throw new IllegalArgumentException("Bean with name '" + beanName + "' not found in context.");
    }
}
```

#### **用法**
```java
@Bean
public CommandLineRunner runner(BeanRetriever retriever) {
    return args -> {
        Object bean = retriever.getBeanByName("customService");
        System.out.println("Retrieved bean: " + bean);
    };
}
```

---

### **完整範例：列出 Singleton 並取得 Bean**
#### 設定
```java
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.stereotype.Component;

@Component
public class BeanUtility {

    private final ApplicationContext context;

    public BeanUtility(ApplicationContext context) {
        this.context = context;
    }

    // 列出所有 Singleton
    public void listSingletonBeans() {
        ConfigurableApplicationContext configurableContext = (ConfigurableApplicationContext) context;
        String[] beanNames = configurableContext.getBeanFactory().getBeanDefinitionNames();

        System.out.println("Singleton Beans:");
        for (String name : beanNames) {
            BeanDefinition beanDefinition = configurableContext.getBeanFactory().getBeanDefinition(name);
            if (beanDefinition.getScope().isEmpty() || "singleton".equals(beanDefinition.getScope())) {
                System.out.println("Bean name: " + name);
            }
        }
    }

    // 取得 Bean 實例
    public Object getBeanByName(String beanName) {
        if (context.containsBean(beanName)) {
            return context.getBean(beanName);
        } else {
            throw new IllegalArgumentException("Bean with name '" + beanName + "' not found in context.");
        }
    }
}
```

#### 使用
```java
@Bean
public CommandLineRunner runner(BeanUtility utility) {
    return args -> {
        // 列出 Singleton Beans
        utility.listSingletonBeans();

        // 獲取特定 Bean
        Object bean = utility.getBeanByName("customService");
        System.out.println("Retrieved bean: " + bean);
    };
}
```

---

### **除錯與最佳實踐**
1. **透過名稱檢查範圍：**
   - 使用 `ApplicationContext#getBeanDefinition` 獲取 Bean 的 `scope` 屬性，檢查是否正確。
2. **用 `@Lazy` 解決依賴問題：**
   - 如果 Bean 作用範圍是 `prototype`，可能導致不必要的初始化。使用 `@Lazy` 避免問題。
3. **在開發階段結合 Actuator：**
   - 啟用 Actuator 的 `/actuator/beans` 查看 Bean 和範圍，方便除錯。

有了這些工具，對容器內的所有 Beans 狀態一目了然，能迅速處理名稱衝突、範圍不一致等問題。