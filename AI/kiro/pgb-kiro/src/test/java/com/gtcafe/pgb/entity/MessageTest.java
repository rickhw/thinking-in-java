package com.gtcafe.pgb.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Message entity
 */
@DisplayName("Message Entity Tests")
class MessageTest {

    private User author;
    private User boardOwner;
    private Message message;

    @BeforeEach
    void setUp() {
        author = User.builder()
                .id(1L)
                .ssoId("sso-123")
                .username("author")
                .email("author@example.com")
                .displayName("Author User")
                .isActive(true)
                .build();

        boardOwner = User.builder()
                .id(2L)
                .ssoId("sso-456")
                .username("boardowner")
                .email("boardowner@example.com")
                .displayName("Board Owner")
                .isActive(true)
                .build();

        message = Message.builder()
                .id(1L)
                .user(author)
                .boardOwner(boardOwner)
                .content("Test message content")
                .isDeleted(false)
                .replies(new ArrayList<>())
                .build();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create message with required fields")
        void shouldCreateMessageWithRequiredFields() {
            Message newMessage = new Message(author, boardOwner, "New message");

            assertThat(newMessage.getUser()).isEqualTo(author);
            assertThat(newMessage.getBoardOwner()).isEqualTo(boardOwner);
            assertThat(newMessage.getContent()).isEqualTo("New message");
            assertThat(newMessage.isDeleted()).isFalse();
            assertThat(newMessage.getParentMessage()).isNull();
        }

        @Test
        @DisplayName("Should create reply message with parent")
        void shouldCreateReplyMessageWithParent() {
            Message parentMessage = new Message(boardOwner, boardOwner, "Parent message");
            Message replyMessage = new Message(author, boardOwner, "Reply message", parentMessage);

            assertThat(replyMessage.getUser()).isEqualTo(author);
            assertThat(replyMessage.getBoardOwner()).isEqualTo(boardOwner);
            assertThat(replyMessage.getContent()).isEqualTo("Reply message");
            assertThat(replyMessage.getParentMessage()).isEqualTo(parentMessage);
            assertThat(replyMessage.isReply()).isTrue();
            assertThat(replyMessage.isRootMessage()).isFalse();
        }

        @Test
        @DisplayName("Should create message using builder")
        void shouldCreateMessageUsingBuilder() {
            Message builtMessage = Message.builder()
                    .user(author)
                    .boardOwner(boardOwner)
                    .content("Built message")
                    .build();

            assertThat(builtMessage.getUser()).isEqualTo(author);
            assertThat(builtMessage.getBoardOwner()).isEqualTo(boardOwner);
            assertThat(builtMessage.getContent()).isEqualTo("Built message");
            assertThat(builtMessage.isDeleted()).isFalse();
            assertThat(builtMessage.getReplies()).isNotNull().isEmpty();
        }
    }

    @Nested
    @DisplayName("Message Type Tests")
    class MessageTypeTests {

        @Test
        @DisplayName("Should identify root message correctly")
        void shouldIdentifyRootMessageCorrectly() {
            assertThat(message.isRootMessage()).isTrue();
            assertThat(message.isReply()).isFalse();
            assertThat(message.getParentMessage()).isNull();
        }

        @Test
        @DisplayName("Should identify reply message correctly")
        void shouldIdentifyReplyMessageCorrectly() {
            Message parentMessage = new Message(boardOwner, boardOwner, "Parent");
            Message replyMessage = Message.builder()
                    .user(author)
                    .boardOwner(boardOwner)
                    .content("Reply")
                    .parentMessage(parentMessage)
                    .build();

            assertThat(replyMessage.isReply()).isTrue();
            assertThat(replyMessage.isRootMessage()).isFalse();
            assertThat(replyMessage.getParentMessage()).isEqualTo(parentMessage);
        }
    }

    @Nested
    @DisplayName("Reply Management Tests")
    class ReplyManagementTests {

