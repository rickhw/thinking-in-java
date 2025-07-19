package com.messageboard.config;

import com.messageboard.entity.TokenBlacklist;
import com.messageboard.entity.User;
import com.messageboard.entity.Message;
import com.messageboard.repository.TokenBlacklistRepository;
import com.messageboard.repository.UserRepository;
import com.messageboard.repository.MessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for database configuration and initialization
 * Tests database connection, table creation, and basic CRUD operations
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DatabaseIntegrationTest {
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Test
    void testDatabaseConnection() throws Exception {
        assertNotNull(dataSource);
        
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection);
            assertFalse(connection.isClosed());
            
            DatabaseMetaData metaData = connection.getMetaData();
            assertNotNull(metaData);
            
            // Verify we're using H2 for tests
            assertTrue(metaData.getDatabaseProductName().contains("H2"));
        }
    }
    
    @Test
    void testTokenBlacklistTableExists() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // Check if token_blacklist table exists
            var resultSet = metaData.getTables(null, null, "TOKEN_BLACKLIST", null);
            assertTrue(resultSet.next(), "TOKEN_BLACKLIST table should exist");
        }
    }
    
    @Test
    void testUsersTableExists() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // Check if users table exists
            var resultSet = metaData.getTables(null, null, "USERS", null);
            assertTrue(resultSet.next(), "USERS table should exist");
        }
    }
    
    @Test
    void testMessagesTableExists() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // Check if messages table exists
            var resultSet = metaData.getTables(null, null, "MESSAGES", null);
            assertTrue(resultSet.next(), "MESSAGES table should exist");
        }
    }
    
    @Test
    void testTokenBlacklistRepositoryBasicOperations() {
        // Test repository is properly configured and can perform basic operations
        assertNotNull(tokenBlacklistRepository);
        
        // Test count (should be 0 initially)
        long initialCount = tokenBlacklistRepository.count();
        assertEquals(0, initialCount);
        
        // Test save
        TokenBlacklist token = new TokenBlacklist("test-hash-123", LocalDateTime.now().plusHours(1));
        TokenBlacklist saved = tokenBlacklistRepository.save(token);
        
        assertNotNull(saved.getId());
        assertEquals("test-hash-123", saved.getTokenHash());
        assertNotNull(saved.getCreatedAt());
        
        // Test count after save
        assertEquals(initialCount + 1, tokenBlacklistRepository.count());
        
        // Test find by id
        var found = tokenBlacklistRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("test-hash-123", found.get().getTokenHash());
        
        // Test delete
        tokenBlacklistRepository.delete(saved);
        assertEquals(initialCount, tokenBlacklistRepository.count());
    }
    
    @Test
    void testUserRepositoryBasicOperations() {
        // Test that User repository is properly configured
        assertNotNull(userRepository);
        
        long initialCount = userRepository.count();
        
        // Create and save a user
        User user = new User();
        user.setSsoId("test-sso-123");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setDisplayName("Test User");
        
        User saved = userRepository.save(user);
        assertNotNull(saved.getId());
        assertEquals(initialCount + 1, userRepository.count());
        
        // Clean up
        userRepository.delete(saved);
    }
    
    @Test
    void testMessageRepositoryBasicOperations() {
        // Test that Message repository is properly configured
        assertNotNull(messageRepository);
        
        // First create a user (required for message)
        User user = new User();
        user.setSsoId("test-sso-456");
        user.setUsername("messageuser");
        user.setEmail("message@example.com");
        user.setDisplayName("Message User");
        User savedUser = userRepository.save(user);
        
        long initialCount = messageRepository.count();
        
        // Create and save a message
        Message message = new Message();
        message.setUser(savedUser);
        message.setBoardOwner(savedUser);
        message.setContent("Test message content");
        
        Message saved = messageRepository.save(message);
        assertNotNull(saved.getId());
        assertEquals(initialCount + 1, messageRepository.count());
        
        // Clean up
        messageRepository.delete(saved);
        userRepository.delete(savedUser);
    }
    
    @Test
    void testTransactionManagement() {
        // Test that transactions are properly configured
        long initialCount = tokenBlacklistRepository.count();
        
        try {
            // This should be within a transaction due to @Transactional on the class
            TokenBlacklist token1 = new TokenBlacklist("tx-test-1", LocalDateTime.now().plusHours(1));
            TokenBlacklist token2 = new TokenBlacklist("tx-test-2", LocalDateTime.now().plusHours(1));
            
            tokenBlacklistRepository.save(token1);
            tokenBlacklistRepository.save(token2);
            
            // Both should be saved within the transaction
            assertEquals(initialCount + 2, tokenBlacklistRepository.count());
            
        } catch (Exception e) {
            fail("Transaction should not fail: " + e.getMessage());
        }
        
        // After the test method completes, @Transactional should rollback the transaction
        // So the count should return to initial value
    }
    
    @Test
    void testJpaAuditingConfiguration() {
        // Test that JPA auditing is properly configured
        TokenBlacklist token = new TokenBlacklist("audit-test", LocalDateTime.now().plusHours(1));
        
        // Before saving, createdAt should be set by the constructor
        assertNotNull(token.getCreatedAt());
        
        TokenBlacklist saved = tokenBlacklistRepository.save(token);
        
        // After saving, the timestamps should still be present
        assertNotNull(saved.getCreatedAt());
        
        // Clean up
        tokenBlacklistRepository.delete(saved);
    }
    
    @Test
    void testRepositoryCustomQueries() {
        // Test custom repository methods work correctly
        String testHash = "custom-query-test";
        TokenBlacklist token = new TokenBlacklist(testHash, LocalDateTime.now().plusHours(1));
        TokenBlacklist saved = tokenBlacklistRepository.save(token);
        
        // Test findByTokenHash
        var found = tokenBlacklistRepository.findByTokenHash(testHash);
        assertTrue(found.isPresent());
        assertEquals(testHash, found.get().getTokenHash());
        
        // Test existsByTokenHash
        assertTrue(tokenBlacklistRepository.existsByTokenHash(testHash));
        assertFalse(tokenBlacklistRepository.existsByTokenHash("non-existent"));
        
        // Test countActiveTokens
        long activeCount = tokenBlacklistRepository.countActiveTokens(LocalDateTime.now());
        assertTrue(activeCount >= 1);
        
        // Clean up
        tokenBlacklistRepository.delete(saved);
    }
}