package com.gtcafe.pgb.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.gtcafe.pgb.security.JwtAuthenticationDetails;

/**
 * SecurityUtil 單元測試
 */
class SecurityUtilTest {

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetCurrentUsername_WithUserDetails() {
        // Arrange
        String username = "testuser";
        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(new ArrayList<>())
                .build();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act
        String result = SecurityUtil.getCurrentUsername();

        // Assert
        assertEquals(username, result);
    }

    @Test
    void testGetCurrentUsername_WithStringPrincipal() {
        // Arrange
        String username = "testuser";
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null,
                new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act
        String result = SecurityUtil.getCurrentUsername();

        // Assert
        assertEquals(username, result);
    }

    @Test
    void testGetCurrentUsername_NoAuthentication() {
        // Act
        String result = SecurityUtil.getCurrentUsername();

        // Assert
        assertNull(result);
    }

    @Test
    void testGetCurrentUserId_WithJwtAuthenticationDetails() {
        // Arrange
        String username = "testuser";
        Long userId = 123L;

        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(new ArrayList<>())
                .build();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());

        JwtAuthenticationDetails details = mock(JwtAuthenticationDetails.class);
        when(details.getUserId()).thenReturn(userId);
        authentication.setDetails(details);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act
        Long result = SecurityUtil.getCurrentUserId();

        // Assert
        assertEquals(userId, result);
    }

    @Test
    void testGetCurrentUserId_NoJwtDetails() {
        // Arrange
        String username = "testuser";
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null,
                new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act
        Long result = SecurityUtil.getCurrentUserId();

        // Assert
        assertNull(result);
    }

    @Test
    void testIsAuthenticated_WithValidAuthentication() {
        // Arrange
        String username = "testuser";
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null,
                new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act
        boolean result = SecurityUtil.isAuthenticated();

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsAuthenticated_NoAuthentication() {
        // Act
        boolean result = SecurityUtil.isAuthenticated();

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsAuthenticated_AnonymousUser() {
        // Arrange
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("anonymousUser",
                null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act
        boolean result = SecurityUtil.isAuthenticated();

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsCurrentUser_ByUserId_Match() {
        // Arrange
        Long userId = 123L;
        String username = "testuser";

        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(new ArrayList<>())
                .build();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());

        JwtAuthenticationDetails details = mock(JwtAuthenticationDetails.class);
        when(details.getUserId()).thenReturn(userId);
        authentication.setDetails(details);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act
        boolean result = SecurityUtil.isCurrentUser(userId);

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsCurrentUser_ByUserId_NoMatch() {
        // Arrange
        Long currentUserId = 123L;
        Long otherUserId = 456L;
        String username = "testuser";

        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(new ArrayList<>())
                .build();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());

        JwtAuthenticationDetails details = mock(JwtAuthenticationDetails.class);
        when(details.getUserId()).thenReturn(currentUserId);
        authentication.setDetails(details);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act
        boolean result = SecurityUtil.isCurrentUser(otherUserId);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsCurrentUser_ByUsername_Match() {
        // Arrange
        String username = "testuser";
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null,
                new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act
        boolean result = SecurityUtil.isCurrentUser(username);

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsCurrentUser_ByUsername_NoMatch() {
        // Arrange
        String currentUsername = "testuser";
        String otherUsername = "otheruser";
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(currentUsername,
                null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act
        boolean result = SecurityUtil.isCurrentUser(otherUsername);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsCurrentUser_NullValues() {
        // Act & Assert
        assertFalse(SecurityUtil.isCurrentUser((Long) null));
        assertFalse(SecurityUtil.isCurrentUser((String) null));
        assertFalse(SecurityUtil.isCurrentUser(""));
        assertFalse(SecurityUtil.isCurrentUser("   "));
    }
}