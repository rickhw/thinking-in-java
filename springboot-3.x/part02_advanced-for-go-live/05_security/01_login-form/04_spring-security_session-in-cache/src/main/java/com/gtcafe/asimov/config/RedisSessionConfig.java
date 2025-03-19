// package com.gtcafe.asimov.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.data.redis.connection.RedisConnectionFactory;
// import org.springframework.session.data.redis.RedisIndexedSessionRepository;
// import org.springframework.data.redis.core.RedisTemplate;
// import org.springframework.data.redis.core.RedisOperations;
// import org.springframework.session.data.redis.config.ConfigureRedisAction;
// import org.springframework.session.Session;

// @Configuration
// public class RedisSessionConfig {
    
//     @Bean
//     public ConfigureRedisAction configureRedisAction() {
//         return ConfigureRedisAction.NO_OP;
//     }
    
//     @Bean
//     public RedisIndexedSessionRepository sessionRepository(RedisOperations<Object, Object> redisOperations) {
//         RedisIndexedSessionRepository sessionRepository = new RedisIndexedSessionRepository(redisOperations);
//         sessionRepository.setRedisKeyNamespace("asimov:session");
//         return sessionRepository;
//     }
// }
