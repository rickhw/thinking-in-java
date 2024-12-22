package com.gtcafe.asimov.cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // /** Solution 1: <String, Object> */

        // // 1. 使用 Jackson2JsonRedisSerializer 來序列化不同的 Object
        // Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        // ObjectMapper objectMapper = new ObjectMapper();

        // // 2. 啟用多態處理（保存類型資訊），以支援不同子類型的反序列化
        // objectMapper.activateDefaultTyping(BasicPolymorphicTypeValidator.builder().build(),
        //         ObjectMapper.DefaultTyping.NON_FINAL);
        // objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        // serializer.setObjectMapper(objectMapper);

        // // 3. set Serializer
        // template.setKeySerializer(new StringRedisSerializer());
        // template.setValueSerializer(serializer);

        // // 4.
        // template.afterPropertiesSet();

        /** Solution 2: <String, String> */
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        return template;
    }
}
