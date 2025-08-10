#!/bin/bash

# Docker Backup and Restore Script for PGB4 Message Board
# Handles backup and restoration of persistent data and configurations

set -euo pipefail

# Script configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BACKUP_DIR="${PROJECT_ROOT}/backups"

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Usage information
usage() {
    cat << EOF
Usage: $(basename "$0") [OPTIONS] COMMAND

Docker backup and restore script for PGB4 Message Board

COMMANDS:
    backup      Create backup of all data
    restore     Restore from backup
    list        List available backups
    cleanup     Clean old backups

OPTIONS:
    -b, --backup-name NAME    Specific backup name
    -d, --backup-dir DIR      Backup directory [default: ./backups]
    -k, --keep DAYS          Keep backups for N days [default: 30]
    -c, --compress           Compress backup files
    -v, --verbose            Verbose output
    -f, --force              Force operation without confirmation
    -h, --help               Show this help message

EXAMPLES:
    # Create backup
    $(basename "$0") backup

    # Create named backup
    $(basename "$0") -b production-v1.2.3 backup

    # Restore from specific backup
    $(basename "$0") -b 20240110_143022 restore

    # List backups
    $(basename "$0") list

    # Clean old backups
    $(basename "$0") -k 7 cleanup

EOF
}

# Parse arguments
parse_arguments() {
    BACKUP_NAME=""
    KEEP_DAYS=30
    COMPRESS=false
    VERBOSE=false
    FORCE=false
    COMMAND=""

    while [[ $# -gt 0 ]]; do
        case $1 in
            -b|--backup-name)
                BACKUP_NAME="$2"
                shift 2
                ;;
            -d|--backup-dir)
                BACKUP_DIR="$2"
                shift 2
                ;;
            -k|--keep)
                KEEP_DAYS="$2"
                shift 2
                ;;
            -c|--compress)
                COMPRESS=true
                shift
                ;;
            -v|--verbose)
                VERBOSE=true
                shift
                ;;
            -f|--force)
                FORCE=true
                shift
                ;;
            -h|--help)
                usage
                exit 0
                ;;
            backup|restore|list|cleanup)
                COMMAND="$1"
                shift
                ;;
            *)
                log_error "Unknown option: $1"
                usage
                exit 1
                ;;
        esac
    done

    if [[ -z "$COMMAND" ]]; then
        log_error "Command is required"
        usage
        exit 1
    fi
}

# Check prerequisites
check_prerequisites() {
    # Check Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed"
        exit 1
    fi

    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        log_error "Docker Compose is not installed"
        exit 1
    fi

    # Create backup directory
    mkdir -p "$BACKUP_DIR"
}

# Get Docker Compose command
get_compose_command() {
    if command -v docker-compose &> /dev/null; then
        echo "docker-compose"
    else
        echo "docker compose"
    fi
}

# Check if service is running
is_service_running() {
    local service_name="$1"
    docker ps --format '{{.Names}}' | grep -q "^${service_name}$"
}

# Create MySQL backup
backup_mysql() {
    local backup_path="$1"
    local container_name="pgb4-mysql"
    
    if ! is_service_running "$container_name"; then
        log_warning "MySQL container is not running, skipping database backup"
        return 0
    fi
    
    log_info "Backing up MySQL database..."
    
    # Get database credentials from environment
    local db_root_password="${DB_ROOT_PASSWORD:-medusa}"
    local db_name="${DB_NAME:-pgb}"
    
    # Create database dump
    local mysql_backup="$backup_path/mysql_dump.sql"
    
    if docker exec "$container_name" mysqldump \
        -u root \
        -p"$db_root_password" \
        --single-transaction \
        --routines \
        --triggers \
        --all-databases > "$mysql_backup" 2>/dev/null; then
        
        log_success "MySQL backup created: $(basename "$mysql_backup")"
        
        # Get database size
        local db_size
        db_size=$(du -h "$mysql_backup" | cut -f1)
        log_info "Database backup size: $db_size"
        
    else
        log_error "Failed to create MySQL backup"
        return 1
    fi
    
    # Backup MySQL configuration
    if docker cp "$container_name:/etc/mysql/conf.d" "$backup_path/mysql_config" 2>/dev/null; then
        log_info "MySQL configuration backed up"
    fi
}

