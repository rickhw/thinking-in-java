package com.gtcafe.asimov;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseHealthIndicator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Health health() {
        try {
            // 執行一個簡單的查詢來檢查資料庫連線
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return Health.up()
                .withDetail("database", "MySQL is ready")
                .withDetail("users_count", getUsersCount())
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", "Cannot connect to database")
                .withDetail("message", e.getMessage())
                .build();
        }
    }

    private int getUsersCount() {
        try {
            return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        } catch (Exception e) {
            return -1;
        }
    }
}