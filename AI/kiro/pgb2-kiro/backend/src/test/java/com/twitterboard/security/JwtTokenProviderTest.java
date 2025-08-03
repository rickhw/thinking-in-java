package com.twitterboard.security;

import com.twitterboard.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {
    
    private JwtTokenProvider jwtTokenProvider;
    private JwtProperties jwtProperties;
    
    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret("mySecretKeyThatIsAtLeast256BitsLongForHS512Algorithm");
        jwtProperties.setExpiration(3600000L); // 1 hour
        jwtProperties.setRefreshExpiration(2592000000L); // 30 days
        
        jwtTokenProvider = new JwtTokenProvider(jwtProperties);
    }
    
    @Test
    void testGenerateAccessToken() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        String googleId = "google123";
        
        // When
        String token = jwtTokenProvider.generateAccessToken(userId, email, googleId);
        
        // Then
        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertTrue(jwtTokenProvider.isAccessToken(token));
        assertFalse(jwtTokenProvider.isRefreshToken(token));
    }
    
    @Test
    void testGenerateRefreshToken() {
        // Given
        Long userId = 1L;
        
        // When
        String token = jwtTokenProvider.generateRefreshToken(userId);
        
        // Then
        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertTrue(jwtTokenProvider.isRefreshToken(token));
        assertFalse(jwtTokenProvider.isAccessToken(token));
    }
    
    @Test
    void testGetUserIdFromToken() {
        // Given
        Long userId = 123L;
        String email = "test@example.com";
        String googleId = "google123";
        String token = jwtTokenProvider.generateAccessToken(userId, email, googleId);
        
        // When
        Long extractedUserId = jwtTokenProvider.getUserIdFromToken(token);
        
        // Then
        assertEquals(userId, extractedUserId);
    }
    
    @Test
    void testGetEmailFromToken() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        String googleId = "google123";
        String token = jwtTokenProvider.generateAccessToken(userId, email, googleId);
        
        // When
        String extractedEmail = jwtTokenProvider.getEmailFromToken(token);
        
        // Then
        assertEquals(email, extractedEmail);
    }
    
    @Test
    void testGetGoogleIdFromToken() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        String googleId = "google123";
        String token = jwtTokenProvider.generateAccessToken(userId, email, googleId);
        
        // When
        String extractedGoogleId = jwtTokenProvider.getGoogleIdFromToken(token);
        
        // Then
        assertEquals(googleId, extractedGoogleId);
    }
    
    @Test
    void testGetTokenTypeFromToken() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        String googleId = "google123";
        String accessToken = jwtTokenProvider.generateAccessToken(userId, email, googleId);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);
        
        // When & Then
        assertEquals("access", jwtTokenProvider.getTokenTypeFromToken(accessToken));
        assertEquals("refresh", jwtTokenProvider.getTokenTypeFromToken(refreshToken));
    }
    
    @Test
    void testGetExpirationDateFromToken() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        String googleId = "google123";
        Date beforeGeneration = new Date();
        String token = jwtTokenProvider.generateAccessToken(userId, email, googleId);
        Date afterGeneration = new Date(System.currentTimeMillis() + jwtProperties.getExpiration());
        
        // When
        Date expirationDate = jwtTokenProvider.getExpirationDateFromToken(token);
        
        // Then
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(beforeGeneration));
        assertTrue(expirationDate.before(afterGeneration));
    }
    
    @Test
    void testValidateValidToken() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        String googleId = "google123";
        String token = jwtTokenProvider.generateAccessToken(userId, email, googleId);
        
        // When & Then
        assertTrue(jwtTokenProvider.validateToken(token));
    }
    
    @Test
    void testValidateInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";
        
        // When & Then
        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }
    
    @Test
    void testValidateNullToken() {
        // When & Then
        assertFalse(jwtTokenProvider.validateToken(null));
    }
    
    @Test
    void testValidateEmptyToken() {
        // When & Then
        assertFalse(jwtTokenProvider.validateToken(""));
    }
    
    @Test
    void testIsTokenExpired() {
        // Given - Create token with very short expiration
        JwtProperties shortExpirationProperties = new JwtProperties();
        shortExpirationProperties.setSecret("mySecretKeyThatIsAtLeast256BitsLongForHS512Algorithm");
        shortExpirationProperties.setExpiration(1L); // 1 millisecond
        shortExpirationProperties.setRefreshExpiration(1L);
        
        JwtTokenProvider shortExpirationProvider = new JwtTokenProvider(shortExpirationProperties);
        String token = shortExpirationProvider.generateAccessToken(1L, "test@example.com", "google123");
        
        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // When & Then
        assertTrue(shortExpirationProvider.isTokenExpired(token));
    }
    
    @Test
    void testIsTokenNotExpired() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        String googleId = "google123";
        String token = jwtTokenProvider.generateAccessToken(userId, email, googleId);
        
        // When & Then
        assertFalse(jwtTokenProvider.isTokenExpired(token));
    }
    
    @Test
    void testGetRemainingTime() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        String googleId = "google123";
        String token = jwtTokenProvider.generateAccessToken(userId, email, googleId);
        
        // When
        long remainingTime = jwtTokenProvider.getRemainingTime(token);
        
        // Then
        assertTrue(remainingTime > 0);
        assertTrue(remainingTime <= jwtProperties.getExpiration());
    }
    
    @Test
    void testExtractTokenFromHeader() {
        // Given
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        String authHeader = "Bearer " + token;
        
        // When
        String extractedToken = jwtTokenProvider.extractTokenFromHeader(authHeader);
        
        // Then
        assertEquals(token, extractedToken);
    }
    
    @Test
    void testExtractTokenFromInvalidHeader() {
        // Given
        String invalidHeader = "InvalidHeader token";
        
        // When
        String extractedToken = jwtTokenProvider.extractTokenFromHeader(invalidHeader);
        
        // Then
        assertNull(extractedToken);
    }
    
    @Test
    void testExtractTokenFromNullHeader() {
        // When
        String extractedToken = jwtTokenProvider.extractTokenFromHeader(null);
        
        // Then
        assertNull(extractedToken);
    }
    
    @Test
    void testTokenTypeMethods() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        String googleId = "google123";
        String accessToken = jwtTokenProvider.generateAccessToken(userId, email, googleId);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);
        
        // When & Then
        assertTrue(jwtTokenProvider.isAccessToken(accessToken));
        assertFalse(jwtTokenProvider.isRefreshToken(accessToken));
        
        assertTrue(jwtTokenProvider.isRefreshToken(refreshToken));
        assertFalse(jwtTokenProvider.isAccessToken(refreshToken));
    }
    
    @Test
    void testTokenTypeMethodsWithInvalidToken() {
        // Given
        String invalidToken = "invalid.token";
        
        // When & Then
        assertFalse(jwtTokenProvider.isAccessToken(invalidToken));
        assertFalse(jwtTokenProvider.isRefreshToken(invalidToken));
    }
}