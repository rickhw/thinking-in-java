#!/bin/bash

# PGB4 Kubernetes Deployment Script
# This script automates the deployment of PGB4 Message Board to Kubernetes
# Usage: ./deploy-k8s.sh [environment] [action] [options]

set -euo pipefail

# Script configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
K8S_DIR="${PROJECT_ROOT}/deployment/k8s"

# Default values
ENVIRONMENT="${1:-production}"
ACTION="${2:-deploy}"
NAMESPACE="pgb4"
CONTEXT=""
DRY_RUN=false
VERBOSE=false
FORCE=false
TIMEOUT=300
IMAGE_TAG="latest"

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

# Help function
show_help() {
    cat << EOF
PGB4 Kubernetes Deployment Script

Usage: $0 [environment] [action] [options]

Environments:
    production      Deploy to production namespace (default)
    staging         Deploy to staging namespace
    development     Deploy to development namespace

Actions:
    deploy          Deploy all components (default)
    update          Update existing deployment
    rollback        Rollback to previous version
    delete          Delete all components
    status          Show deployment status
    logs            Show application logs
    scale           Scale deployments

Options:
    --namespace NAME        Kubernetes namespace (default: pgb4)
    --context NAME          Kubernetes context to use
    --image-tag TAG         Docker image tag (default: latest)
    --dry-run              Show what would be done without executing
    --verbose              Enable verbose output
    --force                Force deployment without confirmation
    --timeout SECONDS      Deployment timeout (default: 300)
    --help                 Show this help message

Examples:
    $0 production deploy --image-tag v1.2.3
    $0 staging update --dry-run
    $0 production rollback --force
    $0 development delete --namespace pgb4-dev

EOF
}

# Parse command line arguments
parse_args() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            --namespace)
                NAMESPACE="$2"
                shift 2
                ;;
            --context)
                CONTEXT="$2"
                shift 2
                ;;
            --image-tag)
                IMAGE_TAG="$2"
                shift 2
                ;;
            --dry-run)
                DRY_RUN=true
                shift
                ;;
            --verbose)
                VERBOSE=true
                shift
                ;;
            --force)
                FORCE=true
                shift
                ;;
            --timeout)
                TIMEOUT="$2"
                shift 2
                ;;
            --help)
                show_help
                exit 0
                ;;
            *)
                log_error "Unknown option: $1"
                show_help
                exit 1
                ;;
        esac
    done
}

# Validate prerequisites
validate_prerequisites() {
    log_info "Validating prerequisites..."
    
    # Check if kubectl is installed
    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl is not installed or not in PATH"
        exit 1
    fi
    
    # Check if kubectl can connect to cluster
    if ! kubectl cluster-info &> /dev/null; then
        log_error "Cannot connect to Kubernetes cluster"
        exit 1
    fi
    
    # Set context if specified
    if [[ -n "$CONTEXT" ]]; then
        log_info "Setting kubectl context to: $CONTEXT"
        kubectl config use-context "$CONTEXT"
    fi
    
    # Check if namespace exists
    if ! kubectl get namespace "$NAMESPACE" &> /dev/null; then
        log_warning "Namespace '$NAMESPACE' does not exist. Creating..."
        if [[ "$DRY_RUN" == "false" ]]; then
            kubectl create namespace "$NAMESPACE"
        fi
    fi
    
    # Validate K8s configuration files exist
    local required_files=(
        "backend-deployment.yaml"
        "frontend-deployment.yaml"
        "services.yaml"
        "ingress.yaml"
        "configmap.yaml"
        "secrets.yaml"
    )
    
    for file in "${required_files[@]}"; do
        if [[ ! -f "$K8S_DIR/$file" ]]; then
            log_error "Required K8s configuration file not found: $K8S_DIR/$file"
            exit 1
        fi
    done
    
    log_success "Prerequisites validated"
}

# Create or update secrets
deploy_secrets() {
    log_info "Deploying secrets..."
    
    local kubectl_cmd="kubectl apply -f $K8S_DIR/secrets.yaml --namespace=$NAMESPACE"
    
    if [[ "$DRY_RUN" == "true" ]]; then
        kubectl_cmd="$kubectl_cmd --dry-run=client"
    fi
    
    if [[ "$VERBOSE" == "true" ]]; then
        log_info "Executing: $kubectl_cmd"
    fi
    
    if eval "$kubectl_cmd"; then
        log_success "Secrets deployed successfully"
    else
        log_error "Failed to deploy secrets"
        return 1
    fi
}

# Create or update configmaps
deploy_configmaps() {
    log_info "Deploying configmaps..."
    
    local kubectl_cmd="kubectl apply -f $K8S_DIR/configmap.yaml --namespace=$NAMESPACE"
    
    if [[ "$DRY_RUN" == "true" ]]; then
        kubectl_cmd="$kubectl_cmd --dry-run=client"
    fi
    
    if [[ "$VERBOSE" == "true" ]]; then
        log_info "Executing: $kubectl_cmd"
    fi
    
    if eval "$kubectl_cmd"; then
        log_success "ConfigMaps deployed successfully"
    else
        log_error "Failed to deploy configmaps"
        return 1
    fi
}

