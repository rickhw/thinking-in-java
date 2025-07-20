package com.gtcafe.pgb.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.gtcafe.pgb.entity.TokenBlacklist;

/**
 * Integration tests for TokenBlacklistRepository
 */
@DataJpaTest
@ActiveProfiles("test")
class TokenBlacklistRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;

    private TokenBlacklist activeToken;
    private TokenBlacklist expiredToken;
    private String activeTokenHash;
    private String expiredTokenHash;

    @BeforeEach
    void setUp() {
        activeTokenHash = "active-token-hash-123";
        expiredTokenHash = "expired-token-hash-456";

        // Create an active (non-expired) token
        activeToken = new TokenBlacklist();
        activeToken.setTokenHash(activeTokenHash);
        activeToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        activeToken.setCreatedAt(LocalDateTime.now().minusMinutes(30));

        // Create an expired token
        expiredToken = new TokenBlacklist();
        expiredToken.setTokenHash(expiredTokenHash);
        expiredToken.setExpiresAt(LocalDateTime.now().minusHours(1));
        expiredToken.setCreatedAt(LocalDateTime.now().minusHours(2));

        // Persist the entities
        entityManager.persistAndFlush(activeToken);
        entityManager.persistAndFlush(expiredToken);
    }

    @Test
    void testFindByTokenHash_WhenTokenExists() {
        Optional<TokenBlacklist> found = tokenBlacklistRepository.findByTokenHash(activeTokenHash);

        assertTrue(found.isPresent());
        assertEquals(activeTokenHash, found.get().getTokenHash());
        assertEquals(activeToken.getExpiresAt(), found.get().getExpiresAt());
    }

    @Test
    void testFindByTokenHash_WhenTokenDoesNotExist() {
        Optional<TokenBlacklist> found = tokenBlacklistRepository.findByTokenHash("non-existent-hash");

        assertFalse(found.isPresent());
    }

    @Test
    void testExistsByTokenHash_WhenTokenExists() {
        boolean exists = tokenBlacklistRepository.existsByTokenHash(activeTokenHash);

        assertTrue(exists);
    }

    @Test
    void testExistsByTokenHash_WhenTokenDoesNotExist() {
        boolean exists = tokenBlacklistRepository.existsByTokenHash("non-existent-hash");

        assertFalse(exists);
    }

    @Test
    void testDeleteExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();

        // Verify we have both tokens initially
        assertEquals(2, tokenBlacklistRepository.count());

        // Delete expired tokens
        int deletedCount = tokenBlacklistRepository.deleteExpiredTokens(now);

        // Should delete 1 expired token
        assertEquals(1, deletedCount);

        // Verify only the active token remains
        assertEquals(1, tokenBlacklistRepository.count());
        assertTrue(tokenBlacklistRepository.existsByTokenHash(activeTokenHash));
        assertFalse(tokenBlacklistRepository.existsByTokenHash(expiredTokenHash));
    }

    @Test
    void testDeleteExpiredTokens_WhenNoExpiredTokens() {
        // Delete the expired token first
        tokenBlacklistRepository.delete(expiredToken);
        entityManager.flush();

        LocalDateTime now = LocalDateTime.now();
        int deletedCount = tokenBlacklistRepository.deleteExpiredTokens(now);

        // Should delete 0 tokens
        assertEquals(0, deletedCount);

        // Active token should still exist
        assertEquals(1, tokenBlacklistRepository.count());
        assertTrue(tokenBlacklistRepository.existsByTokenHash(activeTokenHash));
    }

    @Test
    void testFindExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();

        List<TokenBlacklist> expiredTokens = tokenBlacklistRepository.findExpiredTokens(now);

        assertEquals(1, expiredTokens.size());
        assertEquals(expiredTokenHash, expiredTokens.get(0).getTokenHash());
    }

    @Test
    void testFindExpiredTokens_WhenNoExpiredTokens() {
        // Use a time in the past so no tokens are considered expired
        LocalDateTime pastTime = LocalDateTime.now().minusHours(3);

        List<TokenBlacklist> expiredTokens = tokenBlacklistRepository.findExpiredTokens(pastTime);

        assertEquals(0, expiredTokens.size());
    }

    @Test
    void testCountActiveTokens() {
        LocalDateTime now = LocalDateTime.now();

        long activeCount = tokenBlacklistRepository.countActiveTokens(now);

        assertEquals(1, activeCount);
    }

    @Test
    void testCountActiveTokens_WhenAllTokensExpired() {
        // Use a future time so all tokens are considered expired
        LocalDateTime futureTime = LocalDateTime.now().plusHours(2);

        long activeCount = tokenBlacklistRepository.countActiveTokens(futureTime);

        assertEquals(0, activeCount);
    }

    @Test
    void testCountActiveTokens_WhenAllTokensActive() {
        // Delete the expired token
        tokenBlacklistRepository.delete(expiredToken);
        entityManager.flush();

        LocalDateTime now = LocalDateTime.now();
        long activeCount = tokenBlacklistRepository.countActiveTokens(now);

        assertEquals(1, activeCount);
    }

    @Test
    void testSaveAndRetrieve() {
        String newTokenHash = "new-token-hash-789";
        LocalDateTime newExpiresAt = LocalDateTime.now().plusMinutes(30);

        TokenBlacklist newToken = new TokenBlacklist(newTokenHash, newExpiresAt);
        TokenBlacklist saved = tokenBlacklistRepository.save(newToken);

        assertNotNull(saved.getId());
        assertEquals(newTokenHash, saved.getTokenHash());
        assertEquals(newExpiresAt, saved.getExpiresAt());
        assertNotNull(saved.getCreatedAt());

        // Verify it can be retrieved
        Optional<TokenBlacklist> retrieved = tokenBlacklistRepository.findById(saved.getId());
        assertTrue(retrieved.isPresent());
        assertEquals(newTokenHash, retrieved.get().getTokenHash());
    }

    @Test
    void testUniqueConstraintOnTokenHash() {
        String duplicateHash = "duplicate-hash";

        TokenBlacklist token1 = new TokenBlacklist(duplicateHash, LocalDateTime.now().plusHours(1));
        TokenBlacklist token2 = new TokenBlacklist(duplicateHash, LocalDateTime.now().plusHours(2));

        // Save the first token
        tokenBlacklistRepository.save(token1);
        entityManager.flush();

        // Attempting to save the second token with the same hash should fail
        assertThrows(Exception.class, () -> {
            tokenBlacklistRepository.save(token2);
            entityManager.flush();
        });
    }

    @Test
    void testDeleteById() {
        Long tokenId = activeToken.getId();

        assertTrue(tokenBlacklistRepository.existsById(tokenId));

        tokenBlacklistRepository.deleteById(tokenId);
        entityManager.flush();

        assertFalse(tokenBlacklistRepository.existsById(tokenId));
    }

    @Test
    void testFindAll() {
        List<TokenBlacklist> allTokens = tokenBlacklistRepository.findAll();

        assertEquals(2, allTokens.size());

        // Verify both tokens are present
        boolean activeFound = false;
        boolean expiredFound = false;

        for (TokenBlacklist token : allTokens) {
            if (activeTokenHash.equals(token.getTokenHash())) {
                activeFound = true;
            } else if (expiredTokenHash.equals(token.getTokenHash())) {
                expiredFound = true;
            }
        }

        assertTrue(activeFound);
        assertTrue(expiredFound);
    }
}