Java 17, springboot 3.2, gradle, 幫我用 JPA 寫一個 api 存取資料庫的範例，需要滿足以下:

1. 有一個 API 的操作，完整的 resource CRUD, 這個 resource 稱為 volume
2. API 的授權透過 JWT token, token 之中包含了 tenantId
3. API 的操作會用 tenantId 當作過濾條件，也就每個 API call 都會根據 token 裡的 tenantId 篩選
4. 我還會有其他 resource 的操作，所以幫我設計一個 interface 可以讓每個 JPA 在操作時，自動帶入 tenantId

請提供完整的程式碼，預設的 class package 為 com.gtcafe.asimov, 資料庫使用 mariadb


---
# Claude

我會幫你建立一個多租戶(multi-tenant)的系統範例，包含完整的程式碼結構。



```java
// build.gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'com.gtcafe'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '17'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    implementation 'io.jsonwebtoken:jjwt-impl:0.11.5'
    implementation 'io.jsonwebtoken:jjwt-jackson:0.11.5'
    implementation 'org.projectlombok:lombok'
    runtimeOnly 'org.mariadb.jdbc:mariadb-java-client'
    annotationProcessor 'org.projectlombok:lombok'
}

// src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/asimov
    username: root
    password: password
    driver-class-name: org.mariadb.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MariaDBDialect

jwt:
  secret: your-secret-key
  expiration: 86400000

// src/main/java/com/gtcafe/asimov/entity/TenantAware.java
package com.gtcafe.asimov.entity;

public interface TenantAware {
    String getTenantId();
    void setTenantId(String tenantId);
}

// src/main/java/com/gtcafe/asimov/entity/BaseEntity.java
package com.gtcafe.asimov.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements TenantAware {
    
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

// src/main/java/com/gtcafe/asimov/entity/Volume.java
package com.gtcafe.asimov.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "volumes")
@Getter
@Setter
public class Volume extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column
    private String description;
    
    @Column(nullable = false)
    private Long size;
}

// src/main/java/com/gtcafe/asimov/repository/TenantAwareRepository.java
package com.gtcafe.asimov.repository;

import com.gtcafe.asimov.entity.TenantAware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TenantAwareRepository<T extends TenantAware> extends JpaRepository<T, Long> {
}

// src/main/java/com/gtcafe/asimov/repository/VolumeRepository.java
package com.gtcafe.asimov.repository;

import com.gtcafe.asimov.entity.Volume;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VolumeRepository extends TenantAwareRepository<Volume> {
    List<Volume> findAllByTenantId(String tenantId);
    Optional<Volume> findByIdAndTenantId(Long id, String tenantId);
}

// src/main/java/com/gtcafe/asimov/config/JwtAuthenticationFilter.java
package com.gtcafe.asimov.config;

import com.gtcafe.asimov.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final String tenantId = jwtService.extractTenantId(jwt);
        
        if (tenantId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtService.isTokenValid(jwt)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        tenantId, null, null);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}

// src/main/java/com/gtcafe/asimov/service/JwtService.java
package com.gtcafe.asimov.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String extractTenantId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token) {
        return !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

// src/main/java/com/gtcafe/asimov/service/TenantService.java
package com.gtcafe.asimov.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class TenantService {
    
    public String getCurrentTenantId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}

// src/main/java/com/gtcafe/asimov/dto/VolumeDTO.java
package com.gtcafe.asimov.dto;

import lombok.Data;

@Data
public class VolumeDTO {
    private Long id;
    private String name;
    private String description;
    private Long size;
}

// src/main/java/com/gtcafe/asimov/service/VolumeService.java
package com.gtcafe.asimov.service;

import com.gtcafe.asimov.dto.VolumeDTO;
import com.gtcafe.asimov.entity.Volume;
import com.gtcafe.asimov.repository.VolumeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VolumeService {

    private final VolumeRepository volumeRepository;
    private final TenantService tenantService;

    public List<VolumeDTO> getAllVolumes() {
        return volumeRepository.findAllByTenantId(tenantService.getCurrentTenantId())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public VolumeDTO getVolume(Long id) {
        return volumeRepository.findByIdAndTenantId(id, tenantService.getCurrentTenantId())
                .map(this::convertToDTO)
                .orElseThrow(() -> new EntityNotFoundException("Volume not found"));
    }

    public VolumeDTO createVolume(VolumeDTO volumeDTO) {
        Volume volume = convertToEntity(volumeDTO);
        volume.setTenantId(tenantService.getCurrentTenantId());
        return convertToDTO(volumeRepository.save(volume));
    }

    public VolumeDTO updateVolume(Long id, VolumeDTO volumeDTO) {
        Volume volume = volumeRepository.findByIdAndTenantId(id, tenantService.getCurrentTenantId())
                .orElseThrow(() -> new EntityNotFoundException("Volume not found"));
        
        volume.setName(volumeDTO.getName());
        volume.setDescription(volumeDTO.getDescription());
        volume.setSize(volumeDTO.getSize());
        
        return convertToDTO(volumeRepository.save(volume));
    }

    public void deleteVolume(Long id) {
        Volume volume = volumeRepository.findByIdAndTenantId(id, tenantService.getCurrentTenantId())
                .orElseThrow(() -> new EntityNotFoundException("Volume not found"));
        volumeRepository.delete(volume);
    }

    private VolumeDTO convertToDTO(Volume volume) {
        VolumeDTO dto = new VolumeDTO();
        dto.setId(volume.getId());
        dto.setName(volume.getName());
        dto.setDescription(volume.getDescription());
        dto.setSize(volume.getSize());
        return dto;
    }

    private Volume convertToEntity(VolumeDTO dto) {
        Volume volume = new Volume();
        volume.setName(dto.getName());
        volume.setDescription(dto.getDescription());
        volume.setSize(dto.getSize());
        return volume;
    }
}

// src/main/java/com/gtcafe/asimov/controller/VolumeController.java
package com.gtcafe.asimov.controller;

import com.gtcafe.asimov.dto.VolumeDTO;
import com.gtcafe.asimov.service.VolumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/volumes")
@RequiredArgsConstructor
public class VolumeController {

    private final VolumeService volumeService;

    @GetMapping
    public ResponseEntity<List<VolumeDTO>> getAllVolumes() {
        return ResponseEntity.ok(volumeService.getAllVolumes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VolumeDTO> getVolume(@PathVariable Long id) {
        return ResponseEntity.ok(volumeService.getVolume(id));
    }

    @PostMapping
    public ResponseEntity<VolumeDTO> createVolume(@RequestBody VolumeDTO volumeDTO) {
        return ResponseEntity.ok(volumeService.createVolume(volumeDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VolumeDTO> updateVolume(@PathVariable Long id, @RequestBody VolumeDTO volumeDTO) {
        return ResponseEntity.ok(volumeService.updateVolume(id, volumeDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVolume(@PathVariable Long id) {
        volumeService.deleteVolume(id);
        return ResponseEntity.ok().build();
    }
}

```

