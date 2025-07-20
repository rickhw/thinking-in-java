package com.gtcafe.pgb.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.gtcafe.pgb.entity.Message;
import com.gtcafe.pgb.entity.User;

/**
 * Service interface for Message operations
 */
public interface MessageService {

    /**
     * Create a new message on a user's board
     * 
     * @param author the message author
     * @param boardOwner the board owner
     * @param content the message content
     * @return the created message
     * @throws IllegalArgumentException if content is invalid or exceeds limit
     */
    Message createMessage(User author, User boardOwner, String content);

    /**
     * Create a reply to an existing message
     * 
     * @param author the reply author
     * @param parentMessage the parent message
     * @param content the reply content
     * @return the created reply message
     * @throws IllegalArgumentException if content is invalid or parent message not found
     */
    Message createReply(User author, Message parentMessage, String content);

    /**
     * Update an existing message
     * 
     * @param messageId the message ID
     * @param author the user attempting to update (must be the author)
     * @param content the new content
     * @return the updated message
     * @throws IllegalArgumentException if content is invalid
     * @throws SecurityException if user is not the author
     * @throws RuntimeException if message not found
     */
    Message updateMessage(Long messageId, User author, String content);

    /**
     * Delete a message (soft delete)
     * 
     * @param messageId the message ID
     * @param user the user attempting to delete (must be author or board owner)
     * @throws SecurityException if user doesn't have permission
     * @throws RuntimeException if message not found
     */
    void deleteMessage(Long messageId, User user);

    /**
     * Find message by ID
     * 
     * @param messageId the message ID
     * @return the message if found and not deleted
     */
    Optional<Message> findById(Long messageId);

    /**
     * Get all messages on a user's board (root messages only, with pagination)
     * 
     * @param boardOwner the board owner
     * @param pageable pagination information
     * @return page of root messages
     */
    Page<Message> getBoardMessages(User boardOwner, Pageable pageable);

    /**
     * Get all messages on a user's board including replies (with pagination)
     * 
     * @param boardOwner the board owner
     * @param pageable pagination information
     * @return page of all messages
     */
    Page<Message> getAllBoardMessages(User boardOwner, Pageable pageable);

    /**
     * Get replies to a specific message
     * 
     * @param parentMessage the parent message
     * @return list of replies ordered by creation time
     */
    List<Message> getReplies(Message parentMessage);

    /**
     * Get messages by a specific user (with pagination)
     * 
     * @param user the message author
     * @param pageable pagination information
     * @return page of user's messages
     */
    Page<Message> getUserMessages(User user, Pageable pageable);

    /**
     * Search messages on a user's board by content
     * 
     * @param boardOwner the board owner
     * @param keyword the search keyword
     * @param pageable pagination information
     * @return page of matching messages
     */
    Page<Message> searchBoardMessages(User boardOwner, String keyword, Pageable pageable);

    /**
     * Get recent messages across all boards (for activity feed)
     * 
     * @param pageable pagination information
     * @return page of recent root messages
     */
    Page<Message> getRecentMessages(Pageable pageable);

    /**
     * Count total messages on a user's board (root messages only)
     * 
     * @param boardOwner the board owner
     * @return total message count
     */
    long countBoardMessages(User boardOwner);

    /**
     * Count replies to a specific message
     * 
     * @param parentMessage the parent message
     * @return reply count
     */
    long countReplies(Message parentMessage);

    /**
     * Check if a user can edit a message
     * 
     * @param message the message
     * @param user the user
     * @return true if user can edit the message
     */
    boolean canEditMessage(Message message, User user);

    /**
     * Check if a user can delete a message
     * 
     * @param message the message
     * @param user the user
     * @return true if user can delete the message
     */
    boolean canDeleteMessage(Message message, User user);

    /**
     * Validate message content
     * 
     * @param content the message content
     * @throws IllegalArgumentException if content is invalid
     */
    void validateMessageContent(String content);

    /**
     * Get messages with their replies for a specific board (optimized for display)
     * 
     * @param boardOwner the board owner
     * @return list of messages with replies
     */
    List<Message> getBoardMessagesWithReplies(User boardOwner);

    /**
     * Check if a user has posted any messages on a specific board
     * 
     * @param user the user
     * @param boardOwner the board owner
     * @return true if user has posted messages on the board
     */
    boolean hasUserPostedOnBoard(User user, User boardOwner);

    // Enhanced reply functionality for task 6.2

    /**
     * Check if a user can reply to a message
     * 
     * @param message the message to reply to
     * @param user the user attempting to reply
     * @return true if user can reply to the message
     */
    boolean canReplyToMessage(Message message, User user);

    /**
     * Get the reply depth/level of a message
     * 
     * @param message the message
     * @return the depth level (0 for root messages, 1 for direct replies, etc.)
     */
    int getMessageDepth(Message message);

    /**
     * Get all replies in a thread (including nested replies)
     * 
     * @param rootMessage the root message
     * @return list of all replies in the thread, ordered by creation time
     */
    List<Message> getThreadReplies(Message rootMessage);

    /**
     * Get replies with pagination
     * 
     * @param parentMessage the parent message
     * @param pageable pagination information
     * @return page of replies
     */
    Page<Message> getRepliesWithPagination(Message parentMessage, Pageable pageable);

    /**
     * Delete all replies to a message (cascade delete)
     * 
     * @param parentMessage the parent message
     * @param user the user attempting to delete (must have permission)
     * @throws SecurityException if user doesn't have permission
     */
    void deleteAllReplies(Message parentMessage, User user);

    /**
     * Get reply statistics for a message
     * 
     * @param message the message
     * @return reply statistics including total count, unique authors, etc.
     */
    ReplyStatistics getReplyStatistics(Message message);

    /**
     * Inner class for reply statistics
     */
    class ReplyStatistics {
        private final long totalReplies;
        private final long uniqueAuthors;
        private final LocalDateTime lastReplyTime;

        public ReplyStatistics(long totalReplies, long uniqueAuthors, LocalDateTime lastReplyTime) {
            this.totalReplies = totalReplies;
            this.uniqueAuthors = uniqueAuthors;
            this.lastReplyTime = lastReplyTime;
        }

        public long getTotalReplies() {
            return totalReplies;
        }

        public long getUniqueAuthors() {
            return uniqueAuthors;
        }

        public LocalDateTime getLastReplyTime() {
            return lastReplyTime;
        }
    }
}
