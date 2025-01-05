


以下是一個使用 **Java 17** 和 **Spring Boot 3.2** 的檔案加解密程式範例，適用於最大 1GiB 的檔案處理。程式使用 **AES** 加密演算法，並使用流式處理來確保效能和資源管理。

### 1. 專案結構
```
encryption-service/
├── src/main/java/com/example/encryption
│   ├── EncryptionService.java
│   ├── EncryptionController.java
│   └── EncryptionApplication.java
├── src/main/resources/application.yml
├── build.gradle
└── settings.gradle
```

---

### 2. `build.gradle`

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.3'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '17'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework:spring-web'
}
```

---

### 3. `EncryptionApplication.java`

```java
package com.example.encryption;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EncryptionApplication {
    public static void main(String[] args) {
        SpringApplication.run(EncryptionApplication.class, args);
    }
}
```

---

### 4. `EncryptionService.java`

```java
package com.example.encryption;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.SecureRandom;

@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 256;

    public SecretKey generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(KEY_SIZE, new SecureRandom());
        return keyGen.generateKey();
    }

    public void encryptFile(File inputFile, File outputFile, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile);
             CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                cos.write(buffer, 0, bytesRead);
            }
        }
    }

    public void decryptFile(File inputFile, File outputFile, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile);
             CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                cos.write(buffer, 0, bytesRead);
            }
        }
    }

    public SecretKey loadKey(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    public byte[] saveKey(SecretKey secretKey) {
        return secretKey.getEncoded();
    }
}
```

---

### 5. `EncryptionController.java`

```java
package com.example.encryption;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

@RestController
@RequestMapping("/api/encryption")
public class EncryptionController {

    private final EncryptionService encryptionService;

    public EncryptionController(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    @PostMapping("/encrypt")
    public ResponseEntity<?> encryptFile(@RequestParam("file") MultipartFile file) throws Exception {
        File inputFile = new File(System.getProperty("java.io.tmpdir"), file.getOriginalFilename());
        File outputFile = new File(System.getProperty("java.io.tmpdir"), file.getOriginalFilename() + ".enc");

        file.transferTo(inputFile);

        SecretKey secretKey = encryptionService.generateKey();
        encryptionService.encryptFile(inputFile, outputFile, secretKey);

        byte[] keyBytes = encryptionService.saveKey(secretKey);
        String keyBase64 = Base64.getEncoder().encodeToString(keyBytes);

        return ResponseEntity.ok()
                .header("Encryption-Key", keyBase64)
                .body(Files.readAllBytes(outputFile.toPath()));
    }

    @PostMapping("/decrypt")
    public ResponseEntity<?> decryptFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("key") String base64Key
    ) throws Exception {
        File inputFile = new File(System.getProperty("java.io.tmpdir"), file.getOriginalFilename());
        File outputFile = new File(System.getProperty("java.io.tmpdir"), file.getOriginalFilename().replace(".enc", ".dec"));

        file.transferTo(inputFile);

        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        SecretKey secretKey = encryptionService.loadKey(keyBytes);
        encryptionService.decryptFile(inputFile, outputFile, secretKey);

        return ResponseEntity.ok(Files.readAllBytes(outputFile.toPath()));
    }
}
```

---

### 6. 測試方式
- 啟動應用程式：`./gradlew bootRun`
- 加密檔案：
  ```bash
  curl -F "file=@/path/to/input.txt" http://localhost:8080/api/encryption/encrypt -o encrypted_file.enc
  ```
  **回應 Header** 中會提供加密密鑰 `Encryption-Key`。

- 解密檔案：
  ```bash
  curl -F "file=@/path/to/encrypted_file.enc" \
       -F "key=<your-encryption-key>" \
       http://localhost:8080/api/encryption/decrypt -o decrypted_file.txt
  ```

---

### 注意事項
1. **檔案大小限制**：請確保伺服器設定允許處理最大 1GiB 的檔案（例如 Spring Boot 默認的 multipart 限制）。
2. **安全性**：在實際應用中，請使用安全的方式儲存和傳遞加密密鑰，例如透過 HTTPS 傳輸密鑰。
3. **效能**：程式使用緩衝區流式處理確保資源效率。



---

針對注意事項的 **Spring Boot 建議與配置**，以下提供相關實務配置，幫助你提升應用程式的效能、安全性與可靠性。

---

### **1. 檔案大小限制**
Spring Boot 的默認上傳大小限制通常為 1MB，你可以透過以下方式提高限制：

#### **配置方式：`application.yml`**
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 2GB       # 單個檔案最大大小
      max-request-size: 2GB    # 整個請求最大大小
```

