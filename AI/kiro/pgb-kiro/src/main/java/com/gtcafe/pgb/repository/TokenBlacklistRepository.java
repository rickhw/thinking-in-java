package com.gtcafe.pgb.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gtcafe.pgb.entity.TokenBlacklist;

/**
 * Repository interface for TokenBlacklist entity
 * Provides data access methods for JWT token blacklist management
 */
@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

    /**
     * Find a blacklisted token by its hash
     * 
     * @param tokenHash the hash of the token to find
     * @return Optional containing the TokenBlacklist if found
     */
    Optional<TokenBlacklist> findByTokenHash(String tokenHash);

    /**
     * Check if a token hash exists in the blacklist
     * 
     * @param tokenHash the hash of the token to check
     * @return true if the token is blacklisted, false otherwise
     */
    boolean existsByTokenHash(String tokenHash);

    /**
     * Delete expired tokens from the blacklist
     * This method should be called periodically to clean up expired entries
     * 
     * @param now the current timestamp
     * @return the number of deleted records
     */
    @Modifying
    @Query("DELETE FROM TokenBlacklist t WHERE t.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Find all tokens that expire before the given timestamp
     * 
     * @param timestamp the timestamp to compare against
     * @return list of expired TokenBlacklist entries
     */
    @Query("SELECT t FROM TokenBlacklist t WHERE t.expiresAt < :timestamp")
    java.util.List<TokenBlacklist> findExpiredTokens(@Param("timestamp") LocalDateTime timestamp);

    /**
     * Count the number of active (non-expired) tokens in the blacklist
     * 
     * @param now the current timestamp
     * @return the count of active blacklisted tokens
     */
    @Query("SELECT COUNT(t) FROM TokenBlacklist t WHERE t.expiresAt >= :now")
    long countActiveTokens(@Param("now") LocalDateTime now);

    /**
     * Check if a token hash exists and is not expired
     * 
     * @param tokenHash the hash of the token to check
     * @param now       the current timestamp
     * @return true if the token exists and is not expired
     */
    boolean existsByTokenHashAndExpiresAtAfter(String tokenHash, LocalDateTime now);

    /**
     * Delete tokens that expire before the given timestamp
     * 
     * @param timestamp the timestamp to compare against
     * @return the number of deleted records
     */
    @Modifying
    int deleteByExpiresAtBefore(LocalDateTime timestamp);

    /**
     * Count tokens that expire after the given timestamp
     * 
     * @param timestamp the timestamp to compare against
     * @return the count of non-expired tokens
     */
    long countByExpiresAtAfter(LocalDateTime timestamp);
}