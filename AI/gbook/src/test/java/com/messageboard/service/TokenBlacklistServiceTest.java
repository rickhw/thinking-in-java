package com.messageboard.service;

import com.messageboard.entity.TokenBlacklist;
import com.messageboard.repository.TokenBlacklistRepository;
import com.messageboard.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * TokenBlacklistService 單元測試
 */
@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {
    
    @Mock
    private TokenBlacklistRepository tokenBlacklistRepository;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    
    @Mock
    private ValueOperations<String, String> valueOperations;
    
    private TokenBlacklistService tokenBlacklistService;
    
    private static final String TEST_TOKEN = "test.jwt.token";
    private static final String TEST_TOKEN_HASH = "testhash123456789";
    private static final Date TEST_EXPIRATION = new Date(System.currentTimeMillis() + 900000); // 15 minutes from now
    
    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        tokenBlacklistService = new TokenBlacklistService(tokenBlacklistRepository, jwtUtil, redisTemplate);
    }
    
    @Test
    void shouldBlacklistToken() {
        // Given
        when(jwtUtil.getTokenHash(TEST_TOKEN)).thenReturn(TEST_TOKEN_HASH);
        when(jwtUtil.extractExpiration(TEST_TOKEN)).thenReturn(TEST_EXPIRATION);
        
        // When
        tokenBlacklistService.blacklistToken(TEST_TOKEN);
        
        // Then
        ArgumentCaptor<TokenBlacklist> captor = ArgumentCaptor.forClass(TokenBlacklist.class);
        verify(tokenBlacklistRepository).save(captor.capture());
        
        TokenBlacklist savedToken = captor.getValue();
        assertThat(savedToken.getTokenHash()).isEqualTo(TEST_TOKEN_HASH);
        assertThat(savedToken.getExpiresAt()).isNotNull();
        assertThat(savedToken.getCreatedAt()).isNotNull();
        
        // Verify Redis cache
        String expectedRedisKey = "token:blacklist:" + TEST_TOKEN_HASH;
        verify(valueOperations).set(eq(expectedRedisKey), eq("blacklisted"), anyLong(), eq(TimeUnit.SECONDS));
    }
    
    @Test
    void shouldNotCacheExpiredTokenInRedis() {
        // Given - Token that expires in the past
        Date pastExpiration = new Date(System.currentTimeMillis() - 1000);
        when(jwtUtil.getTokenHash(TEST_TOKEN)).thenReturn(TEST_TOKEN_HASH);
        when(jwtUtil.extractExpiration(TEST_TOKEN)).thenReturn(pastExpiration);
        
        // When
        tokenBlacklistService.blacklistToken(TEST_TOKEN);
        
        // Then
        verify(tokenBlacklistRepository).save(any(TokenBlacklist.class));
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }
    
    @Test
    void shouldHandleBlacklistTokenError() {
        // Given
        when(jwtUtil.getTokenHash(TEST_TOKEN)).thenThrow(new RuntimeException("Hash generation failed"));
        
        // When & Then
        assertThatThrownBy(() -> tokenBlacklistService.blacklistToken(TEST_TOKEN))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to blacklist token");
    }
    
    @Test
    void shouldReturnTrueWhenTokenFoundInRedis() {
        // Given
        when(jwtUtil.getTokenHash(TEST_TOKEN)).thenReturn(TEST_TOKEN_HASH);
        when(redisTemplate.hasKey("token:blacklist:" + TEST_TOKEN_HASH)).thenReturn(true);
        
        // When
        boolean isBlacklisted = tokenBlacklistService.isTokenBlacklisted(TEST_TOKEN);
        
        // Then
        assertThat(isBlacklisted).isTrue();
        verify(tokenBlacklistRepository, never()).existsByTokenHashAndExpiresAtAfter(anyString(), any(LocalDateTime.class));
    }
    
    @Test
    void shouldReturnTrueWhenTokenFoundInDatabase() {
        // Given
        when(jwtUtil.getTokenHash(TEST_TOKEN)).thenReturn(TEST_TOKEN_HASH);
        when(jwtUtil.extractExpiration(TEST_TOKEN)).thenReturn(TEST_EXPIRATION);
        when(redisTemplate.hasKey("token:blacklist:" + TEST_TOKEN_HASH)).thenReturn(false);
        when(tokenBlacklistRepository.existsByTokenHashAndExpiresAtAfter(eq(TEST_TOKEN_HASH), any(LocalDateTime.class)))
                .thenReturn(true);
        
        // When
        boolean isBlacklisted = tokenBlacklistService.isTokenBlacklisted(TEST_TOKEN);
        
        // Then
        assertThat(isBlacklisted).isTrue();
        
        // Verify that token is synced to Redis
        String expectedRedisKey = "token:blacklist:" + TEST_TOKEN_HASH;
        verify(valueOperations).set(eq(expectedRedisKey), eq("blacklisted"), anyLong(), eq(TimeUnit.SECONDS));
    }
    
    @Test
    void shouldReturnFalseWhenTokenNotFoundAnywhere() {
        // Given
        when(jwtUtil.getTokenHash(TEST_TOKEN)).thenReturn(TEST_TOKEN_HASH);
        when(redisTemplate.hasKey("token:blacklist:" + TEST_TOKEN_HASH)).thenReturn(false);
        when(tokenBlacklistRepository.existsByTokenHashAndExpiresAtAfter(eq(TEST_TOKEN_HASH), any(LocalDateTime.class)))
                .thenReturn(false);
        
        // When
        boolean isBlacklisted = tokenBlacklistService.isTokenBlacklisted(TEST_TOKEN);
        
        // Then
        assertThat(isBlacklisted).isFalse();
    }
    
    @Test
    void shouldReturnTrueOnErrorForSafety() {
        // Given
        when(jwtUtil.getTokenHash(TEST_TOKEN)).thenThrow(new RuntimeException("Hash generation failed"));
        
        // When
        boolean isBlacklisted = tokenBlacklistService.isTokenBlacklisted(TEST_TOKEN);
        
        // Then
        assertThat(isBlacklisted).isTrue(); // Should return true for safety
    }
    
    @Test
    void shouldNotSyncExpiredTokenToRedis() {
        // Given - Token that expires in the past
        Date pastExpiration = new Date(System.currentTimeMillis() - 1000);
        when(jwtUtil.getTokenHash(TEST_TOKEN)).thenReturn(TEST_TOKEN_HASH);
        when(jwtUtil.extractExpiration(TEST_TOKEN)).thenReturn(pastExpiration);
        when(redisTemplate.hasKey("token:blacklist:" + TEST_TOKEN_HASH)).thenReturn(false);
        when(tokenBlacklistRepository.existsByTokenHashAndExpiresAtAfter(eq(TEST_TOKEN_HASH), any(LocalDateTime.class)))
                .thenReturn(true);
        
        // When
        boolean isBlacklisted = tokenBlacklistService.isTokenBlacklisted(TEST_TOKEN);
        
        // Then
        assertThat(isBlacklisted).isTrue();
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }
    
    @Test
    void shouldCleanupExpiredTokens() {
        // Given
        int deletedCount = 5;
        when(tokenBlacklistRepository.deleteByExpiresAtBefore(any(LocalDateTime.class)))
                .thenReturn(deletedCount);
        
        // When
        tokenBlacklistService.cleanupExpiredTokens();
        
        // Then
        verify(tokenBlacklistRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }
    
    @Test
    void shouldHandleCleanupError() {
        // Given
        when(tokenBlacklistRepository.deleteByExpiresAtBefore(any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Database error"));
        
        // When & Then - Should not throw exception
        assertThatCode(() -> tokenBlacklistService.cleanupExpiredTokens())
                .doesNotThrowAnyException();
    }
    
    @Test
    void shouldGetBlacklistCount() {
        // Given
        long expectedCount = 10L;
        when(tokenBlacklistRepository.countByExpiresAtAfter(any(LocalDateTime.class)))
                .thenReturn(expectedCount);
        
        // When
        long actualCount = tokenBlacklistService.getBlacklistCount();
        
        // Then
        assertThat(actualCount).isEqualTo(expectedCount);
    }
    
    @Test
    void shouldReturnZeroOnCountError() {
        // Given
        when(tokenBlacklistRepository.countByExpiresAtAfter(any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Database error"));
        
        // When
        long count = tokenBlacklistService.getBlacklistCount();
        
        // Then
        assertThat(count).isZero();
    }
}