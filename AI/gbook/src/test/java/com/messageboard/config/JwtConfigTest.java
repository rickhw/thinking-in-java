package com.messageboard.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JwtConfig 單元測試
 */
class JwtConfigTest {
    
    @Test
    void shouldSetAndGetSecret() {
        // Given
        JwtConfig config = new JwtConfig();
        String expectedSecret = "testSecret";
        
        // When
        config.setSecret(expectedSecret);
        
        // Then
        assertThat(config.getSecret()).isEqualTo(expectedSecret);
    }
    
    @Test
    void shouldSetAndGetAccessTokenExpiration() {
        // Given
        JwtConfig config = new JwtConfig();
        long expectedExpiration = 900000L;
        
        // When
        config.setAccessTokenExpiration(expectedExpiration);
        
        // Then
        assertThat(config.getAccessTokenExpiration()).isEqualTo(expectedExpiration);
    }
    
    @Test
    void shouldSetAndGetRefreshTokenExpiration() {
        // Given
        JwtConfig config = new JwtConfig();
        long expectedExpiration = 604800000L;
        
        // When
        config.setRefreshTokenExpiration(expectedExpiration);
        
        // Then
        assertThat(config.getRefreshTokenExpiration()).isEqualTo(expectedExpiration);
    }
}