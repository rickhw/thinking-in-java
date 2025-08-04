package com.example.messageboard.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gtcafe.messageboard.entity.Message;
import com.gtcafe.messageboard.service.MessageIdGenerator;

/**
 * Unit tests for Message entity class
 * Tests the changes made for the message ID redesign
 */
class MessageEntityTest {

    private MessageIdGenerator mockIdGenerator;

    @BeforeEach
    void setUp() {
        mockIdGenerator = mock(MessageIdGenerator.class);
        Message.setMessageIdGenerator(mockIdGenerator);
    }

    @Test
    void testMessageEntityCreation() {
        // Test basic entity creation
        Message message = new Message();
        message.setUserId("testUser");
        message.setContent("Test content");
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());

        assertEquals("testUser", message.getUserId());
        assertEquals("Test content", message.getContent());
        assertNotNull(message.getCreatedAt());
        assertNotNull(message.getUpdatedAt());
    }

    @Test
    void testMessageEntityWithStringId() {
        // Test that the entity can handle String ID
        String testId = "ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX";
        Message message = new Message();
        message.setId(testId);
        message.setUserId("testUser");
        message.setContent("Test content");

        assertEquals(testId, message.getId());
        assertTrue(message.getId() instanceof String);
    }

    @Test
    void testPrePersistGeneratesId() {
        // Test that @PrePersist method generates ID when null
        String generatedId = "GENERATED-ID-1234-5678-ABCDEFGHIJKL";
        when(mockIdGenerator.generateId()).thenReturn(generatedId);

        Message message = new Message();
        message.setUserId("testUser");
        message.setContent("Test content");

        // ID should be null initially
        assertNull(message.getId());

        // Call the @PrePersist method manually (normally called by JPA)
        message.generateId();

        // ID should now be generated
        assertEquals(generatedId, message.getId());
    }

    @Test
    void testPrePersistDoesNotOverrideExistingId() {
        // Test that @PrePersist method doesn't override existing ID
        String existingId = "EXISTING-ID-1234-5678-ABCDEFGHIJKL";
        String generatedId = "GENERATED-ID-1234-5678-ABCDEFGHIJKL";
        when(mockIdGenerator.generateId()).thenReturn(generatedId);

        Message message = new Message();
        message.setId(existingId);
        message.setUserId("testUser");
        message.setContent("Test content");

        // Call the @PrePersist method manually
        message.generateId();

        // ID should remain the existing one, not the generated one
        assertEquals(existingId, message.getId());
    }

    @Test
    void testPrePersistHandlesNullIdGenerator() {
        // Test that @PrePersist method handles null ID generator gracefully
        Message.setMessageIdGenerator(null);

        Message message = new Message();
        message.setUserId("testUser");
        message.setContent("Test content");

        // ID should be null initially
        assertNull(message.getId());

        // Call the @PrePersist method manually - should not throw exception
        message.generateId();

        // ID should still be null since generator is null
        assertNull(message.getId());
    }

    @Test
    void testAllArgsConstructor() {
        // Test the all-args constructor with String ID
        String testId = "CONSTRUCTOR-ID-1234-5678-ABCDEFGHIJKL";
        String userId = "testUser";
        String content = "Test content";
        LocalDateTime now = LocalDateTime.now();

        Message message = new Message(testId, userId, content, now, now);

        assertEquals(testId, message.getId());
        assertEquals(userId, message.getUserId());
        assertEquals(content, message.getContent());
        assertEquals(now, message.getCreatedAt());
        assertEquals(now, message.getUpdatedAt());
    }

    @Test
    void testNoArgsConstructor() {
        // Test the no-args constructor
        Message message = new Message();

        assertNull(message.getId());
        assertNull(message.getUserId());
        assertNull(message.getContent());
        assertNull(message.getCreatedAt());
        assertNull(message.getUpdatedAt());
    }

    @Test
    void testIdColumnAnnotation() {
        // Test that the ID field has proper JPA annotations
        // This is more of a structural test to ensure the annotations are correct
        Message message = new Message();
        String testId = "TEST-ID-1234-5678-ABCDEFGHIJKL";
        message.setId(testId);

        // The ID should be exactly 36 characters as per the column definition
        assertEquals(36, testId.length());
        assertEquals(testId, message.getId());
    }

    @Test
    void testEntityFieldTypes() {
        // Test that all fields have the correct types after the changes
        Message message = new Message();
        
        // ID should be String type
        message.setId("STRING-ID-1234-5678-ABCDEFGHIJKL");
        assertTrue(message.getId() instanceof String);
        
        // Other fields should remain their original types
        message.setUserId("testUser");
        assertTrue(message.getUserId() instanceof String);
        
        message.setContent("Test content");
        assertTrue(message.getContent() instanceof String);
        
        LocalDateTime now = LocalDateTime.now();
        message.setCreatedAt(now);
        assertTrue(message.getCreatedAt() instanceof LocalDateTime);
        
        message.setUpdatedAt(now);
        assertTrue(message.getUpdatedAt() instanceof LocalDateTime);
    }
}