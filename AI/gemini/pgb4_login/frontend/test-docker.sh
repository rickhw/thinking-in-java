#!/bin/bash

# Test script for PGB4 Frontend Docker container
# Builds and tests the Docker image

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Configuration
IMAGE_NAME="pgb4-frontend"
TAG="test"
CONTAINER_NAME="pgb4-frontend-test"
PORT="3001"

print_status "Starting Docker container test..."

# Clean up any existing container
print_status "Cleaning up existing containers..."
docker rm -f $CONTAINER_NAME 2>/dev/null || true

# Build the image
print_status "Building Docker image..."
docker build -t $IMAGE_NAME:$TAG \
    --build-arg VITE_API_BASE_URL=http://localhost:8080/api/v1 \
    --build-arg VITE_APP_NAME="PGB4 Message Board" \
    --build-arg VITE_APP_VERSION="1.0.0" \
    --build-arg VITE_APP_ENV=production \
    .

if [[ $? -eq 0 ]]; then
    print_success "Docker image built successfully"
else
    print_error "Docker build failed"
    exit 1
fi

# Run the container
print_status "Starting container..."
docker run -d \
    --name $CONTAINER_NAME \
    -p $PORT:80 \
    -e BACKEND_URL=http://localhost:8080 \
    $IMAGE_NAME:$TAG

if [[ $? -eq 0 ]]; then
    print_success "Container started successfully"
else
    print_error "Failed to start container"
    exit 1
fi

# Wait for container to be ready
print_status "Waiting for container to be ready..."
sleep 10

# Test health endpoint
print_status "Testing health endpoint..."
if curl -f -s http://localhost:$PORT/health > /dev/null; then
    print_success "Health check passed"
else
    print_error "Health check failed"
    docker logs $CONTAINER_NAME
    docker rm -f $CONTAINER_NAME
    exit 1
fi

# Test main page
print_status "Testing main page..."
if curl -f -s http://localhost:$PORT/ > /dev/null; then
    print_success "Main page accessible"
else
    print_error "Main page not accessible"
    docker logs $CONTAINER_NAME
    docker rm -f $CONTAINER_NAME
    exit 1
fi

# Show container status
print_status "Container status:"
docker ps --filter name=$CONTAINER_NAME --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

print_success "All tests passed!"
print_status "Container is running at http://localhost:$PORT"
print_status "To stop the container: docker rm -f $CONTAINER_NAME"

# Show logs
print_status "Container logs:"
docker logs $CONTAINER_NAME --tail 10