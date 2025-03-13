package com.gtcafe.asimov.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.gtcafe.asimov.model.MemberAuthority;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
            return httpSecurity
                    .authorizeHttpRequests(requests -> requests
                            .requestMatchers(HttpMethod.POST, "/members").permitAll()
                            .requestMatchers(HttpMethod.GET, "/members").hasAuthority(MemberAuthority.ADMIN.toString())
                            .requestMatchers(HttpMethod.GET, "/selected-courses").hasAuthority(MemberAuthority.STUDENT.toString())
                            .requestMatchers(HttpMethod.GET, "/course-feedback").hasAnyAuthority(MemberAuthority.TEACHER.toString(), MemberAuthority.ADMIN.toString())
                            .anyRequest().authenticated()
                    )
                    .formLogin(Customizer.withDefaults())
                    .csrf(csrf -> csrf.disable())
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