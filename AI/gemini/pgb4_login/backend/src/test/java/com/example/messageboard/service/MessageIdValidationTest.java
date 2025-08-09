package com.example.messageboard.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gtcafe.messageboard.repository.MessageRepository;
import com.gtcafe.messageboard.service.MessageIdGenerator;

/**
 * Tests for MessageIdGenerator validation functionality
 * This test focuses on the validation aspects that are used by error handling
 */
@ExtendWith(MockitoExtension.class)
class MessageIdValidationTest {

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private MessageIdGenerator messageIdGenerator;

    @BeforeEach
    void setUp() {
        // Setup will be done per test as needed
    }

    @Test
    void isValidId_WithValidFormat_ShouldReturnTrue() {
        // Given
        String validId = "ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX";

        // When
        boolean result = messageIdGenerator.isValidId(validId);

        // Then
        assertTrue(result);
    }

    @Test
    void isValidId_WithNullId_ShouldReturnFalse() {
        // Given
        String nullId = null;

        // When
        boolean result = messageIdGenerator.isValidId(nullId);

        // Then
        assertFalse(result);
    }

    @Test
    void isValidId_WithEmptyId_ShouldReturnFalse() {
        // Given
        String emptyId = "";

        // When
        boolean result = messageIdGenerator.isValidId(emptyId);

        // Then
        assertFalse(result);
    }

    @Test
    void isValidId_WithTooShortId_ShouldReturnFalse() {
        // Given
        String shortId = "ABC123";

        // When
        boolean result = messageIdGenerator.isValidId(shortId);

        // Then
        assertFalse(result);
    }

    @Test
    void isValidId_WithTooLongId_ShouldReturnFalse() {
        // Given
        String longId = "ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWXYZ-EXTRA";

        // When
        boolean result = messageIdGenerator.isValidId(longId);

        // Then
        assertFalse(result);
    }

    @Test
    void isValidId_WithLowercaseCharacters_ShouldReturnFalse() {
        // Given
        String lowercaseId = "abcd1234-efgh-5678-ijkl-mnopqrstuvwx";

        // When
        boolean result = messageIdGenerator.isValidId(lowercaseId);

        // Then
        assertFalse(result);
    }

    @Test
    void isValidId_WithSpecialCharacters_ShouldReturnFalse() {
        // Given
        String specialCharId = "ABCD@#$%-EFGH-5678-IJKL-MNOPQRSTUVWX";

        // When
        boolean result = messageIdGenerator.isValidId(specialCharId);

        // Then
        assertFalse(result);
    }

    @Test
    void isValidId_WithWrongDashPositions_ShouldReturnFalse() {
        // Given
        String wrongDashId = "ABCD12-34EFGH-5678-IJKL-MNOPQRSTUVWX";

        // When
        boolean result = messageIdGenerator.isValidId(wrongDashId);

        // Then
        assertFalse(result);
    }

    @Test
    void isValidId_WithMissingDashes_ShouldReturnFalse() {
        // Given
        String noDashId = "ABCD1234EFGH5678IJKLMNOPQRSTUVWX";

        // When
        boolean result = messageIdGenerator.isValidId(noDashId);

        // Then
        assertFalse(result);
    }

    @Test
    void isValidId_WithExtraDashes_ShouldReturnFalse() {
        // Given
        String extraDashId = "ABCD-1234-EFGH-5678-IJKL-MNOP-QRSTUVWX";

        // When
        boolean result = messageIdGenerator.isValidId(extraDashId);

        // Then
        assertFalse(result);
    }

    @Test
    void isValidId_WithWhitespace_ShouldReturnFalse() {
        // Given
        String whitespaceId = "ABCD 1234-EFGH-5678-IJKL-MNOPQRSTUVWX";

        // When
        boolean result = messageIdGenerator.isValidId(whitespaceId);

        // Then
        assertFalse(result);
    }

