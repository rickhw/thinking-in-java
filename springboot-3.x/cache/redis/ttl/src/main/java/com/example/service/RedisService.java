package com.example.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.RedisConfig;
import com.example.model.DataModel;

@Service
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final HashOperations<String, String, String> hashOperations;
    private final RedisConfig redisConfig;

    @Autowired
    public RedisService(RedisTemplate<String, Object> redisTemplate, RedisConfig redisConfig) {
        this.redisTemplate = redisTemplate;
        this.hashOperations = redisTemplate.opsForHash();
        this.redisConfig = redisConfig;
    }

    public void saveData(String dataType, DataModel dataModel) {
        String prefix = redisConfig.getPrefixes().get(dataType).get("prefix");
        String key = prefix + dataModel.getId();
        int ttl = Integer.parseInt(redisConfig.getPrefixes().get(dataType).getOrDefault("ttl", 
                String.valueOf(redisConfig.getDefaultTtl())));

        // 保存資料到 Redis Hash 中
        List<Map<String, String>> l = dataModel.getData();
        for(int i = 0; i<l.size(); i++) {
            Map<String, String> item = l.get(i);
            hashOperations.putAll(key, item);
        }
        
        // 設定 TTL
        redisTemplate.expire(key, Duration.ofSeconds(ttl));
    }
}
