用 java 17, springboot 3.2, gradle, 幫我寫一個 REST API 的 payload validation, 滿足以下需求:

1. 有一個 resource 叫做 tenant, 主要有 name, email, description 三個 attributes.ß
2. 針對 tenant 提供 CRUD 的 RESTful API 操作, 包含 HTTP 200, 400, 401, 500 的處理, 不用實作 service
3. 當 input payload 有格式錯誤的時候，全部都丟到 HTTP 400 的 error handler
4. 當認證失敗的時候，則丟到 HTTP 401 error handler
5. 實作 GlobalExceptionHandler 以及 ErrorResponse

請提供完整的 java 程式給我。


---
## Claude

很好的要求！我會用 Java 17、Spring Boot 3.2 和 Gradle 來實現您的需求。我將建立一個完整的專案結構，並使用 Bean Validation 來處理 payload validation。



```java
// 第一步: 建立 Tenant DTO (Data Transfer Object)
package com.example.tenantmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TenantDTO(
    Long id,
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    String name,

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    String email,

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    String description
) {}

// 第二步: 定義錯誤響應物件
package com.example.tenantmanagement.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    List<String> messages
) {
    public static ErrorResponse of(int status, String error, List<String> messages) {
        return new ErrorResponse(LocalDateTime.now(), status, error, messages);
    }
}

// 第三步: 全域異常處理器
package com.example.tenantmanagement.exception;

import com.example.tenantmanagement.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(), 
            "Validation Error", 
            errors
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationExceptions(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations()
            .stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(), 
            "Constraint Violation", 
            errors
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.UNAUTHORIZED.value(), 
            "Unauthorized", 
            List.of(ex.getMessage())
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralExceptions(Exception ex) {
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(), 
            "Internal Server Error", 
            List.of(ex.getMessage())
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

// 自定義未授權異常
package com.example.tenantmanagement.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}

// 租戶控制器
package com.example.tenantmanagement.controller;

import com.example.tenantmanagement.dto.TenantDTO;
import com.example.tenantmanagement.exception.UnauthorizedException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private List<TenantDTO> tenants = new ArrayList<>();
    private Long idCounter = 1L;

    // 模擬簡單的授權檢查
    private void checkAuthorization() {
        // 這裡你可以加入實際的授權邏輯
        boolean isAuthorized = Math.random() > 0.2; // 80% 成功授權
        if (!isAuthorized) {
            throw new UnauthorizedException("Unauthorized access");
        }
    }

    @PostMapping
    public ResponseEntity<TenantDTO> createTenant(@Valid @RequestBody TenantDTO tenantDTO) {
        checkAuthorization();
        TenantDTO newTenant = new TenantDTO(
            idCounter++, 
            tenantDTO.name(), 
            tenantDTO.email(), 
            tenantDTO.description()
        );
        tenants.add(newTenant);
        return ResponseEntity.ok(newTenant);
    }

    @GetMapping
    public ResponseEntity<List<TenantDTO>> getAllTenants() {
        checkAuthorization();
        return ResponseEntity.ok(tenants);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenantDTO> getTenantById(@PathVariable Long id) {
        checkAuthorization();
        Optional<TenantDTO> tenant = tenants.stream()
            .filter(t -> t.id().equals(id))
            .findFirst();
        
        return tenant.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TenantDTO> updateTenant(
        @PathVariable Long id, 
        @Valid @RequestBody TenantDTO tenantDTO
    ) {
        checkAuthorization();
        for (int i = 0; i < tenants.size(); i++) {
            if (tenants.get(i).id().equals(id)) {
                TenantDTO updatedTenant = new TenantDTO(
                    id, 
                    tenantDTO.name(), 
                    tenantDTO.email(), 
                    tenantDTO.description()
                );
                tenants.set(i, updatedTenant);
                return ResponseEntity.ok(updatedTenant);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenant(@PathVariable Long id) {
        checkAuthorization();
        boolean removed = tenants.removeIf(tenant -> tenant.id().equals(id));
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}

```

此外，我會準備一個 `build.gradle` 檔案，確保具備所需的依賴：

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '17'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

test {
    useJUnitPlatform()
}

bootRun {
    mainClass = 'com.example.tenantmanagement.TenantManagementApplication'
}

```

最後，添加一個主應用程式入口：

```java
package com.example.tenantmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TenantManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(TenantManagementApplication.class, args);
    }
}

```

這個實現滿足您的所有要求：

1. 租戶（Tenant）資源包含 name, email, description 屬性
2. 提供完整的 CRUD RESTful API
3. 使用 `GlobalExceptionHandler` 處理 HTTP 400 錯誤
4. 模擬授權檢查，觸發 HTTP 401
5. 實作 `ErrorResponse` 和全域異常處理

專案架構：
```
src/main/java/com/example/tenantmanagement/
├── controller/
│   └── TenantController.java
├── dto/
│   ├── TenantDTO.java
│   └── ErrorResponse.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── UnauthorizedException.java
└── TenantManagementApplication.java
```

特點：
- 使用 Java 17 record
- Spring Boot 3.2
- Bean Validation
- 全域異常處理
- 模擬記憶體中的租戶管理

運行時，您可以透過如 Postman 或 cURL 測試 API。授權檢查是隨機的，模擬 80% 成功率。

需要我解釋任何部分或提供更多細節嗎？