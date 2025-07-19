package com.messageboard.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * Redis 健康檢查元件
 * 提供 Redis 連線狀態檢查功能
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisHealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;

    /**
     * 檢查 Redis 連線健康狀態
     * @return true 如果 Redis 連線正常，否則 false
     */
    public boolean isHealthy() {
        try {
            RedisConnection connection = redisConnectionFactory.getConnection();
            
            if (connection != null) {
                // 執行 PING 命令測試連線
                String pong = connection.ping();
                connection.close();
                
                if ("PONG".equals(pong)) {
                    log.debug("Redis health check passed");
                    return true;
                } else {
                    log.warn("Redis health check failed: unexpected ping response: {}", pong);
                    return false;
                }
            } else {
                log.error("Redis health check failed: unable to get connection");
                return false;
            }
        } catch (Exception e) {
            log.error("Redis health check failed with exception", e);
            return false;
        }
    }

    /**
     * 取得 Redis 連線資訊
     * @return Redis 連線資訊字串
     */
    public String getConnectionInfo() {
        try {
            RedisConnection connection = redisConnectionFactory.getConnection();
            if (connection != null) {
                String info = connection.getNativeConnection().toString();
                connection.close();
                return info;
            }
            return "No connection available";
        } catch (Exception e) {
            log.error("Failed to get Redis connection info", e);
            return "Error: " + e.getMessage();
        }
    }
}