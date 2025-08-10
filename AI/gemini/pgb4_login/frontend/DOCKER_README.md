# PGB4 Frontend Docker Setup

This document describes how to build and deploy the PGB4 Message Board frontend using Docker.

## Overview

The frontend is containerized using a multi-stage Docker build:
1. **Build Stage**: Uses Node.js to build the React application
2. **Production Stage**: Uses Nginx to serve the static files and proxy API requests

## Files

- `Dockerfile` - Standard production Dockerfile
- `Dockerfile.prod` - Production-optimized Dockerfile with enhanced security
- `nginx.conf` - Static Nginx configuration
- `nginx.conf.template` - Template with environment variable substitution
- `docker-compose.yml` - Docker Compose configuration for testing
- `build-docker.sh` - Build script with multiple options
- `test-docker.sh` - Test script to validate the Docker setup
- `.dockerignore` - Files to exclude from Docker build context

## Quick Start

### Build and Run

```bash
# Build the Docker image
docker build -t pgb4-frontend .

# Run the container
docker run -d -p 3000:80 --name pgb4-frontend pgb4-frontend
```

### Using the Build Script

```bash
# Build production image
./build-docker.sh

# Build staging image
./build-docker.sh -m staging -t staging

# Build and push to registry
./build-docker.sh -p -r your-registry.com
```

### Using Docker Compose

```bash
# Start the frontend with mock backend
docker-compose up -d

# View logs
docker-compose logs -f frontend
```

## Environment Variables

### Build Arguments

- `BUILD_MODE` - Build mode (development|staging|production)
- `VITE_API_BASE_URL` - API base URL for the frontend
- `VITE_APP_NAME` - Application name
- `VITE_APP_VERSION` - Application version
- `VITE_APP_ENV` - Application environment

### Runtime Environment Variables

- `BACKEND_URL` - Backend API URL for Nginx proxy (default: http://backend:8080)

## Configuration Examples

### Development Build

```bash
docker build -t pgb4-frontend:dev \
  --build-arg BUILD_MODE=development \
  --build-arg VITE_API_BASE_URL=http://localhost:8080/api/v1 \
  .
```

### Production Build

```bash
docker build -t pgb4-frontend:prod \
  --build-arg BUILD_MODE=production \
  --build-arg VITE_API_BASE_URL=https://api.example.com/api/v1 \
  .
```

### Running with Custom Backend

```bash
docker run -d -p 3000:80 \
  -e BACKEND_URL=https://api.example.com \
  --name pgb4-frontend \
  pgb4-frontend:prod
```

## Nginx Configuration

The container uses Nginx to:
- Serve static React files
- Proxy API requests to the backend
- Handle SPA routing (React Router)
- Provide gzip compression
- Set security headers
- Cache static assets

### Proxy Configuration

API requests to `/api/*` are proxied to the backend server specified by the `BACKEND_URL` environment variable.

### Health Check

The container includes a health check endpoint at `/health` that returns a simple "healthy" response.

## Security Features

- Runs as non-root user (nginx-app:1001)
- Security headers (X-Frame-Options, X-Content-Type-Options, etc.)
- Minimal attack surface with Alpine Linux base
- No sensitive information in image layers

## Performance Optimizations

- Multi-stage build reduces final image size
- Gzip compression for text assets
- Proper caching headers for static assets
- Manual chunk splitting for better caching
- Optimized Nginx configuration

## Troubleshooting

### Container Won't Start

```bash
# Check container logs
docker logs pgb4-frontend

# Check if port is available
netstat -tulpn | grep :3000
```

### API Requests Failing

```bash
# Check backend URL configuration
docker exec pgb4-frontend env | grep BACKEND_URL

# Test backend connectivity from container
docker exec pgb4-frontend wget -qO- http://backend:8080/health
```

### Build Failures

```bash
# Clean Docker cache
docker system prune -a

# Rebuild without cache
docker build --no-cache -t pgb4-frontend .
```

## Testing

### Automated Testing

```bash
# Run the test script
./test-docker.sh
```

### Manual Testing

```bash
# Build and run
docker build -t pgb4-frontend .
docker run -d -p 3000:80 --name pgb4-frontend pgb4-frontend

# Test endpoints
curl http://localhost:3000/health
curl http://localhost:3000/

# Clean up
docker rm -f pgb4-frontend
```

## Deployment

### Kubernetes

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: pgb4-frontend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: pgb4-frontend
  template:
    metadata:
      labels:
        app: pgb4-frontend
    spec:
      containers:
      - name: frontend
        image: pgb4-frontend:latest
        ports:
        - containerPort: 80
        env:
        - name: BACKEND_URL
          value: "http://pgb4-backend:8080"
        livenessProbe:
          httpGet:
            path: /health
            port: 80
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /health
            port: 80
          initialDelaySeconds: 5
          periodSeconds: 5
```

### Docker Swarm

```yaml
version: '3.8'
services:
  frontend:
    image: pgb4-frontend:latest
    ports:
      - "3000:80"
    environment:
      - BACKEND_URL=http://backend:8080
    deploy:
      replicas: 2
      restart_policy:
        condition: on-failure
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:80/health"]
      interval: 30s
      timeout: 10s
      retries: 3
```

## Best Practices

1. **Use specific tags** - Don't use `latest` in production
2. **Set resource limits** - Configure memory and CPU limits
3. **Use health checks** - Enable proper health monitoring
4. **Secure secrets** - Use secret management for sensitive data
5. **Monitor logs** - Set up proper log aggregation
6. **Regular updates** - Keep base images updated for security

## Support

For issues related to the Docker setup, check:
1. Container logs: `docker logs pgb4-frontend`
2. Nginx configuration: `docker exec pgb4-frontend cat /etc/nginx/conf.d/default.conf`
3. Environment variables: `docker exec pgb4-frontend env`