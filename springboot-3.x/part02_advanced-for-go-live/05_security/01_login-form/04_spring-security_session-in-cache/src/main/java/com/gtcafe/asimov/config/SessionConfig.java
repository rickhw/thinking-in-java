// package com.gtcafe.asimov.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.data.redis.connection.RedisConnectionFactory;
// import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
// import org.springframework.data.redis.serializer.RedisSerializer;
// import org.springframework.session.data.redis.RedisIndexedSessionRepository;
// import org.springframework.session.data.redis.config.ConfigureRedisAction;
// import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

// @Configuration
// @EnableRedisHttpSession
// public class SessionConfig {

//     @Bean
//     public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
//         return new JdkSerializationRedisSerializer();
//     }

//     @Bean
//     public ConfigureRedisAction configureRedisAction() {
//         return ConfigureRedisAction.NO_OP;
//     }

//     @Bean
//     public RedisOperationsSessionRepository sessionRepository(
//             RedisConnectionFactory connectionFactory,
//             RedisSerializer<Object> springSessionDefaultRedisSerializer) {
        
//         RedisIndexedSessionRepository sessionRepository = new RedisIndexedSessionRepository(connectionFactory);
//         sessionRepository.setDefaultSerializer(springSessionDefaultRedisSerializer);
        
//         // 設置命名空間
//         sessionRepository.setRedisKeyNamespace("asimov:session");
        
//         return sessionRepository;
//     }
// }