package com.gtcafe.pgb.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.gtcafe.pgb.service.CacheService;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 快取服務實作類別
 * 基於 Redis 實作的快取服務，提供各種快取操作功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            log.debug("Cache set: key={}", key);
        } catch (Exception e) {
            log.error("Failed to set cache: key={}", key, e);
            throw new RuntimeException("Failed to set cache", e);
        }
    }

    @Override
    public void set(String key, Object value, Duration timeout) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout);
            log.debug("Cache set with timeout: key={}, timeout={}", key, timeout);
        } catch (Exception e) {
            log.error("Failed to set cache with timeout: key={}, timeout={}", key, timeout, e);
            throw new RuntimeException("Failed to set cache with timeout", e);
        }
    }

    @Override
    public Object get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            log.debug("Cache get: key={}, found={}", key, value != null);
            return value;
        } catch (Exception e) {
            log.error("Failed to get cache: key={}", key, e);
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                log.debug("Cache get: key={}, not found", key);
                return null;
            }
            
            if (clazz.isInstance(value)) {
                log.debug("Cache get: key={}, found with type={}", key, clazz.getSimpleName());
                return (T) value;
            } else {
                log.warn("Cache get: key={}, type mismatch. Expected={}, Actual={}", 
                        key, clazz.getSimpleName(), value.getClass().getSimpleName());
                return null;
            }
        } catch (Exception e) {
            log.error("Failed to get cache with type: key={}, type={}", key, clazz.getSimpleName(), e);
            return null;
        }
    }

    @Override
    public Boolean delete(String key) {
        try {
            Boolean result = redisTemplate.delete(key);
            log.debug("Cache delete: key={}, success={}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to delete cache: key={}", key, e);
            return false;
        }
    }

    @Override
    public Long delete(Set<String> keys) {
        try {
            Long result = redisTemplate.delete(keys);
            log.debug("Cache batch delete: keys={}, deleted={}", keys.size(), result);
            return result;
        } catch (Exception e) {
            log.error("Failed to batch delete cache: keys={}", keys, e);
            return 0L;
        }
    }

    @Override
    public Boolean exists(String key) {
        try {
            Boolean result = redisTemplate.hasKey(key);
            log.debug("Cache exists: key={}, exists={}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to check cache existence: key={}", key, e);
            return false;
        }
    }

    @Override
    public Boolean expire(String key, Duration timeout) {
        try {
            Boolean result = redisTemplate.expire(key, timeout);
            log.debug("Cache expire: key={}, timeout={}, success={}", key, timeout, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to set cache expiration: key={}, timeout={}", key, timeout, e);
            return false;
        }
    }

    @Override
    public Long getExpire(String key) {
        try {
            Long result = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            log.debug("Cache get expire: key={}, ttl={}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to get cache expiration: key={}", key, e);
            return -2L; // 鍵不存在
        }
    }

    @Override
    public void hSet(String key, String hashKey, Object value) {
        try {
            redisTemplate.opsForHash().put(key, hashKey, value);
            log.debug("Hash cache set: key={}, hashKey={}", key, hashKey);
        } catch (Exception e) {
            log.error("Failed to set hash cache: key={}, hashKey={}", key, hashKey, e);
            throw new RuntimeException("Failed to set hash cache", e);
        }
    }

    @Override
    public Object hGet(String key, String hashKey) {
        try {
            Object value = redisTemplate.opsForHash().get(key, hashKey);
            log.debug("Hash cache get: key={}, hashKey={}, found={}", key, hashKey, value != null);
            return value;
        } catch (Exception e) {
            log.error("Failed to get hash cache: key={}, hashKey={}", key, hashKey, e);
            return null;
        }
    }

    @Override
    public Long hDelete(String key, String... hashKeys) {
        try {
            Long result = redisTemplate.opsForHash().delete(key, (Object[]) hashKeys);
            log.debug("Hash cache delete: key={}, hashKeys={}, deleted={}", key, hashKeys, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to delete hash cache: key={}, hashKeys={}", key, hashKeys, e);
            return 0L;
        }
    }

    @Override
    public Boolean hExists(String key, String hashKey) {
        try {
            Boolean result = redisTemplate.opsForHash().hasKey(key, hashKey);
            log.debug("Hash cache exists: key={}, hashKey={}, exists={}", key, hashKey, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to check hash cache existence: key={}, hashKey={}", key, hashKey, e);
            return false;
        }
    }

    @Override
    public Long lLeftPush(String key, Object... values) {
        try {
            Long result = redisTemplate.opsForList().leftPushAll(key, values);
            log.debug("List left push: key={}, values={}, size={}", key, values.length, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to left push list: key={}", key, e);
            return 0L;
        }
    }

    @Override
    public Long lRightPush(String key, Object... values) {
        try {
            Long result = redisTemplate.opsForList().rightPushAll(key, values);
            log.debug("List right push: key={}, values={}, size={}", key, values.length, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to right push list: key={}", key, e);
            return 0L;
        }
    }

    @Override
    public Object lLeftPop(String key) {
        try {
            Object result = redisTemplate.opsForList().leftPop(key);
            log.debug("List left pop: key={}, value={}", key, result != null);
            return result;
        } catch (Exception e) {
            log.error("Failed to left pop list: key={}", key, e);
            return null;
        }
    }

    @Override
    public Object lRightPop(String key) {
        try {
            Object result = redisTemplate.opsForList().rightPop(key);
            log.debug("List right pop: key={}, value={}", key, result != null);
            return result;
        } catch (Exception e) {
            log.error("Failed to right pop list: key={}", key, e);
            return null;
        }
    }

    @Override
    public List<Object> lRange(String key, long start, long end) {
        try {
            List<Object> result = redisTemplate.opsForList().range(key, start, end);
            log.debug("List range: key={}, start={}, end={}, size={}", key, start, end, 
                    result != null ? result.size() : 0);
            return result;
        } catch (Exception e) {
            log.error("Failed to get list range: key={}, start={}, end={}", key, start, end, e);
            return null;
        }
    }

    @Override
    public Long lSize(String key) {
        try {
            Long result = redisTemplate.opsForList().size(key);
            log.debug("List size: key={}, size={}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to get list size: key={}", key, e);
            return 0L;
        }
    }

    @Override
    public Long sAdd(String key, Object... values) {
        try {
            Long result = redisTemplate.opsForSet().add(key, values);
            log.debug("Set add: key={}, values={}, added={}", key, values.length, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to add to set: key={}", key, e);
            return 0L;
        }
    }

    @Override
    public Long sRemove(String key, Object... values) {
        try {
            Long result = redisTemplate.opsForSet().remove(key, values);
            log.debug("Set remove: key={}, values={}, removed={}", key, values.length, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to remove from set: key={}", key, e);
            return 0L;
        }
    }

    @Override
    public Boolean sIsMember(String key, Object value) {
        try {
            Boolean result = redisTemplate.opsForSet().isMember(key, value);
            log.debug("Set is member: key={}, value={}, isMember={}", key, value, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to check set membership: key={}, value={}", key, value, e);
            return false;
        }
    }

    @Override
    public Set<Object> sMembers(String key) {
        try {
            Set<Object> result = redisTemplate.opsForSet().members(key);
            log.debug("Set members: key={}, size={}", key, result != null ? result.size() : 0);
            return result;
        } catch (Exception e) {
            log.error("Failed to get set members: key={}", key, e);
            return null;
        }
    }

    @Override
    public Long sSize(String key) {
        try {
            Long result = redisTemplate.opsForSet().size(key);
            log.debug("Set size: key={}, size={}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to get set size: key={}", key, e);
            return 0L;
        }
    }

    @Override
    public Set<String> keys(String pattern) {
        try {
            Set<String> result = redisTemplate.keys(pattern);
            log.debug("Keys search: pattern={}, found={}", pattern, result != null ? result.size() : 0);
            return result;
        } catch (Exception e) {
            log.error("Failed to search keys: pattern={}", pattern, e);
            return null;
        }
    }

    @Override
    public Long clearByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                Long result = redisTemplate.delete(keys);
                log.info("Cache cleared by pattern: pattern={}, cleared={}", pattern, result);
                return result;
            } else {
                log.debug("No keys found for pattern: {}", pattern);
                return 0L;
            }
        } catch (Exception e) {
            log.error("Failed to clear cache by pattern: pattern={}", pattern, e);
            return 0L;
        }
    }
}