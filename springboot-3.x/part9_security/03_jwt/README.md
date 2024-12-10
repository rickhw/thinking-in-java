以下是使用 **Java 17**, **Spring Boot 3.2**, **Gradle** 編寫的完整 REST API，實現您要求的三個功能。

### 使用 OpenSSL 產生密鑰對
1. **產生 RSA 私鑰：**
   ```bash
   openssl genpkey -algorithm RSA -out private_key.pem -pkeyopt rsa_keygen_bits:2048
   ```
2. **從私鑰產生公鑰：**
   ```bash
   openssl rsa -pubout -in private_key.pem -out public_key.pem
   ```

將 `private_key.pem` 和 `public_key.pem` 放在專案的 `src/main/resources` 資料夾中。

---

### Gradle `build.gradle` 設定
確保您已添加以下依賴項：
```groovy
plugins {
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.3'
    id 'java'
}

group = 'com.example'
version = '1.0.0'
sourceCompatibility = '17'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5' // for JSON processing
}
```

---

### 程式碼實作

#### 1. **配置類別**
設定金鑰加載與 JWT 工具類。

```java
package com.example.jwtapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Configuration
public class JwtConfig {

    @Bean
    public KeyPair keyPair() throws Exception {
        // 讀取 private_key.pem 和 public_key.pem
        String privateKeyPem = new String(Files.readAllBytes(Paths.get("src/main/resources/private_key.pem")))
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        String publicKeyPem = new String(Files.readAllBytes(Paths.get("src/main/resources/public_key.pem")))
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        // 解析 PEM 格式
        byte[] privateKeyBytes = java.util.Base64.getDecoder().decode(privateKeyPem);
        byte[] publicKeyBytes = java.util.Base64.getDecoder().decode(publicKeyPem);

        // 建立 KeyPair
        PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));

        return new KeyPair(publicKey, privateKey);
    }
}
```

---

#### 2. **控制器實作**
實現三個 API。

```java
package com.example.jwtapi.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class JwtController {

    @Autowired
    private KeyPair keyPair;

    // 1. 產生 JWT Token
    @PostMapping("/generate-token")
    public ResponseEntity<Map<String, String>> generateToken(@RequestParam String username) {
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60 * 60 * 1000)) // 1 小時有效
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .compact();

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", token);
        return ResponseEntity.ok(response);
    }

    // 2. 提供 Public Key
    @GetMapping("/public-key")
    public ResponseEntity<String> getPublicKey() {
        PublicKey publicKey = keyPair.getPublic();
        String publicKeyString = "-----BEGIN PUBLIC KEY-----\n" +
                java.util.Base64.getEncoder().encodeToString(publicKey.getEncoded()) +
                "\n-----END PUBLIC KEY-----";
        return ResponseEntity.ok(publicKeyString);
    }

    // 3. 需要透過 Token 操作的 API
    @GetMapping("/secure-data")
    public ResponseEntity<String> secureData(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Jwts.parserBuilder()
                    .setSigningKey(keyPair.getPublic())
                    .build()
                    .parseClaimsJws(token); // 驗證 JWT
            return ResponseEntity.ok("Secure data accessed.");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid or expired token.");
        }
    }
}
```

---

#### 3. **啟動類**
```java
package com.example.jwtapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JwtApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(JwtApiApplication.class, args);
    }
}
```

---

### 測試步驟

1. 啟動應用程式：
   ```bash
   ./gradlew bootRun
   ```

2. 測試產生 Token：
   ```bash
   curl -X POST "http://localhost:8080/api/generate-token?username=testuser"
   ```

3. 獲取 Public Key：
   ```bash
   curl -X GET "http://localhost:8080/api/public-key"
   ```

4. 測試需要 Token 的 API：
   ```bash
   curl -H "Authorization: Bearer <YOUR_TOKEN>" "http://localhost:8080/api/secure-data"
   ```

此專案簡潔易讀，並且展示了如何以 Spring Boot 配合 JWT 實現安全驗證。



---

## create access token

curl -X POST http://localhost:8080/api/generate-token?username=testuser

{"accessToken":"eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTczMzM5ODkxOCwiZXhwIjoxNzMzNDAyNTE4fQ.TssT7XMo5XpsBf57PeZ0n-oNWNXxEnD5WjS3rONRYgJVeopzcrKy2IjH7lIcW5BHcSUAHYh9DQqcKkjg9fx-mMBU3quzXNNGFB36-Pizn3WCvo56_4EzumrmBbfjOzfyqGrkXfsLkV1IIsuF2Vhvu2BrI9CgUGRQwAmuz1rSl2FvYxFxIZ-ZdUV1tEM1qpJpa5XlJQs7b5AOOZieDvCvz2TEIH4PyHYwMlilNZ3JnNcSZZxS7spVYShO_LCFAwiCfFPSjPqUr-lBFf_Y8SoLE0P_GBITngZ2xejnC7r0zOpwAfJKcLqH2laY1vRJ4JyxV2ZsCB7mKu5JtsBZAMNncg"}


## get public key

curl -X GET "http://localhost:8080/api/public-key"

-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyBztvhQEEKJzsovoTlkfG9vxuJhD13TY5c2XyGdi/oqQxADH+nLrBra4Q1/oZhbI+jtyK4Jh/4DWcCtHcSy2rxj9xKU1cITrpvvkyECezzQzsy5MZYmXBWAD4Z+VyV4zZ/tp+AWdVZ1jIzVRZk1M9cQv/su32qsbkVE8qtqrIEc359GQnkNNtBjisMyacR3BjE12f9rViwWhZrJX+HmeTWFIHRgeaMWYtSy/dYP+AIWFmm7UHvSDEwSTKK6C1bS668TMBxwyMuDfd2hZ8ljCQqk6n8WUOUUIfQw0w6bALINMcGCSdv6UVa54TJwWURoc5doCWApXGNcFbUeqpmBtBQIDAQAB
-----END PUBLIC KEY-----%



