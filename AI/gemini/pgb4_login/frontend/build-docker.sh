#!/bin/bash

# Build script for PGB4 Frontend Docker images
# Supports different build modes and environments

set -e

# Default values
BUILD_MODE="production"
DOCKERFILE="Dockerfile"
IMAGE_NAME="pgb4-frontend"
TAG="latest"
BACKEND_URL="http://backend:8080"
PUSH_IMAGE=false
REGISTRY=""

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to show usage
show_usage() {
    cat << EOF
Usage: $0 [OPTIONS]

Build Docker image for PGB4 Frontend

OPTIONS:
    -m, --mode MODE         Build mode (development|staging|production) [default: production]
    -d, --dockerfile FILE   Dockerfile to use [default: Dockerfile]
    -n, --name NAME         Image name [default: pgb4-frontend]
    -t, --tag TAG           Image tag [default: latest]
    -b, --backend-url URL   Backend URL [default: http://backend:8080]
    -p, --push              Push image to registry after build
    -r, --registry URL      Registry URL for pushing
    -h, --help              Show this help message

EXAMPLES:
    $0                                          # Build production image
    $0 -m staging -t staging                    # Build staging image
    $0 -d Dockerfile.prod -t prod -p            # Build with production Dockerfile and push
    $0 -m development -b http://localhost:8080  # Build development image with local backend

EOF
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -m|--mode)
            BUILD_MODE="$2"
            shift 2
            ;;
        -d|--dockerfile)
            DOCKERFILE="$2"
            shift 2
            ;;
        -n|--name)
            IMAGE_NAME="$2"
            shift 2
            ;;
        -t|--tag)
            TAG="$2"
            shift 2
            ;;
        -b|--backend-url)
            BACKEND_URL="$2"
            shift 2
            ;;
        -p|--push)
            PUSH_IMAGE=true
            shift
            ;;
        -r|--registry)
            REGISTRY="$2"
            shift 2
            ;;
        -h|--help)
            show_usage
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
done

# Validate build mode
if [[ ! "$BUILD_MODE" =~ ^(development|staging|production)$ ]]; then
    print_error "Invalid build mode: $BUILD_MODE"
    print_error "Valid modes: development, staging, production"
    exit 1
fi

# Validate Dockerfile exists
if [[ ! -f "$DOCKERFILE" ]]; then
    print_error "Dockerfile not found: $DOCKERFILE"
    exit 1
fi

# Set full image name with registry if provided
FULL_IMAGE_NAME="$IMAGE_NAME:$TAG"
if [[ -n "$REGISTRY" ]]; then
    FULL_IMAGE_NAME="$REGISTRY/$FULL_IMAGE_NAME"
fi

# Print build configuration
print_status "Build Configuration:"
echo "  Build Mode: $BUILD_MODE"
echo "  Dockerfile: $DOCKERFILE"
echo "  Image Name: $FULL_IMAGE_NAME"
echo "  Backend URL: $BACKEND_URL"
echo "  Push Image: $PUSH_IMAGE"

# Check if Docker is available
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed or not in PATH"
    exit 1
fi

# Check if Docker daemon is running
if ! docker info &> /dev/null; then
    print_error "Docker daemon is not running"
    exit 1
fi

print_status "Starting Docker build..."

# Build the Docker image
docker build \
    --file "$DOCKERFILE" \
    --tag "$FULL_IMAGE_NAME" \
    --build-arg BUILD_MODE="$BUILD_MODE" \
    --build-arg VITE_API_BASE_URL="$BACKEND_URL/api/v1" \
    --build-arg VITE_APP_NAME="PGB4 Message Board" \
    --build-arg VITE_APP_VERSION="1.0.0" \
    --build-arg VITE_APP_ENV="$BUILD_MODE" \
    .

if [[ $? -eq 0 ]]; then
    print_success "Docker image built successfully: $FULL_IMAGE_NAME"
else
    print_error "Docker build failed"
    exit 1
fi

# Push image if requested
if [[ "$PUSH_IMAGE" == true ]]; then
    if [[ -z "$REGISTRY" ]]; then
        print_warning "No registry specified, pushing to default registry"
    fi
    
    print_status "Pushing image to registry..."
    docker push "$FULL_IMAGE_NAME"
    
    if [[ $? -eq 0 ]]; then
        print_success "Image pushed successfully: $FULL_IMAGE_NAME"
    else
        print_error "Failed to push image"
        exit 1
    fi
fi

# Show image information
print_status "Image Information:"
docker images "$FULL_IMAGE_NAME" --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}\t{{.CreatedAt}}"

print_success "Build completed successfully!"

# Provide usage instructions
echo ""
print_status "To run the container:"
echo "  docker run -d -p 3000:80 --name pgb4-frontend $FULL_IMAGE_NAME"
echo ""
print_status "To run with custom backend URL:"
echo "  docker run -d -p 3000:80 -e BACKEND_URL=http://your-backend:8080 --name pgb4-frontend $FULL_IMAGE_NAME"