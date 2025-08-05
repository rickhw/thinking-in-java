#!/bin/bash

# Message ID Migration Script
# This script migrates message IDs from BIGINT to VARCHAR(36)

set -e  # Exit on any error

# Configuration
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-pgb}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-medusa}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Logging function
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

warn() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] WARNING: $1${NC}"
}

error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR: $1${NC}"
}

# Function to execute SQL and check result
execute_sql() {
    local sql="$1"
    local description="$2"
    
    log "Executing: $description"
    
    if mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -e "$sql"; then
        log "✓ $description completed successfully"
    else
        error "✗ $description failed"
        exit 1
    fi
}

# Function to check if migration is needed
check_migration_needed() {
    log "Checking if migration is needed..."
    
    # Check if messages table exists and has BIGINT id
    local result=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -sN -e "
        SELECT DATA_TYPE 
        FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = '$DB_NAME' 
        AND TABLE_NAME = 'messages' 
        AND COLUMN_NAME = 'id'
    " 2>/dev/null || echo "")
    
    if [ "$result" = "bigint" ]; then
        log "Migration needed: messages.id is currently BIGINT"
        return 0
    elif [ "$result" = "varchar" ]; then
        warn "Migration not needed: messages.id is already VARCHAR"
        return 1
    else
        error "Cannot determine current id column type or messages table doesn't exist"
        exit 1
    fi
}

# Function to backup database
backup_database() {
    local backup_file="backup_messages_$(date +%Y%m%d_%H%M%S).sql"
    
    log "Creating backup: $backup_file"
    
    if mysqldump -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" messages > "$backup_file"; then
        log "✓ Backup created successfully: $backup_file"
        echo "$backup_file"
    else
        error "✗ Backup failed"
        exit 1
    fi
}

# Function to verify migration
verify_migration() {
    log "Verifying migration results..."
    
    # Check column type
    local data_type=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -sN -e "
        SELECT DATA_TYPE 
        FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = '$DB_NAME' 
        AND TABLE_NAME = 'messages' 
        AND COLUMN_NAME = 'id'
    ")
    
    if [ "$data_type" != "varchar" ]; then
        error "Migration verification failed: id column is not VARCHAR"
        exit 1
    fi
    
    # Check column length
    local max_length=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -sN -e "
        SELECT CHARACTER_MAXIMUM_LENGTH 
        FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = '$DB_NAME' 
        AND TABLE_NAME = 'messages' 
        AND COLUMN_NAME = 'id'
    ")
    
    if [ "$max_length" != "36" ]; then
        error "Migration verification failed: id column length is not 36"
        exit 1
    fi
    
    # Check record count
    local record_count=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -sN -e "
        SELECT COUNT(*) FROM messages
    ")
    
    # Check valid ID format count
    local valid_id_count=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -sN -e "
        SELECT COUNT(*) FROM messages 
        WHERE id REGEXP '^[A-Z0-9]{8}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{12}$'
    ")
    
    if [ "$record_count" != "$valid_id_count" ]; then
        error "Migration verification failed: $valid_id_count/$record_count records have valid ID format"
        exit 1
    fi
    
    # Check uniqueness
    local unique_id_count=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -sN -e "
        SELECT COUNT(DISTINCT id) FROM messages
    ")
    
    if [ "$record_count" != "$unique_id_count" ]; then
        error "Migration verification failed: IDs are not unique ($unique_id_count unique out of $record_count total)"
        exit 1
    fi
    
    log "✓ Migration verification passed:"
    log "  - Column type: VARCHAR(36)"
    log "  - Total records: $record_count"
    log "  - Valid ID format: $valid_id_count"
    log "  - Unique IDs: $unique_id_count"
}

# Main migration function
run_migration() {
    log "Starting message ID migration..."
    
    # Read and execute migration script
    local migration_script="src/main/resources/db/migration/V2__migrate_message_id_to_varchar.sql"
    
    if [ ! -f "$migration_script" ]; then
        error "Migration script not found: $migration_script"
        exit 1
    fi
    
    log "Executing migration script: $migration_script"
    
    if mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" < "$migration_script"; then
        log "✓ Migration script executed successfully"
    else
        error "✗ Migration script execution failed"
        exit 1
    fi
}

# Main execution
main() {
    log "=== Message ID Migration Tool ==="
    log "Database: $DB_HOST:$DB_PORT/$DB_NAME"
    log "User: $DB_USER"
    
    # Check if migration is needed
    if ! check_migration_needed; then
        exit 0
    fi
    
    # Confirm with user
    echo
    warn "This migration will change the message ID format from BIGINT to VARCHAR(36)"
    warn "This is a potentially destructive operation that cannot be easily reversed"
    read -p "Do you want to continue? (yes/no): " confirm
    
    if [ "$confirm" != "yes" ]; then
        log "Migration cancelled by user"
        exit 0
    fi
    
    # Create backup
    backup_file=$(backup_database)
    
    # Run migration
    run_migration
    
    # Verify migration
    verify_migration
    
    log "=== Migration completed successfully ==="
    log "Backup file: $backup_file"
    log "You can now start your application with the new VARCHAR(36) message IDs"
}

# Run main function
main "$@"