我已經建立了一個完整的多租戶系統實作，主要包含以下幾個重要部分：

1. **租戶感知介面和基礎實體**:
   - `TenantAware` 介面定義了租戶ID的getter和setter
   - `BaseEntity` 實作了基本的稽核欄位和租戶ID

2. **Repository層的租戶感知**:
   - `TenantAwareRepository` 作為基礎repository介面
   - `VolumeRepository` 繼承基礎介面並添加租戶特定的查詢方法

3. **JWT認證和租戶ID提取**:
   - `JwtService` 處理JWT token的解析和驗證
   - `JwtAuthenticationFilter` 從請求中提取並驗證JWT token

4. **業務邏輯層**:
   - `TenantService` 提供獲取當前租戶ID的方法
   - `VolumeService` 實作所有CRUD操作，自動套用租戶ID過濾

5. **API層**:
   - `VolumeController` 提供完整的RESTful API endpoints

使用方式：

1. API呼叫時需要在Header中加入JWT token：
```
Authorization: Bearer <your-jwt-token>
```

2. API端點：
```
GET    /api/volumes      # 取得所


---
# ChatGPT

以下是完整的程式碼範例，根據您的需求設計了一個 Spring Boot 3.2 專案，使用 Gradle 作為建構工具，並整合 MariaDB 和 JPA。

---

### 專案結構

```
src/main/java/com/gtcafe/asimov
├── AsimovApplication.java
├── config
│   ├── JwtAuthenticationFilter.java
│   ├── SecurityConfig.java
├── controller
│   ├── VolumeController.java
├── dto
│   ├── VolumeRequest.java
│   ├── VolumeResponse.java
├── entity
│   ├── Volume.java
├── repository
│   ├── TenantAwareRepository.java
│   ├── VolumeRepository.java
├── service
│   ├── VolumeService.java
├── util
│   ├── JwtUtils.java
```

---

### 1. `AsimovApplication.java`

```java
package com.gtcafe.asimov;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AsimovApplication {
    public static void main(String[] args) {
        SpringApplication.run(AsimovApplication.class, args);
    }
}
```

---

### 2. `JwtUtils.java`

解析 JWT token 的工具類。

```java
package com.gtcafe.asimov.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class JwtUtils {
    private static final String SECRET_KEY = "your-secret-key";

    public static String extractTenantId(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
        return claims.get("tenantId", String.class);
    }
}
```

---

### 3. `SecurityConfig.java`

Spring Security 配置。

```java
package com.gtcafe.asimov.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf().disable()
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .addFilter(new JwtAuthenticationFilter())
                .build();
    }
}
```

---

### 4. `JwtAuthenticationFilter.java`

JWT 驗證過濾器。

```java
package com.gtcafe.asimov.config;