# Deploy services
deploy_services() {
    log_info "Deploying services..."
    
    local kubectl_cmd="kubectl apply -f $K8S_DIR/services.yaml --namespace=$NAMESPACE"
    
    if [[ "$DRY_RUN" == "true" ]]; then
        kubectl_cmd="$kubectl_cmd --dry-run=client"
    fi
    
    if [[ "$VERBOSE" == "true" ]]; then
        log_info "Executing: $kubectl_cmd"
    fi
    
    if eval "$kubectl_cmd"; then
        log_success "Services deployed successfully"
    else
        log_error "Failed to deploy services"
        return 1
    fi
}

# Deploy applications
deploy_applications() {
    log_info "Deploying applications with image tag: $IMAGE_TAG..."
    
    # Update image tags in deployment files
    local backend_deployment=$(mktemp)
    local frontend_deployment=$(mktemp)
    
    # Replace image tags
    sed "s|image: pgb4/backend:latest|image: pgb4/backend:$IMAGE_TAG|g" "$K8S_DIR/backend-deployment.yaml" > "$backend_deployment"
    sed "s|image: pgb4/frontend:latest|image: pgb4/frontend:$IMAGE_TAG|g" "$K8S_DIR/frontend-deployment.yaml" > "$frontend_deployment"
    
    # Deploy backend
    local backend_cmd="kubectl apply -f $backend_deployment --namespace=$NAMESPACE"
    if [[ "$DRY_RUN" == "true" ]]; then
        backend_cmd="$backend_cmd --dry-run=client"
    fi
    
    if [[ "$VERBOSE" == "true" ]]; then
        log_info "Executing: $backend_cmd"
    fi
    
    if eval "$backend_cmd"; then
        log_success "Backend deployment applied successfully"
    else
        log_error "Failed to deploy backend"
        rm -f "$backend_deployment" "$frontend_deployment"
        return 1
    fi
    
    # Deploy frontend
    local frontend_cmd="kubectl apply -f $frontend_deployment --namespace=$NAMESPACE"
    if [[ "$DRY_RUN" == "true" ]]; then
        frontend_cmd="$frontend_cmd --dry-run=client"
    fi
    
    if [[ "$VERBOSE" == "true" ]]; then
        log_info "Executing: $frontend_cmd"
    fi
    
    if eval "$frontend_cmd"; then
        log_success "Frontend deployment applied successfully"
    else
        log_error "Failed to deploy frontend"
        rm -f "$backend_deployment" "$frontend_deployment"
        return 1
    fi
    
    # Clean up temporary files
    rm -f "$backend_deployment" "$frontend_deployment"
}

# Deploy ingress
deploy_ingress() {
    log_info "Deploying ingress..."
    
    local kubectl_cmd="kubectl apply -f $K8S_DIR/ingress.yaml --namespace=$NAMESPACE"
    
    if [[ "$DRY_RUN" == "true" ]]; then
        kubectl_cmd="$kubectl_cmd --dry-run=client"
    fi
    
    if [[ "$VERBOSE" == "true" ]]; then
        log_info "Executing: $kubectl_cmd"
    fi
    
    if eval "$kubectl_cmd"; then
        log_success "Ingress deployed successfully"
    else
        log_error "Failed to deploy ingress"
        return 1
    fi
}

# Wait for deployments to be ready
wait_for_deployments() {
    if [[ "$DRY_RUN" == "true" ]]; then
        log_info "Skipping deployment wait (dry-run mode)"
        return 0
    fi
    
    log_info "Waiting for deployments to be ready (timeout: ${TIMEOUT}s)..."
    
    local deployments=("pgb4-backend" "pgb4-frontend")
    
    for deployment in "${deployments[@]}"; do
        log_info "Waiting for deployment: $deployment"
        
        if kubectl wait --for=condition=available --timeout="${TIMEOUT}s" deployment/"$deployment" --namespace="$NAMESPACE"; then
            log_success "Deployment $deployment is ready"
        else
            log_error "Deployment $deployment failed to become ready within ${TIMEOUT}s"
            return 1
        fi
    done
    
    log_success "All deployments are ready"
}

# Check deployment status
check_status() {
    log_info "Checking deployment status in namespace: $NAMESPACE"
    
    echo
    log_info "Deployments:"
    kubectl get deployments --namespace="$NAMESPACE" -o wide
    
    echo
    log_info "Pods:"
    kubectl get pods --namespace="$NAMESPACE" -o wide
    
    echo
    log_info "Services:"
    kubectl get services --namespace="$NAMESPACE" -o wide
    
    echo
    log_info "Ingress:"
    kubectl get ingress --namespace="$NAMESPACE" -o wide
    
    echo
    log_info "ConfigMaps:"
    kubectl get configmaps --namespace="$NAMESPACE"
    
    echo
    log_info "Secrets:"
    kubectl get secrets --namespace="$NAMESPACE"
}

