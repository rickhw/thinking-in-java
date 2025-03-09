package com.gtcafe.asimov.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
            .authorizeHttpRequests(requests -> requests
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll() // 允許訪問靜態資源
                // .requestMatchers(HttpMethod.POST, "/members").permitAll()
                // .requestMatchers(HttpMethod.GET, "/members").hasAuthority(MemberAuthority.ADMIN.toString())
                // .requestMatchers(HttpMethod.GET, "/selected-courses").hasAuthority(MemberAuthority.STUDENT.toString())
                // .requestMatchers(HttpMethod.GET, "/course-feedback").hasAnyAuthority(MemberAuthority.TEACHER.toString(), MemberAuthority.ADMIN.toString())
                
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login") // 指定自定義登入頁面的URL
                .loginProcessingUrl("/process-login") // 登入表單提交的URL
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
            .build();
    }


    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins); // 允許的來源
        config.addAllowedMethod("*"); // 允許所有 HTTP 方法
        config.addAllowedHeader("*"); // 允許所有請求 Header
        config.setAllowCredentials(true); // 允許攜帶 Cookie

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

   
    // 密碼加密，這裡使用 NoOpPasswordEncoder，不做任何加密
    // 實際應用中，應該使用 BCryptPasswordEncoder 或其他安全的加密方式
    @Bean
    public PasswordEncoder passwordEncoder() {
        // return NoOpPasswordEncoder.getInstance();
        return new BCryptPasswordEncoder();
    }
}