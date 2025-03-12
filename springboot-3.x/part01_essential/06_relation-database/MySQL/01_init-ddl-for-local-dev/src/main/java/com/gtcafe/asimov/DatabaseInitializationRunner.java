package com.gtcafe.asimov;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
            System.out.println("成功連線到資料庫！目前使用者數量：" + userCount);
        } catch (Exception e) {
            System.err.println("無法連線到資料庫：" + e.getMessage());
        }
    }
}