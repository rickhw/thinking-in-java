package com.gtcafe.asimov.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
            .authorizeHttpRequests(requests -> requests
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll() // 允許訪問靜態資源
                .requestMatchers(HttpMethod.POST, "/api/members").permitAll()
                .requestMatchers("/api/tokens").permitAll()
                .requestMatchers(HttpMethod.GET, "/_/**").permitAll()
                
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login") // 指定自定義登入頁面的URL
                .loginProcessingUrl("/process-login") // 登入表單提交的URL, 改動的話, 要自己處理. Method=POST, 建議不要動, 但是也不能不寫
                .defaultSuccessUrl("/home", true) // 登入成功後跳轉的頁面
                .failureUrl("/login?error=true") // 登入失敗後跳轉的頁面
            .failureHandler((request, response, exception) -> {
                log.error("Authentication failed: [{}]", exception.getMessage());
                response.sendRedirect("/login?error=true");
            })
            .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/perform-logout") // 登出處理的URL
                .logoutSuccessUrl("/login?logout=true") // 登出成功後跳轉的頁面
                .invalidateHttpSession(true) // 使HTTP Session失效
                .clearAuthentication(true) // 清除認證信息
                .deleteCookies("JSESSIONID") // 刪除 Session Cookie
                .permitAll()
            )
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .maximumSessions(1)
                // .maxSessionsPreventsLogin(true) // 不允許新登入，會拒絕新的 session
                .maxSessionsPreventsLogin(false) // 允許新登入，會踢掉舊的 session
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