## pass

~$ curl -H "Authorization: Bearer eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTczMzQ2MTcwMiwiZXhwIjoxNzMzNDY1MzAyfQ.LPU_rjFsFfwqo4TeTtMIhC0T1iUAxix5SMHIren0Jx7pnzeauf-fztC4ybLeKjbvBp7xg9yfjgDj3Ddb1P4hz03Nx8-QZjuoP6ZGIoVLoFlwx0udh5mNUn8XlnwBYCvhwuDH1xFMzppEaW0rxZ7C1rnqZqjd0_FSR8cJ27YQ7Q3xU_wjpFbW1sp8bcz5ya3HWzoxNvmcvRidaPrgNSZiBUtAVXszRDi1GVG-_AgfMOYqMN-V9W6AZylEfal4ClKK7VjPn9VTL0UlnaLKsaaVij0D-wObcMnVhSv1pytgripd6AexvYPL2qkxHrbJQKo1dW3bPeCAEwy6ntTXPVUUog" "http://localhost:8080/api/validate"

Secure data accessed.


## error: JWT strings must contain exactly 2 period characters. Found: 0

~$ curl  -H "Authorization: Bearer " "http://localhost:8080/api/validate"

Invalid or expired token.


## length not enough: Unable to verify RSA signature using configured PublicKey. Signature length not correct: got 255 but was expecting 256

curl -H "Authorization: Bearer eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTczMzQ2MTcwMiwiZXhwIjoxNzMzNDY1MzAyfQ.LPU_rjFsFfwqo4TeTtMIhC0T1iUAxix5SMHIrn0Jx7pnzeauf-fztC4ybLeKjbvBp7xg9yfjgDj3Ddb1P4hz03Nx8-QZjuoP6ZGIoVLoFlwx0udh5mNUn8XlnwBYCvhwuDH1xFMzppEaW0rxZ7C1rnqZqjd0_FSR8cJ27YQ7Q3xU_wjpFbW1sp8bcz5ya3HWzoxNvmcvRidaPrgNSZiBUtAVXszRDi1GVG-_AgfMOYqMN-V9W6AZylEfal4ClKK7VjPn9VTL0UlnaLKsaaVij0D-wObcMnVhSv1pytgripd6AexvYPL2qkxHrbJQKo1dW3bPeCAEwy6ntTXPVUUog" "http://localhost:8080/api/validate"


## header: error JWT string has a digest/signature, but the header does not reference a valid signature algorithm.

~$ curl -H "Authorization: Bearer eyJhb3ciOiJSUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTczMzQ2MTcwMiwiZXhwIjoxNzMzNDY1MzAyfQ.LPU_rjFsFfwqo4TeTtMIhC0T1iUAxix5SMHIren0Jx7pnzeauf-fztC4ybLeKjbvBp7xg9yfjgDj3Ddb1P4hz03Nx8-QZjuoP6ZGIoVLoFlwx0udh5mNUn8XlnwBYCvhwuDH1xFMzppEaW0rxZ7C1rnqZqjd0_FSR8cJ27YQ7Q3xU_wjpFbW1sp8bcz5ya3HWzoxNvmcvRidaPrgNSZiBUtAVXszRDi1GVG-_AgfMOYqMN-V9W6AZylEfal4ClKK7VjPn9VTL0UlnaLKsaaVij0D-wObcMnVhSv1pytgripd6AexvYPL2qkxHrbJQKo1dW3bPeCAEwy6ntTXPVUUog" "http://localhost:8080/api/validate"


## payload: JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted.
curl -H "Authorization: Bearer eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTczMzQ2MTc3MiwiZXhwIjoxNzMzNDY1MzAyfQ.LPU_rjFsFfwqo4TeTtMIhC0T1iUAxix5SMHIren0Jx7pnzeauf-fztC4ybLeKjbvBp7xg9yfjgDj3Ddb1P4hz03Nx8-QZjuoP6ZGIoVLoFlwx0udh5mNUn8XlnwBYCvhwuDH1xFMzppEaW0rxZ7C1rnqZqjd0_FSR8cJ27YQ7Q3xU_wjpFbW1sp8bcz5ya3HWzoxNvmcvRidaPrgNSZiBUtAVXszRDi1GVG-_AgfMOYqMN-V9W6AZylEfal4ClKK7VjPn9VTL0UlnaLKsaaVij0D-wObcMnVhSv1pytgripd6AexvYPL2qkxHrbJQKo1dW3bPeCAEwy6ntTXPVUUog" "http://localhost:8080/api/validate"


## signture: JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted.

~$ curl -H "Authorization: Bearer eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTczMzQ2MTcwMiwiZXhwIjoxNzMzNDY1MzAyfQ.LPU_rjFsFfwqo4TeTtMIhC0T1iUAxix5SMHIren0Jx7pnzeauf-fztC4ybLeKjbvBp7xg9yfjgDj3Ddb1P4hz03Nx8-QZjuoP6ZGIoVLoFlwx0udh5mNUn8XlnwBYCvhwuDH1xFMzppEaW0rxZ7C1rnqZqjd0_FSR8cJ27YQ7Q3xU_wjpFbW1sp8bcz5ya3HWzoxNvmcvRidaPrgNSZiBUtAVXszRDi1GVG-_AgfMOYqMN-V9W6AZylEfal4ClKK7VjPn9VTL0UlnaLKsaaeij0D-wObcMnVhSv1pytgripd6AexvYPL2qkxHrbJQKo1dW3bPeCAEwy6ntTXPVUUog" "http://localhost:8080/api/validate"
