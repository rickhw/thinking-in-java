package com.gtcafe.asimov.platform;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.gtcafe.asimov.system.intercepter.ApiMetadataContextInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ApiMetadataContextInterceptor apiMetadataContextInterceptor;

    public WebConfig(ApiMetadataContextInterceptor apiMetadataContextInterceptor) {
        this.apiMetadataContextInterceptor = apiMetadataContextInterceptor;
    }

    @Override
    public void addInterceptors( InterceptorRegistry registry) {
        // registry.addInterceptor(tenantContextInterceptor)
        //         .addPathPatterns("/**")  // 攔截所有請求
        //         .excludePathPatterns("/health", "/metrics")
        //         .excludePathPatterns("/swagger-ui/**", "/v3/api-docs/**")
        //         ;  // 排除

        registry.addInterceptor(apiMetadataContextInterceptor)
                .addPathPatterns("/**") 
            ;  // 排除

    }
}