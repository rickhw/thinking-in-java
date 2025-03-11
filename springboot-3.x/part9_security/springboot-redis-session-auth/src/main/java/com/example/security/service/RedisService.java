
package com.example.security.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    public RedisService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveSession(String sessionId, String username, long expiration) {
        redisTemplate.opsForValue().set(sessionId, username, Duration.ofSeconds(expiration));
    }

    public String getUsernameFromSession(String sessionId) {
        return redisTemplate.opsForValue().get(sessionId);
    }

    public void deleteSession(String sessionId) {
        redisTemplate.delete(sessionId);
    }
}
