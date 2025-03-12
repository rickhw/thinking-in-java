
請接續幫我完成 RegionService 裡的功能，這個功能是 flush cache，
主要的目的是把 db 裡的資料更新到 cache 裡。

幫我考慮一下，如果 cache 裡有一些不一致的問題，應該怎麼處理？是先清空 cache 再重新塞入資料，還是直接更新 cache 裡的資料？


---


我來幫你完成 flush 功能的實作。對於 cache 不一致的問題，我建議採用「先清空再重新塞入」的策略，原因是：

1. 更簡單且可靠：避免部分更新可能造成的不一致
2. 避免髒讀：防止其他線程在更新過程中讀取到不一致的數據
3. 批次處理效率較高：可以使用 pipeline 或批次操作



```java
@Service
@Slf4j
@RequiredArgsConstructor
public class RegionService {
    // ... 原有的程式碼 ...

    private static final String FLUSH_LOCK_KEY = "region:flush:lock";
    private static final Duration FLUSH_LOCK_TIMEOUT = Duration.ofMinutes(5);
    private static final int BATCH_SIZE = 100;

    /**
     * 將資料庫中的所有 Region 資料同步到快取
     * 使用批次處理來提高效能
     */
    @Transactional(readOnly = true)
    @Retryable(value = DataAccessException.class, maxAttempts = 3)
    public void flush() {
        boolean lockAcquired = false;
        
        try {
            // 1. 嘗試獲取全局刷新鎖
            lockAcquired = cacheRepos.setIfNotExists(FLUSH_LOCK_KEY, getLockString(), FLUSH_LOCK_TIMEOUT);
            if (!lockAcquired) {
                log.warn("Another flush operation is in progress");
                throw new ConcurrentOperationException("Another flush operation is in progress");
            }

            log.info("Starting region cache flush operation");
            
            // 2. 從資料庫獲取所有資料（使用分頁避免內存溢出）
            int pageNumber = 0;
            List<RegionEntity> batch;
            Set<String> processedKeys = new HashSet<>();
            
            do {
                batch = dbRepos.findAllWithPagination(pageNumber, BATCH_SIZE);
                if (batch.isEmpty()) {
                    break;
                }

                // 3. 處理每一批次資料
                processBatch(batch, processedKeys);
                pageNumber++;
                
                log.debug("Processed batch {}, size: {}", pageNumber, batch.size());
            } while (!batch.isEmpty());

            // 4. 清理過期的快取項（清除那些在資料庫中已不存在的項）
            cleanupStaleCache(processedKeys);
            
            log.info("Region cache flush completed successfully. Total processed: {}", processedKeys.size());
            
        } catch (Exception e) {
            log.error("Error during region cache flush operation", e);
            throw new RuntimeException("Failed to flush region cache", e);
        } finally {
            if (lockAcquired) {
                cacheRepos.delete(FLUSH_LOCK_KEY);
                log.debug("Released flush lock");
            }
        }
    }

    private void processBatch(List<RegionEntity> entities, Set<String> processedKeys) {
        for (RegionEntity entity : entities) {
            try {
                Region region = RegionMapper.mapEntityToDomain(entity);
                updateCache(region);
                processedKeys.add(generateCacheKey(region.getRegionCode()));
            } catch (Exception e) {
                log.error("Error processing region entity: {}", entity, e);
                // 繼續處理下一個，不中斷整個批次
            }
        }
    }

    private void cleanupStaleCache(Set<String> validKeys) {
        try {
            // 注意：這裡假設 CacheRepository 有實作 findKeysByPattern 方法
            Set<String> existingKeys = cacheRepos.findKeysByPattern(CACHE_KEY_PREFIX + "*");
            existingKeys.removeAll(validKeys);

            if (!existingKeys.isEmpty()) {
                log.info("Removing {} stale cache entries", existingKeys.size());
                for (String key : existingKeys) {
                    cacheRepos.delete(key);
                }
            }
        } catch (Exception e) {
            log.warn("Error during cache cleanup", e);
            // 清理過期快取失敗不應影響主要的刷新操作
        }
    }
}

// 新增的異常類別
@ResponseStatus(HttpStatus.CONFLICT)
public class ConcurrentOperationException extends RuntimeException {
    public ConcurrentOperationException(String message) {
        super(message);
    }
}

// RegionRepository 介面的擴展
public interface RegionRepository extends JpaRepository<RegionEntity, Long> {
    // ... 原有的方法 ...
    
    @Query("SELECT r FROM RegionEntity r")
    List<RegionEntity> findAllWithPagination(Pageable pageable);
    
    default List<RegionEntity> findAllWithPagination(int pageNumber, int pageSize) {
        return findAllWithPagination(PageRequest.of(pageNumber, pageSize));
    }
}

```

這個實作的主要特點：

1. 併發控制：
   - 使用分布式鎖確保同一時間只有一個 flush 操作
   - 設置較長的鎖超時時間以支援大量資料處理

2. 批次處理：
   - 使用分頁查詢避免一次載入過多資料
   - 可配置的批次大小
   - 錯誤處理不影響整個批次

3. 資料一致性：
   - 完整替換快取中的資料
   - 清理過期的快取項
   - 事務處理確保資料一致性

4. 效能考慮：
   - 使用分頁避免記憶體溢出
   - 批次處理提高效能
   - 使用 Set 提高查找效率

5. 錯誤處理：
   - 詳細的日誌記錄
   - 適當的異常處理
   - 確保鎖的釋放

使用範例：
```java
@Service
public class YourService {
    private final RegionService regionService;
    
    public void refreshCache() {
        try {
            regionService.flush();
        } catch (ConcurrentOperationException e) {
            // 處理併發情況
        } catch (Exception e) {
            // 處理其他錯誤
        }
    }
}
```

你可能需要：
1. 在 CacheRepository 中實作 findKeysByPattern 方法
2. 調整 BATCH_SIZE 和超時時間
3. 加入監控指標收集
4. 考慮增加異步處理的支援

這個實作方式你覺得如何？需要調整哪些部分？