# Create Redis backup
backup_redis() {
    local backup_path="$1"
    local container_name="pgb4-redis"
    
    if ! is_service_running "$container_name"; then
        log_info "Redis container is not running, skipping Redis backup"
        return 0
    fi
    
    log_info "Backing up Redis data..."
    
    # Force Redis to save current state
    if docker exec "$container_name" redis-cli BGSAVE > /dev/null 2>&1; then
        # Wait for background save to complete
        sleep 2
        
        # Copy Redis dump file
        if docker cp "$container_name:/data/dump.rdb" "$backup_path/redis_dump.rdb" 2>/dev/null; then
            log_success "Redis backup created: redis_dump.rdb"
        else
            log_warning "Failed to copy Redis dump file"
        fi
        
        # Copy Redis configuration
        if docker cp "$container_name:/usr/local/etc/redis/redis.conf" "$backup_path/redis.conf" 2>/dev/null; then
            log_info "Redis configuration backed up"
        fi
    else
        log_warning "Failed to trigger Redis background save"
    fi
}

# Backup application logs
backup_logs() {
    local backup_path="$1"
    
    if [[ -d "$PROJECT_ROOT/logs" ]]; then
        log_info "Backing up application logs..."
        
        local logs_backup="$backup_path/logs"
        cp -r "$PROJECT_ROOT/logs" "$logs_backup"
        
        log_success "Application logs backed up"
        
        # Get logs size
        local logs_size
        logs_size=$(du -sh "$logs_backup" | cut -f1)
        log_info "Logs backup size: $logs_size"
    else
        log_info "No logs directory found, skipping logs backup"
    fi
}

# Backup configuration files
backup_configs() {
    local backup_path="$1"
    
    log_info "Backing up configuration files..."
    
    local config_backup="$backup_path/config"
    mkdir -p "$config_backup"
    
    # Backup Docker Compose files
    for file in docker-compose*.yml; do
        if [[ -f "$file" ]]; then
            cp "$file" "$config_backup/"
        fi
    done
    
    # Backup environment files
    for file in .env*; do
        if [[ -f "$file" ]]; then
            cp "$file" "$config_backup/"
        fi
    done
    
    # Backup config directory
    if [[ -d "$PROJECT_ROOT/config" ]]; then
        cp -r "$PROJECT_ROOT/config" "$config_backup/"
    fi
    
    log_success "Configuration files backed up"
}

# Create backup metadata
create_backup_metadata() {
    local backup_path="$1"
    local backup_name="$2"
    
    local metadata_file="$backup_path/backup_metadata.json"
    
    cat > "$metadata_file" << EOF
{
    "backup_name": "$backup_name",
    "timestamp": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
    "hostname": "$(hostname)",
    "user": "$(whoami)",
    "project_root": "$PROJECT_ROOT",
    "services": {
        "mysql": $(is_service_running "pgb4-mysql" && echo "true" || echo "false"),
        "backend": $(is_service_running "pgb4-backend" && echo "true" || echo "false"),
        "frontend": $(is_service_running "pgb4-frontend" && echo "true" || echo "false"),
        "redis": $(is_service_running "pgb4-redis" && echo "true" || echo "false")
    },
    "docker_version": "$(docker --version)",
    "compose_version": "$($(get_compose_command) --version)"
}
EOF
    
    log_info "Backup metadata created"
}

# Compress backup
compress_backup() {
    local backup_path="$1"
    local backup_name="$2"
    
    log_info "Compressing backup..."
    
    local compressed_file="$BACKUP_DIR/${backup_name}.tar.gz"
    
    if tar -czf "$compressed_file" -C "$BACKUP_DIR" "$backup_name"; then
        # Remove uncompressed backup
        rm -rf "$backup_path"
        
        local compressed_size
        compressed_size=$(du -h "$compressed_file" | cut -f1)
        
        log_success "Backup compressed: $(basename "$compressed_file") ($compressed_size)"
    else
        log_error "Failed to compress backup"
        return 1
    fi
}

