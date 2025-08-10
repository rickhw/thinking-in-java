# Docker Deployment Guide for PGB4 Message Board

This guide provides step-by-step instructions for deploying the PGB4 Message Board application using Docker and Docker Compose.

## Quick Start

### 1. Prerequisites Check

```bash
# Run the deployment script with prerequisites check
./scripts/deploy-docker.sh --help

# Or manually check prerequisites
docker --version
docker-compose --version
curl --version
jq --version
```

### 2. Environment Setup

```bash
# Copy environment template
cp .env.docker .env

# Edit configuration for your environment
nano .env
```

### 3. Deploy Application

```bash
# Development deployment
./scripts/deploy-docker.sh deploy

# Production deployment
./scripts/deploy-docker.sh -e production deploy

# With Redis and Load Balancer
./scripts/deploy-docker.sh -e production -p with-redis -p with-loadbalancer deploy
```

## Detailed Deployment Steps

### Step 1: Environment Preparation

#### 1.1 System Requirements

- **CPU**: 2+ cores recommended
- **Memory**: 4GB+ RAM
- **Storage**: 10GB+ free space
- **OS**: Linux, macOS, or Windows with WSL2

#### 1.2 Docker Installation

```bash
# Ubuntu/Debian
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# macOS (using Homebrew)
brew install docker docker-compose

# Verify installation
docker --version
docker-compose --version
```

#### 1.3 Project Setup

```bash
# Clone repository (if not already done)
git clone <repository-url>
cd pgb4_login

# Make scripts executable
chmod +x scripts/*.sh

# Create data directories
mkdir -p data/{mysql,redis} logs/{backend,frontend,nginx}
```

### Step 2: Configuration

#### 2.1 Environment Configuration

Choose your environment configuration:

```bash
# Development (default)
cp .env.development .env

# Production
cp .env.production .env
```

#### 2.2 Customize Configuration

Edit `.env` file with your specific settings:

```bash
# Database settings
DB_PASSWORD=your_secure_password
DB_ROOT_PASSWORD=your_root_password

# Application settings
FRONTEND_API_URL=http://your-domain.com/api/v1

# Resource limits (adjust based on your system)
BACKEND_MEMORY_LIMIT=2G
FRONTEND_MEMORY_LIMIT=512M
```

#### 2.3 Production Security

For production deployments, use Docker secrets:

```bash
# Create secrets
echo "your_mysql_root_password" | docker secret create mysql_root_password -
echo "your_mysql_password" | docker secret create mysql_password -
echo "your_redis_password" | docker secret create redis_password -
```

### Step 3: Deployment

#### 3.1 Build and Deploy

```bash
# Full deployment (build + deploy)
./scripts/deploy-docker.sh deploy

# Build only
./scripts/deploy-docker.sh build

# Deploy with specific tag
./scripts/deploy-docker.sh -t v1.2.3 deploy
```

#### 3.2 Production Deployment

```bash
# Production with all services
./scripts/deploy-docker.sh \
  -e production \
  -p with-redis \
  -p with-loadbalancer \
  deploy
```

#### 3.3 Registry Deployment

```bash
# Build and push to registry
./scripts/deploy-docker.sh \
  -r registry.example.com \
  -t v1.2.3 \
  build push

# Deploy from registry
./scripts/deploy-docker.sh \
  -r registry.example.com \
  -t v1.2.3 \
  deploy
```

### Step 4: Verification

#### 4.1 Check Service Status

```bash
# Using deployment script
./scripts/deploy-docker.sh status

# Using health check script
./scripts/docker-health-check.sh

# Manual check
docker-compose ps
```

#### 4.2 Test Endpoints

```bash
# Backend health
curl http://localhost:8080/health

# Frontend
curl http://localhost:3000

# Database connectivity
docker-compose exec mysql mysqladmin ping
```

#### 4.3 View Logs

```bash
# All services
./scripts/deploy-docker.sh logs

# Specific service
./scripts/deploy-docker.sh -s backend logs

# Follow logs
docker-compose logs -f
```

## Service Management

### Starting and Stopping Services

```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# Restart specific service
docker-compose restart backend

# Scale services
docker-compose up -d --scale backend=3
```

### Service Profiles

Use profiles to control which services are started:

```bash
# Core services only (default)
docker-compose up -d

# With Redis
docker-compose --profile with-redis up -d

# With load balancer
docker-compose --profile with-loadbalancer up -d

# All services
docker-compose --profile with-redis --profile with-loadbalancer up -d
```

## Data Management

### Backup and Restore

```bash
# Create backup
./scripts/docker-backup.sh backup

# Create named backup
./scripts/docker-backup.sh -b production-v1.2.3 backup

# List backups
./scripts/docker-backup.sh list

# Restore from backup
./scripts/docker-backup.sh -b backup_20240110_143022 restore

# Clean old backups
./scripts/docker-backup.sh -k 7 cleanup
```

