# Message ID Migration Documentation

## Overview

This directory contains scripts and documentation for migrating message IDs from BIGINT auto-increment values to 36-character VARCHAR strings in the format `XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX`.

## Migration Files

### Core Migration Scripts

1. **V1__create_messages_table_with_bigint_id.sql**
   - Represents the original table structure with BIGINT ID
   - Used for reference and fresh installations

2. **V2__migrate_message_id_to_varchar.sql**
   - Main migration script that converts existing BIGINT IDs to VARCHAR(36)
   - Handles data preservation and constraint updates
   - Includes verification queries

### Rollback Scripts

3. **rollback/rollback_V2__revert_message_id_to_bigint.sql**
   - Emergency rollback script (WARNING: causes data loss)
   - Only use if migration fails and you need to revert quickly

### Test Scripts

4. **test_migration.sql** (in test/resources/db/migration/)
   - Standalone test script to verify migration logic
   - Can be run independently to test the migration process

## Migration Process

### Prerequisites

1. **Backup your database** before running any migration
2. Ensure you have sufficient disk space (migration creates temporary columns)
3. Stop your application to prevent data inconsistency during migration
4. Verify you have appropriate database privileges (ALTER, CREATE, DROP)

### Step-by-Step Migration

#### Option 1: Using the Shell Script (Recommended)

```bash
cd backend
./migrate_message_ids.sh
```

The script will:
- Check if migration is needed
- Create a backup automatically
- Execute the migration
- Verify the results
- Provide detailed logging

#### Option 2: Manual Migration

1. **Create Backup**
   ```bash
   mysqldump -u root -p pgb messages > backup_messages_$(date +%Y%m%d).sql
   ```

2. **Execute Migration Script**
   ```bash
   mysql -u root -p pgb < src/main/resources/db/migration/V2__migrate_message_id_to_varchar.sql
   ```

3. **Verify Results**
   ```sql
   SELECT 
       COUNT(*) as total_records,
       COUNT(CASE WHEN id REGEXP '^[A-Z0-9]{8}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{12}$' THEN 1 END) as valid_ids
   FROM messages;
   ```

### Migration Steps Explained

The migration script performs the following operations:

1. **Add new VARCHAR(36) column** (`new_id`)
2. **Generate new IDs** for all existing records using a custom algorithm
3. **Add constraints** (NOT NULL, UNIQUE) to the new column
4. **Drop old primary key** constraint
5. **Remove old BIGINT column**
6. **Rename new column** to `id`
7. **Add new primary key** constraint
8. **Update indexes** for optimal performance
9. **Verify migration** with built-in checks

### ID Generation Algorithm

The new IDs follow this structure:
- **Format**: `XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX`
- **Character Set**: A-Z, 0-9 (36 possible characters)
- **Total Length**: 36 characters including hyphens

**Structure Breakdown**:
- First 8 chars: Timestamp encoding (seconds since epoch)
- Next 4 chars: Random component
- Next 4 chars: Random component  
- Next 4 chars: Machine identifier + random
- Last 12 chars: Random + sequence + checksum

## Testing

### Unit Tests

Run the migration test class:

```bash
cd backend
./gradlew test --tests MessageIdMigrationTest
```

### Manual Testing

Execute the test SQL script:

```bash
mysql -u root -p pgb < src/test/resources/db/migration/test_migration.sql
```

## Rollback Procedure

⚠️ **WARNING**: Rollback will cause data loss as VARCHAR IDs cannot be meaningfully converted back to BIGINT.

### Emergency Rollback

If the migration fails and you need to rollback immediately:

```bash
mysql -u root -p pgb < src/main/resources/db/migration/rollback/rollback_V2__revert_message_id_to_bigint.sql
```

### Restore from Backup

The safer option is to restore from the backup created before migration:

```bash
mysql -u root -p pgb < backup_messages_YYYYMMDD.sql
```

## Verification Checklist

After migration, verify:

- [ ] All records have 36-character IDs
- [ ] All IDs match the expected format pattern
- [ ] All IDs are unique
- [ ] Record count matches pre-migration count
- [ ] Primary key constraint exists on new ID column
- [ ] Indexes are properly created
- [ ] Application can start and function normally

## Performance Considerations

### Index Performance

The migration maintains indexes on:
- `id` (PRIMARY KEY)
- `user_id` 
- `created_at`

VARCHAR(36) primary keys may have slightly different performance characteristics than BIGINT, but the impact should be minimal for typical workloads.

### Storage Impact

- **BIGINT**: 8 bytes per ID
- **VARCHAR(36)**: 36 bytes per ID
- **Storage increase**: ~4.5x per ID

For a table with 1 million records, this represents approximately 28MB additional storage.

## Troubleshooting

### Common Issues

1. **"Table doesn't exist" error**
   - Ensure you're connected to the correct database
   - Verify the messages table exists

2. **"Duplicate entry" error during migration**
   - This is extremely rare due to the ID generation algorithm
   - If it occurs, the script will retry with a new ID

3. **"Out of disk space" error**
   - The migration temporarily doubles storage requirements
   - Ensure sufficient disk space before starting

4. **Application startup errors after migration**
   - Verify all application code has been updated to handle String IDs
   - Check that MessageIdGenerator is properly configured

### Recovery Procedures

1. **If migration fails mid-process**:
   - Restore from backup
   - Check error logs
   - Fix the issue and retry

2. **If application fails to start after migration**:
   - Check application logs
   - Verify entity classes are updated
   - Ensure MessageIdGenerator is working

## Support

For issues with the migration:

1. Check the application logs
2. Verify database state using the verification queries
3. Review the backup files
4. Test the migration process in a development environment first

## Migration History

- **V1**: Initial table structure with BIGINT ID
- **V2**: Migration to VARCHAR(36) ID format

## Related Files

- `MessageIdGenerator.java`: Service for generating new format IDs
- `Message.java`: Updated entity class
- `MessageRepository.java`: Updated repository interface
- `MessageService.java`: Updated service layer
- `MessageController.java`: Updated controller layer