package com.gtcafe.asimov;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitConfig rateLimitConfig;

    public WebMvcConfig(RateLimitConfig rateLimitConfig) {
        this.rateLimitConfig = rateLimitConfig;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor(rateLimitConfig))
                .addPathPatterns("/demo/**"); // 只對 /demo 下的路徑生效
    }
}