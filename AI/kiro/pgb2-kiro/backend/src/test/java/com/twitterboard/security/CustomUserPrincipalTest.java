package com.twitterboard.security;

import com.twitterboard.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserPrincipalTest {
    
    private User testUser;
    private CustomUserPrincipal userPrincipal;
    
    @BeforeEach
    void setUp() {
        testUser = new User("google123", "test@example.com", "Test User", "http://avatar.url");
        testUser.setId(1L);
        userPrincipal = new CustomUserPrincipal(testUser);
    }
    
    @Test
    void testGetUser() {
        // When & Then
        assertEquals(testUser, userPrincipal.getUser());
    }
    
    @Test
    void testGetId() {
        // When & Then
        assertEquals(1L, userPrincipal.getId());
    }
    
    @Test
    void testGetEmail() {
        // When & Then
        assertEquals("test@example.com", userPrincipal.getEmail());
    }
    
    @Test
    void testGetName() {
        // When & Then
        assertEquals("Test User", userPrincipal.getName());
    }
    
    @Test
    void testGetGoogleId() {
        // When & Then
        assertEquals("google123", userPrincipal.getGoogleId());
    }
    
    @Test
    void testGetAvatarUrl() {
        // When & Then
        assertEquals("http://avatar.url", userPrincipal.getAvatarUrl());
    }
    
    @Test
    void testGetAuthorities() {
        // When
        Collection<? extends GrantedAuthority> authorities = userPrincipal.getAuthorities();
        
        // Then
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(auth -> "ROLE_USER".equals(auth.getAuthority())));
    }
    
    @Test
    void testGetPassword() {
        // When & Then
        assertNull(userPrincipal.getPassword());
    }
    
    @Test
    void testGetUsername() {
        // When & Then
        assertEquals("test@example.com", userPrincipal.getUsername());
    }
    
    @Test
    void testIsAccountNonExpired() {
        // When & Then
        assertTrue(userPrincipal.isAccountNonExpired());
    }
    
    @Test
    void testIsAccountNonLocked() {
        // When & Then
        assertTrue(userPrincipal.isAccountNonLocked());
    }
    
    @Test
    void testIsCredentialsNonExpired() {
        // When & Then
        assertTrue(userPrincipal.isCredentialsNonExpired());
    }
    
    @Test
    void testIsEnabled() {
        // When & Then
        assertTrue(userPrincipal.isEnabled());
    }
    
    @Test
    void testEquals() {
        // Given
        User sameUser = new User("google123", "test@example.com", "Test User", "http://avatar.url");
        sameUser.setId(1L);
        CustomUserPrincipal samePrincipal = new CustomUserPrincipal(sameUser);
        
        User differentUser = new User("google456", "test2@example.com", "Test User 2", null);
        differentUser.setId(2L);
        CustomUserPrincipal differentPrincipal = new CustomUserPrincipal(differentUser);
        
        // When & Then
        assertEquals(userPrincipal, samePrincipal);
        assertNotEquals(userPrincipal, differentPrincipal);
        assertNotEquals(userPrincipal, null);
        assertNotEquals(userPrincipal, "not a principal");
    }
    
    @Test
    void testHashCode() {
        // Given
        User sameUser = new User("google123", "test@example.com", "Test User", "http://avatar.url");
        sameUser.setId(1L);
        CustomUserPrincipal samePrincipal = new CustomUserPrincipal(sameUser);
        
        User differentUser = new User("google456", "test2@example.com", "Test User 2", null);
        differentUser.setId(2L);
        CustomUserPrincipal differentPrincipal = new CustomUserPrincipal(differentUser);
        
        // When & Then
        assertEquals(userPrincipal.hashCode(), samePrincipal.hashCode());
        assertNotEquals(userPrincipal.hashCode(), differentPrincipal.hashCode());
    }
    
    @Test
    void testToString() {
        // When
        String toString = userPrincipal.toString();
        
        // Then
        assertTrue(toString.contains("CustomUserPrincipal"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("email='test@example.com'"));
        assertTrue(toString.contains("name='Test User'"));
    }
    
    @Test
    void testWithNullAvatarUrl() {
        // Given
        User userWithoutAvatar = new User("google123", "test@example.com", "Test User", null);
        userWithoutAvatar.setId(1L);
        CustomUserPrincipal principalWithoutAvatar = new CustomUserPrincipal(userWithoutAvatar);
        
        // When & Then
        assertNull(principalWithoutAvatar.getAvatarUrl());
        assertEquals("test@example.com", principalWithoutAvatar.getEmail());
        assertEquals("Test User", principalWithoutAvatar.getName());
    }
}