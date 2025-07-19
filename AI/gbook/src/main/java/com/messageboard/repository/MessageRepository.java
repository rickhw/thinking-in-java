package com.messageboard.repository;

import com.messageboard.entity.Message;
import com.messageboard.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Message entity
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Find all messages on a specific user's board (non-deleted, root messages only)
     */
    @Query("SELECT m FROM Message m WHERE m.boardOwner = :boardOwner AND m.isDeleted = false AND m.parentMessage IS NULL ORDER BY m.createdAt DESC")
    Page<Message> findByBoardOwnerAndNotDeletedAndParentMessageIsNull(@Param("boardOwner") User boardOwner, Pageable pageable);

    /**
     * Find all messages on a specific user's board including replies (non-deleted)
     */
    @Query("SELECT m FROM Message m WHERE m.boardOwner = :boardOwner AND m.isDeleted = false ORDER BY m.createdAt DESC")
    Page<Message> findByBoardOwnerAndNotDeleted(@Param("boardOwner") User boardOwner, Pageable pageable);

    /**
     * Find all replies to a specific message (non-deleted)
     */
    @Query("SELECT m FROM Message m WHERE m.parentMessage = :parentMessage AND m.isDeleted = false ORDER BY m.createdAt ASC")
    List<Message> findRepliesByParentMessageAndNotDeleted(@Param("parentMessage") Message parentMessage);

    /**
     * Find all messages by a specific user (non-deleted)
     */
    @Query("SELECT m FROM Message m WHERE m.user = :user AND m.isDeleted = false ORDER BY m.createdAt DESC")
    Page<Message> findByUserAndNotDeleted(@Param("user") User user, Pageable pageable);

    /**
     * Find a message by ID if not deleted
     */
    @Query("SELECT m FROM Message m WHERE m.id = :id AND m.isDeleted = false")
    Optional<Message> findByIdAndNotDeleted(@Param("id") Long id);

    /**
     * Count total messages on a user's board (non-deleted, root messages only)
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.boardOwner = :boardOwner AND m.isDeleted = false AND m.parentMessage IS NULL")
    long countByBoardOwnerAndNotDeletedAndParentMessageIsNull(@Param("boardOwner") User boardOwner);

    /**
     * Count total replies to a specific message (non-deleted)
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.parentMessage = :parentMessage AND m.isDeleted = false")
    long countRepliesByParentMessageAndNotDeleted(@Param("parentMessage") Message parentMessage);

    /**
     * Find messages created after a specific date on a user's board
     */
    @Query("SELECT m FROM Message m WHERE m.boardOwner = :boardOwner AND m.createdAt > :since AND m.isDeleted = false ORDER BY m.createdAt DESC")
    List<Message> findByBoardOwnerAndCreatedAfterAndNotDeleted(@Param("boardOwner") User boardOwner, @Param("since") LocalDateTime since);

    /**
     * Find recent messages across all boards (for activity feed)
     */
    @Query("SELECT m FROM Message m WHERE m.isDeleted = false AND m.parentMessage IS NULL ORDER BY m.createdAt DESC")
    Page<Message> findRecentRootMessages(Pageable pageable);

    /**
     * Search messages by content on a specific user's board
     */
    @Query("SELECT m FROM Message m WHERE m.boardOwner = :boardOwner AND m.isDeleted = false AND LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY m.createdAt DESC")
    Page<Message> searchByBoardOwnerAndContent(@Param("boardOwner") User boardOwner, @Param("keyword") String keyword, Pageable pageable);

    /**
     * Find messages with their replies for a specific board (optimized for display)
     */
    @Query("SELECT DISTINCT m FROM Message m LEFT JOIN FETCH m.replies r WHERE m.boardOwner = :boardOwner AND m.isDeleted = false AND m.parentMessage IS NULL AND (r IS NULL OR r.isDeleted = false) ORDER BY m.createdAt DESC")
    List<Message> findByBoardOwnerWithReplies(@Param("boardOwner") User boardOwner);

    /**
     * Check if a user has any messages on a specific board
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Message m WHERE m.user = :user AND m.boardOwner = :boardOwner AND m.isDeleted = false")
    boolean existsByUserAndBoardOwnerAndNotDeleted(@Param("user") User user, @Param("boardOwner") User boardOwner);

    /**
     * Find all messages that need to be cleaned up (soft deleted messages older than specified date)
     */
    @Query("SELECT m FROM Message m WHERE m.isDeleted = true AND m.updatedAt < :cutoffDate")
    List<Message> findDeletedMessagesOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
}