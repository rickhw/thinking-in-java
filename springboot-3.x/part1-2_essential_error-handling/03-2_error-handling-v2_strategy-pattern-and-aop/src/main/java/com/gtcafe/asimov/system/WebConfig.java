package com.gtcafe.asimov.system;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.gtcafe.asimov.system.context.TenantContextInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final TenantContextInterceptor tenantContextInterceptor;

    public WebConfig(TenantContextInterceptor tenantContextInterceptor) {
        this.tenantContextInterceptor = tenantContextInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantContextInterceptor)
                .addPathPatterns("/**")  // 攔截所有請求
                .excludePathPatterns("/health", "/metrics");  // 排除
    }
}