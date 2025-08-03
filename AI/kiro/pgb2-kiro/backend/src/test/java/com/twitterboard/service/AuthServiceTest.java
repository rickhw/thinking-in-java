package com.twitterboard.service;

import com.twitterboard.config.JwtProperties;
import com.twitterboard.dto.AuthResponse;
import com.twitterboard.dto.GoogleUserInfo;
import com.twitterboard.dto.UserInfo;
import com.twitterboard.entity.User;
import com.twitterboard.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private GoogleOAuthService googleOAuthService;
    
    @Mock
    private UserService userService;
    
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    
    @Mock
    private JwtProperties jwtProperties;
    
    @InjectMocks
    private AuthService authService;
    
    private GoogleUserInfo mockGoogleUserInfo;
    private User mockUser;
    private String mockAuthCode;
    private String mockAccessToken;
    private String mockRefreshToken;
    
    @BeforeEach
    void setUp() {
        mockAuthCode = "mock_auth_code_123";
        mockAccessToken = "mock_access_token_123";
        mockRefreshToken = "mock_refresh_token_123";
        
        mockGoogleUserInfo = new GoogleUserInfo();
        mockGoogleUserInfo.setId("google_123");
        mockGoogleUserInfo.setEmail("test@example.com");
        mockGoogleUserInfo.setName("Test User");
        mockGoogleUserInfo.setPicture("https://example.com/avatar.jpg");
        mockGoogleUserInfo.setVerifiedEmail(true);
        
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setGoogleId("google_123");
        mockUser.setEmail("test@example.com");
        mockUser.setName("Test User");
        mockUser.setAvatarUrl("https://example.com/avatar.jpg");
        mockUser.setCreatedAt(LocalDateTime.now());
        mockUser.setUpdatedAt(LocalDateTime.now());
    }
    
    @Test
    void authenticateWithGoogle_Success() throws Exception {
        // Arrange
        when(googleOAuthService.getUserInfo(mockAuthCode)).thenReturn(mockGoogleUserInfo);
        when(userService.findOrCreateUser(mockGoogleUserInfo)).thenReturn(mockUser);
        when(jwtTokenProvider.generateAccessToken(mockUser.getId(), mockUser.getEmail(), mockUser.getGoogleId()))
                .thenReturn(mockAccessToken);
        when(jwtTokenProvider.generateRefreshToken(mockUser.getId())).thenReturn(mockRefreshToken);
        when(jwtProperties.getExpiration()).thenReturn(3600000L); // 1 hour in milliseconds
        
        // Act
        AuthResponse response = authService.authenticateWithGoogle(mockAuthCode);
        
        // Assert
        assertNotNull(response);
        assertEquals(mockAccessToken, response.getAccessToken());
        assertEquals(mockRefreshToken, response.getRefreshToken());
        assertEquals(3600L, response.getExpiresIn()); // 1 hour in seconds
        
        UserInfo userInfo = response.getUser();
        assertNotNull(userInfo);
        assertEquals(mockUser.getId(), userInfo.getId());
        assertEquals(mockUser.getEmail(), userInfo.getEmail());
        assertEquals(mockUser.getName(), userInfo.getName());
        
        // Verify interactions
        verify(googleOAuthService).getUserInfo(mockAuthCode);
        verify(userService).findOrCreateUser(mockGoogleUserInfo);
        verify(jwtTokenProvider).generateAccessToken(mockUser.getId(), mockUser.getEmail(), mockUser.getGoogleId());
        verify(jwtTokenProvider).generateRefreshToken(mockUser.getId());
    }
    
    @Test
    void authenticateWithGoogle_GoogleOAuthServiceFails() throws Exception {
        // Arrange
        when(googleOAuthService.getUserInfo(mockAuthCode))
                .thenThrow(new Exception("Google OAuth failed"));
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.authenticateWithGoogle(mockAuthCode);
        });
        
        assertTrue(exception.getMessage().contains("Authentication failed"));
        
        // Verify interactions
        verify(googleOAuthService).getUserInfo(mockAuthCode);
        verify(userService, never()).findOrCreateUser(any());
        verify(jwtTokenProvider, never()).generateAccessToken(anyLong(), anyString(), anyString());
    }
    
    @Test
    void authenticateWithGoogle_UserServiceFails() throws Exception {
        // Arrange
        when(googleOAuthService.getUserInfo(mockAuthCode)).thenReturn(mockGoogleUserInfo);
        when(userService.findOrCreateUser(mockGoogleUserInfo))
                .thenThrow(new RuntimeException("User creation failed"));
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.authenticateWithGoogle(mockAuthCode);
        });
        
        assertTrue(exception.getMessage().contains("Authentication failed"));
        
        // Verify interactions
        verify(googleOAuthService).getUserInfo(mockAuthCode);
        verify(userService).findOrCreateUser(mockGoogleUserInfo);
        verify(jwtTokenProvider, never()).generateAccessToken(anyLong(), anyString(), anyString());
    }
    
    @Test
    void refreshToken_Success() throws Exception {
        // Arrange
        when(jwtTokenProvider.validateToken(mockRefreshToken)).thenReturn(true);
        when(jwtTokenProvider.isRefreshToken(mockRefreshToken)).thenReturn(true);
        when(jwtTokenProvider.isTokenExpired(mockRefreshToken)).thenReturn(false);
        when(jwtTokenProvider.getUserIdFromToken(mockRefreshToken)).thenReturn(mockUser.getId());
        when(userService.getUserById(mockUser.getId())).thenReturn(mockUser);
        when(jwtTokenProvider.generateAccessToken(mockUser.getId(), mockUser.getEmail(), mockUser.getGoogleId()))
                .thenReturn(mockAccessToken);
        when(jwtTokenProvider.generateRefreshToken(mockUser.getId())).thenReturn(mockRefreshToken);
        when(jwtProperties.getExpiration()).thenReturn(3600000L);
        
        // Act
        AuthResponse response = authService.refreshToken(mockRefreshToken);
        
        // Assert
        assertNotNull(response);
        assertEquals(mockAccessToken, response.getAccessToken());
        assertEquals(mockRefreshToken, response.getRefreshToken());
        assertEquals(3600L, response.getExpiresIn());
        
        // Verify interactions
        verify(jwtTokenProvider).validateToken(mockRefreshToken);
        verify(jwtTokenProvider).isRefreshToken(mockRefreshToken);
        verify(jwtTokenProvider).isTokenExpired(mockRefreshToken);
        verify(jwtTokenProvider).getUserIdFromToken(mockRefreshToken);
        verify(userService).getUserById(mockUser.getId());
    }
    
    @Test
    void refreshToken_InvalidToken() {
        // Arrange
        when(jwtTokenProvider.validateToken(mockRefreshToken)).thenReturn(false);
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.refreshToken(mockRefreshToken);
        });
        
        assertTrue(exception.getMessage().contains("Token refresh failed"));
        
        // Verify interactions
        verify(jwtTokenProvider).validateToken(mockRefreshToken);
        verify(jwtTokenProvider, never()).isRefreshToken(anyString());
    }
    
    @Test
    void refreshToken_NotRefreshToken() {
        // Arrange
        when(jwtTokenProvider.validateToken(mockRefreshToken)).thenReturn(true);
        when(jwtTokenProvider.isRefreshToken(mockRefreshToken)).thenReturn(false);
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.refreshToken(mockRefreshToken);
        });
        
        assertTrue(exception.getMessage().contains("Token refresh failed"));
        
        // Verify interactions
        verify(jwtTokenProvider).validateToken(mockRefreshToken);
        verify(jwtTokenProvider).isRefreshToken(mockRefreshToken);
        verify(jwtTokenProvider, never()).isTokenExpired(anyString());
    }
    
    @Test
    void refreshToken_ExpiredToken() {
        // Arrange
        when(jwtTokenProvider.validateToken(mockRefreshToken)).thenReturn(true);
        when(jwtTokenProvider.isRefreshToken(mockRefreshToken)).thenReturn(true);
        when(jwtTokenProvider.isTokenExpired(mockRefreshToken)).thenReturn(true);
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.refreshToken(mockRefreshToken);
        });
        
        assertTrue(exception.getMessage().contains("Token refresh failed"));
        
        // Verify interactions
        verify(jwtTokenProvider).validateToken(mockRefreshToken);
        verify(jwtTokenProvider).isRefreshToken(mockRefreshToken);
        verify(jwtTokenProvider).isTokenExpired(mockRefreshToken);
        verify(jwtTokenProvider, never()).getUserIdFromToken(anyString());
    }
    
    @Test
    void refreshToken_UserNotFound() {
        // Arrange
        when(jwtTokenProvider.validateToken(mockRefreshToken)).thenReturn(true);
        when(jwtTokenProvider.isRefreshToken(mockRefreshToken)).thenReturn(true);
        when(jwtTokenProvider.isTokenExpired(mockRefreshToken)).thenReturn(false);
        when(jwtTokenProvider.getUserIdFromToken(mockRefreshToken)).thenReturn(mockUser.getId());
        when(userService.getUserById(mockUser.getId())).thenReturn(null);
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.refreshToken(mockRefreshToken);
        });
        
        assertTrue(exception.getMessage().contains("Token refresh failed"));
        
        // Verify interactions
        verify(userService).getUserById(mockUser.getId());
    }
    
    @Test
    void validateToken_Success() throws Exception {
        // Arrange
        when(jwtTokenProvider.validateToken(mockAccessToken)).thenReturn(true);
        when(jwtTokenProvider.isAccessToken(mockAccessToken)).thenReturn(true);
        when(jwtTokenProvider.isTokenExpired(mockAccessToken)).thenReturn(false);
        when(jwtTokenProvider.getUserIdFromToken(mockAccessToken)).thenReturn(mockUser.getId());
        when(userService.getUserById(mockUser.getId())).thenReturn(mockUser);
        
        // Act
        UserInfo userInfo = authService.validateToken(mockAccessToken);
        
        // Assert
        assertNotNull(userInfo);
        assertEquals(mockUser.getId(), userInfo.getId());
        assertEquals(mockUser.getEmail(), userInfo.getEmail());
        assertEquals(mockUser.getName(), userInfo.getName());
        
        // Verify interactions
        verify(jwtTokenProvider).validateToken(mockAccessToken);
        verify(jwtTokenProvider).isAccessToken(mockAccessToken);
        verify(jwtTokenProvider).isTokenExpired(mockAccessToken);
        verify(jwtTokenProvider).getUserIdFromToken(mockAccessToken);
        verify(userService).getUserById(mockUser.getId());
    }
    
    @Test
    void validateToken_InvalidToken() {
        // Arrange
        when(jwtTokenProvider.validateToken(mockAccessToken)).thenReturn(false);
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.validateToken(mockAccessToken);
        });
        
        assertTrue(exception.getMessage().contains("Token validation failed"));
        
        // Verify interactions
        verify(jwtTokenProvider).validateToken(mockAccessToken);
        verify(jwtTokenProvider, never()).isAccessToken(anyString());
    }
    
    @Test
    void validateToken_NotAccessToken() {
        // Arrange
        when(jwtTokenProvider.validateToken(mockAccessToken)).thenReturn(true);
        when(jwtTokenProvider.isAccessToken(mockAccessToken)).thenReturn(false);
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.validateToken(mockAccessToken);
        });
        
        assertTrue(exception.getMessage().contains("Token validation failed"));
        
        // Verify interactions
        verify(jwtTokenProvider).validateToken(mockAccessToken);
        verify(jwtTokenProvider).isAccessToken(mockAccessToken);
        verify(jwtTokenProvider, never()).isTokenExpired(anyString());
    }
    
    @Test
    void getTokenRemainingTime_Success() {
        // Arrange
        long remainingTimeMs = 1800000L; // 30 minutes in milliseconds
        when(jwtTokenProvider.getRemainingTime(mockAccessToken)).thenReturn(remainingTimeMs);
        
        // Act
        long remainingTimeSeconds = authService.getTokenRemainingTime(mockAccessToken);
        
        // Assert
        assertEquals(1800L, remainingTimeSeconds); // 30 minutes in seconds
        
        // Verify interactions
        verify(jwtTokenProvider).getRemainingTime(mockAccessToken);
    }
    
    @Test
    void getTokenRemainingTime_Exception() {
        // Arrange
        when(jwtTokenProvider.getRemainingTime(mockAccessToken))
                .thenThrow(new RuntimeException("Token error"));
        
        // Act
        long remainingTimeSeconds = authService.getTokenRemainingTime(mockAccessToken);
        
        // Assert
        assertEquals(0L, remainingTimeSeconds);
        
        // Verify interactions
        verify(jwtTokenProvider).getRemainingTime(mockAccessToken);
    }
    
    @Test
    void isTokenExpiringSoon_True() {
        // Arrange
        long remainingTimeMs = 240000L; // 4 minutes in milliseconds (less than 5 minutes)
        when(jwtTokenProvider.getRemainingTime(mockAccessToken)).thenReturn(remainingTimeMs);
        
        // Act
        boolean isExpiringSoon = authService.isTokenExpiringSoon(mockAccessToken);
        
        // Assert
        assertTrue(isExpiringSoon);
        
        // Verify interactions
        verify(jwtTokenProvider).getRemainingTime(mockAccessToken);
    }
    
    @Test
    void isTokenExpiringSoon_False() {
        // Arrange
        long remainingTimeMs = 600000L; // 10 minutes in milliseconds (more than 5 minutes)
        when(jwtTokenProvider.getRemainingTime(mockAccessToken)).thenReturn(remainingTimeMs);
        
        // Act
        boolean isExpiringSoon = authService.isTokenExpiringSoon(mockAccessToken);
        
        // Assert
        assertFalse(isExpiringSoon);
        
        // Verify interactions
        verify(jwtTokenProvider).getRemainingTime(mockAccessToken);
    }
    
    @Test
    void isTokenExpiringSoon_Exception() {
        // Arrange
        when(jwtTokenProvider.getRemainingTime(mockAccessToken))
                .thenThrow(new RuntimeException("Token error"));
        
        // Act
        boolean isExpiringSoon = authService.isTokenExpiringSoon(mockAccessToken);
        
        // Assert
        assertTrue(isExpiringSoon); // Should assume expired if can't check
        
        // Verify interactions
        verify(jwtTokenProvider).getRemainingTime(mockAccessToken);
    }
}