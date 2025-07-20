package com.gtcafe.pgb.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import com.gtcafe.pgb.entity.User;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("User Repository Tests")
class UserRepositoryTest {

        @Autowired
        private TestEntityManager entityManager;

        @Autowired
        private UserRepository userRepository;

        @Test
        @DisplayName("Should save and find user by ID")
        void shouldSaveAndFindUserById() {
                // Given
                User user = User.builder()
                                .ssoId("sso123")
                                .username("testuser")
                                .email("test@example.com")
                                .displayName("Test User")
                                .build();

                // When
                User savedUser = userRepository.save(user);
                Optional<User> foundUser = userRepository.findById(savedUser.getId());

                // Then
                assertThat(foundUser).isPresent();
                assertThat(foundUser.get().getSsoId()).isEqualTo("sso123");
                assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
                assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
                assertThat(foundUser.get().getDisplayName()).isEqualTo("Test User");
                assertThat(foundUser.get().getIsActive()).isTrue();
                assertThat(foundUser.get().getCreatedAt()).isNotNull();
                assertThat(foundUser.get().getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should find user by SSO ID")
        void shouldFindUserBySsoId() {
                // Given
                User user = User.builder()
                                .ssoId("sso123")
                                .username("testuser")
                                .email("test@example.com")
                                .build();
                entityManager.persistAndFlush(user);

                // When
                Optional<User> foundUser = userRepository.findBySsoId("sso123");

                // Then
                assertThat(foundUser).isPresent();
                assertThat(foundUser.get().getSsoId()).isEqualTo("sso123");
        }

        @Test
        @DisplayName("Should find user by username")
        void shouldFindUserByUsername() {
                // Given
                User user = User.builder()
                                .ssoId("sso123")
                                .username("testuser")
                                .email("test@example.com")
                                .build();
                entityManager.persistAndFlush(user);

                // When
                Optional<User> foundUser = userRepository.findByUsername("testuser");

                // Then
                assertThat(foundUser).isPresent();
                assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("Should find user by email")
        void shouldFindUserByEmail() {
                // Given
                User user = User.builder()
                                .ssoId("sso123")
                                .username("testuser")
                                .email("test@example.com")
                                .build();
                entityManager.persistAndFlush(user);

                // When
                Optional<User> foundUser = userRepository.findByEmail("test@example.com");

                // Then
                assertThat(foundUser).isPresent();
                assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should check if user exists by SSO ID")
        void shouldCheckIfUserExistsBySsoId() {
                // Given
                User user = User.builder()
                                .ssoId("sso123")
                                .username("testuser")
                                .email("test@example.com")
                                .build();
                entityManager.persistAndFlush(user);

                // When & Then
                assertThat(userRepository.existsBySsoId("sso123")).isTrue();
                assertThat(userRepository.existsBySsoId("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("Should check if user exists by username")
        void shouldCheckIfUserExistsByUsername() {
                // Given
                User user = User.builder()
                                .ssoId("sso123")
                                .username("testuser")
                                .email("test@example.com")
                                .build();
                entityManager.persistAndFlush(user);

                // When & Then
                assertThat(userRepository.existsByUsername("testuser")).isTrue();
                assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("Should check if user exists by email")
        void shouldCheckIfUserExistsByEmail() {
                // Given
                User user = User.builder()
                                .ssoId("sso123")
                                .username("testuser")
                                .email("test@example.com")
                                .build();
                entityManager.persistAndFlush(user);

                // When & Then
                assertThat(userRepository.existsByEmail("test@example.com")).isTrue();
                assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();
        }

        @Test
        @DisplayName("Should find only active users")
        void shouldFindOnlyActiveUsers() {
                // Given
                User activeUser = User.builder()
                                .ssoId("sso123")
                                .username("activeuser")
                                .email("active@example.com")
                                .isActive(true)
                                .build();

                User inactiveUser = User.builder()
                                .ssoId("sso456")
                                .username("inactiveuser")
                                .email("inactive@example.com")
                                .isActive(false)
                                .build();

                entityManager.persistAndFlush(activeUser);
                entityManager.persistAndFlush(inactiveUser);

                // When
                Pageable pageable = PageRequest.of(0, 10);
                Page<User> activeUsers = userRepository.findActiveUsers(pageable);

                // Then
                assertThat(activeUsers.getContent()).hasSize(1);
                assertThat(activeUsers.getContent().get(0).getUsername()).isEqualTo("activeuser");
                assertThat(activeUsers.getContent().get(0).getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should search active users by keyword")
        void shouldSearchActiveUsersByKeyword() {
                // Given
                User user1 = User.builder()
                                .ssoId("sso123")
                                .username("johndoe")
                                .email("john@example.com")
                                .displayName("John Doe")
                                .isActive(true)
                                .build();

                User user2 = User.builder()
                                .ssoId("sso456")
                                .username("janedoe")
                                .email("jane@example.com")
                                .displayName("Jane Doe")
                                .isActive(true)
                                .build();

                User inactiveUser = User.builder()
                                .ssoId("sso789")
                                .username("johninactive")
                                .email("johninactive@example.com")
                                .displayName("John Inactive")
                                .isActive(false)
                                .build();

                entityManager.persistAndFlush(user1);
                entityManager.persistAndFlush(user2);
                entityManager.persistAndFlush(inactiveUser);

                // When
                Pageable pageable = PageRequest.of(0, 10);
                Page<User> searchResults = userRepository.searchActiveUsersByKeyword("john", pageable);

                // Then
                assertThat(searchResults.getContent()).hasSize(1);
                assertThat(searchResults.getContent().get(0).getUsername()).isEqualTo("johndoe");
        }

        @Test
        @DisplayName("Should search active users by username containing keyword")
        void shouldSearchActiveUsersByUsernameContaining() {
                // Given
                User user1 = User.builder()
                                .ssoId("sso123")
                                .username("testuser1")
                                .email("test1@example.com")
                                .isActive(true)
                                .build();

                User user2 = User.builder()
                                .ssoId("sso456")
                                .username("testuser2")
                                .email("test2@example.com")
                                .isActive(true)
                                .build();

                User user3 = User.builder()
                                .ssoId("sso789")
                                .username("differentuser")
                                .email("different@example.com")
                                .isActive(true)
                                .build();

                entityManager.persistAndFlush(user1);
                entityManager.persistAndFlush(user2);
                entityManager.persistAndFlush(user3);

                // When
                Pageable pageable = PageRequest.of(0, 10);
                Page<User> searchResults = userRepository.findActiveUsersByUsernameContaining("testuser", pageable);

                // Then
                assertThat(searchResults.getContent()).hasSize(2);
                assertThat(searchResults.getContent())
                                .extracting(User::getUsername)
                                .containsExactlyInAnyOrder("testuser1", "testuser2");
        }

        @Test
        @DisplayName("Should return empty result when no users match search criteria")
        void shouldReturnEmptyResultWhenNoUsersMatchSearchCriteria() {
                // Given
                User user = User.builder()
                                .ssoId("sso123")
                                .username("testuser")
                                .email("test@example.com")
                                .isActive(true)
                                .build();
                entityManager.persistAndFlush(user);

                // When
                Pageable pageable = PageRequest.of(0, 10);
                Page<User> searchResults = userRepository.searchActiveUsersByKeyword("nonexistent", pageable);

                // Then
                assertThat(searchResults.getContent()).isEmpty();
                assertThat(searchResults.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("Should handle case insensitive search")
        void shouldHandleCaseInsensitiveSearch() {
                // Given
                User user = User.builder()
                                .ssoId("sso123")
                                .username("TestUser")
                                .email("test@example.com")
                                .displayName("Test User")
                                .isActive(true)
                                .build();
                entityManager.persistAndFlush(user);

                // When
                Pageable pageable = PageRequest.of(0, 10);
                Page<User> searchResults = userRepository.searchActiveUsersByKeyword("testuser", pageable);

                // Then
                assertThat(searchResults.getContent()).hasSize(1);
                assertThat(searchResults.getContent().get(0).getUsername()).isEqualTo("TestUser");
        }
}