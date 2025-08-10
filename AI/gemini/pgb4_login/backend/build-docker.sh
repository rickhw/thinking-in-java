#!/bin/bash

# Docker build script for PGB4 Backend
# Usage: ./build-docker.sh [dev|prod] [tag]

set -e

# Default values
ENVIRONMENT=${1:-dev}
TAG=${2:-latest}
IMAGE_NAME="pgb4-backend"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Validate environment parameter
if [[ "$ENVIRONMENT" != "dev" && "$ENVIRONMENT" != "prod" ]]; then
    print_error "Invalid environment. Use 'dev' or 'prod'"
    exit 1
fi

# Set Dockerfile based on environment
if [[ "$ENVIRONMENT" == "prod" ]]; then
    DOCKERFILE="Dockerfile.prod"
    FULL_TAG="${IMAGE_NAME}:${TAG}-prod"
else
    DOCKERFILE="Dockerfile"
    FULL_TAG="${IMAGE_NAME}:${TAG}-dev"
fi

print_status "Building Docker image for $ENVIRONMENT environment"
print_status "Using Dockerfile: $DOCKERFILE"
print_status "Image tag: $FULL_TAG"

# Check if Dockerfile exists
if [[ ! -f "$DOCKERFILE" ]]; then
    print_error "Dockerfile not found: $DOCKERFILE"
    exit 1
fi

# Build the Docker image
print_status "Starting Docker build..."
docker build -f "$DOCKERFILE" -t "$FULL_TAG" .

if [[ $? -eq 0 ]]; then
    print_status "Docker build completed successfully!"
    print_status "Image: $FULL_TAG"
    
    # Show image size
    IMAGE_SIZE=$(docker images "$FULL_TAG" --format "table {{.Size}}" | tail -n 1)
    print_status "Image size: $IMAGE_SIZE"
    
    # Optional: Run basic tests on the image
    print_status "Running basic image validation..."
    
    # Test if the image can start (without actually running the app)
    docker run --rm "$FULL_TAG" java -version
    
    if [[ $? -eq 0 ]]; then
        print_status "Image validation passed!"
    else
        print_warning "Image validation failed, but build was successful"
    fi
    
else
    print_error "Docker build failed!"
    exit 1
fi

print_status "Build script completed successfully!"
print_status "To run the container:"
print_status "  docker run -p 8080:8080 $FULL_TAG"
print_status "To run with docker-compose:"
print_status "  docker-compose up"