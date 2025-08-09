package com.gtcafe.messageboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gtcafe.messageboard.repository.MessageRepository;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.regex.Pattern;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Optimized service for generating unique 36-character message IDs
 * Format: XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX
 * Character set: A-Z, 0-9
 * 
 * Performance optimizations:
 * - Uses ThreadLocalRandom for better concurrent performance
 * - Caches charset array for faster access
 * - Uses atomic counter for sequence numbers
 * - Pre-compiled regex pattern for validation
 */
@Service
public class MessageIdGenerator {

    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final char[] CHARSET_ARRAY = CHARSET.toCharArray();
    private static final int CHARSET_SIZE = CHARSET_ARRAY.length;

    // Pattern for validating ID format: 8-4-4-4-12 with A-Z and 0-9
    private static final Pattern ID_PATTERN = Pattern.compile(
            "^[A-Z0-9]{8}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{12}$");

    // Machine identifier (last 2 digits of current time in milliseconds)
    private static final String MACHINE_ID = String.format("%02X",
            (int) (System.currentTimeMillis() % 256));

    // Atomic counter for sequence numbers to ensure uniqueness within the same millisecond
    private static final AtomicLong SEQUENCE_COUNTER = new AtomicLong(0);

    // Cache for StringBuilder to reduce object allocation
    private static final ThreadLocal<StringBuilder> STRING_BUILDER_CACHE = 
            ThreadLocal.withInitial(() -> new StringBuilder(36));

    @Autowired
    private MessageRepository messageRepository;

    /**
     * Generates a 36-character unique ID
     * Format: XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX
     * 
     * Structure:
     * - First 8 chars: Timestamp encoding (seconds since epoch)
     * - Next 4 chars: Random component
     * - Next 4 chars: Random component
     * - Next 4 chars: Machine ID + Random
     * - Last 12 chars: Random + Sequence + Checksum
     * 
     * @return 36-character unique ID string
     */
    public String generateId() {
        String id;
        int attempts = 0;
        final int maxAttempts = 10;

        do {
            id = generateIdInternal();
            attempts++;

            if (attempts >= maxAttempts) {
                throw new RuntimeException(
                        "Failed to generate unique ID after " + maxAttempts + " attempts");
            }
        } while (!isIdUnique(id));

        return id;
    }

    /**
     * Internal method to generate ID without uniqueness check
     * Public for testing purposes
     * 
     * Optimized version with reduced object allocation and better performance
     */
    public String generateIdInternal() {
        StringBuilder sb = STRING_BUILDER_CACHE.get();
        sb.setLength(0); // Reset the cached StringBuilder

        // Part 1: Timestamp encoding (8 chars)
        long timestamp = Instant.now().getEpochSecond();
        String timestampEncoded = encodeBase32(timestamp, 8);
        sb.append(timestampEncoded);
        sb.append('-');

        // Part 2: Random component (4 chars)
        generateRandomStringDirect(sb, 4);
        sb.append('-');

        // Part 3: Random component (4 chars)
        generateRandomStringDirect(sb, 4);
        sb.append('-');

        // Part 4: Machine ID + Sequence (4 chars)
        sb.append(MACHINE_ID);
        // Use sequence counter for better uniqueness within same millisecond
        long sequence = SEQUENCE_COUNTER.incrementAndGet() % (CHARSET_SIZE * CHARSET_SIZE);
        sb.append(CHARSET_ARRAY[(int) (sequence / CHARSET_SIZE)]);
        sb.append(CHARSET_ARRAY[(int) (sequence % CHARSET_SIZE)]);
        sb.append('-');

        // Part 5: Random + Checksum (12 chars)
        generateRandomStringDirect(sb, 10);

        // Add checksum (2 chars)
        String checksum = calculateChecksumOptimized(sb);
        sb.append(checksum);

        return sb.toString();
    }

    /**
     * Encodes a number to base-32 using our custom charset
     */
    private String encodeBase32(long value, int length) {
        StringBuilder result = new StringBuilder();

        if (value == 0) {
            result.append(CHARSET.charAt(0));
        } else {
            while (value > 0) {
                result.insert(0, CHARSET.charAt((int) (value % CHARSET_SIZE)));
                value /= CHARSET_SIZE;
            }
        }

        // Pad with leading characters if necessary
        while (result.length() < length) {
            result.insert(0, CHARSET.charAt(0));
        }

        // Truncate if too long
        if (result.length() > length) {
            result = new StringBuilder(result.substring(result.length() - length));
        }

        return result.toString();
    }

    /**
     * Generates a random string of specified length using our charset
     * Optimized version that appends directly to StringBuilder
     */
    private void generateRandomStringDirect(StringBuilder sb, int length) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            sb.append(CHARSET_ARRAY[random.nextInt(CHARSET_SIZE)]);
        }
    }

    /**
     * Legacy method for backward compatibility
     */
    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        generateRandomStringDirect(sb, length);
        return sb.toString();
    }

    /**
     * Calculates a simple checksum for the ID
     * Optimized version that works directly with StringBuilder
     */
    private String calculateChecksumOptimized(StringBuilder input) {
        int sum = 0;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != '-') {
                sum += c;
            }
        }

        // Convert sum to 2-character string using our charset
        int checksum = sum % (CHARSET_SIZE * CHARSET_SIZE);
        char first = CHARSET_ARRAY[checksum / CHARSET_SIZE];
        char second = CHARSET_ARRAY[checksum % CHARSET_SIZE];

        return "" + first + second;
    }

    /**
     * Legacy method for backward compatibility
     */
    private String calculateChecksum(String input) {
        int sum = 0;
        for (char c : input.toCharArray()) {
            if (c != '-') {
                sum += c;
            }
        }

        // Convert sum to 2-character string using our charset
        int checksum = sum % (CHARSET_SIZE * CHARSET_SIZE);
        char first = CHARSET_ARRAY[checksum / CHARSET_SIZE];
        char second = CHARSET_ARRAY[checksum % CHARSET_SIZE];

        return "" + first + second;
    }

    /**
     * Validates if the given string matches the expected ID format
     * 
     * @param id The ID string to validate
     * @return true if the ID format is valid, false otherwise
     */
    public boolean isValidId(String id) {
        if (id == null) {
            return false;
        }

        return ID_PATTERN.matcher(id).matches();
    }

    /**
     * Checks if the given ID is unique (doesn't exist in the database)
     * 
     * @param id The ID to check for uniqueness
     * @return true if the ID is unique, false if it already exists
     */
    public boolean isIdUnique(String id) {
        try {
            // Check if the ID already exists in the database
            return !messageRepository.existsById(id);
        } catch (Exception e) {
            // If we can't check against the database, assume it's unique
            return true;
        }
    }
}