# Show application logs
show_logs() {
    local component="${3:-all}"
    
    log_info "Showing logs for: $component"
    
    case $component in
        backend)
            kubectl logs -l app=pgb4-backend --namespace="$NAMESPACE" --tail=100 -f
            ;;
        frontend)
            kubectl logs -l app=pgb4-frontend --namespace="$NAMESPACE" --tail=100 -f
            ;;
        all|*)
            log_info "Backend logs:"
            kubectl logs -l app=pgb4-backend --namespace="$NAMESPACE" --tail=50
            echo
            log_info "Frontend logs:"
            kubectl logs -l app=pgb4-frontend --namespace="$NAMESPACE" --tail=50
            ;;
    esac
}

# Scale deployments
scale_deployments() {
    local replicas="${3:-2}"
    
    log_info "Scaling deployments to $replicas replicas..."
    
    if [[ "$DRY_RUN" == "false" ]]; then
        kubectl scale deployment pgb4-backend --replicas="$replicas" --namespace="$NAMESPACE"
        kubectl scale deployment pgb4-frontend --replicas="$replicas" --namespace="$NAMESPACE"
        
        log_success "Deployments scaled to $replicas replicas"
    else
        log_info "Would scale deployments to $replicas replicas (dry-run mode)"
    fi
}

# Rollback deployments
rollback_deployments() {
    log_info "Rolling back deployments..."
    
    if [[ "$FORCE" == "false" ]]; then
        read -p "Are you sure you want to rollback? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            log_info "Rollback cancelled"
            return 0
        fi
    fi
    
    if [[ "$DRY_RUN" == "false" ]]; then
        kubectl rollout undo deployment/pgb4-backend --namespace="$NAMESPACE"
        kubectl rollout undo deployment/pgb4-frontend --namespace="$NAMESPACE"
        
        log_success "Rollback initiated"
        wait_for_deployments
    else
        log_info "Would rollback deployments (dry-run mode)"
    fi
}

# Delete all resources
delete_resources() {
    log_warning "This will delete all PGB4 resources in namespace: $NAMESPACE"
    
    if [[ "$FORCE" == "false" ]]; then
        read -p "Are you sure you want to delete all resources? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            log_info "Deletion cancelled"
            return 0
        fi
    fi
    
    if [[ "$DRY_RUN" == "false" ]]; then
        kubectl delete -f "$K8S_DIR" --namespace="$NAMESPACE" --ignore-not-found=true
        log_success "All resources deleted"
    else
        log_info "Would delete all resources (dry-run mode)"
    fi
}

# Main deployment function
deploy_all() {
    log_info "Starting PGB4 deployment to namespace: $NAMESPACE"
    
    if [[ "$FORCE" == "false" && "$DRY_RUN" == "false" ]]; then
        read -p "Continue with deployment? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            log_info "Deployment cancelled"
            return 0
        fi
    fi
    
    # Deploy in order
    deploy_secrets || return 1
    deploy_configmaps || return 1
    deploy_services || return 1
    deploy_applications || return 1
    deploy_ingress || return 1
    
    # Wait for deployments to be ready
    wait_for_deployments || return 1
    
    # Show final status
    check_status
    
    log_success "PGB4 deployment completed successfully!"
    
    if [[ "$DRY_RUN" == "false" ]]; then
        log_info "Application should be available at the configured ingress endpoints"
        log_info "Use '$0 $ENVIRONMENT status --namespace=$NAMESPACE' to check status"
        log_info "Use '$0 $ENVIRONMENT logs --namespace=$NAMESPACE' to view logs"
    fi
}

# Main script execution
main() {
    # Skip first two arguments (environment and action) for parse_args
    shift 2 2>/dev/null || true
    parse_args "$@"
    
    log_info "PGB4 Kubernetes Deployment Script"
    log_info "Environment: $ENVIRONMENT"
    log_info "Action: $ACTION"
    log_info "Namespace: $NAMESPACE"
    log_info "Image Tag: $IMAGE_TAG"
    
    if [[ "$DRY_RUN" == "true" ]]; then
        log_warning "DRY RUN MODE - No changes will be made"
    fi
    
    validate_prerequisites
    
    case $ACTION in
        deploy)
            deploy_all
            ;;
        update)
            deploy_applications
            wait_for_deployments
            check_status
            ;;
        rollback)
            rollback_deployments
            ;;
        delete)
            delete_resources
            ;;
        status)
            check_status
            ;;
        logs)
            show_logs "$@"
            ;;
        scale)
            scale_deployments "$@"
            ;;
        *)
            log_error "Unknown action: $ACTION"
            show_help
            exit 1
            ;;
    esac
}

# Run main function with all arguments
main "$@"