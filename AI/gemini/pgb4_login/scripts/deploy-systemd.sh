#!/bin/bash

# PGB4 Message Board SystemD Deployment Script
# This script automates the deployment of PGB4 application using SystemD services

set -euo pipefail

# Script configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
DEPLOYMENT_DIR="$PROJECT_ROOT/deployment/systemd"

# Default configuration
DEFAULT_INSTALL_DIR="/opt/pgb4"
DEFAULT_LOG_DIR="/var/log/pgb4"
DEFAULT_BACKEND_USER="pgb4"
DEFAULT_FRONTEND_USER="nginx"
DEFAULT_BACKEND_PORT="8080"
DEFAULT_FRONTEND_PORT="80"

# Colors for output
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
Usage: $0 [OPTIONS] COMMAND

Commands:
    install     Install and configure SystemD services
    start       Start PGB4 services
    stop        Stop PGB4 services
    restart     Restart PGB4 services
    status      Show service status
    logs        Show service logs
    uninstall   Remove SystemD services
    health      Check service health

Options:
    -d, --install-dir DIR    Installation directory (default: $DEFAULT_INSTALL_DIR)
    -l, --log-dir DIR        Log directory (default: $DEFAULT_LOG_DIR)
    -p, --backend-port PORT  Backend port (default: $DEFAULT_BACKEND_PORT)
    -f, --frontend-port PORT Frontend port (default: $DEFAULT_FRONTEND_PORT)
    -u, --backend-user USER  Backend user (default: $DEFAULT_BACKEND_USER)
    -n, --frontend-user USER Frontend user (default: $DEFAULT_FRONTEND_USER)
    -c, --config FILE        Configuration file
    -v, --verbose            Verbose output
    -h, --help               Show this help message

Examples:
    $0 install                          # Install with default settings
    $0 -d /opt/myapp install           # Install to custom directory
    $0 start                           # Start services
    $0 status                          # Check service status
    $0 logs                            # View logs

EOF
}

# Parse command line arguments
parse_args() {
    INSTALL_DIR="$DEFAULT_INSTALL_DIR"
    LOG_DIR="$DEFAULT_LOG_DIR"
    BACKEND_USER="$DEFAULT_BACKEND_USER"
    FRONTEND_USER="$DEFAULT_FRONTEND_USER"
    BACKEND_PORT="$DEFAULT_BACKEND_PORT"
    FRONTEND_PORT="$DEFAULT_FRONTEND_PORT"
    CONFIG_FILE=""
    VERBOSE=false
    COMMAND=""

    while [[ $# -gt 0 ]]; do
        case $1 in
            -d|--install-dir)
                INSTALL_DIR="$2"
                shift 2
                ;;
            -l|--log-dir)
                LOG_DIR="$2"
                shift 2
                ;;
            -p|--backend-port)
                BACKEND_PORT="$2"
                shift 2
                ;;
            -f|--frontend-port)
                FRONTEND_PORT="$2"
                shift 2
                ;;
            -u|--backend-user)
                BACKEND_USER="$2"
                shift 2
                ;;
            -n|--frontend-user)
                FRONTEND_USER="$2"
                shift 2
                ;;
            -c|--config)
                CONFIG_FILE="$2"
                shift 2
                ;;
            -v|--verbose)
                VERBOSE=true
                shift
                ;;
            -h|--help)
                usage
                exit 0
                ;;
            install|start|stop|restart|status|logs|uninstall|health)
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
        log_error "No command specified"
        usage
        exit 1
    fi
}

# Load configuration file if provided
load_config() {
    if [[ -n "$CONFIG_FILE" && -f "$CONFIG_FILE" ]]; then
        log_info "Loading configuration from $CONFIG_FILE"
        source "$CONFIG_FILE"
    fi
}

# Check if running as root
check_root() {
    if [[ $EUID -ne 0 ]]; then
        log_error "This script must be run as root (use sudo)"
        exit 1
    fi
}