#### **程式中的驗證**
除了配置檔案限制，還可以在控制器中加入大小驗證，避免不當的資源消耗：

```java
@PostMapping("/encrypt")
public ResponseEntity<?> encryptFile(@RequestParam("file") MultipartFile file) throws Exception {
    long maxFileSize = 1L * 1024 * 1024 * 1024; // 1 GiB
    if (file.getSize() > maxFileSize) {
        return ResponseEntity.badRequest().body("File size exceeds the limit of 1 GiB");
    }
    // 繼續處理檔案加密
}
```

---

### **2. 確保安全性**
加解密服務中最重要的是密鑰管理與傳輸的安全性。以下為相關建議：

#### **使用 HTTPS**
確保應用程式運行在 HTTPS 下，以避免密鑰在網路傳輸過程中被攔截。

#### **密鑰管理**
密鑰最好不要直接透過 HTTP 回應給使用者，而是考慮以下方式：
1. **儲存於安全存放區（如 HSM、Vault）**：
   - 可以使用工具如 HashiCorp Vault 或 AWS KMS 來安全管理密鑰。
   - 配置範例：
     ```yaml
     vault:
       uri: https://vault.example.com
       token: YOUR_VAULT_TOKEN
     ```

2. **密鑰回傳的改進方式**：
   - 透過加密密鑰（例如 RSA 公鑰加密後傳回）：
     ```java
     String encryptedKey = encryptKeyWithPublicKey(secretKey);
     return ResponseEntity.ok().header("Encrypted-Key", encryptedKey).body(Files.readAllBytes(outputFile.toPath()));
     ```

   - 加解密時使用單次使用的 **Token** 作為密鑰檢索方式，而非直接傳遞密鑰。

---

### **3. 效能調整**
針對大檔案上傳與加解密作業，請進行以下效能配置：

#### **提高執行緒數量**
使用 **TaskExecutor** 提高檔案處理的併發能力：
```java
@Configuration
public class AsyncConfig {
    @Bean
    public Executor fileProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("FileProcessor-");
        executor.initialize();
        return executor;
    }
}
```

#### **非同步處理**
使用 `@Async` 處理大檔案上傳與加解密作業：
```java
@Service
public class EncryptionService {

    @Async("fileProcessingExecutor")
    public CompletableFuture<Void> encryptFileAsync(File inputFile, File outputFile, SecretKey secretKey) throws Exception {
        encryptFile(inputFile, outputFile, secretKey);
        return CompletableFuture.completedFuture(null);
    }
}
```

---

### **4. 資源清理**
確保在處理過程中釋放資源，避免因為大檔案導致伺服器記憶體耗盡。可以透過以下方式管理資源：
- **嘗試用 `try-with-resources` 確保關閉流**。
- **檢查臨時檔案是否被刪除**：
  ```java
  @PreDestroy
  public void cleanupTempFiles() {
      File tempDir = new File(System.getProperty("java.io.tmpdir"));
      for (File file : tempDir.listFiles()) {
          if (file.getName().endsWith(".enc") || file.getName().endsWith(".dec")) {
              file.delete();
          }
      }
  }
  ```

---

### **5. 監控與限制**
可以透過以下方式防止應用程式遭受濫用攻擊：
1. **速率限制**：使用 `Spring WebFlux` 或整合外部工具如 **RateLimiter**。
   ```java
   @GetMapping("/status")
   @RateLimiter(name = "default")
   public ResponseEntity<?> checkStatus() {
       return ResponseEntity.ok("Service is running");
   }
   ```

2. **檔案大小與請求次數統計監控**：整合 **Spring Boot Actuator** 或 Grafana/Prometheus。

---

### **6. 日誌與錯誤處理**
確保敏感資訊不會記錄在日誌中，例如密鑰或檔案內容：
```java
try {
    encryptionService.encryptFile(inputFile, outputFile, secretKey);
} catch (Exception e) {
    logger.error("Encryption failed for file: {}", inputFile.getName(), e);
    throw e; // 適當傳遞錯誤
}
```

---

### 配置範例檔案
完整的 `application.yml`：
```yaml
server:
  port: 8080

spring:
  servlet:
    multipart:
      max-file-size: 2GB
      max-request-size: 2GB

management:
  endpoints:
    web:
      exposure:
        include: "*"
  metrics:
    export:
      prometheus:
        enabled: true
```

透過這些配置，你可以安全且高效地處理 1GiB 的檔案加解密需求！