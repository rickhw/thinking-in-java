#!/bin/bash

# Docker Deployment Script for PGB4 Message Board
# This script automates the deployment process including build, push, and deployment
# Supports development, staging, and production environments

set -euo pipefail

# Script configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
SCRIPT_NAME="$(basename "$0")"

# Default configuration
DEFAULT_ENVIRONMENT="development"
DEFAULT_REGISTRY=""
DEFAULT_TAG="latest"
DEFAULT_COMPOSE_FILE="docker-compose.yml"
DEFAULT_TIMEOUT=300

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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
Usage: $SCRIPT_NAME [OPTIONS] COMMAND

Docker deployment automation script for PGB4 Message Board

COMMANDS:
    build       Build Docker images
    push        Push images to registry
    deploy      Deploy services using Docker Compose
    rollback    Rollback to previous version
    status      Show deployment status
    logs        Show service logs
    cleanup     Clean up unused resources
    backup      Backup persistent data
    restore     Restore from backup

OPTIONS:
    -e, --environment ENV    Target environment (development|staging|production) [default: $DEFAULT_ENVIRONMENT]
    -r, --registry REGISTRY  Docker registry URL [default: none]
    -t, --tag TAG           Image tag [default: $DEFAULT_TAG]
    -f, --file FILE         Docker Compose file [default: $DEFAULT_COMPOSE_FILE]
    -p, --profile PROFILE   Docker Compose profile (with-redis|with-loadbalancer)
    -s, --service SERVICE   Target specific service
    --timeout SECONDS       Deployment timeout [default: $DEFAULT_TIMEOUT]
    --no-cache             Build without cache
    --force                Force operation without confirmation
    --dry-run              Show what would be done without executing
    -v, --verbose          Verbose output
    -h, --help             Show this help message

EXAMPLES:
    # Development deployment
    $SCRIPT_NAME deploy

    # Production deployment with load balancer
    $SCRIPT_NAME -e production -p with-loadbalancer deploy

    # Build and push to registry
    $SCRIPT_NAME -r registry.example.com -t v1.2.3 build push

    # Rollback production deployment
    $SCRIPT_NAME -e production rollback

    # Show logs for backend service
    $SCRIPT_NAME -s backend logs

    # Backup production data
    $SCRIPT_NAME -e production backup

ENVIRONMENT FILES:
    .env                    Default environment file
    .env.development        Development environment
    .env.staging           Staging environment
    .env.production        Production environment

EOF
}

# Parse command line arguments
parse_arguments() {
    ENVIRONMENT="$DEFAULT_ENVIRONMENT"
    REGISTRY="$DEFAULT_REGISTRY"
    TAG="$DEFAULT_TAG"
    COMPOSE_FILE="$DEFAULT_COMPOSE_FILE"
    PROFILE=""
    SERVICE=""
    TIMEOUT="$DEFAULT_TIMEOUT"
    NO_CACHE=false
    FORCE=false
    DRY_RUN=false
    VERBOSE=false
    COMMAND=""

    while [[ $# -gt 0 ]]; do
        case $1 in
            -e|--environment)
                ENVIRONMENT="$2"
                shift 2
                ;;
            -r|--registry)
                REGISTRY="$2"
                shift 2
                ;;
            -t|--tag)
                TAG="$2"
                shift 2
                ;;
            -f|--file)
                COMPOSE_FILE="$2"
                shift 2
                ;;
            -p|--profile)
                PROFILE="$2"
                shift 2
                ;;
            -s|--service)
                SERVICE="$2"
                shift 2
                ;;
            --timeout)
                TIMEOUT="$2"
                shift 2
                ;;
            --no-cache)
                NO_CACHE=true
                shift
                ;;
            --force)
                FORCE=true
                shift
                ;;
            --dry-run)
                DRY_RUN=true
                shift
                ;;
            -v|--verbose)
                VERBOSE=true
                shift
                ;;
            -h|--help)
                usage
                exit 0
                ;;
            build|push|deploy|rollback|status|logs|cleanup|backup|restore)
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

# Validate environment
validate_environment() {
    case "$ENVIRONMENT" in
        development|staging|production)
            ;;
        *)
            log_error "Invalid environment: $ENVIRONMENT"
            log_error "Valid environments: development, staging, production"
            exit 1
            ;;
    esac
}