    @Test
    void isValidId_WithLeadingWhitespace_ShouldReturnFalse() {
        // Given
        String leadingWhitespaceId = " ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX";

        // When
        boolean result = messageIdGenerator.isValidId(leadingWhitespaceId);

        // Then
        assertFalse(result);
    }

    @Test
    void isValidId_WithTrailingWhitespace_ShouldReturnFalse() {
        // Given
        String trailingWhitespaceId = "ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX ";

        // When
        boolean result = messageIdGenerator.isValidId(trailingWhitespaceId);

        // Then
        assertFalse(result);
    }

    @Test
    void isValidId_WithValidCharactersButWrongLength_ShouldReturnFalse() {
        // Given - Valid characters but wrong segment lengths
        String wrongLengthId = "ABC1234-EFGH-5678-IJKL-MNOPQRSTUVWX";

        // When
        boolean result = messageIdGenerator.isValidId(wrongLengthId);

        // Then
        assertFalse(result);
    }

    @Test
    void isValidId_WithAllValidCharacters_ShouldReturnTrue() {
        // Given - Test with all valid characters A-Z, 0-9
        String allValidCharsId = "ABCDEFGH-IJKL-MNOP-QRST-UVWXYZ012345";

        // When
        boolean result = messageIdGenerator.isValidId(allValidCharsId);

        // Then
        assertTrue(result);
    }

    @Test
    void isValidId_WithAllNumbers_ShouldReturnTrue() {
        // Given
        String allNumbersId = "01234567-8901-2345-6789-012345678901";

        // When
        boolean result = messageIdGenerator.isValidId(allNumbersId);

        // Then
        assertTrue(result);
    }

    @Test
    void isValidId_WithAllLetters_ShouldReturnTrue() {
        // Given
        String allLettersId = "ABCDEFGH-IJKL-MNOP-QRST-UVWXYZABCDEF";

        // When
        boolean result = messageIdGenerator.isValidId(allLettersId);

        // Then
        assertTrue(result);
    }

    @Test
    void isIdUnique_WithNonExistentId_ShouldReturnTrue() {
        // Given
        String nonExistentId = "ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX";
        when(messageRepository.existsById(nonExistentId)).thenReturn(false);

        // When
        boolean result = messageIdGenerator.isIdUnique(nonExistentId);

        // Then
        assertTrue(result);
        verify(messageRepository).existsById(nonExistentId);
    }

    @Test
    void isIdUnique_WithExistingId_ShouldReturnFalse() {
        // Given
        String existingId = "ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX";
        when(messageRepository.existsById(existingId)).thenReturn(true);

        // When
        boolean result = messageIdGenerator.isIdUnique(existingId);

        // Then
        assertFalse(result);
        verify(messageRepository).existsById(existingId);
    }

    @Test
    void isIdUnique_WithDatabaseException_ShouldReturnTrue() {
        // Given
        String testId = "ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX";
        when(messageRepository.existsById(testId)).thenThrow(new RuntimeException("Database error"));

        // When
        boolean result = messageIdGenerator.isIdUnique(testId);

        // Then
        assertTrue(result); // Should return true when database check fails
        verify(messageRepository).existsById(testId);
    }

    @Test
    void generateId_ShouldProduceValidId() {
        // Given
        when(messageRepository.existsById(anyString())).thenReturn(false);

        // When
        String generatedId = messageIdGenerator.generateId();

        // Then
        assertNotNull(generatedId);
        assertTrue(messageIdGenerator.isValidId(generatedId));
        assertEquals(36, generatedId.length());
    }

    @Test
    void generateId_ShouldProduceUniqueIds() {
        // Given
        when(messageRepository.existsById(anyString())).thenReturn(false);

        // When
        String id1 = messageIdGenerator.generateId();
        String id2 = messageIdGenerator.generateId();

        // Then
        assertNotEquals(id1, id2);
        assertTrue(messageIdGenerator.isValidId(id1));
        assertTrue(messageIdGenerator.isValidId(id2));
    }
}