-- Database optimization script for message ID performance
-- This script adds optimized indexes and constraints for better query performance

-- Drop existing indexes if they exist
DROP INDEX IF EXISTS idx_messages_id;
DROP INDEX IF EXISTS idx_messages_user_id;
DROP INDEX IF EXISTS idx_messages_created_at;

-- Create optimized primary index on ID column
-- Using BTREE index for fast exact lookups
CREATE INDEX idx_messages_id_btree ON messages(id) USING BTREE;

-- Create composite index for user-based queries with ordering
-- This supports queries like "find messages by user ordered by creation time"
CREATE INDEX idx_messages_user_created ON messages(user_id, created_at DESC) USING BTREE;

-- Create index on created_at for time-based queries
CREATE INDEX idx_messages_created_at_desc ON messages(created_at DESC) USING BTREE;

-- Create partial index for recent messages (last 30 days)
-- This speeds up queries for recent messages which are accessed most frequently
CREATE INDEX idx_messages_recent ON messages(created_at DESC, id) 
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY);

-- Create index on content for full-text search (if needed in future)
-- ALTER TABLE messages ADD FULLTEXT(content);

-- Add statistics for query optimizer
ANALYZE TABLE messages;

-- Verify index creation and show index information
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    SEQ_IN_INDEX,
    CARDINALITY,
    INDEX_TYPE
FROM 
    INFORMATION_SCHEMA.STATISTICS 
WHERE 
    TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'messages'
ORDER BY 
    INDEX_NAME, SEQ_IN_INDEX;

-- Show table status for optimization verification
SHOW TABLE STATUS LIKE 'messages';