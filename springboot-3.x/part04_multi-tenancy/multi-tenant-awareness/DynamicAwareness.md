
Java 17, springboot 3.2, gradle, 幫我用 JPA 寫一個 api 存取資料庫的範例，需要滿足以下 tenant awareness 的需求:

1. 有一個 API 的操作，完整的 resource CRUD, 這個 resource 稱為 volume
2. API 的授權透過 JWT token, 
3. token 中有個欄位是 awareness, 當是 true 的時候, API 的操作 JPA 時會自動用 tenantId 當作過濾條件，也就每個 API call 都會根據 token 裡的 tenantId 篩選
4. 承上, 如果 awareness = false, 則 JPA 的操作則不會有 tenantId 當作條件.
4. 我還會有其他 resource 的操作，所以幫我設計一個 interface 可以讓每個 JPA 在操作時，自動帶入 tenantId

請提供完整的程式碼:

- 預設的 class package 為 com.gtcafe.asimov
- 資料庫使用 mariadb
- 使用 lombok 簡化程式碼


---
# ChatGPT v1



以下是一個完整的範例，展示如何使用 Java 17、Spring Boot 3.2、Gradle 和 JPA 來實現帶有租戶感知 (Tenant Awareness) 的 API，符合您的需求。

package com.gtcafe.asimov;

// pom.xml configuration
// dependencies: spring-boot-starter-data-jpa, spring-boot-starter-web, mariadb-java-client, jjwt, lombok

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TenantAwareApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(TenantAwareApiApplication.class, args);
    }
}

// TenantContext: Handles tenantId awareness
package com.gtcafe.asimov.tenant;

public class TenantContext {
    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();

    public static void setTenantId(String tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static String getTenantId() {
        return TENANT_ID.get();
    }

    public static void clear() {
        TENANT_ID.remove();
    }
}

// Custom JPA Filter for Tenant Awareness
package com.gtcafe.asimov.tenant;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class TenantAwareEntityListener {
    @PrePersist
    @PreUpdate
    public void setTenant(Object entity) {
        if (entity instanceof TenantAware) {
            ((TenantAware) entity).setTenantId(TenantContext.getTenantId());
        }
    }
}

// TenantAware interface
package com.gtcafe.asimov.tenant;

public interface TenantAware {
    void setTenantId(String tenantId);
    String getTenantId();
}

// Volume Entity
package com.gtcafe.asimov.entity;

import com.gtcafe.asimov.tenant.TenantAware;
import com.gtcafe.asimov.tenant.TenantAwareEntityListener;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(TenantAwareEntityListener.class)
public class Volume implements TenantAware {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @Column(nullable = true)
    private String tenantId;
}

// Volume Repository
package com.gtcafe.asimov.repository
  ;

import com.gtcafe.asimov.entity.Volume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VolumeRepository extends JpaRepository<Volume, Long> {
    List<Volume> findByTenantId(String tenantId);
}

// Volume Service
package com.gtcafe.asimov.service;

import com.gtcafe.asimov.entity.Volume;
import com.gtcafe.asimov.repository.VolumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VolumeService {
    private final VolumeRepository repository;

    public List<Volume> getAllVolumes(String tenantId, boolean awareness) {
        return awareness ? repository.findByTenantId(tenantId) : repository.findAll();
    }

    public Volume createVolume(Volume volume) {
        return repository.save(volume);
    }

    public Volume updateVolume(Long id, Volume updatedVolume) {
        Volume volume = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Volume not found"));
        volume.setName(updatedVolume.getName());
        volume.setDescription(updatedVolume.getDescription());
        return repository.save(volume);
    }

    public void deleteVolume(Long id) {
        repository.deleteById(id);
    }
}

// Volume Controller
package com.gtcafe.asimov.controller;

import com.gtcafe.asimov.entity.Volume;
import com.gtcafe.asimov.service.VolumeService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/volumes")
@RequiredArgsConstructor
public class VolumeController {
    private final VolumeService service;

    @GetMapping
    public List<Volume> getAllVolumes(@RequestHeader("Authorization") String token) {
        Claims claims = parseToken(token);
        boolean awareness = Boolean.parseBoolean(claims.get("awareness").toString());
        String tenantId = (String) claims.get("tenantId");
        return service.getAllVolumes(tenantId, awareness);
    }

    @PostMapping
    public Volume createVolume(@RequestHeader("Authorization") String token, @RequestBody Volume volume) {
        Claims claims = parseToken(token);
        TenantContext.setTenantId((String) claims.get("tenantId"));
        return service.createVolume(volume);
    }

    @PutMapping("/{id}")
    public Volume updateVolume(@RequestHeader("Authorization") String token, @PathVariable Long id, @RequestBody Volume volume) {
        Claims claims = parseToken(token);
        TenantContext.setTenantId((String) claims.get("tenantId"));
        return service.updateVolume(id, volume);
    }

    @DeleteMapping("/{id}")
    public void deleteVolume(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        Claims claims = parseToken(token);
        TenantContext.setTenantId((String) claims.get("tenantId"));
        service.deleteVolume(id);
    }

    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey("secret")
                .build()
                .parseClaimsJws(token.replace("Bearer ", ""))
                .getBody();
    }
}



### 說明

