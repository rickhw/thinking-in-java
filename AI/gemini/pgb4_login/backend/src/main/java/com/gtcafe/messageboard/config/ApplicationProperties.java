package com.gtcafe.messageboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Configuration properties class that maps environment variables to strongly-typed properties.
 * This provides better validation and IDE support for configuration values.
 */
@Component
@ConfigurationProperties(prefix = "app")
@Validated
public class ApplicationProperties {

    @NotBlank(message = "Application name is required")
    private String name = "PGB4 Message Board";

    @NotBlank(message = "Application version is required")
    private String version = "1.0.0";

    private String description = "Message Board Application";

    @NotBlank(message = "Environment is required")
    private String environment = "development";

    private Database database = new Database();
    private Server server = new Server();
    private Logging logging = new Logging();
    private Monitoring monitoring = new Monitoring();

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public Database getDatabase() { return database; }
    public void setDatabase(Database database) { this.database = database; }

    public Server getServer() { return server; }
    public void setServer(Server server) { this.server = server; }

    public Logging getLogging() { return logging; }
    public void setLogging(Logging logging) { this.logging = logging; }

    public Monitoring getMonitoring() { return monitoring; }
    public void setMonitoring(Monitoring monitoring) { this.monitoring = monitoring; }

    public static class Database {
        @Min(value = 1, message = "Database pool size must be at least 1")
        @Max(value = 100, message = "Database pool size must not exceed 100")
        private int poolSize = 10;

        @Min(value = 1, message = "Database minimum idle connections must be at least 1")
        private int minIdle = 5;

        @Min(value = 1000, message = "Connection timeout must be at least 1000ms")
        private long connectionTimeout = 30000;

        @Min(value = 10000, message = "Idle timeout must be at least 10000ms")
        private long idleTimeout = 600000;

        // Getters and Setters
        public int getPoolSize() { return poolSize; }
        public void setPoolSize(int poolSize) { this.poolSize = poolSize; }

        public int getMinIdle() { return minIdle; }
        public void setMinIdle(int minIdle) { this.minIdle = minIdle; }

        public long getConnectionTimeout() { return connectionTimeout; }
        public void setConnectionTimeout(long connectionTimeout) { this.connectionTimeout = connectionTimeout; }

        public long getIdleTimeout() { return idleTimeout; }
        public void setIdleTimeout(long idleTimeout) { this.idleTimeout = idleTimeout; }
    }

    public static class Server {
        @Min(value = 1, message = "Server port must be at least 1")
        @Max(value = 65535, message = "Server port must not exceed 65535")
        private int port = 8080;

        private String contextPath = "";

        private boolean compressionEnabled = false;

        // Getters and Setters
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }

        public String getContextPath() { return contextPath; }
        public void setContextPath(String contextPath) { this.contextPath = contextPath; }

        public boolean isCompressionEnabled() { return compressionEnabled; }
        public void setCompressionEnabled(boolean compressionEnabled) { this.compressionEnabled = compressionEnabled; }
    }

    public static class Logging {
        @NotNull(message = "Log level is required")
        private String level = "INFO";

        private String file = "logs/application.log";

        private String maxSize = "10MB";

        private int maxHistory = 30;

        // Getters and Setters
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }

        public String getFile() { return file; }
        public void setFile(String file) { this.file = file; }

        public String getMaxSize() { return maxSize; }
        public void setMaxSize(String maxSize) { this.maxSize = maxSize; }

        public int getMaxHistory() { return maxHistory; }
        public void setMaxHistory(int maxHistory) { this.maxHistory = maxHistory; }
    }

    public static class Monitoring {
        private boolean healthEnabled = true;
        private boolean metricsEnabled = true;
        private boolean prometheusEnabled = false;
        private String healthShowDetails = "when-authorized";

        // Getters and Setters
        public boolean isHealthEnabled() { return healthEnabled; }
        public void setHealthEnabled(boolean healthEnabled) { this.healthEnabled = healthEnabled; }

        public boolean isMetricsEnabled() { return metricsEnabled; }
        public void setMetricsEnabled(boolean metricsEnabled) { this.metricsEnabled = metricsEnabled; }

        public boolean isPrometheusEnabled() { return prometheusEnabled; }
        public void setPrometheusEnabled(boolean prometheusEnabled) { this.prometheusEnabled = prometheusEnabled; }

        public String getHealthShowDetails() { return healthShowDetails; }
        public void setHealthShowDetails(String healthShowDetails) { this.healthShowDetails = healthShowDetails; }
    }
}