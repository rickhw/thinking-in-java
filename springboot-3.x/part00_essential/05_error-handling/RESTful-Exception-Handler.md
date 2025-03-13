在開發 RESTful API 時，處理常見的例外狀況是提供良好用戶體驗和維持服務穩定性的關鍵。以下是常見的例外類別與其用途，並提供處理建議：

---

### **常見的 Exception 類別**
#### 1. **參數驗證相關**
- **`MethodArgumentNotValidException`**
  - **用途**: 當 `@Valid` 注解用於方法參數時，Spring 發現參數無效。
  - **處理**: 提供詳細的錯誤訊息，如欄位名稱與錯誤內容。

    ```java
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }
    ```

- **`ConstraintViolationException`**
  - **用途**: 當 `@Validated` 注解驗證類別層級的參數違規。
  - **處理**: 提供詳細的違規資訊。

    ```java
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }
    ```

#### 2. **資源管理相關**
- **`EntityNotFoundException`**
  - **用途**: 資源未找到，通常用於查詢操作。
  - **處理**: 回應 404 狀態碼與自定義訊息。

    ```java
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    ```

- **`NoSuchElementException`**
  - **用途**: 當使用 `Optional#get()` 而發現值為空。
  - **處理**: 回應 404 狀態碼。

    ```java
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNoSuchElement(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resource not found");
    }
    ```

#### 3. **資料庫相關**
- **`DataIntegrityViolationException`**
  - **用途**: 資料庫操作違反唯一性或其他約束。
  - **處理**: 回應 400 或 409 狀態碼。

    ```java
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Database constraint violation");
    }
    ```

#### 4. **權限與驗證相關**
- **`AccessDeniedException`**
  - **用途**: 當用戶嘗試存取無權限的資源。
  - **處理**: 回應 403 狀態碼。

    ```java
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
    }
    ```

- **`AuthenticationException`**
  - **用途**: 驗證失敗時拋出的基礎例外類別。
  - **處理**: 回應 401 狀態碼。

    ```java
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
    }
    ```

#### 5. **系統相關**
- **`HttpRequestMethodNotSupportedException`**
  - **用途**: 當用戶使用不支援的 HTTP 方法。
  - **處理**: 回應 405 狀態碼。

    ```java
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<String> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("HTTP method not supported");
    }
    ```

- **`HttpMediaTypeNotSupportedException`**
  - **用途**: 當用戶提交不支援的 Content-Type。
  - **處理**: 回應 415 狀態碼。

    ```java
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<String> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("Media type not supported");
    }
    ```

#### 6. **其他**
- **`IllegalArgumentException`**
  - **用途**: 當參數不合法時拋出的例外。
  - **處理**: 回應 400 狀態碼。

    ```java
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
    ```

- **`Exception`**
  - **用途**: 捕獲所有未處理的例外，作為預設的回應策略。
  - **處理**: 回應 500 狀態碼。

    ```java
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
    }
    ```

---

### **完整範例: 統一 GlobalExceptionHandler**
```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Database constraint violation");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<String> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("HTTP method not supported");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<String> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("Media type not supported");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
    }
}
```

---

### **建議與注意事項**
1. **錯誤訊息本地化**:
   - 使用 `MessageSource` 進行錯誤訊息的多語系支持。
2. **詳細日誌記錄**:
   - 在每個 ExceptionHandler 方法內記錄例外詳細資訊以方便除錯。
3. **安全性考量**:
   - 避免直接暴露內部例外細節，返回泛化的錯誤訊息。


   ----



   以下是上述範例中會用到的所有必要 import：

```java
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import org.springframework.validation.FieldError;
import java.util.HashMap;
import java.util.Map;
```

---

### **依功能分類的 Import**
1. **Spring Framework**
   - `@ControllerAdvice`、`@ExceptionHandler`、`ResponseEntity`：
     ```java
     import org.springframework.web.bind.annotation.ControllerAdvice;
     import org.springframework.web.bind.annotation.ExceptionHandler;
     import org.springframework.http.ResponseEntity;
     import org.springframework.http.HttpStatus;
     ```

2. **Validation & Binding**
   - 用於處理參數驗證相關的例外：
     ```java
     import org.springframework.web.bind.MethodArgumentNotValidException;
     import org.springframework.validation.FieldError;
     import javax.validation.ConstraintViolationException;
     ```

3. **Security**
   - 用於處理授權與驗證相關的例外：
     ```java
     import org.springframework.security.access.AccessDeniedException;
     import org.springframework.security.core.AuthenticationException;
     ```

4. **JPA**
   - 用於處理資源未找到的例外：
     ```java
     import javax.persistence.EntityNotFoundException;
     ```

5. **Database**
   - 用於處理資料庫約束的例外：
     ```java
     import org.springframework.dao.DataIntegrityViolationException;
     ```

6. **Web**
   - 用於處理 HTTP 方法與媒體類型相關的例外：
     ```java
     import org.springframework.web.HttpMediaTypeNotSupportedException;
     import org.springframework.web.HttpRequestMethodNotSupportedException;
     ```

7. **Java 基本類別**
   - 常用的集合與工具類別：
     ```java
     import java.util.HashMap;
     import java.util.Map;
     ```

---

### **完整的檔案結構**
```java
package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.validation.FieldError;
import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    // 各種 ExceptionHandler 方法 (如上例)
}
```

這樣可以確保所有類別在編譯時都正確導入，並與例外處理邏輯完全吻合。