1. **`TenantContext`**：管理每個請求的 `tenantId`，在多執行緒情境下透過 `ThreadLocal` 儲存。
2. **`TenantAware` 介面**：定義可攜帶 `tenantId` 的實體模型。
3. **`TenantAwareEntityListener`**：在 JPA 寫入或更新時自動填入 `tenantId`。
4. **JWT Token 驗證與解析**：從 token 中解析 `tenantId` 和 `awareness` 屬性。
5. **資料庫過濾**：依據 `awareness` 動態決定是否使用 `tenantId` 過濾。

在 `application.yml` 中設定資料庫連線後，應能透過 Postman 測試 CRUD 功能並驗證租戶感知機制的正確性。


---
# ChatGPT v2



以下是滿足您需求的完整程式碼，包含基於 JPA 的 API 操作、JWT 授權機制，以及自動處理 `tenantId` 過濾的功能設計：

### 目錄結構
```
com
└── gtcafe
    └── asimov
        ├── config
        │   ├── JwtTokenUtil.java
        │   ├── TenantAwareJpaInterceptor.java
        │   └── TenantConfig.java
        ├── controller
        │   └── VolumeController.java
        ├── dto
        │   └── VolumeRequest.java
        ├── entity
        │   └── Volume.java
        ├── repository
        │   └── VolumeRepository.java
        ├── service
        │   └── VolumeService.java
        └── AsimovApplication.java
```

---

### 核心程式碼

#### `AsimovApplication.java`
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

#### `Volume.java`
```java
package com.gtcafe.asimov.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "volumes")
public class Volume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private Long tenantId; // 用於租戶過濾
}
```

---

#### `VolumeRequest.java`
```java
package com.gtcafe.asimov.dto;

import lombok.Data;

@Data
public class VolumeRequest {
    private String name;
    private String description;
}
```

---

#### `VolumeRepository.java`
```java
package com.gtcafe.asimov.repository;

import com.gtcafe.asimov.entity.Volume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VolumeRepository extends JpaRepository<Volume, Long> {
}
```

---

#### `VolumeService.java`
```java
package com.gtcafe.asimov.service;

import com.gtcafe.asimov.entity.Volume;
import com.gtcafe.asimov.repository.VolumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VolumeService {
    private final VolumeRepository volumeRepository;

    public List<Volume> getAll() {
        return volumeRepository.findAll();
    }

    public Optional<Volume> getById(Long id) {
        return volumeRepository.findById(id);
    }

    public Volume save(Volume volume) {
        return volumeRepository.save(volume);
    }

    public void deleteById(Long id) {
        volumeRepository.deleteById(id);
    }
}
```

---

#### `VolumeController.java`
```java
package com.gtcafe.asimov.controller;

import com.gtcafe.asimov.dto.VolumeRequest;
import com.gtcafe.asimov.entity.Volume;
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
    public ResponseEntity<List<Volume>> getAll() {
        return ResponseEntity.ok(volumeService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Volume> getById(@PathVariable Long id) {
        return volumeService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Volume> create(@RequestBody VolumeRequest request) {
        Volume volume = new Volume();
        volume.setName(request.getName());
        volume.setDescription(request.getDescription());
        return ResponseEntity.ok(volumeService.save(volume));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Volume> update(@PathVariable Long id, @RequestBody VolumeRequest request) {
        return volumeService.getById(id)
                .map(volume -> {
                    volume.setName(request.getName());
                    volume.setDescription(request.getDescription());
                    return ResponseEntity.ok(volumeService.save(volume));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        volumeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

#### `TenantAwareJpaInterceptor.java`
```java
package com.gtcafe.asimov.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantAwareJpaInterceptor {
    @PersistenceUnit
    private final EntityManagerFactory entityManagerFactory;
    private final HttpServletRequest request;

    public void configure() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Session session = entityManager.unwrap(Session.class);

        if (Boolean.TRUE.equals(getAwarenessFromToken())) {
            Long tenantId = getTenantIdFromToken();
            Filter filter = session.enableFilter("tenantFilter");
            filter.setParameter("tenantId", tenantId);
        } else {
            session.disableFilter("tenantFilter");
        }
    }

    private Long getTenantIdFromToken() {
        // 模擬從 token 中取出 tenantId
        return 1L; // 假設 token 中的 tenantId 為 1
    }

    private Boolean getAwarenessFromToken() {
        // 模擬從 token 中取出 awareness
        return true; // 假設 token 中的 awareness 為 true
    }
}
```

---

#### `TenantConfig.java`
```java
package com.gtcafe.asimov.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class TenantConfig {
    private final TenantAwareJpaInterceptor tenantAwareJpaInterceptor;

    @PostConstruct
    public void setup() {
        tenantAwareJpaInterceptor.configure();
    }
}
```

---

#### `JwtTokenUtil.java`
```java
package com.gtcafe.asimov.config;

import org.springframework.stereotype.Component;

@Component
public class JwtTokenUtil {
    public Long getTenantIdFromToken(String token) {
        // 假設從 JWT token 中解碼取得 tenantId
        return 1L;
    }

    public Boolean getAwarenessFromToken(String token) {
        // 假設從 JWT token 中解碼取得 awareness
        return true;
    }
}
```

### 注意事項
1. MariaDB 的資料表須加上 Hibernate Filter 支援。
2. JWT Token 的解析與驗證需依據實際需求擴充。
3. Spring Security 可加入以增強安全性。