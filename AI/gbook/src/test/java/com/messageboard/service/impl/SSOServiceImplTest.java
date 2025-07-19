package com.messageboard.service.impl;

import com.messageboard.entity.User;
import com.messageboard.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SSOServiceImplTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private OAuth2User oauth2User;
    
    @InjectMocks
    private SSOServiceImpl ssoService;
    
    private Map<String, Object> googleAttributes;
    private Map<String, Object> githubAttributes;
    
    @BeforeEach
    void setUp() {
        // Google OAuth2 attributes
        googleAttributes = new HashMap<>();
        googleAttributes.put("sub", "google123");
        googleAttributes.put("email", "test@gmail.com");
        googleAttributes.put("name", "Test User");
        googleAttributes.put("picture", "https://example.com/avatar.jpg");
        
        // GitHub OAuth2 attributes
        githubAttributes = new HashMap<>();
        githubAttributes.put("id", 12345);
        githubAttributes.put("email", "test@github.com");
        githubAttributes.put("name", "Test User");
        githubAttributes.put("login", "testuser");
        githubAttributes.put("avatar_url", "https://github.com/avatar.jpg");
    }
    
    @Test
    void processOAuth2User_NewGoogleUser_ShouldCreateUser() {
        // Given
        when(oauth2User.getAttribute("sub")).thenReturn("google123");
        when(oauth2User.getAttribute("email")).thenReturn("test@gmail.com");
        when(oauth2User.getAttribute("name")).thenReturn("Test User");
        when(oauth2User.getAttribute("picture")).thenReturn("https://example.com/avatar.jpg");
        
        when(userRepository.findBySsoId("google:google123")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        
        // When
        User result = ssoService.processOAuth2User(oauth2User, "google");
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSsoId()).isEqualTo("google:google123");
        assertThat(result.getEmail()).isEqualTo("test@gmail.com");
        assertThat(result.getDisplayName()).isEqualTo("Test User");
        assertThat(result.getAvatarUrl()).isEqualTo("https://example.com/avatar.jpg");
        assertThat(result.getUsername()).isEqualTo("test");
        assertThat(result.getIsActive()).isTrue();
        
        verify(userRepository).findBySsoId("google:google123");
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void processOAuth2User_NewGitHubUser_ShouldCreateUser() {
        // Given
        when(oauth2User.getAttribute("id")).thenReturn(12345);
        when(oauth2User.getAttribute("email")).thenReturn("test@github.com");
        when(oauth2User.getAttribute("name")).thenReturn("Test User");
        when(oauth2User.getAttribute("login")).thenReturn("testuser");
        when(oauth2User.getAttribute("avatar_url")).thenReturn("https://github.com/avatar.jpg");
        
        when(userRepository.findBySsoId("github:12345")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        
        // When
        User result = ssoService.processOAuth2User(oauth2User, "github");
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSsoId()).isEqualTo("github:12345");
        assertThat(result.getEmail()).isEqualTo("test@github.com");
        assertThat(result.getDisplayName()).isEqualTo("Test User");
        assertThat(result.getAvatarUrl()).isEqualTo("https://github.com/avatar.jpg");
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getIsActive()).isTrue();
        
        verify(userRepository).findBySsoId("github:12345");
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void processOAuth2User_ExistingUser_ShouldSynchronizeData() {
        // Given
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setSsoId("google:google123");
        existingUser.setEmail("old@gmail.com");
        existingUser.setDisplayName("Old Name");
        existingUser.setAvatarUrl("https://old-avatar.jpg");
        existingUser.setUsername("testuser");
        existingUser.setCreatedAt(LocalDateTime.now().minusDays(1));
        existingUser.setUpdatedAt(LocalDateTime.now().minusDays(1));
        
        when(oauth2User.getAttribute("sub")).thenReturn("google123");
        when(oauth2User.getAttribute("email")).thenReturn("test@gmail.com");
        when(oauth2User.getAttribute("name")).thenReturn("Test User");
        when(oauth2User.getAttribute("picture")).thenReturn("https://example.com/avatar.jpg");
        
        when(userRepository.findBySsoId("google:google123")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        User result = ssoService.processOAuth2User(oauth2User, "google");
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@gmail.com");
        assertThat(result.getDisplayName()).isEqualTo("Test User");
        assertThat(result.getAvatarUrl()).isEqualTo("https://example.com/avatar.jpg");
        assertThat(result.getUsername()).isEqualTo("testuser"); // Should not change
        
        verify(userRepository).findBySsoId("google:google123");
        verify(userRepository).save(existingUser);
    }
    
    @Test
    void synchronizeUserData_NoChanges_ShouldNotSave() {
        // Given
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("test@gmail.com");
        existingUser.setDisplayName("Test User");
        existingUser.setAvatarUrl("https://example.com/avatar.jpg");
        
        when(oauth2User.getAttribute("email")).thenReturn("test@gmail.com");
        when(oauth2User.getAttribute("name")).thenReturn("Test User");
        when(oauth2User.getAttribute("picture")).thenReturn("https://example.com/avatar.jpg");
        
        // When
        User result = ssoService.synchronizeUserData(oauth2User, existingUser, "google");
        
        // Then
        assertThat(result).isSameAs(existingUser);
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void extractSsoId_GoogleProvider_ShouldReturnSub() {
        // Given
        when(oauth2User.getAttribute("sub")).thenReturn("google123");
        
        // When
        String result = ssoService.extractSsoId(oauth2User, "google");
        
        // Then
        assertThat(result).isEqualTo("google123");
    }
    
    @Test
    void extractSsoId_GitHubProvider_ShouldReturnId() {
        // Given
        when(oauth2User.getAttribute("id")).thenReturn(12345);
        
        // When
        String result = ssoService.extractSsoId(oauth2User, "github");
        
        // Then
        assertThat(result).isEqualTo("12345");
    }
    
    @Test
    void createUserFromOAuth2_DuplicateUsername_ShouldGenerateUniqueUsername() {
        // Given
        when(oauth2User.getAttribute("sub")).thenReturn("google123");
        when(oauth2User.getAttribute("email")).thenReturn("test@gmail.com");
        when(oauth2User.getAttribute("name")).thenReturn("Test User");
        when(oauth2User.getAttribute("picture")).thenReturn("https://example.com/avatar.jpg");
        
        when(userRepository.existsByUsername("test")).thenReturn(true);
        when(userRepository.existsByUsername("test1")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        
        // When
        User result = ssoService.createUserFromOAuth2(oauth2User, "google");
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("test1");
        
        verify(userRepository).existsByUsername("test");
        verify(userRepository).existsByUsername("test1");
        verify(userRepository).save(any(User.class));
    }
}