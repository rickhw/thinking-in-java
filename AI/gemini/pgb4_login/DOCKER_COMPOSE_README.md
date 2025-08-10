# Docker Compose Setup for PGB4 Message Board

This document provides comprehensive instructions for deploying the PGB4 Message Board application using Docker Compose.

## Overview

The Docker Compose setup includes:
- **Frontend**: React application served by Nginx
- **Backend**: Spring Boot application
- **Database**: MySQL 8.0
- **Cache**: Redis (optional)
- **Load Balancer**: Nginx (optional, for production)

## Quick Start

### 1. Prerequisites

- Docker Engine 20.10+
- Docker Compose 2.0+
- At least 4GB RAM available
- At least 10GB disk space

### 2. Environment Setup

Copy the environment template:
```bash
cp .env.docker .env
```

Edit `.env` file with your configuration:
```bash
# Basic configuration
DB_PASSWORD=your_secure_password
DB_ROOT_PASSWORD=your_root_password
FRONTEND_PORT=3000
SERVER_PORT=8080
```

### 3. Development Deployment

Start all services for development:
```bash
# Start core services (frontend, backend, database)
docker-compose up -d

# Or start with Redis
docker-compose --profile with-redis up -d

# View logs
docker-compose logs -f
```

### 4. Production Deployment

For production, use the production configuration:
```bash
# Create production secrets
echo "your_mysql_root_password" | docker secret create mysql_root_password -
echo "your_mysql_password" | docker secret create mysql_password -
echo "your_redis_password" | docker secret create redis_password -

# Deploy production stack
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# Or with load balancer
docker-compose -f docker-compose.yml -f docker-compose.prod.yml --profile with-loadbalancer up -d
```

## Configuration Files

### Main Configuration Files

- `docker-compose.yml` - Main configuration
- `docker-compose.override.yml` - Development overrides (auto-loaded)
- `docker-compose.prod.yml` - Production overrides
- `.env` - Environment variables
- `.env.docker` - Environment template

### Service Configuration

- `config/mysql/` - MySQL configuration and initialization
- `config/redis/` - Redis configuration
- `config/nginx/` - Nginx load balancer configuration

## Services

### MySQL Database

**Container**: `pgb4-mysql`
**Port**: 3306 (configurable)
**Data**: Persisted in `mysql_data` volume

Configuration:
- Character set: UTF8MB4
- Collation: utf8mb4_unicode_ci
- Buffer pool: 256MB
- Max connections: 200

### Backend (Spring Boot)

**Container**: `pgb4-backend`
**Port**: 8080 (configurable)
**Health Check**: `/health/live`

Features:
- Auto-restart on failure
- Health monitoring
- Log persistence
- JVM optimization
- Database connection pooling

### Frontend (React + Nginx)

**Container**: `pgb4-frontend`
**Port**: 3000 (configurable)
**Health Check**: `/health`

Features:
- Nginx reverse proxy
- Static file serving
- Gzip compression
- Security headers
- SPA routing support

### Redis (Optional)

**Container**: `pgb4-redis`
**Port**: 6379 (configurable)
**Profile**: `with-redis`

Configuration:
- Memory limit: 100MB
- Persistence: AOF + RDB
- Eviction policy: allkeys-lru

### Load Balancer (Optional)

**Container**: `pgb4-nginx-lb`
**Ports**: 80, 443
**Profile**: `with-loadbalancer`

Features:
- Load balancing
- Rate limiting
- SSL termination (if configured)
- Health checks
- Static asset caching

## Environment Variables

### Database Configuration

```bash
DB_NAME=pgb                    # Database name
DB_USERNAME=pgb_user           # Database user
DB_PASSWORD=pgb_password       # Database password
DB_ROOT_PASSWORD=medusa        # MySQL root password
DB_PORT=3306                   # Database port
DB_POOL_SIZE=20               # Connection pool size
```

### Backend Configuration

```bash
SERVER_PORT=8080              # Backend server port
LOG_LEVEL=WARN               # Root log level
APP_LOG_LEVEL=INFO           # Application log level
JPA_DDL_AUTO=validate        # JPA DDL mode
JVM_MIN_HEAP=512m            # JVM minimum heap
JVM_MAX_HEAP=1024m           # JVM maximum heap
```

### Frontend Configuration

```bash
FRONTEND_PORT=3000           # Frontend port
FRONTEND_API_URL=http://localhost:8080/api/v1  # API URL for build
NGINX_WORKERS=auto           # Nginx worker processes
NGINX_CONNECTIONS=1024       # Nginx worker connections
```

### Resource Limits

```bash
BACKEND_MEMORY_LIMIT=1.5G    # Backend memory limit
BACKEND_CPU_LIMIT=1.0        # Backend CPU limit
FRONTEND_MEMORY_LIMIT=256M   # Frontend memory limit
FRONTEND_CPU_LIMIT=0.5       # Frontend CPU limit
```

## Profiles

Docker Compose profiles allow you to selectively start services:

- **Default**: frontend, backend, mysql
- **with-redis**: Includes Redis cache
- **with-loadbalancer**: Includes Nginx load balancer

