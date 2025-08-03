package com.twitterboard.service;

import com.twitterboard.dto.GoogleUserInfo;
import com.twitterboard.dto.UserInfo;
import com.twitterboard.dto.UserProfileUpdateRequest;
import com.twitterboard.entity.User;
import com.twitterboard.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    private GoogleUserInfo mockGoogleUserInfo;
    private User mockUser;
    private LocalDateTime now;
    
    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        
        mockGoogleUserInfo = new GoogleUserInfo();
        mockGoogleUserInfo.setId("google_123");
        mockGoogleUserInfo.setEmail("test@example.com");
        mockGoogleUserInfo.setName("Test User");
        mockGoogleUserInfo.setPicture("https://example.com/avatar.jpg");
        mockGoogleUserInfo.setVerifiedEmail(true);
        
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setGoogleId("google_123");
        mockUser.setEmail("test@example.com");
        mockUser.setName("Test User");
        mockUser.setAvatarUrl("https://example.com/avatar.jpg");
        mockUser.setCreatedAt(now);
        mockUser.setUpdatedAt(now);
    }
    
    @Test
    void findOrCreateUser_ExistingUser_NoUpdate() {
        // Arrange
        when(userRepository.findByGoogleId("google_123")).thenReturn(Optional.of(mockUser));
        
        // Act
        User result = userService.findOrCreateUser(mockGoogleUserInfo);
        
        // Assert
        assertNotNull(result);
        assertEquals(mockUser.getId(), result.getId());
        assertEquals(mockUser.getEmail(), result.getEmail());
        assertEquals(mockUser.getName(), result.getName());
        
        // Verify interactions
        verify(userRepository).findByGoogleId("google_123");
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void findOrCreateUser_ExistingUser_WithUpdate() {
        // Arrange
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setGoogleId("google_123");
        existingUser.setEmail("old@example.com"); // Different email
        existingUser.setName("Old Name"); // Different name
        existingUser.setAvatarUrl("https://example.com/old_avatar.jpg"); // Different avatar
        existingUser.setCreatedAt(now);
        existingUser.setUpdatedAt(now);
        
        when(userRepository.findByGoogleId("google_123")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        
        // Act
        User result = userService.findOrCreateUser(mockGoogleUserInfo);
        
        // Assert
        assertNotNull(result);
        assertEquals(existingUser.getId(), result.getId());
        
        // Verify interactions
        verify(userRepository).findByGoogleId("google_123");
        verify(userRepository).save(existingUser);
        
        // Verify that the user was updated
        assertEquals(mockGoogleUserInfo.getEmail(), existingUser.getEmail());
        assertEquals(mockGoogleUserInfo.getName(), existingUser.getName());
        assertEquals(mockGoogleUserInfo.getPicture(), existingUser.getAvatarUrl());
    }
    
    @Test
    void findOrCreateUser_NewUser() {
        // Arrange
        when(userRepository.findByGoogleId("google_123")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        
        // Act
        User result = userService.findOrCreateUser(mockGoogleUserInfo);
        
        // Assert
        assertNotNull(result);
        assertEquals(mockUser.getId(), result.getId());
        
        // Verify interactions
        verify(userRepository).findByGoogleId("google_123");
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void getUserById_Found() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        
        // Act
        User result = userService.getUserById(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(mockUser.getId(), result.getId());
        
        // Verify interactions
        verify(userRepository).findById(1L);
    }
    
    @Test
    void getUserById_NotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        // Act
        User result = userService.getUserById(1L);
        
        // Assert
        assertNull(result);
        
        // Verify interactions
        verify(userRepository).findById(1L);
    }
    
    @Test
    void getUserByGoogleId_Found() {
        // Arrange
        when(userRepository.findByGoogleId("google_123")).thenReturn(Optional.of(mockUser));
        
        // Act
        User result = userService.getUserByGoogleId("google_123");
        
        // Assert
        assertNotNull(result);
        assertEquals(mockUser.getGoogleId(), result.getGoogleId());
        
        // Verify interactions
        verify(userRepository).findByGoogleId("google_123");
    }
    
    @Test
    void getUserByGoogleId_NotFound() {
        // Arrange
        when(userRepository.findByGoogleId("google_123")).thenReturn(Optional.empty());
        
        // Act
        User result = userService.getUserByGoogleId("google_123");
        
        // Assert
        assertNull(result);
        
        // Verify interactions
        verify(userRepository).findByGoogleId("google_123");
    }
    
    @Test
    void getUserByEmail_Found() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        
        // Act
        User result = userService.getUserByEmail("test@example.com");
        
        // Assert
        assertNotNull(result);
        assertEquals(mockUser.getEmail(), result.getEmail());
        
        // Verify interactions
        verify(userRepository).findByEmail("test@example.com");
    }
    
    @Test
    void getUserByEmail_NotFound() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        
        // Act
        User result = userService.getUserByEmail("test@example.com");
        
        // Assert
        assertNull(result);
        
        // Verify interactions
        verify(userRepository).findByEmail("test@example.com");
    }
    
    @Test
    void updateUserProfile_Success() throws Exception {
        // Arrange
        UserProfileUpdateRequest updateRequest = new UserProfileUpdateRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setAvatarUrl("https://example.com/new_avatar.jpg");
        
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setGoogleId("google_123");
        updatedUser.setEmail("test@example.com");
        updatedUser.setName("Updated Name");
        updatedUser.setAvatarUrl("https://example.com/new_avatar.jpg");
        updatedUser.setCreatedAt(now);
        updatedUser.setUpdatedAt(now);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        
        // Act
        UserInfo result = userService.updateUserProfile(1L, updateRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("https://example.com/new_avatar.jpg", result.getAvatarUrl());
        
        // Verify interactions
        verify(userRepository).findById(1L);
        verify(userRepository).save(mockUser);
    }
    
    @Test
    void updateUserProfile_UserNotFound() {
        // Arrange
        UserProfileUpdateRequest updateRequest = new UserProfileUpdateRequest();
        updateRequest.setName("Updated Name");
        
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            userService.updateUserProfile(1L, updateRequest);
        });
        
        assertTrue(exception.getMessage().contains("User not found"));
        
        // Verify interactions
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void updateUserProfile_NoChanges() throws Exception {
        // Arrange
        UserProfileUpdateRequest updateRequest = new UserProfileUpdateRequest();
        updateRequest.setName("Test User"); // Same name
        updateRequest.setAvatarUrl("https://example.com/avatar.jpg"); // Same avatar
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        
        // Act
        UserInfo result = userService.updateUserProfile(1L, updateRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals(mockUser.getName(), result.getName());
        assertEquals(mockUser.getAvatarUrl(), result.getAvatarUrl());
        
        // Verify interactions
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class)); // No save since no changes
    }
    
    @Test
    void existsByGoogleId_True() {
        // Arrange
        when(userRepository.existsByGoogleId("google_123")).thenReturn(true);
        
        // Act
        boolean result = userService.existsByGoogleId("google_123");
        
        // Assert
        assertTrue(result);
        
        // Verify interactions
        verify(userRepository).existsByGoogleId("google_123");
    }
    
    @Test
    void existsByGoogleId_False() {
        // Arrange
        when(userRepository.existsByGoogleId("google_123")).thenReturn(false);
        
        // Act
        boolean result = userService.existsByGoogleId("google_123");
        
        // Assert
        assertFalse(result);
        
        // Verify interactions
        verify(userRepository).existsByGoogleId("google_123");
    }
    
    @Test
    void existsByEmail_True() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        
        // Act
        boolean result = userService.existsByEmail("test@example.com");
        
        // Assert
        assertTrue(result);
        
        // Verify interactions
        verify(userRepository).existsByEmail("test@example.com");
    }
    
    @Test
    void existsByEmail_False() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        
        // Act
        boolean result = userService.existsByEmail("test@example.com");
        
        // Assert
        assertFalse(result);
        
        // Verify interactions
        verify(userRepository).existsByEmail("test@example.com");
    }
    
    @Test
    void getAllUsers_Success() {
        // Arrange
        User user2 = new User();
        user2.setId(2L);
        user2.setGoogleId("google_456");
        user2.setEmail("test2@example.com");
        user2.setName("Test User 2");
        user2.setCreatedAt(now);
        user2.setUpdatedAt(now);
        
        List<User> users = Arrays.asList(mockUser, user2);
        when(userRepository.findAll()).thenReturn(users);
        
        // Act
        List<UserInfo> result = userService.getAllUsers();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(mockUser.getId(), result.get(0).getId());
        assertEquals(user2.getId(), result.get(1).getId());
        
        // Verify interactions
        verify(userRepository).findAll();
    }
    
    @Test
    void searchUsersByName_Success() {
        // Arrange
        List<User> users = Arrays.asList(mockUser);
        when(userRepository.findByNameContainingIgnoreCase("Test")).thenReturn(users);
        
        // Act
        List<UserInfo> result = userService.searchUsersByName("Test");
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockUser.getId(), result.get(0).getId());
        
        // Verify interactions
        verify(userRepository).findByNameContainingIgnoreCase("Test");
    }
    
    @Test
    void getUsersCreatedAfter_Success() {
        // Arrange
        LocalDateTime date = now.minusDays(1);
        List<User> users = Arrays.asList(mockUser);
        when(userRepository.findByCreatedAtAfter(date)).thenReturn(users);
        
        // Act
        List<UserInfo> result = userService.getUsersCreatedAfter(date);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockUser.getId(), result.get(0).getId());
        
        // Verify interactions
        verify(userRepository).findByCreatedAtAfter(date);
    }
    
    @Test
    void getTotalUserCount_Success() {
        // Arrange
        when(userRepository.countAllUsers()).thenReturn(10L);
        
        // Act
        long result = userService.getTotalUserCount();
        
        // Assert
        assertEquals(10L, result);
        
        // Verify interactions
        verify(userRepository).countAllUsers();
    }
    
    @Test
    void getActiveUsers_Success() {
        // Arrange
        List<User> users = Arrays.asList(mockUser);
        when(userRepository.findActiveUsers(any(LocalDateTime.class))).thenReturn(users);
        
        // Act
        List<UserInfo> result = userService.getActiveUsers(7);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockUser.getId(), result.get(0).getId());
        
        // Verify interactions
        verify(userRepository).findActiveUsers(any(LocalDateTime.class));
    }
    
    @Test
    void deleteUser_NotImplemented() {
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            userService.deleteUser(1L);
        });
        
        assertTrue(exception.getMessage().contains("User deletion not implemented yet"));
    }
}