package com.gtcafe.rws.booter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisConfig {
    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        // return new JedisConnectionFactory();
        JedisConnectionFactory jedisConFactory = new JedisConnectionFactory();
    //   jedisConFactory.setHostName("localhost");
    //   jedisConFactory.setPort(6379);
      return jedisConFactory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }
}
