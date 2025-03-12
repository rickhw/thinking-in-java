
RegionService.getFromCache(String) 中這行有問題:

return Optional.of(jsonUtils.jsonStringToModel(cachedValue, Region.class));

我提供 JsonUtils 原始碼給你，幫我做適度的調整。

package com.gtcafe.asimov.utils;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class JsonUtils {

    private final ObjectMapper objectMapper;

    public JsonUtils() {
        this.objectMapper = new ObjectMapper();
    }
    
    public <T> T jsonStringToModel(String jsonString, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonString, clazz);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String modelToJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
            
        }
    }
}

---

是的，你說得對。`JsonUtils` 的方法可能返回 `null`，所以直接使用 `Optional.of()` 可能會拋出 `NullPointerException`。我們需要同時改進 `JsonUtils` 和 `RegionService` 的相關代碼。

首先改進 `JsonUtils`：



```java
package com.gtcafe.asimov.utils;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

@Slf4j
@Service
public class JsonUtils {

    private final ObjectMapper objectMapper;

    public JsonUtils() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 將 JSON 字串轉換為物件
     * @param jsonString JSON 字串
     * @param clazz 目標類別
     * @return Optional 包裝的目標物件
     */
    public <T> Optional<T> jsonStringToModelSafe(String jsonString, Class<T> clazz) {
        if (jsonString == null) {
            return Optional.empty();
        }
        
        try {
            T result = objectMapper.readValue(jsonString, clazz);
            return Optional.ofNullable(result);
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON string to {}: {}", clazz.getSimpleName(), e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 將 JSON 字串轉換為物件（保留原有方法以維持相容性）
     * @deprecated 建議使用 jsonStringToModelSafe 方法
     */
    @Deprecated
    public <T> T jsonStringToModel(String jsonString, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonString, clazz);
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON string to {}: {}", clazz.getSimpleName(), e.getMessage());
            return null;
        }
    }

    /**
     * 將物件轉換為 JSON 字串
     * @param object 要轉換的物件
     * @return Optional 包裝的 JSON 字串
     */
    public Optional<String> modelToJsonStringSafe(Object object) {
        if (object == null) {
            return Optional.empty();
        }
        
        try {
            String result = objectMapper.writeValueAsString(object);
            return Optional.ofNullable(result);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to JSON string: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 將物件轉換為 JSON 字串（保留原有方法以維持相容性）
     * @deprecated 建議使用 modelToJsonStringSafe 方法
     */
    @Deprecated
    public String modelToJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to JSON string: {}", e.getMessage());
            return null;
        }
    }
}

```

然後相應地修改 `RegionService` 中的 `getFromCache` 方法：

```java
private Optional<Region> getFromCache(String cacheKey) {
    try {
        String cachedValue = cacheRepos.retrieveObject(cacheKey)
            .orElse(null);
            
        if (cachedValue == null) {
            return Optional.empty();
        }
        
        return jsonUtils.jsonStringToModelSafe(cachedValue, Region.class);
    } catch (Exception e) {
        log.warn("Error reading from cache for key: {}", cacheKey, e);
        return Optional.empty();
    }
}

```

主要改進內容：

1. `JsonUtils` 的改進：
   - 新增了返回 Optional 的安全版本方法
   - 保留了原有方法以維持向後相容性
   - 改進了錯誤處理和日誌記錄
   - 加入了 null 檢查
   - 使用 lombok 的 @Slf4j 進行日誌處理

2. 使用方式的改進：
```java
// 舊方式
Region region = jsonUtils.jsonStringToModel(jsonString, Region.class);
if (region == null) {
    // 處理錯誤情況
}

// 新方式
Optional<Region> region = jsonUtils.jsonStringToModelSafe(jsonString, Region.class);
region.ifPresent(r -> {
    // 處理正常情況
});

// 或者
Region region = jsonUtils.jsonStringToModelSafe(jsonString, Region.class)
    .orElseThrow(() -> new ResourceNotFoundException("Region not found in cache"));
```

3. 錯誤處理的改進：
   - 更清晰的錯誤日誌
   - 更安全的 null 處理
   - 使用 Optional 來處理可能的空值情況

這樣的改進可以：
1. 避免 NullPointerException
2. 提供更清晰的 API
3. 保持與現有程式碼的相容性
4. 提供更好的錯誤處理機制

你覺得這樣的改進如何？需要調整其他部分嗎？
