package com.example.messageboard.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import com.gtcafe.messageboard.Main;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify the message ID migration from BIGINT to VARCHAR(36)
 */
@SpringBootTest(classes = Main.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class MessageIdMigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String TEST_TABLE = "messages_migration_test";
    private static final Pattern ID_PATTERN = Pattern.compile(
            "^[A-Z0-9]{8}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{12}$");

    @BeforeEach
    void setUp() {
        // Create test table with original BIGINT structure
        jdbcTemplate.execute("DROP TABLE IF EXISTS " + TEST_TABLE);
        jdbcTemplate.execute("""
            CREATE TABLE %s (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id VARCHAR(255) NOT NULL,
                content TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """.formatted(TEST_TABLE));
        
        // Create indexes separately for H2 compatibility
        jdbcTemplate.execute("CREATE INDEX idx_" + TEST_TABLE + "_user_id ON " + TEST_TABLE + "(user_id)");
        jdbcTemplate.execute("CREATE INDEX idx_" + TEST_TABLE + "_created_at ON " + TEST_TABLE + "(created_at)");

        // Insert test data
        jdbcTemplate.update(
            "INSERT INTO " + TEST_TABLE + " (user_id, content) VALUES (?, ?)",
            "user1", "Test message 1");
        jdbcTemplate.update(
            "INSERT INTO " + TEST_TABLE + " (user_id, content) VALUES (?, ?)",
            "user2", "Test message 2");
        jdbcTemplate.update(
            "INSERT INTO " + TEST_TABLE + " (user_id, content) VALUES (?, ?)",
            "user1", "Test message 3");
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS " + TEST_TABLE);
    }

    @Test
    void testMigrationFromBigintToVarchar() {
        // Verify initial state
        Integer initialCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM " + TEST_TABLE, Integer.class);
        assertEquals(3, initialCount);

        // Verify initial ID type is BIGINT
        List<Map<String, Object>> initialIds = jdbcTemplate.queryForList(
            "SELECT id FROM " + TEST_TABLE + " ORDER BY id");
        
        for (Map<String, Object> row : initialIds) {
            Object id = row.get("id");
            assertTrue(id instanceof Long || id instanceof Integer, 
                "Initial ID should be numeric type");
        }

        // Apply migration steps
        applyMigration();

        // Verify migration results
        verifyMigrationResults();
    }

    @Test
    void testIdFormatValidation() {
        // Apply migration first
        applyMigration();

        // Get all IDs and validate format
        List<String> ids = jdbcTemplate.queryForList(
            "SELECT id FROM " + TEST_TABLE, String.class);

        assertFalse(ids.isEmpty(), "Should have IDs after migration");

        for (String id : ids) {
            assertNotNull(id, "ID should not be null");
            assertEquals(36, id.length(), "ID should be 36 characters long");
            assertTrue(ID_PATTERN.matcher(id).matches(), 
                "ID should match the expected format: " + id);
        }
    }

    @Test
    void testIdUniqueness() {
        // Apply migration first
        applyMigration();

        // Check uniqueness
        Integer totalCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM " + TEST_TABLE, Integer.class);
        Integer uniqueCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(DISTINCT id) FROM " + TEST_TABLE, Integer.class);

        assertEquals(totalCount, uniqueCount, "All IDs should be unique");
    }

    @Test
    void testDataIntegrityAfterMigration() {
        // Get original data
        List<Map<String, Object>> originalData = jdbcTemplate.queryForList(
            "SELECT user_id, content FROM " + TEST_TABLE + " ORDER BY id");

        // Apply migration
        applyMigration();

        // Verify data integrity
        Integer finalCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM " + TEST_TABLE, Integer.class);
        assertEquals(3, finalCount, "Should have same number of records after migration");

        // Verify content is preserved (order might change due to new IDs)
        List<Map<String, Object>> finalData = jdbcTemplate.queryForList(
            "SELECT user_id, content FROM " + TEST_TABLE + " ORDER BY user_id, content");

        assertEquals(originalData.size(), finalData.size(), 
            "Should have same number of records");

        // Verify all original content exists
        for (Map<String, Object> originalRow : originalData) {
            boolean found = finalData.stream().anyMatch(finalRow ->
                originalRow.get("user_id").equals(finalRow.get("user_id")) &&
                originalRow.get("content").equals(finalRow.get("content"))
            );
            assertTrue(found, "Original data should be preserved: " + originalRow);
        }
    }

    @Test
    void testIndexesAfterMigration() {
        // Apply migration
        applyMigration();

        // Verify indexes exist
        List<Map<String, Object>> indexes = jdbcTemplate.queryForList("""
            SELECT INDEX_NAME, COLUMN_NAME 
            FROM INFORMATION_SCHEMA.STATISTICS 
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME = ? 
            ORDER BY INDEX_NAME, SEQ_IN_INDEX
            """, TEST_TABLE);

        assertFalse(indexes.isEmpty(), "Should have indexes after migration");

        // Verify primary key exists
        boolean hasPrimaryKey = indexes.stream()
            .anyMatch(index -> "PRIMARY".equals(index.get("INDEX_NAME")));
        assertTrue(hasPrimaryKey, "Should have PRIMARY key index");

        // Verify other indexes exist
        boolean hasUserIdIndex = indexes.stream()
            .anyMatch(index -> index.get("INDEX_NAME").toString().contains("user_id"));
        assertTrue(hasUserIdIndex, "Should have user_id index");
    }

    private void applyMigration() {
        try {
            // Step 1: Add new VARCHAR(36) ID column
            jdbcTemplate.execute("ALTER TABLE " + TEST_TABLE + " ADD COLUMN new_id VARCHAR(36) NULL");

            // Step 2: Generate new IDs for existing records (H2 compatible)
            List<Map<String, Object>> existingRecords = jdbcTemplate.queryForList(
                "SELECT id FROM " + TEST_TABLE + " WHERE new_id IS NULL");
            
            for (Map<String, Object> record : existingRecords) {
                Long oldId = (Long) record.get("id");
                String newId = generateTestId(oldId);
                jdbcTemplate.update(
                    "UPDATE " + TEST_TABLE + " SET new_id = ? WHERE id = ?", 
                    newId, oldId);
            }

            // Step 3: Make new_id NOT NULL (H2 compatible)
            jdbcTemplate.execute("ALTER TABLE " + TEST_TABLE + " ALTER COLUMN new_id SET NOT NULL");

            // Step 4: Add unique constraint
            jdbcTemplate.execute("ALTER TABLE " + TEST_TABLE + " ADD CONSTRAINT uk_" + TEST_TABLE + "_new_id UNIQUE (new_id)");

            // Step 5: Drop old primary key
            jdbcTemplate.execute("ALTER TABLE " + TEST_TABLE + " DROP PRIMARY KEY");

            // Step 6: Drop old id column
            jdbcTemplate.execute("ALTER TABLE " + TEST_TABLE + " DROP COLUMN id");

            // Step 7: Rename new_id to id (H2 compatible - use separate steps)
            jdbcTemplate.execute("ALTER TABLE " + TEST_TABLE + " ALTER COLUMN new_id RENAME TO id");

            // Step 8: Add new primary key
            jdbcTemplate.execute("ALTER TABLE " + TEST_TABLE + " ADD PRIMARY KEY (id)");

        } catch (Exception e) {
            fail("Migration should not throw exception: " + e.getMessage());
        }
    }

    private void verifyMigrationResults() {
        // Verify table structure
        List<Map<String, Object>> columns = jdbcTemplate.queryForList("""
            SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, IS_NULLABLE, COLUMN_KEY
            FROM INFORMATION_SCHEMA.COLUMNS 
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME = ? 
            AND COLUMN_NAME = 'id'
            """, TEST_TABLE);

        assertFalse(columns.isEmpty(), "Should have id column after migration");

        Map<String, Object> idColumn = columns.get(0);
        assertEquals("varchar", idColumn.get("DATA_TYPE"), "ID column should be VARCHAR");
        assertEquals(36L, idColumn.get("CHARACTER_MAXIMUM_LENGTH"), "ID column should have length 36");
        assertEquals("NO", idColumn.get("IS_NULLABLE"), "ID column should be NOT NULL");
        assertEquals("PRI", idColumn.get("COLUMN_KEY"), "ID column should be PRIMARY KEY");

        // Verify all records have valid IDs (H2 compatible)
        Integer validIdCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM " + TEST_TABLE + " WHERE id IS NOT NULL AND LENGTH(id) = 36", 
            Integer.class);

        Integer totalCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM " + TEST_TABLE, Integer.class);

        assertEquals(totalCount, validIdCount, "All records should have valid IDs");
    }

    /**
     * Helper method to generate test IDs in the correct format
     */
    private String generateTestId(Long oldId) {
        // Simple test ID generation that follows the format
        String timestamp = String.format("%08X", System.currentTimeMillis() / 1000 + oldId);
        String random1 = String.format("%04X", (int)(Math.random() * 65536));
        String random2 = String.format("%04X", (int)(Math.random() * 65536));
        String machine = String.format("%04X", oldId.intValue() % 65536);
        String random3 = String.format("%012X", (long)(Math.random() * 281474976710656L));
        
        return String.format("%s-%s-%s-%s-%s", 
            timestamp.substring(0, 8),
            random1.substring(0, 4),
            random2.substring(0, 4),
            machine.substring(0, 4),
            random3.substring(0, 12));
    }
}