package com.gtcafe.asimov.config;

import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class TenantConfig {
    private final TenantAwareJpaInterceptor tenantAwareJpaInterceptor;

    @PostConstruct
    public void setup() {
        tenantAwareJpaInterceptor.configure();
    }
}
