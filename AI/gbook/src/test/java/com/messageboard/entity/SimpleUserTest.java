package com.messageboard.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleUserTest {

    @Test
    public void testUserCreation() {
        User user = User.builder()
                .ssoId("test123")
                .username("testuser")
                .email("test@example.com")
                .build();
        
        assertNotNull(user);
        assertEquals("test123", user.getSsoId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertTrue(user.isActive());
    }
    
    @Test
    public void testUserWithMinimalConstructor() {
        User user = new User("sso123", "testuser", "test@example.com");
        
        assertNotNull(user);
        assertEquals("sso123", user.getSsoId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertTrue(user.isActive());
    }
    
    @Test
    public void testEffectiveDisplayName() {
        User userWithDisplayName = User.builder()
                .ssoId("test123")
                .username("testuser")
                .email("test@example.com")
                .displayName("Test User")
                .build();
        
        assertEquals("Test User", userWithDisplayName.getEffectiveDisplayName());
        
        User userWithoutDisplayName = User.builder()
                .ssoId("test123")
                .username("testuser")
                .email("test@example.com")
                .build();
        
        assertEquals("testuser", userWithoutDisplayName.getEffectiveDisplayName());
    }
}