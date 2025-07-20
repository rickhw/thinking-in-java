package com.gtcafe.pgb.service.impl;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.gtcafe.pgb.entity.Message;
import com.gtcafe.pgb.entity.User;
import com.gtcafe.pgb.repository.MessageRepository;
import com.gtcafe.pgb.service.CacheService;
import com.gtcafe.pgb.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of MessageService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final CacheService cacheService;

    // Cache key constants
    private static final String MESSAGE_CACHE_KEY = "message:";
    private static final String BOARD_MESSAGES_CACHE_KEY = "board:%d:messages";
    private static final String USER_MESSAGES_CACHE_KEY = "user:%d:messages";
    private static final String MESSAGE_REPLIES_CACHE_KEY = "message:%d:replies";
    private static final String BOARD_COUNT_CACHE_KEY = "board:%d:count";

    // Cache expiration times
    private static final Duration MESSAGE_CACHE_DURATION = Duration.ofMinutes(15);
    private static final Duration COUNT_CACHE_DURATION = Duration.ofMinutes(30);

    // Message content validation constants
    private static final int MAX_MESSAGE_LENGTH = 1000;
    private static final int MIN_MESSAGE_LENGTH = 1;

    @Override
    public Message createMessage(User author, User boardOwner, String content) {
        validateMessageContent(content);
        validateUsers(author, boardOwner);

        log.debug("Creating message by user {} on board {}", author.getId(), boardOwner.getId());

        Message message = Message.builder().user(author).boardOwner(boardOwner)
                .content(content.trim()).build();

        Message savedMessage = messageRepository.save(message);

        // Clear related caches
        clearBoardCaches(boardOwner.getId());
        clearUserCaches(author.getId());

        log.info("Created message {} by user {} on board {}", savedMessage.getId(), author.getId(),
                boardOwner.getId());

        return savedMessage;
    }

    @Override
    public Message createReply(User author, Message parentMessage, String content) {
        validateMessageContent(content);
        validateUser(author);

        if (parentMessage == null || parentMessage.isDeleted()) {
            throw new IllegalArgumentException("Parent message not found or deleted");
        }

        log.debug("Creating reply by user {} to message {}", author.getId(), parentMessage.getId());

        Message reply = Message.builder().user(author).boardOwner(parentMessage.getBoardOwner())
                .content(content.trim()).parentMessage(parentMessage).build();

        Message savedReply = messageRepository.save(reply);

        // Clear related caches
        clearMessageCaches(parentMessage.getId());
        clearBoardCaches(parentMessage.getBoardOwner().getId());
        clearUserCaches(author.getId());

        log.info("Created reply {} by user {} to message {}", savedReply.getId(), author.getId(),
                parentMessage.getId());

        return savedReply;
    }

    @Override
    public Message updateMessage(Long messageId, User author, String content) {
        validateMessageContent(content);
        validateUser(author);

        log.debug("Updating message {} by user {}", messageId, author.getId());

        Message message = findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found: " + messageId));

        if (!canEditMessage(message, author)) {
            throw new SecurityException("User does not have permission to edit this message");
        }

        message.setContent(content.trim());
        Message updatedMessage = messageRepository.save(message);

        // Clear related caches
        clearMessageCaches(messageId);
        clearBoardCaches(message.getBoardOwner().getId());

        log.info("Updated message {} by user {}", messageId, author.getId());

        return updatedMessage;
    }

    @Override
    public void deleteMessage(Long messageId, User user) {
        validateUser(user);

        log.debug("Deleting message {} by user {}", messageId, user.getId());

        Message message = findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found: " + messageId));

        if (!canDeleteMessage(message, user)) {
            throw new SecurityException("User does not have permission to delete this message");
        }

        message.softDelete();
        messageRepository.save(message);

        // Clear related caches
        clearMessageCaches(messageId);
        clearBoardCaches(message.getBoardOwner().getId());

        log.info("Deleted message {} by user {}", messageId, user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Message> findById(Long messageId) {
        if (messageId == null) {
            return Optional.empty();
        }

        // Try cache first
        String cacheKey = MESSAGE_CACHE_KEY + messageId;
        Message cachedMessage = cacheService.get(cacheKey, Message.class);
        if (cachedMessage != null) {
            return Optional.of(cachedMessage);
        }

        // Fetch from database
        Optional<Message> message = messageRepository.findByIdAndNotDeleted(messageId);

        // Cache the result if found
        message.ifPresent(msg -> cacheService.set(cacheKey, msg, MESSAGE_CACHE_DURATION));

        return message;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Message> getBoardMessages(User boardOwner, Pageable pageable) {
        log.debug("Getting board messages for user {} with page {}", boardOwner.getId(),
                pageable.getPageNumber());

        validateUser(boardOwner);
        return messageRepository.findByBoardOwnerAndNotDeletedAndParentMessageIsNull(boardOwner,
                pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Message> getAllBoardMessages(User boardOwner, Pageable pageable) {
        log.debug("Getting all board messages for user {} with page {}", boardOwner.getId(),
                pageable.getPageNumber());

        validateUser(boardOwner);
        return messageRepository.findByBoardOwnerAndNotDeleted(boardOwner, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getReplies(Message parentMessage) {
        if (parentMessage == null) {
            throw new IllegalArgumentException("Parent message cannot be null");
        }

        // Try cache first
        String cacheKey = String.format(MESSAGE_REPLIES_CACHE_KEY, parentMessage.getId());
        @SuppressWarnings("unchecked")
        List<Message> cachedReplies = (List<Message>) cacheService.get(cacheKey);
        if (cachedReplies != null) {
            return cachedReplies;
        }

        // Fetch from database
        List<Message> replies =
                messageRepository.findRepliesByParentMessageAndNotDeleted(parentMessage);

        // Cache the result
        cacheService.set(cacheKey, replies, MESSAGE_CACHE_DURATION);

        return replies;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Message> getUserMessages(User user, Pageable pageable) {
        log.debug("Getting messages for user {} with page {}", user.getId(),
                pageable.getPageNumber());

        validateUser(user);
        return messageRepository.findByUserAndNotDeleted(user, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Message> searchBoardMessages(User boardOwner, String keyword, Pageable pageable) {
        log.debug("Searching board messages for user {} with keyword '{}'", boardOwner.getId(),
                keyword);

        validateUser(boardOwner);

        if (!StringUtils.hasText(keyword)) {
            return getBoardMessages(boardOwner, pageable);
        }

        return messageRepository.searchByBoardOwnerAndContent(boardOwner, keyword.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Message> getRecentMessages(Pageable pageable) {
        log.debug("Getting recent messages with page {}", pageable.getPageNumber());

        return messageRepository.findRecentRootMessages(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long countBoardMessages(User boardOwner) {
        validateUser(boardOwner);

        // Try cache first
        String cacheKey = String.format(BOARD_COUNT_CACHE_KEY, boardOwner.getId());
        Long cachedCount = cacheService.get(cacheKey, Long.class);
        if (cachedCount != null) {
            return cachedCount;
        }

        // Fetch from database
        long count =
                messageRepository.countByBoardOwnerAndNotDeletedAndParentMessageIsNull(boardOwner);

        // Cache the result
        cacheService.set(cacheKey, count, COUNT_CACHE_DURATION);

        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public long countReplies(Message parentMessage) {
        if (parentMessage == null) {
            return 0;
        }

        return messageRepository.countRepliesByParentMessageAndNotDeleted(parentMessage);
    }

    @Override
    public boolean canEditMessage(Message message, User user) {
        if (message == null || user == null) {
            return false;
        }

        // Only the author can edit their message
        return message.isAuthor(user) && !message.isDeleted();
    }

    @Override
    public boolean canDeleteMessage(Message message, User user) {
        if (message == null || user == null) {
            return false;
        }

        // Author or board owner can delete the message
        return (message.isAuthor(user) || message.isBoardOwner(user)) && !message.isDeleted();
    }

    @Override
    public void validateMessageContent(String content) {
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }

        String trimmedContent = content.trim();

        if (trimmedContent.length() < MIN_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("Message content is too short");
        }

        if (trimmedContent.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException(String.format(
                    "Message content exceeds maximum length of %d characters", MAX_MESSAGE_LENGTH));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getBoardMessagesWithReplies(User boardOwner) {
        log.debug("Getting board messages with replies for user {}", boardOwner.getId());

        validateUser(boardOwner);
        return messageRepository.findByBoardOwnerWithReplies(boardOwner);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserPostedOnBoard(User user, User boardOwner) {
        if (user == null || boardOwner == null) {
            return false;
        }

        return messageRepository.existsByUserAndBoardOwnerAndNotDeleted(user, boardOwner);
    }

    // Private helper methods

    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (user.getId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (!user.isActive()) {
            throw new IllegalArgumentException("User is not active");
        }
    }

    private void validateUsers(User author, User boardOwner) {
        validateUser(author);
        validateUser(boardOwner);
    }

    private void clearMessageCaches(Long messageId) {
        if (messageId != null) {
            cacheService.delete(MESSAGE_CACHE_KEY + messageId);
            cacheService.delete(String.format(MESSAGE_REPLIES_CACHE_KEY, messageId));
        }
    }

    private void clearBoardCaches(Long boardOwnerId) {
        if (boardOwnerId != null) {
            String pattern = String.format(BOARD_MESSAGES_CACHE_KEY, boardOwnerId) + "*";
            cacheService.clearByPattern(pattern);
            cacheService.delete(String.format(BOARD_COUNT_CACHE_KEY, boardOwnerId));
        }
    }

    private void clearUserCaches(Long userId) {
        if (userId != null) {
            String pattern = String.format(USER_MESSAGES_CACHE_KEY, userId) + "*";
            cacheService.clearByPattern(pattern);
        }
    }
}
