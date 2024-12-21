package com.gtcafe.asimov.cache;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CacheRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final ValueOperations<String, String> valueOps;
    private final CacheMetrics cacheMetrics;

    @Value("${cache.default.ttl:3600}") // 預設 1 小時
    private long defaultTtl;

    // Lua 腳本：如果 cacheKey 不存在則設值
    private static final String SET_IF_NOT_EXISTS_SCRIPT = """
        if redis.call('exists', KEYS[1]) == 0 then 
            redis.call('set', KEYS[1], ARGV[1]) 
            if tonumber(ARGV[2]) > 0 then
                redis.call('expire', KEYS[1], ARGV[2])
            end
            return 1
        else
            return 0 
        end
        """;

    private final RedisScript<Boolean> setIfNotExistsScript;

    public CacheRepository(RedisTemplate<String, String> redisTemplate) {
        Assert.notNull(redisTemplate, "RedisTemplate must not be null");
        this.redisTemplate = redisTemplate;
        this.valueOps = redisTemplate.opsForValue();
        this.cacheMetrics = new CacheMetrics();
        this.setIfNotExistsScript = new DefaultRedisScript<>(SET_IF_NOT_EXISTS_SCRIPT, Boolean.class);
    }

    /**
     * 儲存或更新物件，使用預設的 TTL
     */
    public void saveOrUpdateObject(String cacheKey, String value) {
        saveOrUpdateObject(cacheKey, value, defaultTtl, TimeUnit.SECONDS);
    }

    /**
     * 儲存或更新物件，使用指定的 TTL
     */
    public void saveOrUpdateObject(String cacheKey, String value, long timeout, TimeUnit unit) {
        Assert.hasText(cacheKey, "cacheKey must not be empty");
        Assert.hasText(value, "Value must not be empty");

        try {
            long startTime = System.nanoTime();
            valueOps.set(cacheKey, value, timeout, unit);
            cacheMetrics.recordWriteLatency(System.nanoTime() - startTime);
            cacheMetrics.incrementWriteCount();

            log.debug("Successfully saved object with cacheKey: [{}]", cacheKey);
        } catch (Exception e) {
            cacheMetrics.incrementWriteErrorCount();
            log.error("Error saving object with cacheKey: [{}]", cacheKey, e);
            throw new CacheOperationException("Failed to save object to cache", e);
        }
    }

    /**
     * 如果 cacheKey 不存在，則設定值
     */
    public boolean setIfNotExists(String cacheKey, String value, Duration timeout) {
        Assert.hasText(cacheKey, "cacheKey must not be empty");
        Assert.hasText(value, "Value must not be empty");
        Assert.notNull(timeout, "Timeout must not be null");

        try {
            long startTime = System.nanoTime();
            Boolean result = redisTemplate.execute(
                    setIfNotExistsScript,
                    Collections.singletonList(cacheKey),
                    value,
                    String.valueOf(timeout.getSeconds()));
            cacheMetrics.recordWriteLatency(System.nanoTime() - startTime);

            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            cacheMetrics.incrementWriteErrorCount();
            log.error("Error executing setIfNotExists for cacheKey: [{}]", cacheKey, e);
            throw new CacheOperationException("Failed to execute setIfNotExists", e);
        }
    }

    /**
     * 取得物件
     */
    public Optional<String> retrieveObject(String cacheKey) {
        Assert.hasText(cacheKey, "cacheKey must not be empty");

        try {
            long startTime = System.nanoTime();
            String result = valueOps.get(cacheKey);
            cacheMetrics.recordReadLatency(System.nanoTime() - startTime);
            cacheMetrics.incrementReadCount();

            if (result == null) {
                cacheMetrics.incrementMissCount();
                log.debug("Cache miss for cacheKey: [{}]", cacheKey);
            } else {
                cacheMetrics.incrementHitCount();
                log.debug("Cache hit for cacheKey: [{}]", cacheKey);
            }

            return Optional.ofNullable(result);
        } catch (Exception e) {
            cacheMetrics.incrementReadErrorCount();
            log.error("Error retrieving object with cacheKey: [{}]", cacheKey, e);
            throw new CacheOperationException("Failed to retrieve object from cache", e);
        }
    }

    /**
     * 刪除物件
     */
    public void delete(String cacheKey) {
        Assert.hasText(cacheKey, "cacheKey must not be empty");

        try {
            redisTemplate.delete(cacheKey);
            log.debug("Successfully deleted cacheKey: [{}]", cacheKey);
        } catch (Exception e) {
            log.error("Error deleting cacheKey: [{}]", cacheKey, e);
            throw new CacheOperationException("Failed to delete object from cache", e);
        }
    }

    /**
     * 取得快取統計資訊
     */
    public CacheMetrics getMetrics() {
        return cacheMetrics;
    }

    /**
     * 檢查 cacheKey 是否存在
     */
    public boolean exists(String cacheKey) {
        Assert.hasText(cacheKey, "CacheKey must not be empty");
        return Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey));
    }

    /**
     * 設定 cacheKey 的過期時間
     */
    public boolean expire(String cacheKey, long timeout, TimeUnit unit) {
        Assert.hasText(cacheKey, "cacheKey must not be empty");
        return Boolean.TRUE.equals(redisTemplate.expire(cacheKey, timeout, unit));
    }
}