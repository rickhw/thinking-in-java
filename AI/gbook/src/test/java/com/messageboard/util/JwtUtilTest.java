package com.messageboard.util;

import com.messageboard.config.JwtConfig;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

/**
 * JwtUtil 單元測試
 */
@ExtendWith(MockitoExtension.class)
class JwtUtilTest {
    
    @Mock
    private JwtConfig jwtConfig;
    
    private JwtUtil jwtUtil;
    
    private static final String TEST_SECRET = "testSecretKeyForJwtTokenGenerationAndValidationInUnitTestsThisNeedsToBeAtLeast256BitsLong";
    private static final long ACCESS_TOKEN_EXPIRATION = 900000L; // 15 minutes
    private static final long REFRESH_TOKEN_EXPIRATION = 604800000L; // 7 days
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_USERNAME = "testuser";
    
    @BeforeEach
    void setUp() {
        lenient().when(jwtConfig.getSecret()).thenReturn(TEST_SECRET);
        lenient().when(jwtConfig.getAccessTokenExpiration()).thenReturn(ACCESS_TOKEN_EXPIRATION);
        lenient().when(jwtConfig.getRefreshTokenExpiration()).thenReturn(REFRESH_TOKEN_EXPIRATION);
        
        jwtUtil = new JwtUtil(jwtConfig);
    }
    
    @Test
    void shouldGenerateAccessToken() {
        // When
        String token = jwtUtil.generateAccessToken(TEST_USER_ID, TEST_USERNAME);
        
        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }
    
    @Test
    void shouldGenerateRefreshToken() {
        // When
        String token = jwtUtil.generateRefreshToken(TEST_USER_ID, TEST_USERNAME);
        
        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }
    
    @Test
    void shouldExtractUsernameFromToken() {
        // Given
        String token = jwtUtil.generateAccessToken(TEST_USER_ID, TEST_USERNAME);
        
        // When
        String extractedUsername = jwtUtil.extractUsername(token);
        
        // Then
        assertThat(extractedUsername).isEqualTo(TEST_USERNAME);
    }
    
    @Test
    void shouldExtractUserIdFromToken() {
        // Given
        String token = jwtUtil.generateAccessToken(TEST_USER_ID, TEST_USERNAME);
        
        // When
        Long extractedUserId = jwtUtil.extractUserId(token);
        
        // Then
        assertThat(extractedUserId).isEqualTo(TEST_USER_ID);
    }
    
    @Test
    void shouldExtractExpirationFromToken() {
        // Given
        Date beforeGeneration = new Date();
        String token = jwtUtil.generateAccessToken(TEST_USER_ID, TEST_USERNAME);
        Date afterGeneration = new Date();
        
        // When
        Date expiration = jwtUtil.extractExpiration(token);
        
        // Then
        assertThat(expiration).isNotNull();
        assertThat(expiration).isAfter(beforeGeneration);
        assertThat(expiration).isAfter(afterGeneration);
    }
    
    @Test
    void shouldExtractTokenTypeFromAccessToken() {
        // Given
        String token = jwtUtil.generateAccessToken(TEST_USER_ID, TEST_USERNAME);
        
        // When
        String tokenType = jwtUtil.extractTokenType(token);
        
        // Then
        assertThat(tokenType).isEqualTo(JwtConstants.ACCESS_TOKEN_TYPE);
    }
    
    @Test
    void shouldExtractTokenTypeFromRefreshToken() {
        // Given
        String token = jwtUtil.generateRefreshToken(TEST_USER_ID, TEST_USERNAME);
        
        // When
        String tokenType = jwtUtil.extractTokenType(token);
        
        // Then
        assertThat(tokenType).isEqualTo(JwtConstants.REFRESH_TOKEN_TYPE);
    }
    
    @Test
    void shouldIdentifyAccessToken() {
        // Given
        String accessToken = jwtUtil.generateAccessToken(TEST_USER_ID, TEST_USERNAME);
        String refreshToken = jwtUtil.generateRefreshToken(TEST_USER_ID, TEST_USERNAME);
        
        // When & Then
        assertThat(jwtUtil.isAccessToken(accessToken)).isTrue();
        assertThat(jwtUtil.isAccessToken(refreshToken)).isFalse();
    }
    
    @Test
    void shouldIdentifyRefreshToken() {
        // Given
        String accessToken = jwtUtil.generateAccessToken(TEST_USER_ID, TEST_USERNAME);
        String refreshToken = jwtUtil.generateRefreshToken(TEST_USER_ID, TEST_USERNAME);
        
        // When & Then
        assertThat(jwtUtil.isRefreshToken(refreshToken)).isTrue();
        assertThat(jwtUtil.isRefreshToken(accessToken)).isFalse();
    }
    