Examples:
```bash
# Start with Redis
docker-compose --profile with-redis up -d

# Start with load balancer
docker-compose --profile with-loadbalancer up -d

# Start with both
docker-compose --profile with-redis --profile with-loadbalancer up -d
```

## Data Persistence

### Development

Data is stored in local directories:
- `./data/mysql` - MySQL data
- `./data/redis` - Redis data
- `./logs/backend` - Backend logs
- `./logs/frontend` - Frontend logs

### Production

Data is stored in system directories:
- `/var/lib/pgb4/mysql` - MySQL data
- `/var/lib/pgb4/redis` - Redis data
- `/var/log/pgb4/backend` - Backend logs
- `/var/log/pgb4/frontend` - Frontend logs

## Networking

Services communicate through the `pgb4-network` bridge network:
- **Subnet**: 172.20.0.0/16 (development), 172.21.0.0/16 (production)
- **Internal DNS**: Services can reach each other by container name

## Health Checks

All services include health checks:

- **MySQL**: `mysqladmin ping`
- **Backend**: `curl http://localhost:8080/health/live`
- **Frontend**: `wget http://localhost:80/health`
- **Redis**: `redis-cli ping`
- **Load Balancer**: `wget http://localhost:80/health`

## Monitoring

### Service Status

```bash
# Check service status
docker-compose ps

# View service logs
docker-compose logs -f [service_name]

# Check resource usage
docker stats
```

### Health Endpoints

- Backend Health: http://localhost:8080/health
- Backend Metrics: http://localhost:8080/actuator/metrics
- Frontend Health: http://localhost:3000/health

## Scaling

### Horizontal Scaling

Scale backend instances:
```bash
docker-compose up -d --scale backend=3
```

Scale frontend instances:
```bash
docker-compose up -d --scale frontend=2
```

### Production Scaling

Production configuration includes automatic scaling:
- Backend: 2 replicas
- Frontend: 2 replicas
- Rolling updates with zero downtime

## Troubleshooting

### Common Issues

1. **Port Conflicts**
   ```bash
   # Check port usage
   netstat -tulpn | grep :3000
   
   # Change ports in .env file
   FRONTEND_PORT=3001
   ```

2. **Database Connection Issues**
   ```bash
   # Check MySQL logs
   docker-compose logs mysql
   
   # Verify database connectivity
   docker-compose exec backend curl -f http://localhost:8080/health
   ```

3. **Memory Issues**
   ```bash
   # Check memory usage
   docker stats
   
   # Adjust memory limits in .env
   BACKEND_MEMORY_LIMIT=2G
   ```

### Log Analysis

```bash
# View all logs
docker-compose logs

# Follow specific service logs
docker-compose logs -f backend

# View last 100 lines
docker-compose logs --tail=100 mysql
```

### Service Debugging

```bash
# Execute commands in containers
docker-compose exec backend bash
docker-compose exec mysql mysql -u root -p

# Check service configuration
docker-compose config

# Validate compose file
docker-compose config --quiet
```

## Backup and Recovery

### Database Backup

```bash
# Create backup
docker-compose exec mysql mysqldump -u root -p pgb > backup.sql

# Restore backup
docker-compose exec -T mysql mysql -u root -p pgb < backup.sql
```

### Volume Backup

```bash
# Backup volumes
docker run --rm -v pgb4_mysql_data:/data -v $(pwd):/backup alpine tar czf /backup/mysql-backup.tar.gz -C /data .

# Restore volumes
docker run --rm -v pgb4_mysql_data:/data -v $(pwd):/backup alpine tar xzf /backup/mysql-backup.tar.gz -C /data
```

## Security Considerations

### Production Security

1. **Use Docker Secrets** for sensitive data
2. **Enable SSL/TLS** for external connections
3. **Configure Firewall** rules
4. **Regular Updates** of base images
5. **Resource Limits** to prevent DoS
6. **Network Isolation** between environments

### Environment Separation

- Use different compose files for different environments
- Separate networks and volumes
- Different secrets and configurations
- Isolated data directories

## Performance Optimization

### Database Optimization

- Adjust `innodb_buffer_pool_size` based on available memory
- Configure connection pooling
- Enable query caching if needed
- Regular maintenance and optimization

### Application Optimization

- JVM tuning for backend
- Nginx caching for frontend
- Redis for session storage
- Load balancing for high availability

### Resource Monitoring

- Use `docker stats` for real-time monitoring
- Configure log rotation
- Monitor disk usage
- Set up alerting for critical metrics

## Maintenance

### Regular Tasks

```bash
# Update images
docker-compose pull
docker-compose up -d

# Clean up unused resources
docker system prune -f

# Rotate logs
docker-compose exec backend logrotate /etc/logrotate.conf

# Database maintenance
docker-compose exec mysql mysqlcheck -u root -p --optimize --all-databases
```

### Updates and Rollbacks

```bash
# Rolling update (production)
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d --no-deps backend

# Rollback to previous version
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d --no-deps backend:previous-tag
```

## Support

For issues and questions:
1. Check the logs first
2. Verify configuration
3. Check resource usage
4. Review this documentation
5. Contact the development team