# Check prerequisites
check_prerequisites() {
    local missing_tools=()

    # Check Docker
    if ! command -v docker &> /dev/null; then
        missing_tools+=("docker")
    fi

    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        missing_tools+=("docker-compose")
    fi

    # Check required tools
    for tool in curl jq; do
        if ! command -v "$tool" &> /dev/null; then
            missing_tools+=("$tool")
        fi
    done

    if [[ ${#missing_tools[@]} -gt 0 ]]; then
        log_error "Missing required tools: ${missing_tools[*]}"
        log_error "Please install the missing tools and try again"
        exit 1
    fi

    # Check Docker daemon
    if ! docker info &> /dev/null; then
        log_error "Docker daemon is not running"
        exit 1
    fi
}

# Load environment configuration
load_environment() {
    local env_file=".env"
    local specific_env_file=".env.$ENVIRONMENT"

    # Load default environment
    if [[ -f "$env_file" ]]; then
        log_info "Loading default environment from $env_file"
        set -a
        source "$env_file"
        set +a
    fi

    # Load environment-specific configuration
    if [[ -f "$specific_env_file" ]]; then
        log_info "Loading $ENVIRONMENT environment from $specific_env_file"
        set -a
        source "$specific_env_file"
        set +a
    fi

    # Set build version
    export BUILD_VERSION="${TAG}"
    export BUILD_TIME="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
}

# Get Docker Compose command
get_compose_command() {
    if command -v docker-compose &> /dev/null; then
        echo "docker-compose"
    else
        echo "docker compose"
    fi
}

# Build compose file arguments
build_compose_args() {
    local args=()
    
    # Base compose file
    args+=("-f" "$COMPOSE_FILE")
    
    # Environment-specific compose file
    case "$ENVIRONMENT" in
        production)
            if [[ -f "docker-compose.prod.yml" ]]; then
                args+=("-f" "docker-compose.prod.yml")
            fi
            ;;
        staging)
            if [[ -f "docker-compose.staging.yml" ]]; then
                args+=("-f" "docker-compose.staging.yml")
            fi
            ;;
    esac
    
    # Profile
    if [[ -n "$PROFILE" ]]; then
        args+=("--profile" "$PROFILE")
    fi
    
    echo "${args[@]}"
}

# Execute command with dry run support
execute_command() {
    local cmd="$1"
    
    if [[ "$DRY_RUN" == true ]]; then
        log_info "[DRY RUN] Would execute: $cmd"
    else
        if [[ "$VERBOSE" == true ]]; then
            log_info "Executing: $cmd"
        fi
        eval "$cmd"
    fi
}

# Build Docker images
build_images() {
    log_info "Building Docker images for $ENVIRONMENT environment..."
    
    local compose_cmd
    compose_cmd="$(get_compose_command)"
    
    local compose_args
    compose_args=($(build_compose_args))
    
    local build_args=("build")
    
    if [[ "$NO_CACHE" == true ]]; then
        build_args+=("--no-cache")
    fi
    
    build_args+=("--build-arg" "BUILD_VERSION=$TAG")
    build_args+=("--build-arg" "BUILD_TIME=$BUILD_TIME")
    
    if [[ -n "$SERVICE" ]]; then
        build_args+=("$SERVICE")
    fi
    
    local cmd="$compose_cmd ${compose_args[*]} ${build_args[*]}"
    execute_command "$cmd"
    
    if [[ "$DRY_RUN" == false ]]; then
        log_success "Images built successfully"
    fi
}

# Push images to registry
push_images() {
    if [[ -z "$REGISTRY" ]]; then
        log_warning "No registry specified, skipping push"
        return 0
    fi
    
    log_info "Pushing images to registry: $REGISTRY"
    
    local services=("backend" "frontend")
    if [[ -n "$SERVICE" ]]; then
        services=("$SERVICE")
    fi
    
    for service in "${services[@]}"; do
        local image_name="pgb4-${service}:${TAG}"
        local registry_image="$REGISTRY/$image_name"
        
        log_info "Tagging $image_name as $registry_image"
        execute_command "docker tag $image_name $registry_image"
        
        log_info "Pushing $registry_image"
        execute_command "docker push $registry_image"
    done
    
    if [[ "$DRY_RUN" == false ]]; then
        log_success "Images pushed successfully"
    fi
}

# Deploy services
deploy_services() {
    log_info "Deploying services for $ENVIRONMENT environment..."
    
    # Pre-deployment checks
    check_deployment_prerequisites
    
    local compose_cmd
    compose_cmd="$(get_compose_command)"
    
    local compose_args
    compose_args=($(build_compose_args))
    
    # Create necessary directories
    execute_command "mkdir -p data/mysql data/redis logs/backend logs/frontend logs/nginx"
    
    # Deploy services
    local deploy_args=("up" "-d")
    
    if [[ -n "$SERVICE" ]]; then
        deploy_args+=("$SERVICE")
    fi
    
    local cmd="$compose_cmd ${compose_args[*]} ${deploy_args[*]}"
    execute_command "$cmd"
    
    if [[ "$DRY_RUN" == false ]]; then
        # Wait for services to be healthy
        wait_for_services
        
        # Verify deployment
        verify_deployment
        
        log_success "Deployment completed successfully"
    fi
}

# Check deployment prerequisites
check_deployment_prerequisites() {
    log_info "Checking deployment prerequisites..."
    
    # Check available disk space
    local available_space
    available_space=$(df . | awk 'NR==2 {print $4}')
    local required_space=1048576  # 1GB in KB
    
    if [[ $available_space -lt $required_space ]]; then
        log_error "Insufficient disk space. Required: 1GB, Available: $((available_space/1024))MB"
        exit 1
    fi
    
    # Check available memory
    local available_memory
    available_memory=$(free -m | awk 'NR==2{print $7}')
    local required_memory=2048  # 2GB
    
    if [[ $available_memory -lt $required_memory ]]; then
        log_warning "Low available memory. Required: 2GB, Available: ${available_memory}MB"
        if [[ "$FORCE" == false ]]; then
            read -p "Continue anyway? (y/N): " -n 1 -r
            echo
            if [[ ! $REPLY =~ ^[Yy]$ ]]; then
                exit 1
            fi
        fi
    fi
}

# Wait for services to be healthy
wait_for_services() {
    log_info "Waiting for services to be healthy..."
    
    local compose_cmd
    compose_cmd="$(get_compose_command)"
    
    local compose_args
    compose_args=($(build_compose_args))
    
    local timeout_count=0
    local max_timeout=$((TIMEOUT / 10))
    
    while [[ $timeout_count -lt $max_timeout ]]; do
        local unhealthy_services
        unhealthy_services=$($compose_cmd "${compose_args[@]}" ps --format json | jq -r '.[] | select(.Health != "healthy" and .Health != "") | .Service' 2>/dev/null || echo "")
        
        if [[ -z "$unhealthy_services" ]]; then
            log_success "All services are healthy"
            return 0
        fi
        
        log_info "Waiting for services to be healthy: $unhealthy_services"
        sleep 10
        ((timeout_count++))
    done
    
    log_error "Timeout waiting for services to be healthy"
    show_service_status
    exit 1
}

# Verify deployment
verify_deployment() {
    log_info "Verifying deployment..."
    
    local errors=0
    
    # Check backend health
    if ! curl -f -s "http://localhost:${SERVER_PORT:-8080}/health" > /dev/null; then
        log_error "Backend health check failed"
        ((errors++))
    else
        log_success "Backend is healthy"
    fi
    
    # Check frontend
    if ! curl -f -s "http://localhost:${FRONTEND_PORT:-3000}/health" > /dev/null; then
        log_error "Frontend health check failed"
        ((errors++))
    else
        log_success "Frontend is healthy"
    fi
    
    # Check database connectivity
    local compose_cmd
    compose_cmd="$(get_compose_command)"
    
    local compose_args
    compose_args=($(build_compose_args))
    
    if ! $compose_cmd "${compose_args[@]}" exec -T mysql mysqladmin ping -h localhost > /dev/null 2>&1; then
        log_error "Database connectivity check failed"
        ((errors++))
    else
        log_success "Database is accessible"
    fi
    
    if [[ $errors -gt 0 ]]; then
        log_error "Deployment verification failed with $errors errors"
        exit 1
    fi
    
    log_success "Deployment verification passed"
}

# Rollback deployment
rollback_deployment() {
    log_info "Rolling back deployment..."
    
    if [[ "$FORCE" == false ]]; then
        read -p "Are you sure you want to rollback? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            log_info "Rollback cancelled"
            exit 0
        fi
    fi
    
    local compose_cmd
    compose_cmd="$(get_compose_command)"
    
    local compose_args
    compose_args=($(build_compose_args))
    
    # Stop current services
    execute_command "$compose_cmd ${compose_args[*]} down"
    
    # TODO: Implement proper rollback logic with previous image tags
    log_warning "Rollback functionality requires implementation of image versioning"
    log_info "Manual rollback steps:"
    log_info "1. Identify previous working image tags"
    log_info "2. Update environment variables or compose files"
    log_info "3. Redeploy with previous configuration"
    
    if [[ "$DRY_RUN" == false ]]; then
        log_success "Services stopped for rollback"
    fi
}

# Show deployment status
show_service_status() {
    log_info "Service status:"
    
    local compose_cmd
    compose_cmd="$(get_compose_command)"
    
    local compose_args
    compose_args=($(build_compose_args))
    
    $compose_cmd "${compose_args[@]}" ps
    
    echo
    log_info "Resource usage:"
    docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}\t{{.BlockIO}}"
}