    @Test
    void shouldValidateValidToken() {
        // Given
        String token = jwtUtil.generateAccessToken(TEST_USER_ID, TEST_USERNAME);
        
        // When
        boolean isValid = jwtUtil.validateToken(token, TEST_USERNAME);
        
        // Then
        assertThat(isValid).isTrue();
    }
    
    @Test
    void shouldValidateValidTokenWithoutUsername() {
        // Given
        String token = jwtUtil.generateAccessToken(TEST_USER_ID, TEST_USERNAME);
        
        // When
        boolean isValid = jwtUtil.validateToken(token);
        
        // Then
        assertThat(isValid).isTrue();
    }
    
    @Test
    void shouldRejectTokenWithWrongUsername() {
        // Given
        String token = jwtUtil.generateAccessToken(TEST_USER_ID, TEST_USERNAME);
        
        // When
        boolean isValid = jwtUtil.validateToken(token, "wronguser");
        
        // Then
        assertThat(isValid).isFalse();
    }
    
    @Test
    void shouldRejectExpiredToken() {
        // Given - Create a config with very short expiration
        when(jwtConfig.getAccessTokenExpiration()).thenReturn(1L); // 1 millisecond
        JwtUtil shortExpirationJwtUtil = new JwtUtil(jwtConfig);
        String token = shortExpirationJwtUtil.generateAccessToken(TEST_USER_ID, TEST_USERNAME);
        
        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // When
        boolean isExpired = jwtUtil.isTokenExpired(token);
        boolean isValid = jwtUtil.validateToken(token, TEST_USERNAME);
        
        // Then
        assertThat(isExpired).isTrue();
        assertThat(isValid).isFalse();
    }
    
    @Test
    void shouldRejectMalformedToken() {
        // Given
        String malformedToken = "invalid.jwt.token";
        
        // When & Then
        assertThatThrownBy(() -> jwtUtil.extractUsername(malformedToken))
                .isInstanceOf(JwtException.class);
        
        assertThat(jwtUtil.validateToken(malformedToken, TEST_USERNAME)).isFalse();
        assertThat(jwtUtil.validateToken(malformedToken)).isFalse();
        assertThat(jwtUtil.isTokenExpired(malformedToken)).isTrue();
    }
    
    @Test
    void shouldRejectNullToken() {
        // When & Then
        assertThatThrownBy(() -> jwtUtil.extractUsername(null))
                .isInstanceOf(Exception.class);
        
        assertThat(jwtUtil.validateToken(null, TEST_USERNAME)).isFalse();
        assertThat(jwtUtil.validateToken(null)).isFalse();
        assertThat(jwtUtil.isTokenExpired(null)).isTrue();
    }
    
    @Test
    void shouldRejectEmptyToken() {
        // When & Then
        assertThatThrownBy(() -> jwtUtil.extractUsername(""))
                .isInstanceOf(JwtException.class);
        
        assertThat(jwtUtil.validateToken("", TEST_USERNAME)).isFalse();
        assertThat(jwtUtil.validateToken("")).isFalse();
        assertThat(jwtUtil.isTokenExpired("")).isTrue();
    }
    
    @Test
    void shouldGenerateTokenHash() {
        // Given
        String token = jwtUtil.generateAccessToken(TEST_USER_ID, TEST_USERNAME);
        
        // When
        String hash1 = jwtUtil.getTokenHash(token);
        String hash2 = jwtUtil.getTokenHash(token);
        
        // Then
        assertThat(hash1).isNotNull();
        assertThat(hash1).isNotEmpty();
        assertThat(hash1).hasSize(64); // SHA-256 produces 64 character hex string
        assertThat(hash1).isEqualTo(hash2); // Same token should produce same hash
    }
    
    @Test
    void shouldGenerateDifferentHashesForDifferentTokens() {
        // Given
        String token1 = jwtUtil.generateAccessToken(TEST_USER_ID, TEST_USERNAME);
        String token2 = jwtUtil.generateAccessToken(TEST_USER_ID + 1, TEST_USERNAME);
        
        // When
        String hash1 = jwtUtil.getTokenHash(token1);
        String hash2 = jwtUtil.getTokenHash(token2);
        
        // Then
        assertThat(hash1).isNotEqualTo(hash2);
    }
    
    @Test
    void shouldHandleTokenHashGenerationError() {
        // When & Then
        assertThatThrownBy(() -> jwtUtil.getTokenHash(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to generate token hash");
    }
    
    @Test
    void shouldReturnFalseForInvalidTokenTypeCheck() {
        // Given
        String invalidToken = "invalid.token";
        
        // When & Then
        assertThat(jwtUtil.isAccessToken(invalidToken)).isFalse();
        assertThat(jwtUtil.isRefreshToken(invalidToken)).isFalse();
    }
}