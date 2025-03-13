package com.gtcafe.asimov;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;
    private final ExecutorService executorService;

    public DatabaseHealthIndicator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public Health health() {
        try {
            Future<Health> healthFuture = executorService.submit(() -> checkDatabaseHealth());
            return healthFuture.get(3, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            return Health.down()
                    .withDetail("error", "Database health check timed out")
                    .withDetail("timeout", "3 seconds")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", "Database connection failed")
                    .withDetail("message", e.getMessage())
                    .build();
        }
    }

    private Health checkDatabaseHealth() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);

            return Health.up()
                    .withDetail("database", "MySQL is ready")
                    .withDetail("users_count", userCount)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", "Cannot connect to database")
                    .withDetail("message", e.getMessage())
                    .build();
        }
    }
}
