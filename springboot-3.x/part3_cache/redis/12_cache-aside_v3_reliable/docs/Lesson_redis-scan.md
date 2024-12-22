

我來幫你實作 `CacheRepository` 中的 `findKeysByPattern` 方法。這個方法會使用 Redis 的 KEYS 或 SCAN 命令來查找符合模式的鍵值。

由於 Redis 的 KEYS 命令在大型資料集上可能會有效能問題，所以我們會使用 SCAN 命令來實作。



```java
package com.gtcafe.asimov.cache;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class CacheRepository {
    // ... 原有的程式碼 ...

    /**
     * 使用pattern查找所有符合的keys
     * 使用 SCAN 命令以避免阻塞 Redis
     *
     * @param pattern 查找模式，例如 "region:*"
     * @return 符合模式的鍵值集合
     */
    public Set<String> findKeysByPattern(String pattern) {
        Set<String> keys = new HashSet<>();
        
        try {
            redisTemplate.execute((RedisConnection connection) -> {
                Set<String> matchedKeys = new HashSet<>();
                
                try (Cursor<byte[]> cursor = connection.scan(
                    ScanOptions.scanOptions()
                        .match(pattern)
                        .count(100) // 每次掃描的數量
                        .build())) {
                            
                    while (cursor.hasNext()) {
                        matchedKeys.add(new String(cursor.next()));
                    }
                } catch (Exception e) {
                    log.error("Error scanning keys with pattern: {}", pattern, e);
                    throw new CacheOperationException("Failed to scan keys", e);
                }
                
                return matchedKeys;
            });
        } catch (Exception e) {
            log.error("Error finding keys by pattern: {}", pattern, e);
            throw new CacheOperationException("Failed to find keys by pattern", e);
        }
        
        log.debug("Found {} keys matching pattern: {}", keys.size(), pattern);
        return keys;
    }

    /**
     * 使用pattern刪除所有符合的keys
     * 注意：這是一個潛在的昂貴操作，請謹慎使用
     *
     * @param pattern 刪除模式，例如 "region:*"
     * @return 已刪除的鍵值數量
     */
    public long deleteKeysByPattern(String pattern) {
        try {
            Set<String> keys = findKeysByPattern(pattern);
            if (!keys.isEmpty()) {
                long deletedCount = redisTemplate.delete(keys);
                log.info("Deleted {} keys matching pattern: {}", deletedCount, pattern);
                return deletedCount;
            }
            return 0;
        } catch (Exception e) {
            log.error("Error deleting keys by pattern: {}", pattern, e);
            throw new CacheOperationException("Failed to delete keys by pattern", e);
        }
    }

    /**
     * 檢查key是否存在於快取中
     *
     * @param pattern 查找模式
     * @return 是否存在符合模式的鍵值
     */
    public boolean existsByPattern(String pattern) {
        try {
            Set<String> keys = findKeysByPattern(pattern);
            return !keys.isEmpty();
        } catch (Exception e) {
            log.error("Error checking existence by pattern: {}", pattern, e);
            throw new CacheOperationException("Failed to check existence by pattern", e);
        }
    }
}

```

這個實作：

1. 使用 SCAN 而不是 KEYS：
   - SCAN 命令是遞迴式的，不會阻塞 Redis
   - 可以設定每次掃描的數量（COUNT）
   - 適合處理大量資料

2. 錯誤處理：
   - 使用 try-with-resources 確保資源正確關閉
   - 詳細的錯誤日誌
   - 拋出自定義異常

3. 效能考慮：
   - 使用 HashSet 儲存結果避免重複
   - 可配置的掃描批次大小
   - 使用 cursor 避免一次載入過多資料

4. 額外功能：
   - 提供批量刪除方法
   - 提供模式匹配檢查方法

使用範例：
```java
@Service
public class YourService {
    private final CacheRepository cacheRepository;
    
    public void example() {
        // 查找所有以 "region:" 開頭的鍵值
        Set<String> keys = cacheRepository.findKeysByPattern("region:*");
        
        // 刪除所有符合模式的鍵值
        long deletedCount = cacheRepository.deleteKeysByPattern("region:temp:*");
        
        // 檢查是否存在符合模式的鍵值
        boolean exists = cacheRepository.existsByPattern("region:active:*");
    }
}
```

使用注意事項：
1. 模式匹配可能會影響效能，建議使用具體的前綴
2. COUNT 參數可以根據實際情況調整
3. 在大型資料集上使用時要注意記憶體使用
4. 建議加入監控來追蹤執行時間

你覺得這個實作如何？需要調整哪些部分？