# Show service logs
show_service_logs() {
    local compose_cmd
    compose_cmd="$(get_compose_command)"
    
    local compose_args
    compose_args=($(build_compose_args))
    
    local log_args=("logs" "-f")
    
    if [[ -n "$SERVICE" ]]; then
        log_args+=("$SERVICE")
    fi
    
    $compose_cmd "${compose_args[@]}" "${log_args[@]}"
}

# Cleanup unused resources
cleanup_resources() {
    log_info "Cleaning up unused Docker resources..."
    
    if [[ "$FORCE" == false ]]; then
        read -p "This will remove unused containers, networks, images, and volumes. Continue? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            log_info "Cleanup cancelled"
            exit 0
        fi
    fi
    
    execute_command "docker system prune -f"
    execute_command "docker volume prune -f"
    execute_command "docker network prune -f"
    
    if [[ "$DRY_RUN" == false ]]; then
        log_success "Cleanup completed"
    fi
}

# Backup persistent data
backup_data() {
    log_info "Creating backup of persistent data..."
    
    local backup_dir="backups/$(date +%Y%m%d_%H%M%S)"
    execute_command "mkdir -p $backup_dir"
    
    # Backup MySQL data
    if docker ps --format '{{.Names}}' | grep -q pgb4-mysql; then
        log_info "Backing up MySQL data..."
        execute_command "docker exec pgb4-mysql mysqldump -u root -p\${DB_ROOT_PASSWORD:-medusa} --all-databases > $backup_dir/mysql_backup.sql"
    fi
    
    # Backup Redis data
    if docker ps --format '{{.Names}}' | grep -q pgb4-redis; then
        log_info "Backing up Redis data..."
        execute_command "docker exec pgb4-redis redis-cli BGSAVE"
        execute_command "docker cp pgb4-redis:/data/dump.rdb $backup_dir/redis_backup.rdb"
    fi
    
    # Backup application logs
    if [[ -d "logs" ]]; then
        log_info "Backing up application logs..."
        execute_command "tar -czf $backup_dir/logs_backup.tar.gz logs/"
    fi
    
    if [[ "$DRY_RUN" == false ]]; then
        log_success "Backup created in $backup_dir"
    fi
}