# Create full backup
create_backup() {
    local timestamp
    timestamp=$(date +%Y%m%d_%H%M%S)
    
    local backup_name
    if [[ -n "$BACKUP_NAME" ]]; then
        backup_name="$BACKUP_NAME"
    else
        backup_name="backup_$timestamp"
    fi
    
    local backup_path="$BACKUP_DIR/$backup_name"
    
    log_info "Creating backup: $backup_name"
    
    # Create backup directory
    mkdir -p "$backup_path"
    
    # Backup components
    backup_mysql "$backup_path"
    backup_redis "$backup_path"
    backup_logs "$backup_path"
    backup_configs "$backup_path"
    create_backup_metadata "$backup_path" "$backup_name"
    
    # Compress if requested
    if [[ "$COMPRESS" == true ]]; then
        compress_backup "$backup_path" "$backup_name"
    fi
    
    log_success "Backup completed: $backup_name"
    
    # Show backup size
    if [[ "$COMPRESS" == true ]]; then
        local backup_file="$BACKUP_DIR/${backup_name}.tar.gz"
        local backup_size
        backup_size=$(du -h "$backup_file" | cut -f1)
        log_info "Total backup size: $backup_size"
    else
        local backup_size
        backup_size=$(du -sh "$backup_path" | cut -f1)
        log_info "Total backup size: $backup_size"
    fi
}

# Restore MySQL
restore_mysql() {
    local backup_path="$1"
    local container_name="pgb4-mysql"
    
    local mysql_backup="$backup_path/mysql_dump.sql"
    
    if [[ ! -f "$mysql_backup" ]]; then
        log_warning "MySQL backup file not found, skipping database restore"
        return 0
    fi
    
    if ! is_service_running "$container_name"; then
        log_error "MySQL container is not running"
        return 1
    fi
    
    log_info "Restoring MySQL database..."
    
    # Get database credentials
    local db_root_password="${DB_ROOT_PASSWORD:-medusa}"
    
    if docker exec -i "$container_name" mysql -u root -p"$db_root_password" < "$mysql_backup"; then
        log_success "MySQL database restored"
    else
        log_error "Failed to restore MySQL database"
        return 1
    fi
}

# Restore Redis
restore_redis() {
    local backup_path="$1"
    local container_name="pgb4-redis"
    
    local redis_backup="$backup_path/redis_dump.rdb"
    
    if [[ ! -f "$redis_backup" ]]; then
        log_info "Redis backup file not found, skipping Redis restore"
        return 0
    fi
    
    if ! is_service_running "$container_name"; then
        log_warning "Redis container is not running, skipping Redis restore"
        return 0
    fi
    
    log_info "Restoring Redis data..."
    
    # Stop Redis temporarily
    docker exec "$container_name" redis-cli SHUTDOWN NOSAVE || true
    sleep 2
    
    # Copy backup file
    if docker cp "$redis_backup" "$container_name:/data/dump.rdb"; then
        # Restart Redis
        docker restart "$container_name"
        sleep 5
        
        log_success "Redis data restored"
    else
        log_error "Failed to restore Redis data"
        return 1
    fi
}

# Restore from backup
restore_backup() {
    if [[ -z "$BACKUP_NAME" ]]; then
        log_error "Backup name is required for restore operation"
        log_info "Use: $(basename "$0") list to see available backups"
        exit 1
    fi
    
    local backup_path="$BACKUP_DIR/$BACKUP_NAME"
    local compressed_backup="$BACKUP_DIR/${BACKUP_NAME}.tar.gz"
    
    # Check if backup exists
    if [[ -d "$backup_path" ]]; then
        log_info "Found uncompressed backup: $BACKUP_NAME"
    elif [[ -f "$compressed_backup" ]]; then
        log_info "Found compressed backup: ${BACKUP_NAME}.tar.gz"
        log_info "Extracting backup..."
        
        if tar -xzf "$compressed_backup" -C "$BACKUP_DIR"; then
            log_success "Backup extracted"
        else
            log_error "Failed to extract backup"
            exit 1
        fi
    else
        log_error "Backup not found: $BACKUP_NAME"
        exit 1
    fi
    
    # Confirm restore operation
    if [[ "$FORCE" == false ]]; then
        log_warning "This will overwrite current data!"
        read -p "Are you sure you want to restore from backup '$BACKUP_NAME'? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            log_info "Restore cancelled"
            exit 0
        fi
    fi
    
    log_info "Restoring from backup: $BACKUP_NAME"
    
    # Restore components
    restore_mysql "$backup_path"
    restore_redis "$backup_path"
    
    # TODO: Restore logs and configs if needed
    
    log_success "Restore completed from backup: $BACKUP_NAME"
}

