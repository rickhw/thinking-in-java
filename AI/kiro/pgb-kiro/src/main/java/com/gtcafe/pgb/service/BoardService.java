package com.gtcafe.pgb.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.gtcafe.pgb.entity.Message;
import com.gtcafe.pgb.entity.User;

/**
 * Service interface for Board operations Manages user message boards with view permissions and
 * statistics
 */
public interface BoardService {

    /**
     * Get messages on a user's board with pagination and sorting
     * 
     * @param boardOwner the board owner
     * @param viewer the user viewing the board (for permission checks)
     * @param pageable pagination and sorting information
     * @return page of messages on the board
     * @throws SecurityException if viewer doesn't have permission to view the board
     */
    Page<Message> getBoardMessages(User boardOwner, User viewer, Pageable pageable);

    /**
     * Get all messages (including replies) on a user's board with pagination
     * 
     * @param boardOwner the board owner
     * @param viewer the user viewing the board
     * @param pageable pagination and sorting information
     * @return page of all messages on the board
     * @throws SecurityException if viewer doesn't have permission to view the board
     */
    Page<Message> getAllBoardMessages(User boardOwner, User viewer, Pageable pageable);

    /**
     * Get board statistics for a user
     * 
     * @param boardOwner the board owner
     * @param viewer the user requesting statistics
     * @return board statistics
     * @throws SecurityException if viewer doesn't have permission to view the board
     */
    BoardStatistics getBoardStatistics(User boardOwner, User viewer);

    /**
     * Search messages on a user's board
     * 
     * @param boardOwner the board owner
     * @param viewer the user performing the search
     * @param keyword the search keyword
     * @param pageable pagination and sorting information
     * @return page of matching messages
     * @throws SecurityException if viewer doesn't have permission to view the board
     */
    Page<Message> searchBoardMessages(User boardOwner, User viewer, String keyword,
            Pageable pageable);

    /**
     * Check if a user can view another user's board
     * 
     * @param boardOwner the board owner
     * @param viewer the potential viewer
     * @return true if viewer can access the board
     */
    boolean canViewBoard(User boardOwner, User viewer);

    /**
     * Get recent activity on a user's board
     * 
     * @param boardOwner the board owner
     * @param viewer the user viewing the activity
     * @param limit maximum number of recent activities to return
     * @return list of recent messages
     * @throws SecurityException if viewer doesn't have permission to view the board
     */
    List<Message> getRecentBoardActivity(User boardOwner, User viewer, int limit);

    /**
     * Get board messages with their replies (optimized for display)
     * 
     * @param boardOwner the board owner
     * @param viewer the user viewing the board
     * @return list of messages with their replies
     * @throws SecurityException if viewer doesn't have permission to view the board
     */
    List<Message> getBoardMessagesWithReplies(User boardOwner, User viewer);

    /**
     * Get boards where a user has posted messages
     * 
     * @param user the user
     * @param pageable pagination information
     * @return page of users whose boards the user has posted on
     */
    Page<User> getBoardsWithUserActivity(User user, Pageable pageable);

    /**
     * Get popular boards (boards with most recent activity)
     * 
     * @param viewer the user requesting popular boards
     * @param pageable pagination information
     * @return page of popular board owners
     */
    Page<User> getPopularBoards(User viewer, Pageable pageable);

    /**
     * Validate board access permissions
     * 
     * @param boardOwner the board owner
     * @param viewer the user attempting to view
     * @throws SecurityException if access is denied
     */
    void validateBoardAccess(User boardOwner, User viewer);

    /**
     * Inner class for board statistics
     */
    class BoardStatistics {
        private final long totalMessages;
        private final long totalReplies;
        private final long uniqueContributors;
        private final LocalDateTime lastActivityTime;
        private final LocalDateTime boardCreatedTime;

        public BoardStatistics(long totalMessages, long totalReplies, long uniqueContributors,
                LocalDateTime lastActivityTime, LocalDateTime boardCreatedTime) {
            this.totalMessages = totalMessages;
            this.totalReplies = totalReplies;
            this.uniqueContributors = uniqueContributors;
            this.lastActivityTime = lastActivityTime;
            this.boardCreatedTime = boardCreatedTime;
        }

        public long getTotalMessages() {
            return totalMessages;
        }

        public long getTotalReplies() {
            return totalReplies;
        }

        public long getUniqueContributors() {
            return uniqueContributors;
        }

        public LocalDateTime getLastActivityTime() {
            return lastActivityTime;
        }

        public LocalDateTime getBoardCreatedTime() {
            return boardCreatedTime;
        }

        public long getTotalPosts() {
            return totalMessages + totalReplies;
        }
    }
}
