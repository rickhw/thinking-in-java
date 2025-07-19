package com.messageboard.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaAuditing
@EntityScan(basePackages = "com.messageboard.entity")
@EnableJpaRepositories(basePackages = "com.messageboard.repository")
public class DatabaseConfig {
    // Database configuration will be handled by Spring Boot auto-configuration
    // Additional custom configurations can be added here if needed
}