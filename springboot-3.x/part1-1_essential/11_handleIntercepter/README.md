
可以透過 Spring Boot 提供的 **`HandlerInterceptor`** 或 **`HandlerInterceptorAdapter`** 來實現這個需求。在拦截器中，您可以捕获 HTTP 请求的细节，包括方法、URI 路径等。此外，您可以利用自定义注解（如 `@ExecMode`）并通过反射解析它们。

以下是具体实现步骤：

---

### 1. 创建自定义注解 `@ExecMode`
用于声明 API 的执行模式。

```java
package com.example.demo.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExecMode {
    String value() default "sync"; // 默认模式为同步
}
```

---

### 2. 编写拦截器 `ApiInterceptor`

在拦截器中捕获 `HTTP Method`、`URI Path`，并解析控制器方法上的注解。

```java
package com.example.demo.interceptor;

import com.example.demo.annotation.ExecMode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 捕获 HTTP Method 和 URI Path
        String method = request.getMethod();
        String uri = request.getRequestURI();

        System.out.println("HTTP Method: " + method);
        System.out.println("URI Path: " + uri);

        // 检查是否为 HandlerMethod（即是否映射到控制器方法）
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;

            // 解析 ExecMode 注解
            ExecMode execMode = handlerMethod.getMethodAnnotation(ExecMode.class);
            if (execMode != null) {
                System.out.println("ExecMode: " + execMode.value());
            } else {
                System.out.println("ExecMode: not specified");
            }
        }

        return true; // 返回 true 继续执行请求处理链
    }
}
```

---

### 3. 注册拦截器到 `InterceptorRegistry`

创建配置类以注册 `ApiInterceptor`。

```java
package com.example.demo.config;

import com.example.demo.interceptor.ApiInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private ApiInterceptor apiInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiInterceptor)
                .addPathPatterns("/**"); // 拦截所有路径
    }
}
```

---

### 4. 创建测试控制器

在控制器中使用自定义注解测试。

```java
package com.example.demo.controller;

import com.example.demo.annotation.ExecMode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DemoController {

    @GetMapping("/sync-endpoint")
    public String syncEndpoint() {
        return "Sync endpoint";
    }

    @ExecMode("async")
    @GetMapping("/async-endpoint")
    public String asyncEndpoint() {
        return "Async endpoint";
    }
}
```

---

### 5. 测试结果

启动应用程序并调用以下端点：

#### 访问 `/api/sync-endpoint`
控制台输出：
```
HTTP Method: GET
URI Path: /api/sync-endpoint
ExecMode: not specified
```

#### 访问 `/api/async-endpoint`
控制台输出：
```
HTTP Method: GET
URI Path: /api/async-endpoint
ExecMode: async
```

---

通过以上步骤，您可以成功实现拦截 API 请求并获取 HTTP 方法、URI 路径，以及解析注解中的自定义信息（如 `ExecMode` 的值）。



----



如果需要在拦截器中获取控制器类上的注解信息，可以通过 `HandlerMethod` 对象中的 `getBeanType()` 方法获取到目标类的 `Class` 对象，然后通过反射解析类上的注解。

以下是具体实现：

---

### 1. 添加类级别注解

假设有一个自定义注解 `@ControllerInfo`，用来标注控制器类的信息。

#### 创建注解 `@ControllerInfo`
```java
package com.example.demo.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ControllerInfo {
    String description() default ""; // 描述信息
}
```

#### 在控制器类上使用注解
```java
package com.example.demo.controller;

import com.example.demo.annotation.ControllerInfo;
import com.example.demo.annotation.ExecMode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ControllerInfo(description = "Demo controller for API testing")
@RestController
@RequestMapping("/api")
public class DemoController {

    @GetMapping("/sync-endpoint")
    public String syncEndpoint() {
        return "Sync endpoint";
    }

    @ExecMode("async")
    @GetMapping("/async-endpoint")
    public String asyncEndpoint() {
        return "Async endpoint";
    }
}
```

---

### 2. 修改 `ApiInterceptor`

在 `preHandle` 方法中获取类上的注解。

```java
package com.example.demo.interceptor;

import com.example.demo.annotation.ControllerInfo;
import com.example.demo.annotation.ExecMode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 捕获 HTTP Method 和 URI Path
        String method = request.getMethod();
        String uri = request.getRequestURI();

        System.out.println("HTTP Method: " + method);
        System.out.println("URI Path: " + uri);

        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;

            // 解析控制器方法上的注解
            ExecMode execMode = handlerMethod.getMethodAnnotation(ExecMode.class);
            if (execMode != null) {
                System.out.println("ExecMode: " + execMode.value());
            } else {
                System.out.println("ExecMode: not specified");
            }

            // 获取控制器类的 Class 对象
            Class<?> controllerClass = handlerMethod.getBeanType();

            // 检查类上是否有 @ControllerInfo 注解
            ControllerInfo controllerInfo = controllerClass.getAnnotation(ControllerInfo.class);
            if (controllerInfo != null) {
                System.out.println("Controller Info: " + controllerInfo.description());
            } else {
                System.out.println("Controller Info: not specified");
            }
        }

        return true;
    }
}
```

---

### 3. 测试结果

启动应用程序并访问控制器的端点。

#### 访问 `/api/sync-endpoint`
控制台输出：
```
HTTP Method: GET
URI Path: /api/sync-endpoint
ExecMode: not specified
Controller Info: Demo controller for API testing
```

#### 访问 `/api/async-endpoint`
控制台输出：
```
HTTP Method: GET
URI Path: /api/async-endpoint
ExecMode: async
Controller Info: Demo controller for API testing
```

---

### 总结

在拦截器中，通过以下方式可以获取类级别注解的信息：

1. **判断是否是 `HandlerMethod` 类型**：确保请求被映射到一个控制器方法。
2. **获取控制器类的 `Class` 对象**：使用 `HandlerMethod.getBeanType()`。
3. **解析类上的注解**：通过 `Class.getAnnotation(Class)` 获取注解信息。

这种方式不仅可以获取方法级别的注解，还可以同时解析类级别的注解，满足多层次注解解析的需求。