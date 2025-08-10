#!/bin/bash

# Docker Health Check Script for PGB4 Message Board
# Monitors service health and provides detailed status information

set -euo pipefail

# Script configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

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

# Check if service is running
check_service_running() {
    local service_name="$1"
    if docker ps --format '{{.Names}}' | grep -q "^${service_name}$"; then
        return 0
    else
        return 1
    fi
}

# Check service health
check_service_health() {
    local service_name="$1"
    local health_status
    health_status=$(docker inspect --format='{{.State.Health.Status}}' "$service_name" 2>/dev/null || echo "no-healthcheck")
    
    case "$health_status" in
        "healthy")
            log_success "$service_name is healthy"
            return 0
            ;;
        "unhealthy")
            log_error "$service_name is unhealthy"
            return 1
            ;;
        "starting")
            log_warning "$service_name is starting"
            return 2
            ;;
        "no-healthcheck")
            log_warning "$service_name has no health check configured"
            return 3
            ;;
        *)
            log_error "$service_name has unknown health status: $health_status"
            return 1
            ;;
    esac
}

# Check HTTP endpoint
check_http_endpoint() {
    local name="$1"
    local url="$2"
    local timeout="${3:-10}"
    
    if curl -f -s --max-time "$timeout" "$url" > /dev/null 2>&1; then
        log_success "$name endpoint is responding"
        return 0
    else
        log_error "$name endpoint is not responding: $url"
        return 1
    fi
}

# Check database connectivity
check_database() {
    local container_name="pgb4-mysql"
    
    if ! check_service_running "$container_name"; then
        log_error "MySQL container is not running"
        return 1
    fi
    
    if docker exec "$container_name" mysqladmin ping -h localhost > /dev/null 2>&1; then
        log_success "MySQL database is accessible"
        return 0
    else
        log_error "MySQL database is not accessible"
        return 1
    fi
}

# Check Redis connectivity
check_redis() {
    local container_name="pgb4-redis"
    
    if ! check_service_running "$container_name"; then
        log_warning "Redis container is not running (optional service)"
        return 0
    fi
    
    if docker exec "$container_name" redis-cli ping > /dev/null 2>&1; then
        log_success "Redis is accessible"
        return 0
    else
        log_error "Redis is not accessible"
        return 1
    fi
}

# Get service resource usage
get_service_resources() {
    local service_name="$1"
    
    if check_service_running "$service_name"; then
        local stats
        stats=$(docker stats --no-stream --format "{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}\t{{.BlockIO}}" "$service_name")
        echo "$stats"
    else
        echo "Not running"
    fi
}

# Show detailed service status
show_service_details() {
    local service_name="$1"
    
    echo "=== $service_name ==="
    
    if check_service_running "$service_name"; then
        echo "Status: Running"
        
        # Health check
        check_service_health "$service_name"
        
        # Resource usage
        local resources
        resources=$(get_service_resources "$service_name")
        echo "Resources: $resources"
        
        # Uptime
        local uptime
        uptime=$(docker inspect --format='{{.State.StartedAt}}' "$service_name" 2>/dev/null || echo "Unknown")
        echo "Started: $uptime"
        
        # Port mappings
        local ports
        ports=$(docker port "$service_name" 2>/dev/null || echo "No ports exposed")
        echo "Ports: $ports"
        
    else
        echo "Status: Not running"
    fi
    
    echo
}

# Main health check
main_health_check() {
    cd "$PROJECT_ROOT"
    
    log_info "PGB4 Message Board Health Check"
    echo "================================"
    echo
    
    local overall_status=0
    
    # Check core services
    local services=("pgb4-mysql" "pgb4-backend" "pgb4-frontend")
    
    for service in "${services[@]}"; do
        show_service_details "$service"
        
        if ! check_service_running "$service"; then
            overall_status=1
        fi
    done
    
    # Check optional services
    local optional_services=("pgb4-redis" "pgb4-nginx-lb")
    
    for service in "${optional_services[@]}"; do
        if check_service_running "$service"; then
            show_service_details "$service"
        fi
    done
    
    echo "=== Connectivity Tests ==="
    
    # Check database
    if ! check_database; then
        overall_status=1
    fi
    
    # Check Redis (if running)
    check_redis
    
    # Check HTTP endpoints
    local backend_port="${SERVER_PORT:-8080}"
    local frontend_port="${FRONTEND_PORT:-3000}"
    
    if ! check_http_endpoint "Backend Health" "http://localhost:$backend_port/health"; then
        overall_status=1
    fi
    
    if ! check_http_endpoint "Frontend" "http://localhost:$frontend_port"; then
        overall_status=1
    fi
    
    echo
    echo "=== Overall Status ==="
    
    if [[ $overall_status -eq 0 ]]; then
        log_success "All critical services are healthy"
    else
        log_error "Some services have issues"
    fi
    
    return $overall_status
}

# Show usage
usage() {
    cat << EOF
Usage: $(basename "$0") [OPTIONS]

Docker health check script for PGB4 Message Board

OPTIONS:
    -s, --service SERVICE   Check specific service only
    -w, --watch            Watch mode (continuous monitoring)
    -i, --interval SECONDS  Watch interval [default: 30]
    -h, --help             Show this help message

EXAMPLES:
    # Full health check
    $(basename "$0")
    
    # Check specific service
    $(basename "$0") -s pgb4-backend
    
    # Continuous monitoring
    $(basename "$0") -w -i 10

EOF
}

# Parse arguments
SERVICE=""
WATCH=false
INTERVAL=30

while [[ $# -gt 0 ]]; do
    case $1 in
        -s|--service)
            SERVICE="$2"
            shift 2
            ;;
        -w|--watch)
            WATCH=true
            shift
            ;;
        -i|--interval)
            INTERVAL="$2"
            shift 2
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            log_error "Unknown option: $1"
            usage
            exit 1
            ;;
    esac
done

# Main execution
if [[ -n "$SERVICE" ]]; then
    # Check specific service
    show_service_details "$SERVICE"
elif [[ "$WATCH" == true ]]; then
    # Watch mode
    log_info "Starting health monitoring (interval: ${INTERVAL}s, press Ctrl+C to stop)"
    while true; do
        clear
        main_health_check
        sleep "$INTERVAL"
    done
else
    # Single health check
    main_health_check
fi