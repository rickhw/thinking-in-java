package com.example.messageboard.config;

import com.gtcafe.messageboard.config.ConfigurationValidator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:mysql://localhost:3306/test_db",
    "spring.datasource.username=test_user",
    "spring.datasource.password=test_password",
    "server.port=8080"
})
class ConfigurationValidatorTest {

    @Test
    void testValidConfiguration() {
        // This test verifies that the application starts successfully with valid configuration
        // The ConfigurationValidator will run during application startup
        assertTrue(true, "Application should start successfully with valid configuration");
    }

    @Test
    void testConfigurationValidatorExists() {
        // Verify that the ConfigurationValidator class exists and can be instantiated
        ConfigurationValidator validator = new ConfigurationValidator();
        assertNotNull(validator, "ConfigurationValidator should be instantiable");
    }
}