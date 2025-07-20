package com.gtcafe.pgb.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gtcafe.pgb.entity.TokenBlacklist;
import com.gtcafe.pgb.repository.TokenBlacklistRepository;
import com.gtcafe.pgb.util.JwtConstants;
import com.gtcafe.pgb.util.JwtUtil;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Token 黑名單服務
 * 管理 JWT token 的黑名單，防止已登出或被撤銷的 token 被重複使用
 */
@Service
@Transactional
public class TokenBlacklistService {
    
    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistService.class);
    private static final String BLACKLIST_KEY_PREFIX = JwtConstants.BLACKLIST_KEY_PREFIX;
    
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    
    public TokenBlacklistService(TokenBlacklistRepository tokenBlacklistRepository,
                               JwtUtil jwtUtil,
                               RedisTemplate<String, String> redisTemplate) {
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * 將 token 加入黑名單
     * 
     * @param token JWT token
     */
    public void blacklistToken(String token) {
        try {
            String tokenHash = jwtUtil.getTokenHash(token);
            Date expiration = jwtUtil.extractExpiration(token);
            
            // 儲存到資料庫
            TokenBlacklist blacklistEntry = new TokenBlacklist();
            blacklistEntry.setTokenHash(tokenHash);
            blacklistEntry.setExpiresAt(expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            blacklistEntry.setCreatedAt(LocalDateTime.now());
            
            tokenBlacklistRepository.save(blacklistEntry);
            
            // 儲存到 Redis 快取
            String redisKey = BLACKLIST_KEY_PREFIX + tokenHash;
            long ttlSeconds = (expiration.getTime() - System.currentTimeMillis()) / 1000;
            
            if (ttlSeconds > 0) {
                redisTemplate.opsForValue().set(redisKey, "blacklisted", ttlSeconds, TimeUnit.SECONDS);
            }
            
            logger.info("Token added to blacklist: {}", tokenHash.substring(0, 8) + "...");
            
        } catch (Exception e) {
            logger.error("Failed to blacklist token: {}", e.getMessage());
            throw new RuntimeException("Failed to blacklist token", e);
        }
    }
    
    /**
     * 檢查 token 是否在黑名單中
     * 
     * @param token JWT token
     * @return true 如果在黑名單中，false 如果不在
     */
    public boolean isTokenBlacklisted(String token) {
        try {
            String tokenHash = jwtUtil.getTokenHash(token);
            
            // 先檢查 Redis 快取
            String redisKey = BLACKLIST_KEY_PREFIX + tokenHash;
            Boolean existsInRedis = redisTemplate.hasKey(redisKey);
            
            if (Boolean.TRUE.equals(existsInRedis)) {
                logger.debug("Token found in Redis blacklist: {}", tokenHash.substring(0, 8) + "...");
                return true;
            }
            
            // 如果 Redis 中沒有，檢查資料庫
            boolean existsInDb = tokenBlacklistRepository.existsByTokenHashAndExpiresAtAfter(
                tokenHash, LocalDateTime.now());
            
            if (existsInDb) {
                // 如果在資料庫中找到，同步到 Redis
                Date expiration = jwtUtil.extractExpiration(token);
                long ttlSeconds = (expiration.getTime() - System.currentTimeMillis()) / 1000;
                
                if (ttlSeconds > 0) {
                    redisTemplate.opsForValue().set(redisKey, "blacklisted", ttlSeconds, TimeUnit.SECONDS);
                }
                
                logger.debug("Token found in database blacklist and synced to Redis: {}", 
                    tokenHash.substring(0, 8) + "...");
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("Failed to check token blacklist status: {}", e.getMessage());
            // 在錯誤情況下，為了安全起見，假設 token 已被列入黑名單
            return true;
        }
    }
    
    /**
     * 清理過期的黑名單記錄
     * 這個方法應該定期執行（例如通過排程任務）
     */
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            LocalDateTime now = LocalDateTime.now();
            int deletedCount = tokenBlacklistRepository.deleteByExpiresAtBefore(now);
            
            if (deletedCount > 0) {
                logger.info("Cleaned up {} expired blacklist entries", deletedCount);
            }
            
        } catch (Exception e) {
            logger.error("Failed to cleanup expired tokens: {}", e.getMessage());
        }
    }
    
    /**
     * 獲取黑名單中的 token 數量
     * 
     * @return 黑名單中的 token 數量
     */
    public long getBlacklistCount() {
        try {
            return tokenBlacklistRepository.countByExpiresAtAfter(LocalDateTime.now());
        } catch (Exception e) {
            logger.error("Failed to get blacklist count: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * 檢查 Redis 連線狀態
     * 
     * @return true 如果 Redis 可用，false 如果不可用
     */
    private boolean isRedisAvailable() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            logger.warn("Redis is not available: {}", e.getMessage());
            return false;
        }
    }
}