# Prompt

這個 spring security 我已經大概可以運作，幫我整理一些資訊給我，包含以下：

1. 使用者登入的 session 資訊有哪些？應該如何取得？
2. 承上，我想調整 session 的設定，應該如何配置？
3. 承上，這些設定能不能動態調整？如果可以，那應該怎麼做？


---

# Chine

```java
package com.gtcafe.asimov.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableRedisHttpSession
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }
}
```


```java
package com.gtcafe.asimov.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
            .authorizeHttpRequests(requests -> requests
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll() // 允許訪問靜態資源
                .requestMatchers(HttpMethod.POST, "/api/members").permitAll()
                .requestMatchers(HttpMethod.GET, "/_/**").permitAll()
                
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login") // 指定自定義登入頁面的URL
                .loginProcessingUrl("/process-login") // 登入表單提交的URL, 改動的話, 要自己處理. Method=POST, 建議不要動, 但是也不能不寫
                .defaultSuccessUrl("/home", true) // 登入成功後跳轉的頁面
                .failureUrl("/login?error=true") // 登入失敗後跳轉的頁面
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/perform-logout") // 登出處理的URL
                .logoutSuccessUrl("/login?logout=true") // 登出成功後跳轉的頁面
                .invalidateHttpSession(true) // 使HTTP Session失效
                .clearAuthentication(true) // 清除認證信息
                .permitAll()
            )
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session       // ++
                .maximumSessions(1)                     // ++
                .maxSessionsPreventsLogin(true)         // ++
            )
            .build();
    }
   
    // 密碼加密，這裡使用 NoOpPasswordEncoder，不做任何加密
    // 實際應用中，應該使用 BCryptPasswordEncoder 或其他安全的加密方式
    @Bean
    public PasswordEncoder passwordEncoder() {
        // return NoOpPasswordEncoder.getInstance();
        return new BCryptPasswordEncoder();
    }
}
```
