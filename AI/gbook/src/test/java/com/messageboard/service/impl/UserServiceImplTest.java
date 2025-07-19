package com.messageboard.service.impl;

import com.messageboard.entity.User;
import com.messageboard.repository.UserRepository;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Implementation Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private User existingUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .ssoId("sso123")
                .username("testuser")
                .email("test@example.com")
                .displayName("Test User")
                .avatarUrl("https://example.com/avatar.jpg")
                .isActive(true)
                .build();

        existingUser = User.builder()
                .id(1L)
                .ssoId("existing_sso")
                .username("existing")
                .email("existing@example.com")
                .displayName("Existing User")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully with valid data")
        void shouldCreateUserSuccessfully() {
            // Given
            when(userRepository.existsBySsoId("sso123")).thenReturn(false);
            when(userRepository.existsByUsername("testuser")).thenReturn(false);
            when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            User result = userService.createUser(testUser);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("testuser");
            assertThat(result.getEmail()).isEqualTo("test@example.com");
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should throw exception when user is null")
        void shouldThrowExceptionWhenUserIsNull() {
            // When & Then
            assertThatThrownBy(() -> userService.createUser(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when SSO ID is blank")
        void shouldThrowExceptionWhenSsoIdIsBlank() {
            // Given
            testUser.setSsoId("");

            // When & Then
            assertThatThrownBy(() -> userService.createUser(testUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("SSO ID cannot be blank");
        }

        @Test
        @DisplayName("Should throw exception when username is blank")
        void shouldThrowExceptionWhenUsernameIsBlank() {
            // Given
            testUser.setUsername("");

            // When & Then
            assertThatThrownBy(() -> userService.createUser(testUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Username cannot be blank");
        }

        @Test
        @DisplayName("Should throw exception when email is blank")
        void shouldThrowExceptionWhenEmailIsBlank() {
            // Given
            testUser.setEmail("");

            // When & Then
            assertThatThrownBy(() -> userService.createUser(testUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email cannot be blank");
        }

        @Test
        @DisplayName("Should throw exception when SSO ID already exists")
        void shouldThrowExceptionWhenSsoIdExists() {
            // Given
            when(userRepository.existsBySsoId("sso123")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> userService.createUser(testUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("SSO ID already exists: sso123");
        }

        @Test
        @DisplayName("Should throw exception when username already exists")
        void shouldThrowExceptionWhenUsernameExists() {
            // Given
            when(userRepository.existsBySsoId("sso123")).thenReturn(false);
            when(userRepository.existsByUsername("testuser")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> userService.createUser(testUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Username already exists: testuser");
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            // Given
            when(userRepository.existsBySsoId("sso123")).thenReturn(false);
            when(userRepository.existsByUsername("testuser")).thenReturn(false);
            when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> userService.createUser(testUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email already exists: test@example.com");
        }

        @Test
        @DisplayName("Should throw exception when username is too short")
        void shouldThrowExceptionWhenUsernameIsTooShort() {
            // Given
            testUser.setUsername("ab");

            // When & Then
            assertThatThrownBy(() -> userService.createUser(testUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Username must be between 3 and 50 characters");
        }

        @Test
        @DisplayName("Should throw exception when username is too long")
        void shouldThrowExceptionWhenUsernameIsTooLong() {
            // Given
            testUser.setUsername("a".repeat(51));

            // When & Then
            assertThatThrownBy(() -> userService.createUser(testUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Username must be between 3 and 50 characters");
        }

        @Test
        @DisplayName("Should set isActive to true by default")
        void shouldSetIsActiveToTrueByDefault() {
            // Given
            testUser.setIsActive(null);
            when(userRepository.existsBySsoId("sso123")).thenReturn(false);
            when(userRepository.existsByUsername("testuser")).thenReturn(false);
            when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                assertThat(user.getIsActive()).isTrue();
                return user;
            });

            // When
            userService.createUser(testUser);

            // Then
            verify(userRepository).save(testUser);
        }
    }

    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user successfully")
        void shouldUpdateUserSuccessfully() {
            // Given
            User updateData = User.builder()
                    .username("newusername")
                    .email("newemail@example.com")
                    .displayName("New Display Name")
                    .avatarUrl("https://example.com/newavatar.jpg")
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
            when(userRepository.existsByUsername("newusername")).thenReturn(false);
            when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            User result = userService.updateUser(1L, updateData);

            // Then
            assertThat(result.getUsername()).isEqualTo("newusername");
            assertThat(result.getEmail()).isEqualTo("newemail@example.com");
            assertThat(result.getDisplayName()).isEqualTo("New Display Name");
            assertThat(result.getAvatarUrl()).isEqualTo("https://example.com/newavatar.jpg");
            verify(userRepository).save(existingUser);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.updateUser(999L, testUser))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("User not found with ID: 999");
        }

        @Test
        @DisplayName("Should throw exception when update data is null")
        void shouldThrowExceptionWhenUpdateDataIsNull() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

            // When & Then
            assertThatThrownBy(() -> userService.updateUser(1L, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when new username already exists")
        void shouldThrowExceptionWhenNewUsernameExists() {
            // Given
            User updateData = User.builder().username("existingusername").build();
            when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
            when(userRepository.existsByUsername("existingusername")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> userService.updateUser(1L, updateData))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Username already exists: existingusername");
        }

        @Test
        @DisplayName("Should not update username when it's the same")
        void shouldNotUpdateUsernameWhenSame() {
            // Given
            User updateData = User.builder()
                    .username("existing")
                    .displayName("Updated Display Name")
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            User result = userService.updateUser(1L, updateData);

            // Then
            assertThat(result.getUsername()).isEqualTo("existing");
            assertThat(result.getDisplayName()).isEqualTo("Updated Display Name");
            verify(userRepository, never()).existsByUsername(anyString());
        }
    }

    @Nested
    @DisplayName("Find User Tests")
    class FindUserTests {

        @Test
        @DisplayName("Should find user by ID")
        void shouldFindUserById() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

            // When
            Optional<User> result = userService.findById(1L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should return empty when user not found by ID")
        void shouldReturnEmptyWhenUserNotFoundById() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When
            Optional<User> result = userService.findById(999L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find user by SSO ID")
        void shouldFindUserBySsoId() {
            // Given
            when(userRepository.findBySsoId("sso123")).thenReturn(Optional.of(existingUser));

            // When
            Optional<User> result = userService.findBySsoId("sso123");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getSsoId()).isEqualTo("existing_sso");
        }

        @Test
        @DisplayName("Should return empty when SSO ID is blank")
        void shouldReturnEmptyWhenSsoIdIsBlank() {
            // When
            Optional<User> result = userService.findBySsoId("");

            // Then
            assertThat(result).isEmpty();
            verify(userRepository, never()).findBySsoId(anyString());
        }

        @Test
        @DisplayName("Should find user by username")
        void shouldFindUserByUsername() {
            // Given
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(existingUser));

            // When
            Optional<User> result = userService.findByUsername("testuser");

            // Then
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("Should return empty when username is blank")
        void shouldReturnEmptyWhenUsernameIsBlank() {
            // When
            Optional<User> result = userService.findByUsername("");

            // Then
            assertThat(result).isEmpty();
            verify(userRepository, never()).findByUsername(anyString());
        }

        @Test
        @DisplayName("Should find user by email")
        void shouldFindUserByEmail() {
            // Given
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));

            // When
            Optional<User> result = userService.findByEmail("test@example.com");

            // Then
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("Should return empty when email is blank")
        void shouldReturnEmptyWhenEmailIsBlank() {
            // When
            Optional<User> result = userService.findByEmail("");

            // Then
            assertThat(result).isEmpty();
            verify(userRepository, never()).findByEmail(anyString());
        }
    }

    @Nested
    @DisplayName("Search User Tests")
    class SearchUserTests {

        @Test
        @DisplayName("Should find active users with pagination")
        void shouldFindActiveUsersWithPagination() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> expectedPage = new PageImpl<>(Arrays.asList(existingUser));
            when(userRepository.findActiveUsers(pageable)).thenReturn(expectedPage);

            // When
            Page<User> result = userService.findActiveUsers(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(existingUser);
        }

        @Test
        @DisplayName("Should search users by keyword")
        void shouldSearchUsersByKeyword() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> expectedPage = new PageImpl<>(Arrays.asList(existingUser));
            when(userRepository.searchActiveUsersByKeyword("test", pageable)).thenReturn(expectedPage);

            // When
            Page<User> result = userService.searchUsers("test", pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(userRepository).searchActiveUsersByKeyword("test", pageable);
        }

        @Test
        @DisplayName("Should return active users when keyword is blank")
        void shouldReturnActiveUsersWhenKeywordIsBlank() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> expectedPage = new PageImpl<>(Arrays.asList(existingUser));
            when(userRepository.findActiveUsers(pageable)).thenReturn(expectedPage);

            // When
            Page<User> result = userService.searchUsers("", pageable);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).findActiveUsers(pageable);
            verify(userRepository, never()).searchActiveUsersByKeyword(anyString(), any());
        }

        @Test
        @DisplayName("Should trim keyword before search")
        void shouldTrimKeywordBeforeSearch() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> expectedPage = new PageImpl<>(Arrays.asList(existingUser));
            when(userRepository.searchActiveUsersByKeyword("test", pageable)).thenReturn(expectedPage);

            // When
            userService.searchUsers("  test  ", pageable);

            // Then
            verify(userRepository).searchActiveUsersByKeyword("test", pageable);
        }

        @Test
        @DisplayName("Should search users by username")
        void shouldSearchUsersByUsername() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> expectedPage = new PageImpl<>(Arrays.asList(existingUser));
            when(userRepository.findActiveUsersByUsernameContaining("user", pageable)).thenReturn(expectedPage);

            // When
            Page<User> result = userService.searchUsersByUsername("user", pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(userRepository).findActiveUsersByUsernameContaining("user", pageable);
        }
    }

    @Nested
    @DisplayName("User Status Tests")
    class UserStatusTests {

        @Test
        @DisplayName("Should deactivate user successfully")
        void shouldDeactivateUserSuccessfully() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            userService.deactivateUser(1L);

            // Then
            assertThat(existingUser.getIsActive()).isFalse();
            verify(userRepository).save(existingUser);
        }

        @Test
        @DisplayName("Should throw exception when deactivating non-existent user")
        void shouldThrowExceptionWhenDeactivatingNonExistentUser() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.deactivateUser(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("User not found with ID: 999");
        }

        @Test
        @DisplayName("Should activate user successfully")
        void shouldActivateUserSuccessfully() {
            // Given
            existingUser.setIsActive(false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            userService.activateUser(1L);

            // Then
            assertThat(existingUser.getIsActive()).isTrue();
            verify(userRepository).save(existingUser);
        }

        @Test
        @DisplayName("Should throw exception when activating non-existent user")
        void shouldThrowExceptionWhenActivatingNonExistentUser() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.activateUser(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("User not found with ID: 999");
        }
    }

    @Nested
    @DisplayName("Availability Check Tests")
    class AvailabilityCheckTests {

        @Test
        @DisplayName("Should return true when username is available")
        void shouldReturnTrueWhenUsernameIsAvailable() {
            // Given
            when(userRepository.existsByUsername("newuser")).thenReturn(false);

            // When
            boolean result = userService.isUsernameAvailable("newuser");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when username is not available")
        void shouldReturnFalseWhenUsernameIsNotAvailable() {
            // Given
            when(userRepository.existsByUsername("existinguser")).thenReturn(true);

            // When
            boolean result = userService.isUsernameAvailable("existinguser");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when username is blank")
        void shouldReturnFalseWhenUsernameIsBlank() {
            // When
            boolean result = userService.isUsernameAvailable("");

            // Then
            assertThat(result).isFalse();
            verify(userRepository, never()).existsByUsername(anyString());
        }

        @Test
        @DisplayName("Should return true when email is available")
        void shouldReturnTrueWhenEmailIsAvailable() {
            // Given
            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

            // When
            boolean result = userService.isEmailAvailable("new@example.com");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when email is not available")
        void shouldReturnFalseWhenEmailIsNotAvailable() {
            // Given
            when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

            // When
            boolean result = userService.isEmailAvailable("existing@example.com");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return true when SSO ID is available")
        void shouldReturnTrueWhenSsoIdIsAvailable() {
            // Given
            when(userRepository.existsBySsoId("newsso")).thenReturn(false);

            // When
            boolean result = userService.isSsoIdAvailable("newsso");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when SSO ID is not available")
        void shouldReturnFalseWhenSsoIdIsNotAvailable() {
            // Given
            when(userRepository.existsBySsoId("existingsso")).thenReturn(true);

            // When
            boolean result = userService.isSsoIdAvailable("existingsso");

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Field Validation Tests")
    class FieldValidationTests {

        @Test
        @DisplayName("Should throw exception when SSO ID exceeds 255 characters")
        void shouldThrowExceptionWhenSsoIdTooLong() {
            // Given
            testUser.setSsoId("a".repeat(256));

            // When & Then
            assertThatThrownBy(() -> userService.createUser(testUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("SSO ID cannot exceed 255 characters");
        }

        @Test
        @DisplayName("Should throw exception when email exceeds 255 characters")
        void shouldThrowExceptionWhenEmailTooLong() {
            // Given
            testUser.setEmail("a".repeat(250) + "@example.com");

            // When & Then
            assertThatThrownBy(() -> userService.createUser(testUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email cannot exceed 255 characters");
        }

        @Test
        @DisplayName("Should throw exception when display name exceeds 100 characters")
        void shouldThrowExceptionWhenDisplayNameTooLong() {
            // Given
            testUser.setDisplayName("a".repeat(101));

            // When & Then
            assertThatThrownBy(() -> userService.createUser(testUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Display name cannot exceed 100 characters");
        }

        @Test
        @DisplayName("Should throw exception when avatar URL exceeds 500 characters")
        void shouldThrowExceptionWhenAvatarUrlTooLong() {
            // Given
            testUser.setAvatarUrl("https://example.com/" + "a".repeat(500));

            // When & Then
            assertThatThrownBy(() -> userService.createUser(testUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Avatar URL cannot exceed 500 characters");
        }
    }
}