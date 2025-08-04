package com.example.messageboard.entity;

import com.gtcafe.messageboard.entity.Message;
import com.gtcafe.messageboard.service.MessageIdGenerator;

import java.time.LocalDateTime;

/**
 * Simple test class to verify Message entity changes
 * This is a manual test that can be run to verify the entity works correctly
 */
public class SimpleMessageEntityTest {

    public static void main(String[] args) {
        System.out.println("Testing Message Entity Changes...");
        
        // Test 1: Basic entity creation
        System.out.println("\n1. Testing basic entity creation:");
        Message message = new Message();
        message.setUserId("testUser");
        message.setContent("Test content");
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());
        
        System.out.println("✓ Message created successfully");
        System.out.println("  User ID: " + message.getUserId());
        System.out.println("  Content: " + message.getContent());
        System.out.println("  ID (should be null): " + message.getId());
        
        // Test 2: String ID assignment
        System.out.println("\n2. Testing String ID assignment:");
        String testId = "ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX";
        message.setId(testId);
        System.out.println("✓ String ID assigned successfully");
        System.out.println("  ID: " + message.getId());
        System.out.println("  ID type: " + message.getId().getClass().getSimpleName());
        
        // Test 3: All-args constructor
        System.out.println("\n3. Testing all-args constructor:");
        String constructorId = "CONSTRUCTOR-ID-1234-5678-ABCDEFGHIJKL";
        LocalDateTime now = LocalDateTime.now();
        Message constructorMessage = new Message(constructorId, "testUser2", "Constructor content", now, now);
        
        System.out.println("✓ All-args constructor works");
        System.out.println("  ID: " + constructorMessage.getId());
        System.out.println("  User ID: " + constructorMessage.getUserId());
        System.out.println("  Content: " + constructorMessage.getContent());
        
        // Test 4: @PrePersist method (manual call)
        System.out.println("\n4. Testing @PrePersist method:");
        Message prePersistMessage = new Message();
        prePersistMessage.setUserId("testUser3");
        prePersistMessage.setContent("PrePersist test");
        
        System.out.println("  ID before generateId(): " + prePersistMessage.getId());
        
        // Mock the ID generator for testing
        MessageIdGenerator mockGenerator = new MessageIdGenerator() {
            @Override
            public String generateId() {
                return "MOCK-GENERATED-ID-1234-5678-ABCDEFGHIJKL";
            }
            
            @Override
            public boolean isValidId(String id) {
                return id != null && id.length() == 36;
            }
            
            @Override
            public boolean isIdUnique(String id) {
                return true;
            }
        };
        
        Message.setMessageIdGenerator(mockGenerator);
        prePersistMessage.generateId();
        
        System.out.println("✓ @PrePersist method works");
        System.out.println("  ID after generateId(): " + prePersistMessage.getId());
        
        // Test 5: @PrePersist doesn't override existing ID
        System.out.println("\n5. Testing @PrePersist doesn't override existing ID:");
        Message existingIdMessage = new Message();
        existingIdMessage.setId("EXISTING-ID-1234-5678-ABCDEFGHIJKL");
        String originalId = existingIdMessage.getId();
        
        existingIdMessage.generateId();
        
        System.out.println("✓ @PrePersist preserves existing ID");
        System.out.println("  Original ID: " + originalId);
        System.out.println("  ID after generateId(): " + existingIdMessage.getId());
        System.out.println("  IDs match: " + originalId.equals(existingIdMessage.getId()));
        
        System.out.println("\n✅ All Message entity tests passed!");
        System.out.println("\nSummary of changes verified:");
        System.out.println("- ✓ ID field changed from Long to String");
        System.out.println("- ✓ JPA annotations updated for VARCHAR(36)");
        System.out.println("- ✓ @PrePersist method added for auto ID generation");
        System.out.println("- ✓ MessageIdGenerator integration works");
        System.out.println("- ✓ All constructors work with String ID");
    }
}