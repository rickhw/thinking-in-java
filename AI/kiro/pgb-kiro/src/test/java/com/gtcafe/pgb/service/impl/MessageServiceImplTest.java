package com.gtcafe.pgb.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.gtcafe.pgb.entity.Message;
import com.gtcafe.pgb.entity.User;
import com.gtcafe.pgb.repository.MessageRepository;
import com.gtcafe.pgb.service.CacheService;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageService Implementation Tests")
class MessageServiceImplTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private MessageServiceImpl messageService;

    private User testUser;
    private User boardOwner;
    private Message testMessage;
    private Message parentMessage;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).ssoId("test-sso-1").username("testuser")
                .email("test@example.com").isActive(true).build();

        boardOwner = User.builder().id(2L).ssoId("test-sso-2").username("boardowner")
                .email("owner@example.com").isActive(true).build();

        testMessage = Message.builder().id(1L).user(testUser).boardOwner(boardOwner)
                .content("Test message content").createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now()).isDeleted(false).build();

        parentMessage = Message.builder().id(2L).user(boardOwner).boardOwner(boardOwner)
                .content("Parent message content").createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now()).isDeleted(false).build();
    }

    @Nested
    @DisplayName("Create Message Tests")
    class CreateMessageTests {

        @Test
        @DisplayName("Should create message successfully")
        void shouldCreateMessageSuccessfully() {
            // Given
            String content = "New message content";
            when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

            // When
            Message result = messageService.createMessage(testUser, boardOwner, content);

            // Then
            assertNotNull(result);
            assertEquals(testMessage.getId(), result.getId());
            assertEquals(testUser, result.getUser());
            assertEquals(boardOwner, result.getBoardOwner());

            verify(messageRepository).save(any(Message.class));
            verify(cacheService, times(2)).clearByPattern(anyString());
        }

        @Test
        @DisplayName("Should throw exception for empty content")
        void shouldThrowExceptionForEmptyContent() {
            // Given
            String emptyContent = "";

            // When & Then
            assertThrows(IllegalArgumentException.class,
                    () -> messageService.createMessage(testUser, boardOwner, emptyContent));

            verify(messageRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception for null author")
        void shouldThrowExceptionForNullAuthor() {
            // Given
            String content = "Valid content";

            // When & Then
            assertThrows(IllegalArgumentException.class,
                    () -> messageService.createMessage(null, boardOwner, content));

            verify(messageRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception for inactive user")
        void shouldThrowExceptionForInactiveUser() {
            // Given
            String content = "Valid content";
            testUser.setIsActive(false);

            // When & Then
            assertThrows(IllegalArgumentException.class,
                    () -> messageService.createMessage(testUser, boardOwner, content));

            verify(messageRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception for content exceeding max length")
        void shouldThrowExceptionForContentExceedingMaxLength() {
            // Given
            String longContent = "a".repeat(1001); // Exceeds 1000 character limit

            // When & Then
            assertThrows(IllegalArgumentException.class,
                    () -> messageService.createMessage(testUser, boardOwner, longContent));

            verify(messageRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Create Reply Tests")
    class CreateReplyTests {

        @Test
        @DisplayName("Should create reply successfully")
        void shouldCreateReplySuccessfully() {
            // Given
            String content = "Reply content";
            Message reply = Message.builder().id(3L).user(testUser).boardOwner(boardOwner)
                    .content(content).parentMessage(parentMessage).isDeleted(false).build();

            when(messageRepository.save(any(Message.class))).thenReturn(reply);

            // When
            Message result = messageService.createReply(testUser, parentMessage, content);

            // Then
            assertNotNull(result);
            assertEquals(reply.getId(), result.getId());
            assertEquals(testUser, result.getUser());
            assertEquals(parentMessage, result.getParentMessage());

            verify(messageRepository).save(any(Message.class));
        }

        @Test
        @DisplayName("Should throw exception for null parent message")
        void shouldThrowExceptionForNullParentMessage() {
            // Given
            String content = "Reply content";

            // When & Then
            assertThrows(IllegalArgumentException.class,
                    () -> messageService.createReply(testUser, null, content));

            verify(messageRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception for deleted parent message")
        void shouldThrowExceptionForDeletedParentMessage() {
            // Given
            String content = "Reply content";
            parentMessage.setIsDeleted(true);

            // When & Then
            assertThrows(IllegalArgumentException.class,
                    () -> messageService.createReply(testUser, parentMessage, content));

            verify(messageRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Update Message Tests")
    class UpdateMessageTests {

        @Test
        @DisplayName("Should update message successfully")
        void shouldUpdateMessageSuccessfully() {
            // Given
            String newContent = "Updated content";
            when(messageRepository.findByIdAndNotDeleted(testMessage.getId()))
                    .thenReturn(Optional.of(testMessage));
            when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

            // When
            Message result =
                    messageService.updateMessage(testMessage.getId(), testUser, newContent);

            // Then
            assertNotNull(result);
            verify(messageRepository).save(testMessage);
            // Verify cache operations are called (3 times: 2 from clearMessageCaches + 1 from
            // clearBoardCaches)
            verify(cacheService, times(3)).delete(anyString());
            verify(cacheService).clearByPattern(anyString());
        }

        @Test
        @DisplayName("Should throw exception when message not found")
        void shouldThrowExceptionWhenMessageNotFound() {
            // Given
            Long messageId = 999L;
            String newContent = "Updated content";
            when(messageRepository.findByIdAndNotDeleted(messageId)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(RuntimeException.class,
                    () -> messageService.updateMessage(messageId, testUser, newContent));

            verify(messageRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when user is not author")
        void shouldThrowExceptionWhenUserIsNotAuthor() {
            // Given
            String newContent = "Updated content";
            User otherUser = User.builder().id(99L).ssoId("other-sso").username("otheruser")
                    .email("other@example.com").isActive(true).build();

            when(messageRepository.findByIdAndNotDeleted(testMessage.getId()))
                    .thenReturn(Optional.of(testMessage));

            // When & Then
            assertThrows(SecurityException.class,
                    () -> messageService.updateMessage(testMessage.getId(), otherUser, newContent));

            verify(messageRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Delete Message Tests")
    class DeleteMessageTests {

        @Test
        @DisplayName("Should delete message by author successfully")
        void shouldDeleteMessageByAuthorSuccessfully() {
            // Given
            when(messageRepository.findByIdAndNotDeleted(testMessage.getId()))
                    .thenReturn(Optional.of(testMessage));
            when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

            // When
            messageService.deleteMessage(testMessage.getId(), testUser);

            // Then
            verify(messageRepository).save(testMessage);
            assertTrue(testMessage.isDeleted());
        }

        @Test
        @DisplayName("Should delete message by board owner successfully")
        void shouldDeleteMessageByBoardOwnerSuccessfully() {
            // Given
            when(messageRepository.findByIdAndNotDeleted(testMessage.getId()))
                    .thenReturn(Optional.of(testMessage));
            when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

            // When
            messageService.deleteMessage(testMessage.getId(), boardOwner);

            // Then
            verify(messageRepository).save(testMessage);
            assertTrue(testMessage.isDeleted());
        }

        @Test
        @DisplayName("Should throw exception when user has no permission")
        void shouldThrowExceptionWhenUserHasNoPermission() {
            // Given
            User otherUser = User.builder().id(99L).ssoId("other-sso").username("otheruser")
                    .email("other@example.com").isActive(true).build();

            when(messageRepository.findByIdAndNotDeleted(testMessage.getId()))
                    .thenReturn(Optional.of(testMessage));

            // When & Then
            assertThrows(SecurityException.class,
                    () -> messageService.deleteMessage(testMessage.getId(), otherUser));

            verify(messageRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Find Message Tests")
    class FindMessageTests {

        @Test
        @DisplayName("Should find message by ID from cache")
        void shouldFindMessageByIdFromCache() {
            // Given
            when(cacheService.get(anyString(), eq(Message.class))).thenReturn(testMessage);

            // When
            Optional<Message> result = messageService.findById(testMessage.getId());

            // Then
            assertTrue(result.isPresent());
            assertEquals(testMessage, result.get());
            verify(messageRepository, never()).findByIdAndNotDeleted(any());
        }

        @Test
        @DisplayName("Should find message by ID from database when not cached")
        void shouldFindMessageByIdFromDatabaseWhenNotCached() {
            // Given
            when(cacheService.get(anyString(), eq(Message.class))).thenReturn(null);
            when(messageRepository.findByIdAndNotDeleted(testMessage.getId()))
                    .thenReturn(Optional.of(testMessage));

            // When
            Optional<Message> result = messageService.findById(testMessage.getId());

            // Then
            assertTrue(result.isPresent());
            assertEquals(testMessage, result.get());
            verify(cacheService).set(anyString(), eq(testMessage), any());
        }

        @Test
        @DisplayName("Should return empty when message not found")
        void shouldReturnEmptyWhenMessageNotFound() {
            // Given
            Long messageId = 999L;
            when(cacheService.get(anyString(), eq(Message.class))).thenReturn(null);
            when(messageRepository.findByIdAndNotDeleted(messageId)).thenReturn(Optional.empty());

            // When
            Optional<Message> result = messageService.findById(messageId);

            // Then
            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("Board Messages Tests")
    class BoardMessagesTests {

        @Test
        @DisplayName("Should get board messages with pagination")
        void shouldGetBoardMessagesWithPagination() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            List<Message> messages = Arrays.asList(testMessage);
            Page<Message> messagePage = new PageImpl<>(messages, pageable, 1);

            when(messageRepository.findByBoardOwnerAndNotDeletedAndParentMessageIsNull(boardOwner,
                    pageable)).thenReturn(messagePage);

            // When
            Page<Message> result = messageService.getBoardMessages(boardOwner, pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(testMessage, result.getContent().get(0));
        }

        @Test
        @DisplayName("Should get all board messages including replies")
        void shouldGetAllBoardMessagesIncludingReplies() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            List<Message> messages = Arrays.asList(testMessage, parentMessage);
            Page<Message> messagePage = new PageImpl<>(messages, pageable, 2);

            when(messageRepository.findByBoardOwnerAndNotDeleted(boardOwner, pageable))
                    .thenReturn(messagePage);

            // When
            Page<Message> result = messageService.getAllBoardMessages(boardOwner, pageable);

            // Then
            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should validate message content successfully")
        void shouldValidateMessageContentSuccessfully() {
            // Given
            String validContent = "This is a valid message content";

            // When & Then
            assertDoesNotThrow(() -> messageService.validateMessageContent(validContent));
        }

        @Test
        @DisplayName("Should throw exception for null content")
        void shouldThrowExceptionForNullContent() {
            // When & Then
            assertThrows(IllegalArgumentException.class,
                    () -> messageService.validateMessageContent(null));
        }

        @Test
        @DisplayName("Should throw exception for empty content")
        void shouldThrowExceptionForEmptyContent() {
            // When & Then
            assertThrows(IllegalArgumentException.class,
                    () -> messageService.validateMessageContent(""));
        }

        @Test
        @DisplayName("Should throw exception for whitespace only content")
        void shouldThrowExceptionForWhitespaceOnlyContent() {
            // When & Then
            assertThrows(IllegalArgumentException.class,
                    () -> messageService.validateMessageContent("   "));
        }

        @Test
        @DisplayName("Should throw exception for content exceeding max length")
        void shouldThrowExceptionForContentExceedingMaxLength() {
            // Given
            String longContent = "a".repeat(1001);

            // When & Then
            assertThrows(IllegalArgumentException.class,
                    () -> messageService.validateMessageContent(longContent));
        }
    }

    @Nested
    @DisplayName("Permission Tests")
    class PermissionTests {

        @Test
        @DisplayName("Should allow author to edit message")
        void shouldAllowAuthorToEditMessage() {
            // When
            boolean canEdit = messageService.canEditMessage(testMessage, testUser);

            // Then
            assertTrue(canEdit);
        }

        @Test
        @DisplayName("Should not allow non-author to edit message")
        void shouldNotAllowNonAuthorToEditMessage() {
            // Given
            User otherUser = User.builder().id(99L).ssoId("other-sso").username("otheruser")
                    .email("other@example.com").isActive(true).build();

            // When
            boolean canEdit = messageService.canEditMessage(testMessage, otherUser);

            // Then
            assertFalse(canEdit);
        }

        @Test
        @DisplayName("Should allow author to delete message")
        void shouldAllowAuthorToDeleteMessage() {
            // When
            boolean canDelete = messageService.canDeleteMessage(testMessage, testUser);

            // Then
            assertTrue(canDelete);
        }

        @Test
        @DisplayName("Should allow board owner to delete message")
        void shouldAllowBoardOwnerToDeleteMessage() {
            // When
            boolean canDelete = messageService.canDeleteMessage(testMessage, boardOwner);

            // Then
            assertTrue(canDelete);
        }

        @Test
        @DisplayName("Should not allow other users to delete message")
        void shouldNotAllowOtherUsersToDeleteMessage() {
            // Given
            User otherUser = User.builder().id(99L).ssoId("other-sso").username("otheruser")
                    .email("other@example.com").isActive(true).build();

            // When
            boolean canDelete = messageService.canDeleteMessage(testMessage, otherUser);

            // Then
            assertFalse(canDelete);
        }
    }

    @Nested
    @DisplayName("Count Tests")
    class CountTests {

        @Test
        @DisplayName("Should count board messages from cache")
        void shouldCountBoardMessagesFromCache() {
            // Given
            Long expectedCount = 5L;
            when(cacheService.get(anyString(), eq(Long.class))).thenReturn(expectedCount);

            // When
            long result = messageService.countBoardMessages(boardOwner);

            // Then
            assertEquals(expectedCount, result);
            verify(messageRepository, never())
                    .countByBoardOwnerAndNotDeletedAndParentMessageIsNull(any());
        }

        @Test
        @DisplayName("Should count board messages from database when not cached")
        void shouldCountBoardMessagesFromDatabaseWhenNotCached() {
            // Given
            Long expectedCount = 3L;
            when(cacheService.get(anyString(), eq(Long.class))).thenReturn(null);
            when(messageRepository.countByBoardOwnerAndNotDeletedAndParentMessageIsNull(boardOwner))
                    .thenReturn(expectedCount);

            // When
            long result = messageService.countBoardMessages(boardOwner);

            // Then
            assertEquals(expectedCount, result);
            verify(cacheService).set(anyString(), eq(expectedCount), any());
        }
    }
}
