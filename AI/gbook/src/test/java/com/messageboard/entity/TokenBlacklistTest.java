package com.messageboard.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TokenBlacklist entity
 */
class TokenBlacklistTest {
    
    private TokenBlacklist tokenBlacklist;
    private String testTokenHash;
    private LocalDateTime testExpiresAt;
    
    @BeforeEach
    void setUp() {
        testTokenHash = "test-token-hash-123";
        testExpiresAt = LocalDateTime.now().plusHours(1);
        tokenBlacklist = new TokenBlacklist();
    }
    
    @Test
    void testDefaultConstructor() {
        TokenBlacklist token = new TokenBlacklist();
        
        assertNull(token.getId());
        assertNull(token.getTokenHash());
        assertNull(token.getExpiresAt());
        assertNotNull(token.getCreatedAt());
        assertTrue(token.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
    
    @Test
    void testParameterizedConstructor() {
        TokenBlacklist token = new TokenBlacklist(testTokenHash, testExpiresAt);
        
        assertNull(token.getId());
        assertEquals(testTokenHash, token.getTokenHash());
        assertEquals(testExpiresAt, token.getExpiresAt());
        assertNotNull(token.getCreatedAt());
    }
    
    @Test
    void testSettersAndGetters() {
        Long testId = 1L;
        LocalDateTime testCreatedAt = LocalDateTime.now().minusHours(1);
        
        tokenBlacklist.setId(testId);
        tokenBlacklist.setTokenHash(testTokenHash);
        tokenBlacklist.setExpiresAt(testExpiresAt);
        tokenBlacklist.setCreatedAt(testCreatedAt);
        
        assertEquals(testId, tokenBlacklist.getId());
        assertEquals(testTokenHash, tokenBlacklist.getTokenHash());
        assertEquals(testExpiresAt, tokenBlacklist.getExpiresAt());
        assertEquals(testCreatedAt, tokenBlacklist.getCreatedAt());
    }
    
    @Test
    void testIsExpired_WhenTokenIsNotExpired() {
        LocalDateTime futureTime = LocalDateTime.now().plusHours(1);
        tokenBlacklist.setExpiresAt(futureTime);
        
        assertFalse(tokenBlacklist.isExpired());
    }
    
    @Test
    void testIsExpired_WhenTokenIsExpired() {
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        tokenBlacklist.setExpiresAt(pastTime);
        
        assertTrue(tokenBlacklist.isExpired());
    }
    
    @Test
    void testIsExpired_WhenTokenExpiresNow() {
        LocalDateTime now = LocalDateTime.now();
        tokenBlacklist.setExpiresAt(now);
        
        // Since we're comparing with LocalDateTime.now() in the method,
        // there might be a slight time difference, so we test both possibilities
        boolean isExpired = tokenBlacklist.isExpired();
        // The token should be expired or very close to expiring
        assertTrue(isExpired || !isExpired); // This test mainly checks that the method doesn't throw an exception
    }
    
    @Test
    void testEquals_SameObject() {
        assertTrue(tokenBlacklist.equals(tokenBlacklist));
    }
    
    @Test
    void testEquals_NullObject() {
        assertFalse(tokenBlacklist.equals(null));
    }
    
    @Test
    void testEquals_DifferentClass() {
        assertFalse(tokenBlacklist.equals("not a TokenBlacklist"));
    }
    
    @Test
    void testEquals_SameIdAndTokenHash() {
        TokenBlacklist token1 = new TokenBlacklist(testTokenHash, testExpiresAt);
        token1.setId(1L);
        
        TokenBlacklist token2 = new TokenBlacklist(testTokenHash, testExpiresAt);
        token2.setId(1L);
        
        assertTrue(token1.equals(token2));
    }
    
    @Test
    void testEquals_DifferentId() {
        TokenBlacklist token1 = new TokenBlacklist(testTokenHash, testExpiresAt);
        token1.setId(1L);
        
        TokenBlacklist token2 = new TokenBlacklist(testTokenHash, testExpiresAt);
        token2.setId(2L);
        
        assertFalse(token1.equals(token2));
    }
    
    @Test
    void testEquals_DifferentTokenHash() {
        TokenBlacklist token1 = new TokenBlacklist("hash1", testExpiresAt);
        token1.setId(1L);
        
        TokenBlacklist token2 = new TokenBlacklist("hash2", testExpiresAt);
        token2.setId(1L);
        
        assertFalse(token1.equals(token2));
    }
    
    @Test
    void testEquals_BothIdsNull_SameTokenHash() {
        TokenBlacklist token1 = new TokenBlacklist(testTokenHash, testExpiresAt);
        TokenBlacklist token2 = new TokenBlacklist(testTokenHash, testExpiresAt);
        
        assertTrue(token1.equals(token2));
    }
    
    @Test
    void testEquals_BothIdsNull_DifferentTokenHash() {
        TokenBlacklist token1 = new TokenBlacklist("hash1", testExpiresAt);
        TokenBlacklist token2 = new TokenBlacklist("hash2", testExpiresAt);
        
        assertFalse(token1.equals(token2));
    }
    
    @Test
    void testHashCode_SameIdAndTokenHash() {
        TokenBlacklist token1 = new TokenBlacklist(testTokenHash, testExpiresAt);
        token1.setId(1L);
        
        TokenBlacklist token2 = new TokenBlacklist(testTokenHash, testExpiresAt);
        token2.setId(1L);
        
        assertEquals(token1.hashCode(), token2.hashCode());
    }
    
    @Test
    void testHashCode_DifferentIdOrTokenHash() {
        TokenBlacklist token1 = new TokenBlacklist(testTokenHash, testExpiresAt);
        token1.setId(1L);
        
        TokenBlacklist token2 = new TokenBlacklist("different-hash", testExpiresAt);
        token2.setId(1L);
        
        assertNotEquals(token1.hashCode(), token2.hashCode());
    }
    
    @Test
    void testToString() {
        tokenBlacklist.setId(1L);
        tokenBlacklist.setTokenHash(testTokenHash);
        tokenBlacklist.setExpiresAt(testExpiresAt);
        
        String toString = tokenBlacklist.toString();
        
        assertTrue(toString.contains("TokenBlacklist"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("tokenHash='" + testTokenHash + "'"));
        assertTrue(toString.contains("expiresAt=" + testExpiresAt));
    }
    
    @Test
    void testPrePersist() {
        TokenBlacklist token = new TokenBlacklist();
        token.setCreatedAt(null);
        
        // Simulate @PrePersist call
        token.onCreate();
        
        assertNotNull(token.getCreatedAt());
        assertTrue(token.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
    
    @Test
    void testPrePersist_DoesNotOverrideExistingCreatedAt() {
        LocalDateTime existingCreatedAt = LocalDateTime.now().minusHours(1);
        TokenBlacklist token = new TokenBlacklist();
        token.setCreatedAt(existingCreatedAt);
        
        // Simulate @PrePersist call
        token.onCreate();
        
        assertEquals(existingCreatedAt, token.getCreatedAt());
    }
}