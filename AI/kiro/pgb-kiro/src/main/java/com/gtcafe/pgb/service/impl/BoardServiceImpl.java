package com.gtcafe.pgb.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gtcafe.pgb.entity.Message;
import com.gtcafe.pgb.entity.User;
import com.gtcafe.pgb.repository.MessageRepository;
import com.gtcafe.pgb.repository.UserRepository;
import com.gtcafe.pgb.service.BoardService;
import com.gtcafe.pgb.service.CacheService;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of BoardService for managing user message boards Provides board viewing
 * permissions, pagination, and caching functionality
 */
@Service
@Transactional
@Slf4j
public class BoardServiceImpl implements BoardService {

    private static final String BOARD_MESSAGES_CACHE_KEY = "board:%d:messages";
    private static final String BOARD_ALL_MESSAGES_CACHE_KEY = "board:%d:all_messages";
    private static final String BOARD_STATS_CACHE_KEY = "board:%d:stats";
    private static final String BOARD_SEARCH_CACHE_KEY = "board:%d:search:%s";
    private static final String BOARD_RECENT_CACHE_KEY = "board:%d:recent:%d";
    private static final String POPULAR_BOARDS_CACHE_KEY = "popular_boards";

    private static final Duration CACHE_DURATION = Duration.ofMinutes(10);
    private static final Duration STATS_CACHE_DURATION = Duration.ofMinutes(30);
    private static final Duration SEARCH_CACHE_DURATION = Duration.ofMinutes(5);

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheService cacheService;

