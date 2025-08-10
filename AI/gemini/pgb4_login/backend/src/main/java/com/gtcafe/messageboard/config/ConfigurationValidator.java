package com.gtcafe.messageboard.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration validator that checks required environment variables and configuration
 * values during application startup.
 */
@Component
public class ConfigurationValidator {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationValidator.class);

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Value("${server.port}")
    private int serverPort;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @EventListener(ApplicationReadyEvent.class)
    public void validateConfiguration() {
        logger.info("Starting configuration validation for profile: {}", activeProfile);
        
        List<String> validationErrors = new ArrayList<>();
        
        // Validate database configuration
        validateDatabaseConfiguration(validationErrors);
        
        // Validate server configuration
        validateServerConfiguration(validationErrors);
        
        // Validate production-specific settings
        if ("prod".equals(activeProfile) || "production".equals(activeProfile)) {
            validateProductionConfiguration(validationErrors);
        }
        
        if (!validationErrors.isEmpty()) {
            logger.error("Configuration validation failed with {} errors:", validationErrors.size());
            validationErrors.forEach(error -> logger.error("  - {}", error));
            throw new IllegalStateException("Application configuration is invalid. Check logs for details.");
        }
        
        logger.info("Configuration validation completed successfully");
    }

    private void validateDatabaseConfiguration(List<String> errors) {
        if (datasourceUrl == null || datasourceUrl.trim().isEmpty()) {
            errors.add("Database URL is required (spring.datasource.url or DB_URL)");
        } else if (!datasourceUrl.startsWith("jdbc:")) {
            errors.add("Database URL must be a valid JDBC URL");
        }

        if (datasourceUsername == null || datasourceUsername.trim().isEmpty()) {
            errors.add("Database username is required (spring.datasource.username or DB_USERNAME)");
        }

        if (datasourcePassword == null || datasourcePassword.trim().isEmpty()) {
            errors.add("Database password is required (spring.datasource.password or DB_PASSWORD)");
        }

        logger.debug("Database configuration validated - URL: {}, Username: {}", 
                    maskSensitiveUrl(datasourceUrl), datasourceUsername);
    }

    private void validateServerConfiguration(List<String> errors) {
        if (serverPort < 1 || serverPort > 65535) {
            errors.add("Server port must be between 1 and 65535 (current: " + serverPort + ")");
        }

        logger.debug("Server configuration validated - Port: {}", serverPort);
    }

    private void validateProductionConfiguration(List<String> errors) {
        // In production, ensure sensitive values are not using defaults
        if ("medusa".equals(datasourcePassword)) {
            errors.add("Production environment should not use default database password");
        }

        if (datasourceUrl.contains("localhost")) {
            logger.warn("Production environment is using localhost database URL: {}", 
                       maskSensitiveUrl(datasourceUrl));
        }

        logger.debug("Production-specific configuration validated");
    }

    private String maskSensitiveUrl(String url) {
        if (url == null) return "null";
        // Mask password in JDBC URL if present
        return url.replaceAll("password=[^&;]*", "password=***");
    }
}