package com.example;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@ConfigurationProperties(prefix = "app.redis")
public class RedisConfig {
    private int defaultTtl;
    private Map<String, Map<String, String>> prefixes;

    public int getDefaultTtl() {
        return defaultTtl;
    }

    public void setDefaultTtl(int ttl) {
        this.defaultTtl = ttl;
    }

    public Map<String, Map<String, String>> getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(Map<String, Map<String, String>> prefixes) {
        this.prefixes = prefixes;
    }
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        return template;
    }
}
