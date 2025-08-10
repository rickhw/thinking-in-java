# Multi-stage Dockerfile for PGB4 Message Board Frontend
# Stage 1: Build the React application
# Stage 2: Serve with Nginx

# Build stage
FROM node:18-alpine AS builder

# Set working directory
WORKDIR /app

# Copy package files
COPY package*.json ./

# Install all dependencies (including dev dependencies needed for build)
RUN npm install --silent

# Copy source code
COPY . .

# Build arguments for environment configuration
ARG BUILD_MODE=production
ARG VITE_API_BASE_URL
ARG VITE_APP_NAME
ARG VITE_APP_VERSION
ARG VITE_APP_ENV

# Set environment variables for build
ENV NODE_ENV=production
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
ENV VITE_APP_NAME=${VITE_APP_NAME}
ENV VITE_APP_VERSION=${VITE_APP_VERSION}
ENV VITE_APP_ENV=${VITE_APP_ENV}

# Build the application
RUN npm run build:${BUILD_MODE}

# Production stage
FROM nginx:1.25-alpine AS production

# Install envsubst for environment variable substitution
RUN apk add --no-cache gettext

# Copy custom nginx configuration
COPY nginx.conf.template /etc/nginx/templates/default.conf.template

# Copy built application from builder stage
COPY --from=builder /app/dist /usr/share/nginx/html

# Create nginx user and set permissions
RUN addgroup -g 1001 -S nginx-app && \
    adduser -S nginx-app -u 1001 -G nginx-app && \
    chown -R nginx-app:nginx-app /usr/share/nginx/html && \
    chown -R nginx-app:nginx-app /var/cache/nginx && \
    chown -R nginx-app:nginx-app /var/log/nginx && \
    chown -R nginx-app:nginx-app /etc/nginx/conf.d && \
    touch /var/run/nginx.pid && \
    chown -R nginx-app:nginx-app /var/run/nginx.pid

# Create startup script for environment variable substitution
RUN cat > /docker-entrypoint.sh << 'EOF'
#!/bin/sh
set -e

# Default backend URL if not provided
export BACKEND_URL=${BACKEND_URL:-"http://backend:8080/api"}

# Substitute environment variables in nginx config
envsubst '${BACKEND_URL}' < /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf

# Remove template file
rm -f /etc/nginx/templates/default.conf.template

# Start nginx
exec nginx -g "daemon off;"
EOF

# Make startup script executable
RUN chmod +x /docker-entrypoint.sh

# Switch to non-root user
USER nginx-app

# Expose port
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:80/health || exit 1

# Set entrypoint
ENTRYPOINT ["/docker-entrypoint.sh"]