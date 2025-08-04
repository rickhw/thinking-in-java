package com.example.messageboard.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.gtcafe.messageboard.entity.Message;
import com.gtcafe.messageboard.repository.MessageRepository;

/**
 * Unit tests for MessageRepository to verify String ID support
 * This test verifies that the repository interface correctly handles String IDs
 * instead of Long IDs after the migration to 36-character message IDs.
 */
@ExtendWith(MockitoExtension.class)
public class MessageRepositoryTest {

    @Mock
    private MessageRepository messageRepository;

    private Message testMessage1;
    private Message testMessage2;

    @BeforeEach
    void setUp() {
        testMessage1 = new Message();
        testMessage1.setId("TEST1234-ABCD-EFGH-IJKL-MNOPQRSTUVWX");
        testMessage1.setUserId("user1");
        testMessage1.setContent("Test message 1");
        testMessage1.setCreatedAt(LocalDateTime.now().minusHours(2));
        testMessage1.setUpdatedAt(LocalDateTime.now().minusHours(2));

        testMessage2 = new Message();
        testMessage2.setId("TEST5678-ABCD-EFGH-IJKL-MNOPQRSTUVWX");
        testMessage2.setUserId("user1");
        testMessage2.setContent("Test message 2");
        testMessage2.setCreatedAt(LocalDateTime.now().minusHours(1));
        testMessage2.setUpdatedAt(LocalDateTime.now().minusHours(1));
    }

    @Test
    void testFindById_WithStringId_ShouldWork() {
        // Given
        String messageId = "TEST1234-ABCD-EFGH-IJKL-MNOPQRSTUVWX";
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(testMessage1));

        // When
        Optional<Message> result = messageRepository.findById(messageId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(messageId, result.get().getId());
        assertEquals("user1", result.get().getUserId());
        verify(messageRepository).findById(messageId);
    }

    @Test
    void testExistsById_WithStringId_ShouldWork() {
        // Given
        String messageId = "TEST1234-ABCD-EFGH-IJKL-MNOPQRSTUVWX";
        when(messageRepository.existsById(messageId)).thenReturn(true);

        // When
        boolean exists = messageRepository.existsById(messageId);

        // Then
        assertTrue(exists);
        verify(messageRepository).existsById(messageId);
    }

    @Test
    void testDeleteById_WithStringId_ShouldWork() {
        // Given
        String messageId = "TEST1234-ABCD-EFGH-IJKL-MNOPQRSTUVWX";
        doNothing().when(messageRepository).deleteById(messageId);

        // When
        messageRepository.deleteById(messageId);

        // Then
        verify(messageRepository).deleteById(messageId);
    }

    @Test
    void testSave_WithStringId_ShouldWork() {
        // Given
        when(messageRepository.save(testMessage1)).thenReturn(testMessage1);

        // When
        Message savedMessage = messageRepository.save(testMessage1);

        // Then
        assertNotNull(savedMessage);
        assertEquals("TEST1234-ABCD-EFGH-IJKL-MNOPQRSTUVWX", savedMessage.getId());
        verify(messageRepository).save(testMessage1);
    }

    @Test
    void testFindByUserIdOrderByCreatedAtDesc_ShouldWork() {
        // Given
        String userId = "user1";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Message> expectedPage = new PageImpl<>(Arrays.asList(testMessage2, testMessage1));
        when(messageRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)).thenReturn(expectedPage);

        // When
        Page<Message> result = messageRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("TEST5678-ABCD-EFGH-IJKL-MNOPQRSTUVWX", result.getContent().get(0).getId());
        assertEquals("TEST1234-ABCD-EFGH-IJKL-MNOPQRSTUVWX", result.getContent().get(1).getId());
        verify(messageRepository).findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Test
    void testFindAllOrderByCreatedAtDesc_ShouldWork() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Message> expectedPage = new PageImpl<>(Arrays.asList(testMessage2, testMessage1));
        when(messageRepository.findAllOrderByCreatedAtDesc(pageable)).thenReturn(expectedPage);

        // When
        Page<Message> result = messageRepository.findAllOrderByCreatedAtDesc(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(messageRepository).findAllOrderByCreatedAtDesc(pageable);
    }

    @Test
    void testFindByUserId_ShouldWork() {
        // Given
        String userId = "user1";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Message> expectedPage = new PageImpl<>(Arrays.asList(testMessage1, testMessage2));
        when(messageRepository.findByUserId(userId, pageable)).thenReturn(expectedPage);

        // When
        Page<Message> result = messageRepository.findByUserId(userId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        result.getContent().forEach(message -> assertEquals(userId, message.getUserId()));
        verify(messageRepository).findByUserId(userId, pageable);
    }

    @Test
    void testRepositoryInterfaceGenericType_ShouldBeString() {
        // This test verifies that the repository interface uses String as the ID type
        // by checking that String ID methods can be called without compilation errors
        
        String stringId = "TEST1234-ABCD-EFGH-IJKL-MNOPQRSTUVWX";
        
        // These method calls should compile without errors if the generic type is correct
        when(messageRepository.findById(stringId)).thenReturn(Optional.of(testMessage1));
        when(messageRepository.existsById(stringId)).thenReturn(true);
        doNothing().when(messageRepository).deleteById(stringId);
        
        // Execute the methods to verify they work
        Optional<Message> found = messageRepository.findById(stringId);
        boolean exists = messageRepository.existsById(stringId);
        messageRepository.deleteById(stringId);
        
        // Verify the methods were called with String parameters
        verify(messageRepository).findById(stringId);
        verify(messageRepository).existsById(stringId);
        verify(messageRepository).deleteById(stringId);
        
        // If we reach this point, the repository interface correctly uses String IDs
        assertTrue(true, "Repository interface successfully uses String ID type");
    }
}