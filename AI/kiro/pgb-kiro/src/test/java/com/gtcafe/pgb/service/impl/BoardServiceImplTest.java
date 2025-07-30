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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
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
import com.gtcafe.pgb.repository.UserRepository;
import com.gtcafe.pgb.service.BoardService;
import com.gtcafe.pgb.service.CacheService;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardService Implementation Tests")
class BoardServiceImplTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private BoardServiceImpl boardService;

    private User boardOwner;
    private User viewer;
    private User inactiveUser;
    private Message rootMessage;
    private Message replyMessage;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        boardOwner = User.builder().id(1L).ssoId("board-owner-sso").username("boardowner")
                .email("owner@example.com").displayName("Board Owner").isActive(true)
                .createdAt(LocalDateTime.now().minusDays(30)).build();

        viewer = User.builder().id(2L).ssoId("viewer-sso").username("viewer")
                .email("viewer@example.com").displayName("Viewer").isActive(true)
                .createdAt(LocalDateTime.now().minusDays(15)).build();

        inactiveUser = User.builder().id(3L).ssoId("inactive-sso").username("inactive")
                .email("inactive@example.com").displayName("Inactive User").isActive(false)
                .createdAt(LocalDateTime.now().minusDays(10)).build();

        rootMessage = Message.builder().id(1L).user(boardOwner).boardOwner(boardOwner)
                .content("Root message content").createdAt(LocalDateTime.now().minusHours(2))
                .updatedAt(LocalDateTime.now().minusHours(2)).isDeleted(false).build();

        replyMessage = Message.builder().id(2L).user(viewer).boardOwner(boardOwner)
                .content("Reply message content").parentMessage(rootMessage)
                .createdAt(LocalDateTime.now().minusHours(1))
                .updatedAt(LocalDateTime.now().minusHours(1)).isDeleted(false).build();

        pageable = PageRequest.of(0, 10);
    }

    @Nested
    @DisplayName("Board Messages Tests")
    class BoardMessagesTests {

        @Test
        @DisplayName("Should get board messages successfully")
        void shouldGetBoardMessagesSuccessfully() {
            // Given
            List<Message> messages = Arrays.asList(rootMessage);
            Page<Message> messagePage = new PageImpl<>(messages, pageable, 1);

            when(cacheService.get(anyString(), eq(Page.class))).thenReturn(null);
            when(messageRepository.findByBoardOwnerAndNotDeletedAndParentMessageIsNull(boardOwner,
                    pageable)).thenReturn(messagePage);

            // When
            Page<Message> result = boardService.getBoardMessages(boardOwner, viewer, pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(rootMessage, result.getContent().get(0));
            verify(cacheService).set(anyString(), eq(messagePage), any());
        }

        @Test
        @DisplayName("Should return cached board messages")
        void shouldReturnCachedBoardMessages() {
            // Given
            List<Message> messages = Arrays.asList(rootMessage);
            Page<Message> cachedPage = new PageImpl<>(messages, pageable, 1);

            when(cacheService.get(anyString(), eq(Page.class))).thenReturn(cachedPage);

            // When
            Page<Message> result = boardService.getBoardMessages(boardOwner, viewer, pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(messageRepository, never())
                    .findByBoardOwnerAndNotDeletedAndParentMessageIsNull(any(), any());
        }

        @Test
        @DisplayName("Should get all board messages including replies")
        void shouldGetAllBoardMessagesIncludingReplies() {
            // Given
            List<Message> messages = Arrays.asList(rootMessage, replyMessage);
            Page<Message> messagePage = new PageImpl<>(messages, pageable, 2);

            when(cacheService.get(anyString(), eq(Page.class))).thenReturn(null);
            when(messageRepository.findByBoardOwnerAndNotDeleted(boardOwner, pageable))
                    .thenReturn(messagePage);

            // When
            Page<Message> result = boardService.getAllBoardMessages(boardOwner, viewer, pageable);

            // Then
            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
            verify(cacheService).set(anyString(), eq(messagePage), any());
        }

        @Test
        @DisplayName("Should get board messages with replies")
        void shouldGetBoardMessagesWithReplies() {
            // Given
            List<Message> messages = Arrays.asList(rootMessage);

            when(cacheService.get(anyString(), eq(List.class))).thenReturn(null);
            when(messageRepository.findByBoardOwnerWithReplies(boardOwner)).thenReturn(messages);

            // When
            List<Message> result = boardService.getBoardMessagesWithReplies(boardOwner, viewer);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(rootMessage, result.get(0));
            verify(cacheService).set(anyString(), eq(messages), any());
        }
    }

    @Nested
    @DisplayName("Board Statistics Tests")
    class BoardStatisticsTests {

        @Test
        @DisplayName("Should get board statistics successfully")
        void shouldGetBoardStatisticsSuccessfully() {
            // Given
            List<Message> allMessages = Arrays.asList(rootMessage, replyMessage);
            Page<Message> allMessagesPage = new PageImpl<>(allMessages);

            when(cacheService.get(anyString(), eq(BoardService.BoardStatistics.class)))
                    .thenReturn(null);
            when(messageRepository.countByBoardOwnerAndNotDeletedAndParentMessageIsNull(boardOwner))
                    .thenReturn(1L);
            when(messageRepository.findByBoardOwnerAndNotDeleted(eq(boardOwner),
                    any(Pageable.class))).thenReturn(allMessagesPage);

            // When
            BoardService.BoardStatistics result =
                    boardService.getBoardStatistics(boardOwner, viewer);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getTotalMessages());
            assertEquals(1L, result.getTotalReplies());
            assertEquals(2L, result.getUniqueContributors());
            assertNotNull(result.getLastActivityTime());
            assertEquals(boardOwner.getCreatedAt(), result.getBoardCreatedTime());
            verify(cacheService).set(anyString(), eq(result), any());
        }

        @Test
        @DisplayName("Should return cached board statistics")
        void shouldReturnCachedBoardStatistics() {
            // Given
            BoardService.BoardStatistics cachedStats = new BoardService.BoardStatistics(5L, 10L, 3L,
                    LocalDateTime.now(), boardOwner.getCreatedAt());

            when(cacheService.get(anyString(), eq(BoardService.BoardStatistics.class)))
                    .thenReturn(cachedStats);

            // When
            BoardService.BoardStatistics result =
                    boardService.getBoardStatistics(boardOwner, viewer);

            // Then
            assertNotNull(result);
            assertEquals(5L, result.getTotalMessages());
            assertEquals(10L, result.getTotalReplies());
            assertEquals(3L, result.getUniqueContributors());
            verify(messageRepository, never())
                    .countByBoardOwnerAndNotDeletedAndParentMessageIsNull(any());
        }

        @Test
        @DisplayName("Should calculate total posts correctly")
        void shouldCalculateTotalPostsCorrectly() {
            // Given
            BoardService.BoardStatistics stats = new BoardService.BoardStatistics(5L, 10L, 3L,
                    LocalDateTime.now(), boardOwner.getCreatedAt());

            // When
            long totalPosts = stats.getTotalPosts();

            // Then
            assertEquals(15L, totalPosts);
        }
    }

    @Nested
    @DisplayName("Search Board Messages Tests")
    class SearchBoardMessagesTests {

        @Test
        @DisplayName("Should search board messages successfully")
        void shouldSearchBoardMessagesSuccessfully() {
            // Given
            String keyword = "test";
            List<Message> searchResults = Arrays.asList(rootMessage);
            Page<Message> searchPage = new PageImpl<>(searchResults, pageable, 1);

            when(cacheService.get(anyString(), eq(Page.class))).thenReturn(null);
            when(messageRepository.searchByBoardOwnerAndContent(boardOwner, keyword, pageable))
                    .thenReturn(searchPage);

            // When
            Page<Message> result =
                    boardService.searchBoardMessages(boardOwner, viewer, keyword, pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(rootMessage, result.getContent().get(0));
            verify(cacheService).set(anyString(), eq(searchPage), any());
        }

        @Test
        @DisplayName("Should return board messages when keyword is empty")
        void shouldReturnBoardMessagesWhenKeywordIsEmpty() {
            // Given
            String emptyKeyword = "";
            List<Message> messages = Arrays.asList(rootMessage);
            Page<Message> messagePage = new PageImpl<>(messages, pageable, 1);

            when(cacheService.get(anyString(), eq(Page.class))).thenReturn(null);
            when(messageRepository.findByBoardOwnerAndNotDeletedAndParentMessageIsNull(boardOwner,
                    pageable)).thenReturn(messagePage);

            // When
            Page<Message> result =
                    boardService.searchBoardMessages(boardOwner, viewer, emptyKeyword, pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(messageRepository, never()).searchByBoardOwnerAndContent(any(), any(), any());
        }

        @Test
        @DisplayName("Should return board messages when keyword is null")
        void shouldReturnBoardMessagesWhenKeywordIsNull() {
            // Given
            List<Message> messages = Arrays.asList(rootMessage);
            Page<Message> messagePage = new PageImpl<>(messages, pageable, 1);

            when(cacheService.get(anyString(), eq(Page.class))).thenReturn(null);
            when(messageRepository.findByBoardOwnerAndNotDeletedAndParentMessageIsNull(boardOwner,
                    pageable)).thenReturn(messagePage);

            // When
            Page<Message> result =
                    boardService.searchBoardMessages(boardOwner, viewer, null, pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(messageRepository, never()).searchByBoardOwnerAndContent(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("Board Access Permission Tests")
    class BoardAccessPermissionTests {

        @Test
        @DisplayName("Should allow user to view their own board")
        void shouldAllowUserToViewTheirOwnBoard() {
            // When
            boolean canView = boardService.canViewBoard(boardOwner, boardOwner);

            // Then
            assertTrue(canView);
        }

        @Test
        @DisplayName("Should allow active user to view other active user's board")
        void shouldAllowActiveUserToViewOtherActiveUsersBoard() {
            // When
            boolean canView = boardService.canViewBoard(boardOwner, viewer);

            // Then
            assertTrue(canView);
        }

        @Test
        @DisplayName("Should not allow viewing board when board owner is null")
        void shouldNotAllowViewingBoardWhenBoardOwnerIsNull() {
            // When
            boolean canView = boardService.canViewBoard(null, viewer);

            // Then
            assertFalse(canView);
        }

        @Test
        @DisplayName("Should not allow viewing board when viewer is null")
        void shouldNotAllowViewingBoardWhenViewerIsNull() {
            // When
            boolean canView = boardService.canViewBoard(boardOwner, null);

            // Then
            assertFalse(canView);
        }

        @Test
        @DisplayName("Should not allow inactive user to view board")
        void shouldNotAllowInactiveUserToViewBoard() {
            // When
            boolean canView = boardService.canViewBoard(boardOwner, inactiveUser);

            // Then
            assertFalse(canView);
        }

        @Test
        @DisplayName("Should not allow viewing inactive user's board")
        void shouldNotAllowViewingInactiveUsersBoard() {
            // When
            boolean canView = boardService.canViewBoard(inactiveUser, viewer);

            // Then
            assertFalse(canView);
        }

        @Test
        @DisplayName("Should validate board access successfully")
        void shouldValidateBoardAccessSuccessfully() {
            // When & Then
            assertDoesNotThrow(() -> boardService.validateBoardAccess(boardOwner, viewer));
        }

        @Test
        @DisplayName("Should throw exception when board owner is null")
        void shouldThrowExceptionWhenBoardOwnerIsNull() {
            // When & Then
            assertThrows(IllegalArgumentException.class,
                    () -> boardService.validateBoardAccess(null, viewer));
        }

        @Test
        @DisplayName("Should throw exception when viewer is null")
        void shouldThrowExceptionWhenViewerIsNull() {
            // When & Then
            assertThrows(SecurityException.class,
                    () -> boardService.validateBoardAccess(boardOwner, null));
        }

        @Test
        @DisplayName("Should throw exception when access is denied")
        void shouldThrowExceptionWhenAccessIsDenied() {
            // When & Then
            assertThrows(SecurityException.class,
                    () -> boardService.validateBoardAccess(boardOwner, inactiveUser));
        }
    }

    @Nested
    @DisplayName("Recent Board Activity Tests")
    class RecentBoardActivityTests {

        @Test
        @DisplayName("Should get recent board activity successfully")
        void shouldGetRecentBoardActivitySuccessfully() {
            // Given
            int limit = 5;
            List<Message> recentMessages = Arrays.asList(replyMessage, rootMessage);
            Page<Message> recentPage = new PageImpl<>(recentMessages);

            when(cacheService.get(anyString(), eq(List.class))).thenReturn(null);
            when(messageRepository.findByBoardOwnerAndNotDeleted(eq(boardOwner),
                    any(Pageable.class))).thenReturn(recentPage);

            // When
            List<Message> result = boardService.getRecentBoardActivity(boardOwner, viewer, limit);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(replyMessage, result.get(0));
            assertEquals(rootMessage, result.get(1));
            verify(cacheService).set(anyString(), eq(recentMessages), any());
        }

        @Test
        @DisplayName("Should return cached recent board activity")
        void shouldReturnCachedRecentBoardActivity() {
            // Given
            int limit = 5;
            List<Message> cachedActivity = Arrays.asList(rootMessage);

            when(cacheService.get(anyString(), eq(List.class))).thenReturn(cachedActivity);

            // When
            List<Message> result = boardService.getRecentBoardActivity(boardOwner, viewer, limit);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(rootMessage, result.get(0));
            verify(messageRepository, never()).findByBoardOwnerAndNotDeleted(any(), any());
        }
    }

    @Nested
    @DisplayName("User Activity and Popular Boards Tests")
    class UserActivityAndPopularBoardsTests {

        @Test
        @DisplayName("Should get boards with user activity")
        void shouldGetBoardsWithUserActivity() {
            // Given
            List<Message> userMessages = Arrays.asList(replyMessage);
            Page<Message> userMessagesPage = new PageImpl<>(userMessages);

            when(messageRepository.findByUserAndNotDeleted(eq(viewer), any(Pageable.class)))
                    .thenReturn(userMessagesPage);

            // When
            Page<User> result = boardService.getBoardsWithUserActivity(viewer, pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(boardOwner, result.getContent().get(0));
        }

        @Test
        @DisplayName("Should get popular boards successfully")
        void shouldGetPopularBoardsSuccessfully() {
            // Given
            List<Message> recentMessages = Arrays.asList(rootMessage, replyMessage);
            Page<Message> recentPage = new PageImpl<>(recentMessages);

            when(cacheService.get(anyString(), eq(Page.class))).thenReturn(null);
            when(messageRepository.findRecentRootMessages(any(Pageable.class)))
                    .thenReturn(recentPage);

            // When
            Page<User> result = boardService.getPopularBoards(viewer, pageable);

            // Then
            assertNotNull(result);
            assertTrue(result.getTotalElements() >= 0);
            verify(cacheService).set(anyString(), eq(result), any());
        }

        @Test
        @DisplayName("Should return cached popular boards")
        void shouldReturnCachedPopularBoards() {
            // Given
            List<User> popularUsers = Arrays.asList(boardOwner);
            Page<User> cachedPopularBoards = new PageImpl<>(popularUsers, pageable, 1);

            when(cacheService.get(anyString(), eq(Page.class))).thenReturn(cachedPopularBoards);

            // When
            Page<User> result = boardService.getPopularBoards(viewer, pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(boardOwner, result.getContent().get(0));
            verify(messageRepository, never()).findRecentRootMessages(any());
        }
    }

    @Nested
    @DisplayName("Cache Management Tests")
    class CacheManagementTests {

        @Test
        @DisplayName("Should clear board cache for specific board owner")
        void shouldClearBoardCacheForSpecificBoardOwner() {
            // When
            boardService.clearBoardCache(boardOwner.getId());

            // Then
            verify(cacheService).clearByPattern("board:" + boardOwner.getId() + ":*");
            verify(cacheService).clearByPattern("popular_boards*");
        }

        @Test
        @DisplayName("Should clear all board caches")
        void shouldClearAllBoardCaches() {
            // When
            boardService.clearAllBoardCaches();

            // Then
            verify(cacheService).clearByPattern("board:*");
            verify(cacheService).clearByPattern("popular_boards*");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling Tests")
    class EdgeCasesAndErrorHandlingTests {

        @Test
        @DisplayName("Should handle empty board messages gracefully")
        void shouldHandleEmptyBoardMessagesGracefully() {
            // Given
            Page<Message> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);

            when(cacheService.get(anyString(), eq(Page.class))).thenReturn(null);
            when(messageRepository.findByBoardOwnerAndNotDeletedAndParentMessageIsNull(boardOwner,
                    pageable)).thenReturn(emptyPage);

            // When
            Page<Message> result = boardService.getBoardMessages(boardOwner, viewer, pageable);

            // Then
            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
            assertTrue(result.getContent().isEmpty());
        }

        @Test
        @DisplayName("Should handle board statistics with no messages")
        void shouldHandleBoardStatisticsWithNoMessages() {
            // Given
            Page<Message> emptyPage = new PageImpl<>(Arrays.asList());

            when(cacheService.get(anyString(), eq(BoardService.BoardStatistics.class)))
                    .thenReturn(null);
            when(messageRepository.countByBoardOwnerAndNotDeletedAndParentMessageIsNull(boardOwner))
                    .thenReturn(0L);
            when(messageRepository.findByBoardOwnerAndNotDeleted(eq(boardOwner),
                    any(Pageable.class))).thenReturn(emptyPage);

            // When
            BoardService.BoardStatistics result =
                    boardService.getBoardStatistics(boardOwner, viewer);

            // Then
            assertNotNull(result);
            assertEquals(0L, result.getTotalMessages());
            assertEquals(0L, result.getTotalReplies());
            assertEquals(0L, result.getUniqueContributors());
        }

        @Test
        @DisplayName("Should handle search with whitespace keyword")
        void shouldHandleSearchWithWhitespaceKeyword() {
            // Given
            String whitespaceKeyword = "   ";
            List<Message> messages = Arrays.asList(rootMessage);
            Page<Message> messagePage = new PageImpl<>(messages, pageable, 1);

            when(cacheService.get(anyString(), eq(Page.class))).thenReturn(null);
            when(messageRepository.findByBoardOwnerAndNotDeletedAndParentMessageIsNull(boardOwner,
                    pageable)).thenReturn(messagePage);

            // When
            Page<Message> result = boardService.searchBoardMessages(boardOwner, viewer,
                    whitespaceKeyword, pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(messageRepository, never()).searchByBoardOwnerAndContent(any(), any(), any());
        }

        @Test
        @DisplayName("Should handle recent activity with zero limit")
        void shouldHandleRecentActivityWithZeroLimit() {
            // Given
            int zeroLimit = 0;
            Page<Message> emptyPage = new PageImpl<>(Arrays.asList());

            when(cacheService.get(anyString(), eq(List.class))).thenReturn(null);
            when(messageRepository.findByBoardOwnerAndNotDeleted(eq(boardOwner),
                    any(Pageable.class))).thenReturn(emptyPage);

            // When
            List<Message> result =
                    boardService.getRecentBoardActivity(boardOwner, viewer, zeroLimit);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }
}
