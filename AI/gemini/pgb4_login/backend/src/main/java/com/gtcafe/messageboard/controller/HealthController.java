package com.gtcafe.messageboard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Health controller providing Kubernetes-compatible readiness and liveness probes.
 * These endpoints are designed to be used by container orchestration systems.
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Liveness probe endpoint for Kubernetes.
     * This endpoint should return 200 if the application is running and can handle requests.
     * It should not check external dependencies.
     */
    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> liveness() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        response.put("checks", Map.of(
            "application", "Application is running",
            "jvm", "JVM is responsive"
        ));
        
        return ResponseEntity.ok(response);
    }

    /**
     * Readiness probe endpoint for Kubernetes.
     * This endpoint should return 200 only when the application is ready to serve traffic.
     * It should check external dependencies like database connectivity.
     */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> checks = new HashMap<>();
        boolean isReady = true;

        // Check database connectivity
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                checks.put("database", "Connected");
            } else {
                checks.put("database", "Connection validation failed");
                isReady = false;
            }
        } catch (Exception e) {
            checks.put("database", "Connection failed: " + e.getMessage());
            isReady = false;
        }

        // Check memory usage
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        double memoryUsage = (double) usedMemory / maxMemory * 100;
        
        if (memoryUsage < 95) {
            checks.put("memory", "OK (" + String.format("%.1f%%", memoryUsage) + " used)");
        } else {
            checks.put("memory", "High usage (" + String.format("%.1f%%", memoryUsage) + " used)");
            isReady = false;
        }

        response.put("status", isReady ? "UP" : "DOWN");
        response.put("timestamp", System.currentTimeMillis());
        response.put("checks", checks);

        return isReady ? ResponseEntity.ok(response) : ResponseEntity.status(503).body(response);
    }

    /**
     * Startup probe endpoint for Kubernetes.
     * This endpoint indicates whether the application has finished starting up.
     */
    @GetMapping("/startup")
    public ResponseEntity<Map<String, Object>> startup() {
        Map<String, Object> response = new HashMap<>();
        
        // Simple startup check - if we can respond, we've started up
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        response.put("message", "Application has started successfully");
        
        return ResponseEntity.ok(response);
    }
}