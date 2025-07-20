package com.gtcafe.pgb.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.gtcafe.pgb.service.TokenBlacklistService;
import com.gtcafe.pgb.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT 認證過濾器單元測試
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil, tokenBlacklistService);
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternal_ValidToken_SetsAuthentication() throws Exception {
        // Arrange
        String token = "valid-jwt-token";
        String username = "testuser";
        Long userId = 1L;

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.isAccessToken(token)).thenReturn(true);
        when(tokenBlacklistService.isTokenBlacklisted(token)).thenReturn(false);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(jwtUtil.extractUserId(token)).thenReturn(userId);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(username, authentication.getName());
        assertTrue(authentication.getDetails() instanceof JwtAuthenticationDetails);
        assertEquals(userId, ((JwtAuthenticationDetails) authentication.getDetails()).getUserId());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_NoToken_NoAuthentication() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).validateToken(anyString());
    }

    @Test
    void testDoFilterInternal_InvalidToken_NoAuthentication() throws Exception {
        // Arrange
        String token = "invalid-jwt-token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).validateToken(token);
        verify(jwtUtil, never()).isAccessToken(anyString());
    }

    @Test
    void testDoFilterInternal_RefreshToken_NoAuthentication() throws Exception {
        // Arrange
        String token = "refresh-jwt-token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.isAccessToken(token)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).validateToken(token);
        verify(jwtUtil).isAccessToken(token);
        verify(tokenBlacklistService, never()).isTokenBlacklisted(anyString());
    }

    @Test
    void testDoFilterInternal_BlacklistedToken_NoAuthentication() throws Exception {
        // Arrange
        String token = "blacklisted-jwt-token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.isAccessToken(token)).thenReturn(true);
        when(tokenBlacklistService.isTokenBlacklisted(token)).thenReturn(true);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).validateToken(token);
        verify(jwtUtil).isAccessToken(token);
        verify(tokenBlacklistService).isTokenBlacklisted(token);
        verify(jwtUtil, never()).extractUsername(anyString());
    }

    @Test
    void testDoFilterInternal_ExistingAuthentication_SkipsValidation() throws Exception {
        // Arrange
        String token = "valid-jwt-token";

        // 設定現有的認證
        Authentication existingAuth = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(existingAuth, authentication);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).validateToken(anyString());
    }

    @Test
    void testDoFilterInternal_TokenValidationException_ClearsContext() throws Exception {
        // Arrange
        String token = "problematic-jwt-token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validateToken(token)).thenThrow(new RuntimeException("Token validation error"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testShouldNotFilter_PublicEndpoints_ReturnsTrue() {
        // Test login endpoint
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        assertTrue(jwtAuthenticationFilter.shouldNotFilter(request));

        // Test refresh endpoint
        when(request.getRequestURI()).thenReturn("/api/auth/refresh");
        assertTrue(jwtAuthenticationFilter.shouldNotFilter(request));

        // Test health endpoint
        when(request.getRequestURI()).thenReturn("/actuator/health");
        assertTrue(jwtAuthenticationFilter.shouldNotFilter(request));

        // Test API docs
        when(request.getRequestURI()).thenReturn("/v3/api-docs/swagger-config");
        assertTrue(jwtAuthenticationFilter.shouldNotFilter(request));

        // Test Swagger UI
        when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");
        assertTrue(jwtAuthenticationFilter.shouldNotFilter(request));

        // Test H2 console
        when(request.getRequestURI()).thenReturn("/h2-console/login.jsp");
        assertTrue(jwtAuthenticationFilter.shouldNotFilter(request));
    }

    @Test
    void testShouldNotFilter_ProtectedEndpoints_ReturnsFalse() {
        // Test protected API endpoints
        when(request.getRequestURI()).thenReturn("/api/users");
        assertFalse(jwtAuthenticationFilter.shouldNotFilter(request));

        when(request.getRequestURI()).thenReturn("/api/messages");
        assertFalse(jwtAuthenticationFilter.shouldNotFilter(request));

        when(request.getRequestURI()).thenReturn("/api/boards/1");
        assertFalse(jwtAuthenticationFilter.shouldNotFilter(request));
    }

    @Test
    void testExtractTokenFromRequest_ValidBearerToken() throws Exception {
        // Arrange
        String token = "valid-jwt-token";
        String username = "testuser";
        Long userId = 1L;

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.isAccessToken(token)).thenReturn(true);
        when(tokenBlacklistService.isTokenBlacklisted(token)).thenReturn(false);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(jwtUtil.extractUserId(token)).thenReturn(userId);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtUtil).validateToken(token);
    }

    @Test
    void testExtractTokenFromRequest_InvalidAuthorizationHeader() throws Exception {
        // Arrange - 沒有 Bearer 前綴
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat token");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).validateToken(anyString());
    }
}