### Volume Management

```bash
# List volumes
docker volume ls

# Inspect volume
docker volume inspect pgb4_mysql_data

# Backup volume
docker run --rm -v pgb4_mysql_data:/data -v $(pwd):/backup alpine \
  tar czf /backup/mysql-backup.tar.gz -C /data .

# Restore volume
docker run --rm -v pgb4_mysql_data:/data -v $(pwd):/backup alpine \
  tar xzf /backup/mysql-backup.tar.gz -C /data
```

## Monitoring and Maintenance

### Health Monitoring

```bash
# Continuous monitoring
./scripts/docker-health-check.sh -w -i 30

# Check specific service
./scripts/docker-health-check.sh -s pgb4-backend

# Resource usage
docker stats
```

### Log Management

```bash
# View logs
docker-compose logs -f --tail=100

# Log rotation (if needed)
docker-compose exec backend logrotate /etc/logrotate.conf

# Clean logs
docker system prune -f
```

### Updates and Maintenance

```bash
# Update images
docker-compose pull
docker-compose up -d

# Rolling update
docker-compose up -d --no-deps backend

# Clean unused resources
./scripts/deploy-docker.sh cleanup
```

## Troubleshooting

### Common Issues

#### Port Conflicts

```bash
# Check port usage
netstat -tulpn | grep :3000

# Change port in .env
FRONTEND_PORT=3001
```

#### Memory Issues

```bash
# Check memory usage
docker stats

# Adjust limits in .env
BACKEND_MEMORY_LIMIT=1G
```

#### Database Connection Issues

```bash
# Check MySQL logs
docker-compose logs mysql

# Test database connection
docker-compose exec backend curl -f http://localhost:8080/health

# Reset database
docker-compose down
docker volume rm pgb4_mysql_data
docker-compose up -d
```

#### Service Health Issues

```bash
# Check service health
./scripts/docker-health-check.sh

# Restart unhealthy services
docker-compose restart backend

# Check service logs
docker-compose logs backend
```

### Debug Mode

```bash
# Enable verbose logging
./scripts/deploy-docker.sh -v deploy

# Dry run mode
./scripts/deploy-docker.sh --dry-run deploy

# Check configuration
docker-compose config
```

### Performance Issues

```bash
# Check resource usage
docker stats

# Adjust resource limits
# Edit .env file and restart services

# Scale services
docker-compose up -d --scale backend=2
```

## Security Best Practices

### Production Security

1. **Use Docker Secrets** for sensitive data
2. **Enable SSL/TLS** for external connections
3. **Configure Firewall** rules
4. **Regular Updates** of base images
5. **Resource Limits** to prevent DoS
6. **Network Isolation** between environments

### Environment Separation

```bash
# Use different compose files
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# Separate networks
docker network create pgb4-prod-network

# Different data directories
DATA_DIR=/var/lib/pgb4-prod
LOGS_DIR=/var/log/pgb4-prod
```

## Advanced Configuration

### Custom Nginx Configuration

```bash
# Edit nginx configuration
nano config/nginx/nginx-lb.conf

# Restart nginx
docker-compose restart nginx-lb
```

### Database Tuning

```bash
# Edit MySQL configuration
nano config/mysql/my.cnf

# Restart MySQL
docker-compose restart mysql
```

### Redis Configuration

```bash
# Edit Redis configuration
nano config/redis/redis.conf

# Restart Redis
docker-compose restart redis
```

## Deployment Automation

### CI/CD Integration

```bash
# Example CI/CD pipeline step
./scripts/deploy-docker.sh \
  -e production \
  -r $CI_REGISTRY \
  -t $CI_COMMIT_TAG \
  --force \
  build push deploy
```

### Automated Backups

```bash
# Add to crontab for daily backups
0 2 * * * /path/to/pgb4_login/scripts/docker-backup.sh backup

# Weekly cleanup
0 3 * * 0 /path/to/pgb4_login/scripts/docker-backup.sh -k 30 cleanup
```

## Support and Resources

### Useful Commands

```bash
# Quick status check
docker-compose ps && docker stats --no-stream

# Full system info
docker system info

# Network inspection
docker network ls
docker network inspect pgb4-network

# Volume inspection
docker volume ls
docker volume inspect pgb4_mysql_data
```

### Log Locations

- **Application Logs**: `./logs/`
- **Container Logs**: `docker-compose logs`
- **System Logs**: `/var/log/docker/`

### Configuration Files

- **Main Compose**: `docker-compose.yml`
- **Development**: `docker-compose.override.yml`
- **Production**: `docker-compose.prod.yml`
- **Environment**: `.env`, `.env.development`, `.env.production`

For additional support, check the main `DOCKER_COMPOSE_README.md` file or contact the development team.