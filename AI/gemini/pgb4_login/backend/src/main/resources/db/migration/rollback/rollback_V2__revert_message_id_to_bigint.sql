-- Rollback script to revert message ID from VARCHAR(36) back to BIGINT
-- WARNING: This will result in data loss as VARCHAR IDs cannot be converted back to meaningful BIGINT values
-- Use this script only in emergency situations and ensure you have a full backup

-- Step 1: Add temporary BIGINT ID column
ALTER TABLE messages ADD COLUMN temp_id BIGINT AUTO_INCREMENT UNIQUE;

-- Step 2: Create index on temp_id
CREATE INDEX idx_messages_temp_id ON messages(temp_id);

-- Step 3: Drop primary key constraint on VARCHAR id
ALTER TABLE messages DROP PRIMARY KEY;

-- Step 4: Drop the VARCHAR id column
ALTER TABLE messages DROP COLUMN id;

-- Step 5: Rename temp_id to id
ALTER TABLE messages CHANGE COLUMN temp_id id BIGINT AUTO_INCREMENT;

-- Step 6: Add primary key constraint to the new BIGINT id
ALTER TABLE messages ADD PRIMARY KEY (id);

-- Step 7: Update indexes
DROP INDEX idx_messages_temp_id ON messages;
CREATE INDEX idx_messages_id ON messages(id);

-- Verify the rollback
SELECT 
    COUNT(*) as total_records,
    MIN(id) as min_id,
    MAX(id) as max_id,
    COUNT(CASE WHEN id IS NOT NULL THEN 1 END) as valid_ids
FROM messages;