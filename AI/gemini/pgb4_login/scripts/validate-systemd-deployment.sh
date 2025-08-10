#!/bin/bash

# PGB4 SystemD Deployment Validation Script
# This script validates the deployment configuration and files

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

# Validation functions
validate_systemd_files() {
    log_info "Validating SystemD service files..."
    
    local files=(
        "deployment/systemd/pgb4-backend.service"
        "deployment/systemd/pgb4-frontend.service"
        "deployment/systemd/pgb4.target"
        "deployment/systemd/pgb4-backend.env"
        "deployment/systemd/pgb4-frontend.env"
    )
    
    local missing_files=()
    
    for file in "${files[@]}"; do
        if [[ ! -f "$PROJECT_ROOT/$file" ]]; then
            missing_files+=("$file")
        fi
    done
    
    if [[ ${#missing_files[@]} -gt 0 ]]; then
        log_error "Missing SystemD files:"
        for file in "${missing_files[@]}"; do
            log_error "  - $file"
        done
        return 1
    fi
    
    log_success "All SystemD files are present"
    return 0
}

validate_deployment_script() {
    log_info "Validating deployment script..."
    
    if [[ ! -f "$SCRIPT_DIR/deploy-systemd.sh" ]]; then
        log_error "Deployment script not found: $SCRIPT_DIR/deploy-systemd.sh"
        return 1
    fi
    
    if [[ ! -x "$SCRIPT_DIR/deploy-systemd.sh" ]]; then
        log_error "Deployment script is not executable"
        return 1
    fi
    
    # Test script help
    if ! "$SCRIPT_DIR/deploy-systemd.sh" --help > /dev/null 2>&1; then
        log_error "Deployment script help command failed"
        return 1
    fi
    
    log_success "Deployment script validation passed"
    return 0
}

validate_application_files() {
    log_info "Validating application files..."
    
    # Check backend build
    if [[ ! -d "$PROJECT_ROOT/backend/build/libs" ]]; then
        log_warning "Backend not built. Run: cd backend && ./gradlew build"
    else
        local jar_count=$(find "$PROJECT_ROOT/backend/build/libs" -name "*.jar" | wc -l)
        if [[ $jar_count -eq 0 ]]; then
            log_warning "No JAR files found in backend/build/libs"
        else
            log_success "Backend JAR files found"
        fi
    fi
    
    # Check frontend build
    if [[ ! -d "$PROJECT_ROOT/frontend/dist" ]]; then
        log_warning "Frontend not built. Run: cd frontend && npm run build"
    else
        if [[ ! -f "$PROJECT_ROOT/frontend/dist/index.html" ]]; then
            log_warning "Frontend build appears incomplete (no index.html)"
        else
            log_success "Frontend build files found"
        fi
    fi
    
    # Check nginx configuration
    if [[ ! -f "$PROJECT_ROOT/frontend/nginx.conf" ]]; then
        log_error "Nginx configuration not found: frontend/nginx.conf"
        return 1
    fi
    
    log_success "Application files validation completed"
    return 0
}

validate_systemd_syntax() {
    log_info "Validating SystemD service file syntax..."
    
    local service_files=(
        "$PROJECT_ROOT/deployment/systemd/pgb4-backend.service"
        "$PROJECT_ROOT/deployment/systemd/pgb4-frontend.service"
        "$PROJECT_ROOT/deployment/systemd/pgb4.target"
    )
    
    for service_file in "${service_files[@]}"; do
        if [[ -f "$service_file" ]]; then
            # Basic syntax validation
            if ! grep -q "^\[Unit\]" "$service_file"; then
                log_error "Missing [Unit] section in $(basename "$service_file")"
                return 1
            fi
            
            if ! grep -q "^\[Service\]" "$service_file" && ! grep -q "^\[Install\]" "$service_file"; then
                log_error "Missing [Service] or [Install] section in $(basename "$service_file")"
                return 1
            fi
            
            log_success "$(basename "$service_file") syntax validation passed"
        fi
    done
    
    return 0
}

validate_environment_files() {
    log_info "Validating environment configuration files..."
    
    local env_files=(
        "$PROJECT_ROOT/deployment/systemd/pgb4-backend.env"
        "$PROJECT_ROOT/deployment/systemd/pgb4-frontend.env"
    )
    
    for env_file in "${env_files[@]}"; do
        if [[ -f "$env_file" ]]; then
            # Check for required variables
            if [[ "$env_file" == *"backend"* ]]; then
                local required_vars=("JAVA_OPTS" "SPRING_PROFILES_ACTIVE" "SERVER_PORT")
                for var in "${required_vars[@]}"; do
                    if ! grep -q "^$var=" "$env_file"; then
                        log_warning "Missing $var in $(basename "$env_file")"
                    fi
                done
            fi
            
            if [[ "$env_file" == *"frontend"* ]]; then
                local required_vars=("BACKEND_URL" "FRONTEND_PORT")
                for var in "${required_vars[@]}"; do
                    if ! grep -q "^$var=" "$env_file"; then
                        log_warning "Missing $var in $(basename "$env_file")"
                    fi
                done
            fi
            
            log_success "$(basename "$env_file") validation passed"
        fi
    done
    
    return 0
}

# Main validation function
main() {
    log_info "Starting PGB4 SystemD deployment validation..."
    echo
    
    local validation_passed=true
    
    # Run all validations
    validate_systemd_files || validation_passed=false
    echo
    
    validate_deployment_script || validation_passed=false
    echo
    
    validate_application_files || validation_passed=false
    echo
    
    validate_systemd_syntax || validation_passed=false
    echo
    
    validate_environment_files || validation_passed=false
    echo
    
    # Summary
    if [[ "$validation_passed" == true ]]; then
        log_success "All validations passed! Ready for deployment."
        echo
        log_info "To deploy, run:"
        log_info "  sudo $SCRIPT_DIR/deploy-systemd.sh install"
        echo
        log_info "To check deployment status:"
        log_info "  $SCRIPT_DIR/deploy-systemd.sh status"
    else
        log_error "Some validations failed. Please fix the issues before deployment."
        exit 1
    fi
}

# Run main function
main "$@"