        @Test
        @DisplayName("Should add reply correctly")
        void shouldAddReplyCorrectly() {
            Message reply = new Message(author, boardOwner, "Reply content");

            message.addReply(reply);

            assertThat(message.hasReplies()).isTrue();
            assertThat(message.getReplyCount()).isEqualTo(1);
            assertThat(message.getReplies()).contains(reply);
            assertThat(reply.getParentMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("Should remove reply correctly")
        void shouldRemoveReplyCorrectly() {
            Message reply = new Message(author, boardOwner, "Reply content");
            message.addReply(reply);

            message.removeReply(reply);

            assertThat(message.hasReplies()).isFalse();
            assertThat(message.getReplyCount()).isEqualTo(0);
            assertThat(message.getReplies()).doesNotContain(reply);
            assertThat(reply.getParentMessage()).isNull();
        }

        @Test
        @DisplayName("Should handle multiple replies")
        void shouldHandleMultipleReplies() {
            Message reply1 = new Message(author, boardOwner, "First reply");
            Message reply2 = new Message(boardOwner, boardOwner, "Second reply");

            message.addReply(reply1);
            message.addReply(reply2);

            assertThat(message.getReplyCount()).isEqualTo(2);
            assertThat(message.getReplies()).containsExactly(reply1, reply2);
            assertThat(reply1.getParentMessage()).isEqualTo(message);
            assertThat(reply2.getParentMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("Should handle null replies list")
        void shouldHandleNullRepliesList() {
            Message messageWithNullReplies = Message.builder()
                    .user(author)
                    .boardOwner(boardOwner)
                    .content("Test")
                    .replies(null)
                    .build();

            assertThat(messageWithNullReplies.hasReplies()).isFalse();
            assertThat(messageWithNullReplies.getReplyCount()).isEqualTo(0);

            Message reply = new Message(author, boardOwner, "Reply");
            messageWithNullReplies.addReply(reply);

            assertThat(messageWithNullReplies.hasReplies()).isTrue();
            assertThat(messageWithNullReplies.getReplyCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Authorization Tests")
    class AuthorizationTests {

        @Test
        @DisplayName("Should identify message author correctly")
        void shouldIdentifyMessageAuthorCorrectly() {
            assertThat(message.isAuthor(author)).isTrue();
            assertThat(message.isAuthor(boardOwner)).isFalse();
        }

        @Test
        @DisplayName("Should identify board owner correctly")
        void shouldIdentifyBoardOwnerCorrectly() {
            assertThat(message.isBoardOwner(boardOwner)).isTrue();
            assertThat(message.isBoardOwner(author)).isFalse();
        }

        @Test
        @DisplayName("Should handle null user in authorization checks")
        void shouldHandleNullUserInAuthorizationChecks() {
            assertThat(message.isAuthor(null)).isFalse();
            assertThat(message.isBoardOwner(null)).isFalse();
        }

        @Test
        @DisplayName("Should handle message with null user")
        void shouldHandleMessageWithNullUser() {
            Message messageWithNullUser = Message.builder()
                    .user(null)
                    .boardOwner(boardOwner)
                    .content("Test")
                    .build();

            assertThat(messageWithNullUser.isAuthor(author)).isFalse();
            assertThat(messageWithNullUser.isAuthor(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Deletion Tests")
    class DeletionTests {

        @Test
        @DisplayName("Should soft delete message")
        void shouldSoftDeleteMessage() {
            assertThat(message.isDeleted()).isFalse();

            message.softDelete();

            assertThat(message.isDeleted()).isTrue();
            assertThat(message.getIsDeleted()).isTrue();
        }

        @Test
        @DisplayName("Should handle null isDeleted field")
        void shouldHandleNullIsDeletedField() {
            Message messageWithNullDeleted = Message.builder()
                    .user(author)
                    .boardOwner(boardOwner)
                    .content("Test")
                    .isDeleted(null)
                    .build();

            assertThat(messageWithNullDeleted.isDeleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should validate required fields")
        void shouldValidateRequiredFields() {
            // These tests would typically be run with a validation framework
            // Here we're just testing the entity structure
            assertThat(message.getUser()).isNotNull();
            assertThat(message.getBoardOwner()).isNotNull();
            assertThat(message.getContent()).isNotBlank();
        }

        @Test
        @DisplayName("Should handle long content")
        void shouldHandleLongContent() {
            String longContent = "a".repeat(1000);
            message.setContent(longContent);

            assertThat(message.getContent()).hasSize(1000);
        }
    }

    @Nested
    @DisplayName("Timestamp Tests")
    class TimestampTests {

        @Test
        @DisplayName("Should handle timestamp fields")
        void shouldHandleTimestampFields() {
            LocalDateTime now = LocalDateTime.now();
            message.setCreatedAt(now);
            message.setUpdatedAt(now);

            assertThat(message.getCreatedAt()).isEqualTo(now);
            assertThat(message.getUpdatedAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should implement equals correctly")
        void shouldImplementEqualsCorrectly() {
            Message message1 = Message.builder()
                    .id(1L)
                    .user(author)
                    .boardOwner(boardOwner)
                    .content("Test")
                    .build();

            Message message2 = Message.builder()
                    .id(1L)
                    .user(author)
                    .boardOwner(boardOwner)
                    .content("Test")
                    .build();

            Message message3 = Message.builder()
                    .id(2L)
                    .user(author)
                    .boardOwner(boardOwner)
                    .content("Different")
                    .build();

            assertThat(message1).isEqualTo(message2);
            assertThat(message1).isNotEqualTo(message3);
            assertThat(message1.hashCode()).isEqualTo(message2.hashCode());
        }
    }
}