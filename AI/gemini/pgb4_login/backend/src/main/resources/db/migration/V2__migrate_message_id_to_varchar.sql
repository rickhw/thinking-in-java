-- Migration script to change message ID from BIGINT to VARCHAR(36)
-- This script handles the migration in multiple steps to ensure data integrity

-- Step 1: Add new VARCHAR(36) ID column
ALTER TABLE messages ADD COLUMN new_id VARCHAR(36) NULL;

-- Step 2: Create index on new_id column for performance
CREATE INDEX idx_messages_new_id ON messages(new_id);

-- Step 3: Update existing records with generated IDs
-- Note: This uses a stored procedure to generate IDs in the required format
-- The ID generation logic follows the pattern: XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX

DELIMITER $$

CREATE FUNCTION generate_message_id() RETURNS VARCHAR(36)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE charset VARCHAR(36) DEFAULT 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    DECLARE result VARCHAR(36) DEFAULT '';
    DECLARE i INT DEFAULT 1;
    DECLARE timestamp_part VARCHAR(8);
    DECLARE random_part1 VARCHAR(4);
    DECLARE random_part2 VARCHAR(4);
    DECLARE machine_part VARCHAR(4);
    DECLARE random_part3 VARCHAR(12);
    
    -- Generate timestamp part (8 chars) - using UNIX timestamp
    SET timestamp_part = LPAD(CONV(UNIX_TIMESTAMP(), 10, 36), 8, 'A');
    SET timestamp_part = UPPER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
        timestamp_part, 'A', 'A'), 'B', 'B'), 'C', 'C'), 'D', 'D'), 'E', 'E'), 'F', 'F'), 'G', 'G'), 'H', 'H'), 'I', 'I'), 'J', 'J'));
    
    -- Generate random parts using RAND() and charset
    SET random_part1 = '';
    SET i = 1;
    WHILE i <= 4 DO
        SET random_part1 = CONCAT(random_part1, SUBSTRING(charset, FLOOR(RAND() * 36) + 1, 1));
        SET i = i + 1;
    END WHILE;
    
    SET random_part2 = '';
    SET i = 1;
    WHILE i <= 4 DO
        SET random_part2 = CONCAT(random_part2, SUBSTRING(charset, FLOOR(RAND() * 36) + 1, 1));
        SET i = i + 1;
    END WHILE;
    
    -- Machine part (4 chars) - using connection ID as machine identifier
    SET machine_part = LPAD(CONV(CONNECTION_ID() % 1296, 10, 36), 4, 'A');
    
    SET random_part3 = '';
    SET i = 1;
    WHILE i <= 12 DO
        SET random_part3 = CONCAT(random_part3, SUBSTRING(charset, FLOOR(RAND() * 36) + 1, 1));
        SET i = i + 1;
    END WHILE;
    
    -- Combine all parts with hyphens
    SET result = CONCAT(
        SUBSTRING(timestamp_part, 1, 8), '-',
        random_part1, '-',
        random_part2, '-',
        SUBSTRING(machine_part, 1, 4), '-',
        random_part3
    );
    
    RETURN result;
END$$

DELIMITER ;

-- Step 4: Generate new IDs for all existing records
UPDATE messages 
SET new_id = generate_message_id() 
WHERE new_id IS NULL;

-- Step 5: Ensure all records have new IDs (safety check)
-- If any records still have NULL new_id, generate them
UPDATE messages 
SET new_id = CONCAT(
    LPAD(CONV(UNIX_TIMESTAMP(), 10, 36), 8, 'A'), '-',
    LPAD(CONV(FLOOR(RAND() * 1679616), 10, 36), 4, 'A'), '-',
    LPAD(CONV(FLOOR(RAND() * 1679616), 10, 36), 4, 'A'), '-',
    LPAD(CONV(CONNECTION_ID() % 1296, 10, 36), 4, 'A'), '-',
    LPAD(CONV(FLOOR(RAND() * 4738381338321616896), 10, 36), 12, 'A')
)
WHERE new_id IS NULL;

-- Step 6: Make new_id column NOT NULL
ALTER TABLE messages MODIFY COLUMN new_id VARCHAR(36) NOT NULL;

-- Step 7: Add unique constraint to new_id
ALTER TABLE messages ADD CONSTRAINT uk_messages_new_id UNIQUE (new_id);

-- Step 8: Drop the old primary key constraint
ALTER TABLE messages DROP PRIMARY KEY;

-- Step 9: Drop the old id column
ALTER TABLE messages DROP COLUMN id;

-- Step 10: Rename new_id to id
ALTER TABLE messages CHANGE COLUMN new_id id VARCHAR(36) NOT NULL;

-- Step 11: Add primary key constraint to the new id column
ALTER TABLE messages ADD PRIMARY KEY (id);

-- Step 12: Update indexes
DROP INDEX idx_messages_new_id ON messages;
CREATE INDEX idx_messages_id ON messages(id);

-- Step 13: Clean up the temporary function
DROP FUNCTION generate_message_id;

-- Verify the migration
SELECT 
    COUNT(*) as total_records,
    COUNT(CASE WHEN id IS NOT NULL AND LENGTH(id) = 36 THEN 1 END) as valid_ids,
    COUNT(CASE WHEN id REGEXP '^[A-Z0-9]{8}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{12}$' THEN 1 END) as properly_formatted_ids
FROM messages;