    @Override
    @Transactional(readOnly = true)
    public Page<Message> getBoardMessages(User boardOwner, User viewer, Pageable pageable) {
        log.debug("Getting board messages for owner: {} by viewer: {}", boardOwner.getUsername(),
                viewer.getUsername());

        validateBoardAccess(boardOwner, viewer);

        String cacheKey = String.format(BOARD_MESSAGES_CACHE_KEY, boardOwner.getId()) + ":"
                + pageable.getPageNumber() + ":" + pageable.getPageSize();

        @SuppressWarnings("unchecked")
        Page<Message> cachedMessages = cacheService.get(cacheKey, Page.class);
        if (cachedMessages != null) {
            log.debug("Returning cached board messages for user: {}", boardOwner.getUsername());
            return cachedMessages;
        }

        Page<Message> messages = messageRepository
                .findByBoardOwnerAndNotDeletedAndParentMessageIsNull(boardOwner, pageable);

        cacheService.set(cacheKey, messages, CACHE_DURATION);
        log.debug("Cached board messages for user: {}", boardOwner.getUsername());

        return messages;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Message> getAllBoardMessages(User boardOwner, User viewer, Pageable pageable) {
        log.debug("Getting all board messages (including replies) for owner: {} by viewer: {}",
                boardOwner.getUsername(), viewer.getUsername());

        validateBoardAccess(boardOwner, viewer);

        String cacheKey = String.format(BOARD_ALL_MESSAGES_CACHE_KEY, boardOwner.getId()) + ":"
                + pageable.getPageNumber() + ":" + pageable.getPageSize();

        @SuppressWarnings("unchecked")
        Page<Message> cachedMessages = cacheService.get(cacheKey, Page.class);
        if (cachedMessages != null) {
            log.debug("Returning cached all board messages for user: {}", boardOwner.getUsername());
            return cachedMessages;
        }

        Page<Message> messages =
                messageRepository.findByBoardOwnerAndNotDeleted(boardOwner, pageable);

        cacheService.set(cacheKey, messages, CACHE_DURATION);
        log.debug("Cached all board messages for user: {}", boardOwner.getUsername());

        return messages;
    }

    @Override
    @Transactional(readOnly = true)
    public BoardStatistics getBoardStatistics(User boardOwner, User viewer) {
        log.debug("Getting board statistics for owner: {} by viewer: {}", boardOwner.getUsername(),
                viewer.getUsername());

        validateBoardAccess(boardOwner, viewer);

        String cacheKey = String.format(BOARD_STATS_CACHE_KEY, boardOwner.getId());

        BoardStatistics cachedStats = cacheService.get(cacheKey, BoardStatistics.class);
        if (cachedStats != null) {
            log.debug("Returning cached board statistics for user: {}", boardOwner.getUsername());
            return cachedStats;
        }

        // Get total root messages count
        long totalMessages =
                messageRepository.countByBoardOwnerAndNotDeletedAndParentMessageIsNull(boardOwner);

        // Get all messages to calculate replies and unique contributors
        List<Message> allMessages = messageRepository
                .findByBoardOwnerAndNotDeleted(boardOwner, Pageable.unpaged()).getContent();

        long totalReplies = allMessages.stream().filter(Message::isReply).count();

        long uniqueContributors =
                allMessages.stream().map(message -> message.getUser().getId()).distinct().count();

        LocalDateTime lastActivityTime = allMessages.stream().map(Message::getCreatedAt)
                .max(LocalDateTime::compareTo).orElse(null);

        BoardStatistics stats = new BoardStatistics(totalMessages, totalReplies, uniqueContributors,
                lastActivityTime, boardOwner.getCreatedAt());

        cacheService.set(cacheKey, stats, STATS_CACHE_DURATION);
        log.debug("Cached board statistics for user: {}", boardOwner.getUsername());

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Message> searchBoardMessages(User boardOwner, User viewer, String keyword,
            Pageable pageable) {
        log.debug("Searching board messages for owner: {} by viewer: {} with keyword: {}",
                boardOwner.getUsername(), viewer.getUsername(), keyword);

        validateBoardAccess(boardOwner, viewer);

        if (keyword == null || keyword.trim().isEmpty()) {
            return getBoardMessages(boardOwner, viewer, pageable);
        }

        String cacheKey = String.format(BOARD_SEARCH_CACHE_KEY, boardOwner.getId(), keyword.trim())
                + ":" + pageable.getPageNumber() + ":" + pageable.getPageSize();

        @SuppressWarnings("unchecked")
        Page<Message> cachedResults = cacheService.get(cacheKey, Page.class);
        if (cachedResults != null) {
            log.debug("Returning cached search results for user: {} keyword: {}",
                    boardOwner.getUsername(), keyword);
            return cachedResults;
        }

        Page<Message> searchResults = messageRepository.searchByBoardOwnerAndContent(boardOwner,
                keyword.trim(), pageable);

        cacheService.set(cacheKey, searchResults, SEARCH_CACHE_DURATION);
        log.debug("Cached search results for user: {} keyword: {}", boardOwner.getUsername(),
                keyword);

        return searchResults;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canViewBoard(User boardOwner, User viewer) {
        if (boardOwner == null || viewer == null) {
            return false;
        }

        // Users can always view their own board
        if (boardOwner.getId().equals(viewer.getId())) {
            return true;
        }

        // Check if both users are active
        if (!boardOwner.isActive() || !viewer.isActive()) {
            return false;
        }

        // For MVP, all active users can view each other's boards
        // This can be extended later with privacy settings
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getRecentBoardActivity(User boardOwner, User viewer, int limit) {
        log.debug("Getting recent board activity for owner: {} by viewer: {} limit: {}",
                boardOwner.getUsername(), viewer.getUsername(), limit);

        validateBoardAccess(boardOwner, viewer);

        String cacheKey = String.format(BOARD_RECENT_CACHE_KEY, boardOwner.getId(), limit);

        @SuppressWarnings("unchecked")
        List<Message> cachedActivity = cacheService.get(cacheKey, List.class);
        if (cachedActivity != null) {
            log.debug("Returning cached recent activity for user: {}", boardOwner.getUsername());
            return cachedActivity;
        }

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Message> recentMessages =
                messageRepository.findByBoardOwnerAndNotDeleted(boardOwner, pageable);

        List<Message> activity = recentMessages.getContent();

        cacheService.set(cacheKey, activity, CACHE_DURATION);
        log.debug("Cached recent activity for user: {}", boardOwner.getUsername());

        return activity;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getBoardMessagesWithReplies(User boardOwner, User viewer) {
        log.debug("Getting board messages with replies for owner: {} by viewer: {}",
                boardOwner.getUsername(), viewer.getUsername());

        validateBoardAccess(boardOwner, viewer);

        String cacheKey =
                String.format(BOARD_MESSAGES_CACHE_KEY, boardOwner.getId()) + ":with_replies";

        @SuppressWarnings("unchecked")
        List<Message> cachedMessages = cacheService.get(cacheKey, List.class);
        if (cachedMessages != null) {
            log.debug("Returning cached messages with replies for user: {}",
                    boardOwner.getUsername());
            return cachedMessages;
        }

        List<Message> messagesWithReplies =
                messageRepository.findByBoardOwnerWithReplies(boardOwner);

        cacheService.set(cacheKey, messagesWithReplies, CACHE_DURATION);
        log.debug("Cached messages with replies for user: {}", boardOwner.getUsername());

        return messagesWithReplies;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getBoardsWithUserActivity(User user, Pageable pageable) {
        log.debug("Getting boards with activity for user: {}", user.getUsername());

        // Get all messages by the user
        Page<Message> userMessages = messageRepository.findByUserAndNotDeleted(user,
                PageRequest.of(0, Integer.MAX_VALUE));

        // Extract unique board owners
        List<User> boardOwners = userMessages.getContent().stream().map(Message::getBoardOwner)
                .distinct().collect(Collectors.toList());

        // Apply pagination manually since we need to deduplicate
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), boardOwners.size());

        List<User> paginatedBoardOwners = boardOwners.subList(start, end);

        return new PageImpl<>(paginatedBoardOwners, pageable, boardOwners.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getPopularBoards(User viewer, Pageable pageable) {
        log.debug("Getting popular boards for viewer: {}", viewer.getUsername());

        String cacheKey = POPULAR_BOARDS_CACHE_KEY + ":" + pageable.getPageNumber() + ":"
                + pageable.getPageSize();

        @SuppressWarnings("unchecked")
        Page<User> cachedPopularBoards = cacheService.get(cacheKey, Page.class);
        if (cachedPopularBoards != null) {
            log.debug("Returning cached popular boards");
            return cachedPopularBoards;
        }

        // Get recent messages to determine popular boards
        Pageable recentMessagesPageable =
                PageRequest.of(0, 1000, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Message> recentMessages =
                messageRepository.findRecentRootMessages(recentMessagesPageable);

        // Group by board owner and count activity
        List<User> popularBoardOwners = recentMessages.getContent().stream()
                .collect(Collectors.groupingBy(Message::getBoardOwner, Collectors.counting()))
                .entrySet().stream().sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .map(entry -> entry.getKey()).filter(boardOwner -> canViewBoard(boardOwner, viewer))
                .collect(Collectors.toList());

        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), popularBoardOwners.size());

        List<User> paginatedPopularBoards = popularBoardOwners.subList(start, end);
        Page<User> popularBoards =
                new PageImpl<>(paginatedPopularBoards, pageable, popularBoardOwners.size());

        cacheService.set(cacheKey, popularBoards, CACHE_DURATION);
        log.debug("Cached popular boards");

        return popularBoards;
    }

    @Override
    public void validateBoardAccess(User boardOwner, User viewer) {
        if (boardOwner == null) {
            throw new IllegalArgumentException("Board owner cannot be null");
        }

        if (viewer == null) {
            throw new SecurityException("Viewer cannot be null");
        }

        if (!canViewBoard(boardOwner, viewer)) {
            throw new SecurityException(
                    String.format("User %s does not have permission to view board of user %s",
                            viewer.getUsername(), boardOwner.getUsername()));
        }
    }

    /**
     * Clear cache for a specific board when messages are updated
     */
    public void clearBoardCache(Long boardOwnerId) {
        log.debug("Clearing cache for board owner: {}", boardOwnerId);

        String pattern = String.format("board:%d:*", boardOwnerId);
        cacheService.clearByPattern(pattern);

        // Also clear popular boards cache as it might be affected
        cacheService.clearByPattern(POPULAR_BOARDS_CACHE_KEY + "*");

        log.debug("Cleared cache for board owner: {}", boardOwnerId);
    }

    /**
     * Clear all board-related caches
     */
    public void clearAllBoardCaches() {
        log.debug("Clearing all board caches");

        cacheService.clearByPattern("board:*");
        cacheService.clearByPattern(POPULAR_BOARDS_CACHE_KEY + "*");

        log.debug("Cleared all board caches");
    }
}