# List available backups
list_backups() {
    log_info "Available backups in $BACKUP_DIR:"
    echo
    
    if [[ ! -d "$BACKUP_DIR" ]] || [[ -z "$(ls -A "$BACKUP_DIR" 2>/dev/null)" ]]; then
        log_info "No backups found"
        return 0
    fi
    
    # List directories (uncompressed backups)
    for backup in "$BACKUP_DIR"/*/; do
        if [[ -d "$backup" ]]; then
            local backup_name
            backup_name=$(basename "$backup")
            local backup_size
            backup_size=$(du -sh "$backup" | cut -f1)
            local backup_date
            backup_date=$(stat -c %y "$backup" 2>/dev/null | cut -d' ' -f1 || echo "Unknown")
            
            echo "  📁 $backup_name ($backup_size) - $backup_date"
            
            # Show metadata if available
            local metadata_file="$backup/backup_metadata.json"
            if [[ -f "$metadata_file" ]] && command -v jq &> /dev/null; then
                local timestamp
                timestamp=$(jq -r '.timestamp' "$metadata_file" 2>/dev/null || echo "Unknown")
                echo "     Created: $timestamp"
            fi
        fi
    done
    
    # List compressed backups
    for backup in "$BACKUP_DIR"/*.tar.gz; do
        if [[ -f "$backup" ]]; then
            local backup_name
            backup_name=$(basename "$backup" .tar.gz)
            local backup_size
            backup_size=$(du -sh "$backup" | cut -f1)
            local backup_date
            backup_date=$(stat -c %y "$backup" 2>/dev/null | cut -d' ' -f1 || echo "Unknown")
            
            echo "  📦 $backup_name.tar.gz ($backup_size) - $backup_date"
        fi
    done
}

# Clean old backups
cleanup_backups() {
    log_info "Cleaning backups older than $KEEP_DAYS days..."
    
    if [[ ! -d "$BACKUP_DIR" ]]; then
        log_info "No backup directory found"
        return 0
    fi
    
    local deleted_count=0
    
    # Clean old directories
    while IFS= read -r -d '' backup; do
        if [[ -d "$backup" ]]; then
            rm -rf "$backup"
            log_info "Deleted old backup: $(basename "$backup")"
            ((deleted_count++))
        fi
    done < <(find "$BACKUP_DIR" -maxdepth 1 -type d -mtime +$KEEP_DAYS -print0 2>/dev/null)
    
    # Clean old compressed files
    while IFS= read -r -d '' backup; do
        if [[ -f "$backup" ]]; then
            rm -f "$backup"
            log_info "Deleted old backup: $(basename "$backup")"
            ((deleted_count++))
        fi
    done < <(find "$BACKUP_DIR" -maxdepth 1 -name "*.tar.gz" -mtime +$KEEP_DAYS -print0 2>/dev/null)
    
    if [[ $deleted_count -eq 0 ]]; then
        log_info "No old backups to clean"
    else
        log_success "Cleaned $deleted_count old backups"
    fi
}

# Main execution
main() {
    cd "$PROJECT_ROOT"
    
    parse_arguments "$@"
    check_prerequisites
    
    case "$COMMAND" in
        backup)
            create_backup
            ;;
        restore)
            restore_backup
            ;;
        list)
            list_backups
            ;;
        cleanup)
            cleanup_backups
            ;;
        *)
            log_error "Unknown command: $COMMAND"
            usage
            exit 1
            ;;
    esac
}

# Execute main function
main "$@"