# Restore from backup
restore_data() {
    log_info "Restoring from backup..."
    
    # TODO: Implement restore functionality
    log_warning "Restore functionality not yet implemented"
    log_info "Manual restore steps:"
    log_info "1. Stop services: docker-compose down"
    log_info "2. Restore MySQL: docker exec -i pgb4-mysql mysql -u root -p < backup.sql"
    log_info "3. Restore Redis: docker cp backup.rdb pgb4-redis:/data/dump.rdb"
    log_info "4. Restart services: docker-compose up -d"
}

# Main execution
main() {
    cd "$PROJECT_ROOT"
    
    parse_arguments "$@"
    validate_environment
    check_prerequisites
    load_environment
    
    log_info "Starting $COMMAND for $ENVIRONMENT environment"
    
    case "$COMMAND" in
        build)
            build_images
            ;;
        push)
            push_images
            ;;
        deploy)
            build_images
            deploy_services
            ;;
        rollback)
            rollback_deployment
            ;;
        status)
            show_service_status
            ;;
        logs)
            show_service_logs
            ;;
        cleanup)
            cleanup_resources
            ;;
        backup)
            backup_data
            ;;
        restore)
            restore_data
            ;;
        *)
            log_error "Unknown command: $COMMAND"
            usage
            exit 1
            ;;
    esac
}

# Execute main function with all arguments
main "$@"