# Check system requirements
check_requirements() {
    log_info "Checking system requirements..."
    
    # Check if systemd is available
    if ! command -v systemctl &> /dev/null; then
        log_error "SystemD is not available on this system"
        exit 1
    fi
    
    # Check if Java is available
    if ! command -v java &> /dev/null; then
        log_error "Java is not installed. Please install Java 17 or higher"
        exit 1
    fi
    
    # Check Java version
    JAVA_VERSION=$(java -version 2>&1 | head -n1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [[ "$JAVA_VERSION" -lt 17 ]]; then
        log_error "Java 17 or higher is required. Current version: $JAVA_VERSION"
        exit 1
    fi
    
    # Check if nginx is available
    if ! command -v nginx &> /dev/null; then
        log_error "Nginx is not installed. Please install nginx"
        exit 1
    fi
    
    log_success "System requirements check passed"
}

# Create system users
create_users() {
    log_info "Creating system users..."
    
    # Create backend user
    if ! id "$BACKEND_USER" &>/dev/null; then
        useradd -r -s /bin/false "$BACKEND_USER"
        log_success "Created user: $BACKEND_USER"
    else
        log_info "User $BACKEND_USER already exists"
    fi
    
    # Create frontend user (nginx)
    if ! id "$FRONTEND_USER" &>/dev/null; then
        useradd -r -s /bin/false "$FRONTEND_USER"
        log_success "Created user: $FRONTEND_USER"
    else
        log_info "User $FRONTEND_USER already exists"
    fi
}

# Create directory structure
create_directories() {
    log_info "Creating directory structure..."
    
    # Create installation directories
    mkdir -p "$INSTALL_DIR"/{backend,frontend}
    mkdir -p "$LOG_DIR"
    mkdir -p /var/cache/nginx
    mkdir -p /var/run
    
    # Set permissions
    chown "$BACKEND_USER:$BACKEND_USER" "$INSTALL_DIR/backend" "$LOG_DIR"
    chown "$FRONTEND_USER:$FRONTEND_USER" "$INSTALL_DIR/frontend"
    chown "$FRONTEND_USER:$FRONTEND_USER" /var/cache/nginx
    
    log_success "Directory structure created"
}

# Check if required files exist
check_deployment_files() {
    log_info "Checking deployment files..."
    
    local missing_files=()
    
    # Check service files
    if [[ ! -f "$DEPLOYMENT_DIR/pgb4-backend.service" ]]; then
        missing_files+=("$DEPLOYMENT_DIR/pgb4-backend.service")
    fi
    
    if [[ ! -f "$DEPLOYMENT_DIR/pgb4-frontend.service" ]]; then
        missing_files+=("$DEPLOYMENT_DIR/pgb4-frontend.service")
    fi
    
    if [[ ! -f "$DEPLOYMENT_DIR/pgb4.target" ]]; then
        missing_files+=("$DEPLOYMENT_DIR/pgb4.target")
    fi
    
    # Check application files
    if [[ ! -f "$PROJECT_ROOT/backend/build/libs"/*.jar ]]; then
        missing_files+=("Backend JAR file")
    fi
    
    if [[ ! -d "$PROJECT_ROOT/frontend/dist" ]]; then
        missing_files+=("Frontend build directory")
    fi
    
    if [[ ${#missing_files[@]} -gt 0 ]]; then
        log_error "Missing required files:"
        for file in "${missing_files[@]}"; do
            log_error "  - $file"
        done
        exit 1
    fi
    
    log_success "All required files are present"
}

# Install SystemD service files
install_service_files() {
    log_info "Installing SystemD service files..."
    
    # Copy service files
    cp "$DEPLOYMENT_DIR/pgb4-backend.service" /etc/systemd/system/
    cp "$DEPLOYMENT_DIR/pgb4-frontend.service" /etc/systemd/system/
    cp "$DEPLOYMENT_DIR/pgb4.target" /etc/systemd/system/
    
    # Create service override directories
    mkdir -p /etc/systemd/system/pgb4-backend.service.d
    mkdir -p /etc/systemd/system/pgb4-frontend.service.d
    
    # Create environment override files
    create_environment_overrides
    
    # Reload systemd
    systemctl daemon-reload
    
    log_success "SystemD service files installed"
}

# Create environment override files
create_environment_overrides() {
    log_info "Creating environment configuration..."
    
    # Backend environment override
    cat > /etc/systemd/system/pgb4-backend.service.d/override.conf << EOF
[Service]
Environment=JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC
Environment=SPRING_PROFILES_ACTIVE=prod
Environment=SERVER_PORT=$BACKEND_PORT
Environment=LOG_LEVEL=INFO
Environment=LOG_FILE=$LOG_DIR/backend.log
WorkingDirectory=$INSTALL_DIR/backend
ExecStart=/usr/bin/java -jar $INSTALL_DIR/backend/pgb4-backend.jar --spring.profiles.active=prod
EOF
    
    # Frontend environment override
    cat > /etc/systemd/system/pgb4-frontend.service.d/override.conf << EOF
[Service]
Environment=BACKEND_URL=http://localhost:$BACKEND_PORT
Environment=FRONTEND_PORT=$FRONTEND_PORT
ExecStartPre=/usr/sbin/nginx -t -c $INSTALL_DIR/frontend/nginx.conf
ExecStart=/usr/sbin/nginx -c $INSTALL_DIR/frontend/nginx.conf
EOF
    
    log_success "Environment configuration created"
}

# Deploy application files
deploy_application_files() {
    log_info "Deploying application files..."
    
    # Deploy backend JAR
    local backend_jar=$(find "$PROJECT_ROOT/backend/build/libs" -name "*.jar" | head -n1)
    if [[ -n "$backend_jar" ]]; then
        cp "$backend_jar" "$INSTALL_DIR/backend/pgb4-backend.jar"
        chown "$BACKEND_USER:$BACKEND_USER" "$INSTALL_DIR/backend/pgb4-backend.jar"
        log_success "Backend JAR deployed"
    else
        log_error "Backend JAR not found"
        exit 1
    fi
    
    # Deploy frontend files
    if [[ -d "$PROJECT_ROOT/frontend/dist" ]]; then
        cp -r "$PROJECT_ROOT/frontend/dist"/* "$INSTALL_DIR/frontend/"
        cp "$PROJECT_ROOT/frontend/nginx.conf" "$INSTALL_DIR/frontend/"
        chown -R "$FRONTEND_USER:$FRONTEND_USER" "$INSTALL_DIR/frontend"
        log_success "Frontend files deployed"
    else
        log_error "Frontend build directory not found"
        exit 1
    fi
}

# Enable and start services
enable_services() {
    log_info "Enabling SystemD services..."
    
    systemctl enable pgb4-backend.service
    systemctl enable pgb4-frontend.service
    systemctl enable pgb4.target
    
    log_success "Services enabled"
}

# Start services
start_services() {
    log_info "Starting PGB4 services..."
    
    systemctl start pgb4.target
    
    # Wait for services to start
    sleep 5
    
    if systemctl is-active --quiet pgb4-backend.service && systemctl is-active --quiet pgb4-frontend.service; then
        log_success "Services started successfully"
    else
        log_error "Failed to start services"
        show_service_status
        exit 1
    fi
}

# Stop services
stop_services() {
    log_info "Stopping PGB4 services..."
    
    systemctl stop pgb4.target
    
    log_success "Services stopped"
}

# Restart services
restart_services() {
    log_info "Restarting PGB4 services..."
    
    systemctl restart pgb4.target
    
    # Wait for services to restart
    sleep 5
    
    if systemctl is-active --quiet pgb4-backend.service && systemctl is-active --quiet pgb4-frontend.service; then
        log_success "Services restarted successfully"
    else
        log_error "Failed to restart services"
        show_service_status
        exit 1
    fi
}

# Show service status
show_service_status() {
    echo
    log_info "=== PGB4 Service Status ==="
    
    echo -e "\n${BLUE}Backend Service:${NC}"
    systemctl status pgb4-backend.service --no-pager -l
    
    echo -e "\n${BLUE}Frontend Service:${NC}"
    systemctl status pgb4-frontend.service --no-pager -l
    
    echo -e "\n${BLUE}Target Status:${NC}"
    systemctl status pgb4.target --no-pager -l
}

# Show service logs
show_service_logs() {
    echo
    log_info "=== PGB4 Service Logs ==="
    
    echo -e "\n${BLUE}Backend Logs (last 50 lines):${NC}"
    journalctl -u pgb4-backend.service -n 50 --no-pager
    
    echo -e "\n${BLUE}Frontend Logs (last 50 lines):${NC}"
    journalctl -u pgb4-frontend.service -n 50 --no-pager
}

# Health check
health_check() {
    log_info "Performing health checks..."
    
    local backend_healthy=false
    local frontend_healthy=false
    
    # Check backend health
    if curl -s -f "http://localhost:$BACKEND_PORT/api/health" > /dev/null; then
        log_success "Backend health check passed"
        backend_healthy=true
    else
        log_error "Backend health check failed"
    fi
    
    # Check frontend health
    if curl -s -f "http://localhost:$FRONTEND_PORT/health" > /dev/null; then
        log_success "Frontend health check passed"
        frontend_healthy=true
    else
        log_error "Frontend health check failed"
    fi
    
    # Overall health status
    if [[ "$backend_healthy" == true && "$frontend_healthy" == true ]]; then
        log_success "All services are healthy"
        return 0
    else
        log_error "Some services are unhealthy"
        return 1
    fi
}

# Uninstall services
uninstall_services() {
    log_warning "This will remove PGB4 SystemD services and configuration"
    read -p "Are you sure? (y/N): " -n 1 -r
    echo
    
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_info "Uninstall cancelled"
        return 0
    fi
    
    log_info "Uninstalling PGB4 services..."
    
    # Stop and disable services
    systemctl stop pgb4.target || true
    systemctl disable pgb4-backend.service || true
    systemctl disable pgb4-frontend.service || true
    systemctl disable pgb4.target || true
    
    # Remove service files
    rm -f /etc/systemd/system/pgb4-backend.service
    rm -f /etc/systemd/system/pgb4-frontend.service
    rm -f /etc/systemd/system/pgb4.target
    rm -rf /etc/systemd/system/pgb4-backend.service.d
    rm -rf /etc/systemd/system/pgb4-frontend.service.d
    
    # Reload systemd
    systemctl daemon-reload
    
    log_success "Services uninstalled"
    log_warning "Application files in $INSTALL_DIR were not removed"
}

# Main installation function
install_pgb4() {
    log_info "Starting PGB4 SystemD installation..."
    
    check_requirements
    create_users
    create_directories
    check_deployment_files
    install_service_files
    deploy_application_files
    enable_services
    start_services
    
    # Perform health check
    sleep 10
    if health_check; then
        log_success "PGB4 installation completed successfully!"
        echo
        log_info "Services are running on:"
        log_info "  Backend:  http://localhost:$BACKEND_PORT"
        log_info "  Frontend: http://localhost:$FRONTEND_PORT"
        echo
        log_info "Use '$0 status' to check service status"
        log_info "Use '$0 logs' to view service logs"
    else
        log_error "Installation completed but health checks failed"
        log_info "Check logs with: $0 logs"
        exit 1
    fi
}

# Main function
main() {
    parse_args "$@"
    load_config
    
    case "$COMMAND" in
        install)
            check_root
            install_pgb4
            ;;
        start)
            check_root
            start_services
            ;;
        stop)
            check_root
            stop_services
            ;;
        restart)
            check_root
            restart_services
            ;;
        status)
            show_service_status
            ;;
        logs)
            show_service_logs
            ;;
        health)
            health_check
            ;;
        uninstall)
            check_root
            uninstall_services
            ;;
        *)
            log_error "Unknown command: $COMMAND"
            usage
            exit 1
            ;;
    esac
}

# Run main function
main "$@"