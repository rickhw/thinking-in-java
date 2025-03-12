package com.gtcafe.asimov;

import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class LoggingConfig {
    @PostConstruct
    public void setup() {
        // Add default fields to logs
        MDC.put("application", "api-app");
        MDC.put("environment", "development");
        MDC.put("traceId", UUID.randomUUID().toString());
    }
}
