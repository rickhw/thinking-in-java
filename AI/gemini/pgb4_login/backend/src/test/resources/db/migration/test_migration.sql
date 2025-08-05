-- Test script to verify the message ID migration
-- This script creates test data and verifies the migration works correctly

-- Create a test database (optional - for isolated testing)
-- CREATE DATABASE IF NOT EXISTS test_migration;
-- USE test_migration;

-- Step 1: Create the original table structure
DROP TABLE IF EXISTS messages_test;
CREATE TABLE messages_test (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_messages_user_id (user_id),
    INDEX idx_messages_created_at (created_at)
);

-- Step 2: Insert test data
INSERT INTO messages_test (user_id, content) VALUES
('user1', 'Test message 1'),
('user2', 'Test message 2'),
('user1', 'Test message 3'),
('user3', 'Test message 4'),
('user2', 'Test message 5');

-- Step 3: Verify initial state
SELECT 'Initial state:' as step;
SELECT 
    COUNT(*) as total_records,
    MIN(id) as min_id,
    MAX(id) as max_id,
    AVG(LENGTH(CAST(id AS CHAR))) as avg_id_length
FROM messages_test;

-- Step 4: Apply migration (simplified version for testing)
ALTER TABLE messages_test ADD COLUMN new_id VARCHAR(36) NULL;

-- Generate test IDs (simplified version)
UPDATE messages_test 
SET new_id = CONCAT(
    LPAD(CONV(UNIX_TIMESTAMP() + id, 10, 36), 8, 'A'), '-',
    LPAD(CONV(FLOOR(RAND() * 1679616), 10, 36), 4, 'A'), '-',
    LPAD(CONV(FLOOR(RAND() * 1679616), 10, 36), 4, 'A'), '-',
    LPAD(CONV((id * 123) % 1296, 10, 36), 4, 'A'), '-',
    LPAD(CONV(FLOOR(RAND() * 4738381338321616896), 10, 36), 12, 'A')
);

-- Make new_id NOT NULL and add constraints
ALTER TABLE messages_test MODIFY COLUMN new_id VARCHAR(36) NOT NULL;
ALTER TABLE messages_test ADD CONSTRAINT uk_messages_test_new_id UNIQUE (new_id);

-- Drop old primary key and column
ALTER TABLE messages_test DROP PRIMARY KEY;
ALTER TABLE messages_test DROP COLUMN id;

-- Rename and set new primary key
ALTER TABLE messages_test CHANGE COLUMN new_id id VARCHAR(36) NOT NULL;
ALTER TABLE messages_test ADD PRIMARY KEY (id);

-- Step 5: Verify migration results
SELECT 'After migration:' as step;
SELECT 
    COUNT(*) as total_records,
    COUNT(CASE WHEN id IS NOT NULL AND LENGTH(id) = 36 THEN 1 END) as valid_ids,
    COUNT(CASE WHEN id REGEXP '^[A-Z0-9]{8}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{12}$' THEN 1 END) as properly_formatted_ids,
    MIN(LENGTH(id)) as min_id_length,
    MAX(LENGTH(id)) as max_id_length
FROM messages_test;

-- Step 6: Show sample IDs
SELECT 'Sample migrated IDs:' as step;
SELECT id, user_id, LEFT(content, 20) as content_preview
FROM messages_test
LIMIT 5;

-- Step 7: Test ID format validation
SELECT 'ID format validation:' as step;
SELECT 
    id,
    CASE 
        WHEN id REGEXP '^[A-Z0-9]{8}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{12}$' 
        THEN 'VALID' 
        ELSE 'INVALID' 
    END as format_status
FROM messages_test;

-- Step 8: Test uniqueness
SELECT 'Uniqueness check:' as step;
SELECT 
    COUNT(*) as total_records,
    COUNT(DISTINCT id) as unique_ids,
    CASE 
        WHEN COUNT(*) = COUNT(DISTINCT id) 
        THEN 'ALL_UNIQUE' 
        ELSE 'DUPLICATES_FOUND' 
    END as uniqueness_status
FROM messages_test;

-- Cleanup
DROP TABLE messages_test;