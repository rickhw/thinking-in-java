package com.messageboard.repository;

import com.messageboard.entity.Message;
import com.messageboard.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for MessageRepository
 */
@DataJpaTest
@DisplayName("Message Repository Tests")
class MessageRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MessageRepository messageRepository;

    private User author;
    private User boardOwner;
    private User otherUser;
    private Message rootMessage;
    private Message replyMessage;
    private Message deletedMessage;

    @BeforeEach
    void setUp() {
        // Create test users
        author = User.builder()
                .ssoId("sso-author")
                .username("author")
                .email("author@example.com")
                .displayName("Author User")
                .isActive(true)
                .build();
        author = entityManager.persistAndFlush(author);

        boardOwner = User.builder()
                .ssoId("sso-boardowner")
                .username("boardowner")
                .email("boardowner@example.com")
                .displayName("Board Owner")
                .isActive(true)
                .build();
        boardOwner = entityManager.persistAndFlush(boardOwner);

        otherUser = User.builder()
                .ssoId("sso-other")
                .username("otheruser")
                .email("other@example.com")
                .displayName("Other User")
                .isActive(true)
                .build();
        otherUser = entityManager.persistAndFlush(otherUser);

        // Create test messages
        rootMessage = Message.builder()
                .user(author)
                .boardOwner(boardOwner)
                .content("Root message content")
                .isDeleted(false)
                .build();
        rootMessage = entityManager.persistAndFlush(rootMessage);

        replyMessage = Message.builder()
                .user(boardOwner)
                .boardOwner(boardOwner)
                .content("Reply message content")
                .parentMessage(rootMessage)
                .isDeleted(false)
                .build();
        replyMessage = entityManager.persistAndFlush(replyMessage);

        deletedMessage = Message.builder()
                .user(author)
                .boardOwner(boardOwner)
                .content("Deleted message content")
                .isDeleted(true)
                .build();
        deletedMessage = entityManager.persistAndFlush(deletedMessage);

        entityManager.clear();
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    class BasicCrudOperations {

        @Test
        @DisplayName("Should save and find message by ID")
        void shouldSaveAndFindMessageById() {
            Message newMessage = Message.builder()
                    .user(author)
                    .boardOwner(boardOwner)
                    .content("New message")
                    .isDeleted(false)
                    .build();

            Message saved = messageRepository.save(newMessage);
            Optional<Message> found = messageRepository.findById(saved.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getContent()).isEqualTo("New message");
            assertThat(found.get().getUser().getId()).isEqualTo(author.getId());
            assertThat(found.get().getBoardOwner().getId()).isEqualTo(boardOwner.getId());
        }

        @Test
        @DisplayName("Should find message by ID and not deleted")
        void shouldFindMessageByIdAndNotDeleted() {
            Optional<Message> found = messageRepository.findByIdAndNotDeleted(rootMessage.getId());
            Optional<Message> deletedFound = messageRepository.findByIdAndNotDeleted(deletedMessage.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(rootMessage.getId());
            assertThat(deletedFound).isEmpty();
        }
    }

    @Nested
    @DisplayName("Board Owner Queries")
    class BoardOwnerQueries {

        @Test
        @DisplayName("Should find messages by board owner excluding deleted and replies")
        void shouldFindMessagesByBoardOwnerExcludingDeletedAndReplies() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Message> messages = messageRepository.findByBoardOwnerAndNotDeletedAndParentMessageIsNull(boardOwner, pageable);

            assertThat(messages.getContent()).hasSize(1);
            assertThat(messages.getContent().get(0).getId()).isEqualTo(rootMessage.getId());
            assertThat(messages.getContent()).noneMatch(m -> m.isDeleted() || m.isReply());
        }

        @Test
        @DisplayName("Should find all messages by board owner including replies")
        void shouldFindAllMessagesByBoardOwnerIncludingReplies() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Message> messages = messageRepository.findByBoardOwnerAndNotDeleted(boardOwner, pageable);

            assertThat(messages.getContent()).hasSize(2);
            assertThat(messages.getContent()).extracting(Message::getId)
                    .containsExactlyInAnyOrder(rootMessage.getId(), replyMessage.getId());
            assertThat(messages.getContent()).noneMatch(Message::isDeleted);
        }

        @Test
        @DisplayName("Should count messages by board owner excluding deleted and replies")
        void shouldCountMessagesByBoardOwnerExcludingDeletedAndReplies() {
            long count = messageRepository.countByBoardOwnerAndNotDeletedAndParentMessageIsNull(boardOwner);

            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("Should find messages with replies for board owner")
        void shouldFindMessagesWithRepliesForBoardOwner() {
            List<Message> messages = messageRepository.findByBoardOwnerWithReplies(boardOwner);

            assertThat(messages).hasSize(1);
            assertThat(messages.get(0).getId()).isEqualTo(rootMessage.getId());
            assertThat(messages.get(0).isRootMessage()).isTrue();
        }
    }

    @Nested
    @DisplayName("Reply Queries")
    class ReplyQueries {

        @Test
        @DisplayName("Should find replies by parent message")
        void shouldFindRepliesByParentMessage() {
            List<Message> replies = messageRepository.findRepliesByParentMessageAndNotDeleted(rootMessage);

            assertThat(replies).hasSize(1);
            assertThat(replies.get(0).getId()).isEqualTo(replyMessage.getId());
            assertThat(replies.get(0).getParentMessage().getId()).isEqualTo(rootMessage.getId());
        }

        @Test
        @DisplayName("Should count replies by parent message")
        void shouldCountRepliesByParentMessage() {
            long count = messageRepository.countRepliesByParentMessageAndNotDeleted(rootMessage);

            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return empty list for message with no replies")
        void shouldReturnEmptyListForMessageWithNoReplies() {
            Message messageWithoutReplies = Message.builder()
                    .user(author)
                    .boardOwner(boardOwner)
                    .content("No replies message")
                    .isDeleted(false)
                    .build();
            messageWithoutReplies = entityManager.persistAndFlush(messageWithoutReplies);

            List<Message> replies = messageRepository.findRepliesByParentMessageAndNotDeleted(messageWithoutReplies);

            assertThat(replies).isEmpty();
        }
    }

    @Nested
    @DisplayName("User Queries")
    class UserQueries {

        @Test
        @DisplayName("Should find messages by user")
        void shouldFindMessagesByUser() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Message> messages = messageRepository.findByUserAndNotDeleted(author, pageable);

            assertThat(messages.getContent()).hasSize(1);
            assertThat(messages.getContent().get(0).getId()).isEqualTo(rootMessage.getId());
            assertThat(messages.getContent().get(0).getUser().getId()).isEqualTo(author.getId());
        }

        @Test
        @DisplayName("Should check if user has messages on board")
        void shouldCheckIfUserHasMessagesOnBoard() {
            boolean hasMessages = messageRepository.existsByUserAndBoardOwnerAndNotDeleted(author, boardOwner);
            boolean hasNoMessages = messageRepository.existsByUserAndBoardOwnerAndNotDeleted(otherUser, boardOwner);

            assertThat(hasMessages).isTrue();
            assertThat(hasNoMessages).isFalse();
        }
    }

    @Nested
    @DisplayName("Search and Filter Queries")
    class SearchAndFilterQueries {

        @Test
        @DisplayName("Should search messages by content")
        void shouldSearchMessagesByContent() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Message> messages = messageRepository.searchByBoardOwnerAndContent(boardOwner, "Root", pageable);

            assertThat(messages.getContent()).hasSize(1);
            assertThat(messages.getContent().get(0).getContent()).contains("Root");
        }

        @Test
        @DisplayName("Should find messages created after specific date")
        void shouldFindMessagesCreatedAfterSpecificDate() {
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            List<Message> messages = messageRepository.findByBoardOwnerAndCreatedAfterAndNotDeleted(boardOwner, yesterday);

            assertThat(messages).hasSize(2);
            assertThat(messages).extracting(Message::getId)
                    .containsExactlyInAnyOrder(rootMessage.getId(), replyMessage.getId());
        }

        @Test
        @DisplayName("Should find recent root messages")
        void shouldFindRecentRootMessages() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Message> messages = messageRepository.findRecentRootMessages(pageable);

            assertThat(messages.getContent()).hasSize(1);
            assertThat(messages.getContent().get(0).isRootMessage()).isTrue();
            assertThat(messages.getContent().get(0).getId()).isEqualTo(rootMessage.getId());
        }
    }

    @Nested
    @DisplayName("Cleanup Queries")
    class CleanupQueries {

        @Test
        @DisplayName("Should find deleted messages older than cutoff date")
        void shouldFindDeletedMessagesOlderThanCutoffDate() {
            LocalDateTime futureDate = LocalDateTime.now().plusDays(1);
            List<Message> oldDeletedMessages = messageRepository.findDeletedMessagesOlderThan(futureDate);

            assertThat(oldDeletedMessages).hasSize(1);
            assertThat(oldDeletedMessages.get(0).getId()).isEqualTo(deletedMessage.getId());
            assertThat(oldDeletedMessages.get(0).isDeleted()).isTrue();
        }

        @Test
        @DisplayName("Should not find deleted messages newer than cutoff date")
        void shouldNotFindDeletedMessagesNewerThanCutoffDate() {
            LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
            List<Message> oldDeletedMessages = messageRepository.findDeletedMessagesOlderThan(pastDate);

            assertThat(oldDeletedMessages).isEmpty();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle empty results gracefully")
        void shouldHandleEmptyResultsGracefully() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Message> messages = messageRepository.findByBoardOwnerAndNotDeleted(otherUser, pageable);

            assertThat(messages.getContent()).isEmpty();
            assertThat(messages.getTotalElements()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle pagination correctly")
        void shouldHandlePaginationCorrectly() {
            // Create additional messages
            for (int i = 0; i < 5; i++) {
                Message msg = Message.builder()
                        .user(author)
                        .boardOwner(boardOwner)
                        .content("Message " + i)
                        .isDeleted(false)
                        .build();
                entityManager.persistAndFlush(msg);
            }

            Pageable firstPage = PageRequest.of(0, 3);
            Pageable secondPage = PageRequest.of(1, 3);

            Page<Message> page1 = messageRepository.findByBoardOwnerAndNotDeleted(boardOwner, firstPage);
            Page<Message> page2 = messageRepository.findByBoardOwnerAndNotDeleted(boardOwner, secondPage);

            assertThat(page1.getContent()).hasSize(3);
            assertThat(page2.getContent()).hasSize(3);
            assertThat(page1.getTotalElements()).isEqualTo(7); // 2 original + 5 new
            assertThat(page1.getTotalPages()).isEqualTo(3);
        }
    }
}