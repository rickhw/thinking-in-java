package com.gtcafe.asimov;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.SystemHealth;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    private final HealthEndpoint healthEndpoint;

    public HealthCheckController(HealthEndpoint healthEndpoint) {
        this.healthEndpoint = healthEndpoint;
    }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        SystemHealth systemHealth = (SystemHealth) healthEndpoint.health();
        
        Map<String, Object> health = new HashMap<>();
        health.put("status", systemHealth.getStatus().getCode());
        health.put("application", "Spring Boot Application");
        health.put("version", "1.0.0");
        
        // 如果有額外的健康檢查詳情，也一併回傳
        if (systemHealth.getDetails() != null && !systemHealth.getDetails().isEmpty()) {
            health.put("details", systemHealth.getDetails());
        }
        
        return ResponseEntity.ok(health);
    }
}