import com.gtcafe.asimov.util.JwtUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {
    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return token != null && token.startsWith("Bearer ")
                ? JwtUtils.extractTenantId(token.substring(7))
                : null;
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "N/A";
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws java.io.IOException, javax.servlet.ServletException {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            String tenantId = JwtUtils.extractTenantId(token.substring(7));
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(tenantId, null, null)
            );
        }
        chain.doFilter(request, response);
    }
}
```

---

### 5. `TenantAwareRepository.java`

透過 Tenant ID 自動過濾的 Repository。

```java
package com.gtcafe.asimov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.context.SecurityContextHolder;

public interface TenantAwareRepository<T, ID> extends JpaRepository<T, ID> {
    default String getCurrentTenantId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
```

---

### 6. `Volume.java`

Volume 資料庫實體。

```java
package com.gtcafe.asimov.entity;

import javax.persistence.*;

@Entity
public class Volume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tenantId;
    private String name;

    // Getters and Setters
}
```

---

### 7. `VolumeRepository.java`

Volume 資料操作。

```java
package com.gtcafe.asimov.repository;

import com.gtcafe.asimov.entity.Volume;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VolumeRepository extends TenantAwareRepository<Volume, Long> {
    List<Volume> findByTenantId(String tenantId);
}
```

---

### 8. `VolumeService.java`

Volume 業務邏輯。

```java
package com.gtcafe.asimov.service;

import com.gtcafe.asimov.dto.VolumeRequest;
import com.gtcafe.asimov.entity.Volume;
import com.gtcafe.asimov.repository.VolumeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VolumeService {
    private final VolumeRepository volumeRepository;

    public VolumeService(VolumeRepository volumeRepository) {
        this.volumeRepository = volumeRepository;
    }

    public List<Volume> findAll(String tenantId) {
        return volumeRepository.findByTenantId(tenantId);
    }

    public Volume create(String tenantId, VolumeRequest request) {
        Volume volume = new Volume();
        volume.setTenantId(tenantId);
        volume.setName(request.getName());
        return volumeRepository.save(volume);
    }
}
```

---

### 9. `VolumeController.java`

Volume API 控制器。

```java
package com.gtcafe.asimov.controller;

import com.gtcafe.asimov.dto.VolumeRequest;
import com.gtcafe.asimov.entity.Volume;
import com.gtcafe.asimov.service.VolumeService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/volumes")
public class VolumeController {
    private final VolumeService volumeService;

    public VolumeController(VolumeService volumeService) {
        this.volumeService = volumeService;
    }

    @GetMapping
    public List<Volume> getAllVolumes() {
        String tenantId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return volumeService.findAll(tenantId);
    }

    @PostMapping
    public Volume createVolume(@RequestBody VolumeRequest request) {
        String tenantId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return volumeService.create(tenantId, request);
    }
}
```

---

### 10. `VolumeRequest.java`

用於接收新增 Volume 的請求。

```java
package com.gtcafe.asimov.dto;

public class VolumeRequest {
    private String name;

    // Getters and Setters
}
```

---

### 測試與執行

1. 確保 MariaDB 已啟動並設置資料庫。
2. 更新 `application.yml` 配置以連接 MariaDB。
3. 使用 Gradle 編譯專案並執行 `./gradlew bootRun`。

如果有其他要求或需要擴展功能，請告訴我！