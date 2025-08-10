# PGB4 Kubernetes Deployment

This directory contains Kubernetes configuration files for deploying the PGB4 Message Board application to a Kubernetes cluster.

## Overview

The PGB4 application consists of:
- **Backend**: Spring Boot application (Java 17)
- **Frontend**: React application served by Nginx
- **Database**: MySQL (can be external or in-cluster)
- **Cache**: Redis (optional)

## Prerequisites

1. **Kubernetes Cluster**: A running Kubernetes cluster (v1.20+)
2. **kubectl**: Kubernetes command-line tool configured to access your cluster
3. **Docker Images**: Built and pushed PGB4 backend and frontend images
4. **Ingress Controller**: Nginx Ingress Controller (recommended)
5. **Cert-Manager**: For automatic TLS certificate management (optional)

## Configuration Files

### Core Application
- `namespace.yaml` - Namespace definitions for different environments
- `backend-deployment.yaml` - Backend application deployment
- `frontend-deployment.yaml` - Frontend application deployment
- `services.yaml` - Service definitions for all components
- `ingress.yaml` - Ingress configuration for external access

### Configuration Management
- `configmap.yaml` - Application configuration and environment variables
- `secrets.yaml` - Sensitive data (passwords, keys, certificates)

### Security & Access Control
- `rbac.yaml` - Service accounts, roles, and network policies

## Quick Start

### 1. Prepare Your Environment

```bash
# Clone the repository
git clone <repository-url>
cd pgb4_login

# Build and push Docker images
./scripts/build-docker.sh
docker push pgb4/backend:latest
docker push pgb4/frontend:latest
```

### 2. Configure Secrets

**Important**: Update the secrets in `secrets.yaml` with your actual credentials before deployment.

```bash
# Generate base64 encoded values for your secrets
echo -n "your_database_password" | base64
echo -n "your_jwt_secret" | base64

# Edit secrets.yaml with your actual values
vim deployment/k8s/secrets.yaml
```

### 3. Deploy to Kubernetes

```bash
# Using the deployment script (recommended)
./scripts/deploy-k8s.sh production deploy --image-tag v1.0.0

# Or manually apply configurations
kubectl apply -f deployment/k8s/namespace.yaml
kubectl apply -f deployment/k8s/secrets.yaml
kubectl apply -f deployment/k8s/configmap.yaml
kubectl apply -f deployment/k8s/rbac.yaml
kubectl apply -f deployment/k8s/services.yaml
kubectl apply -f deployment/k8s/backend-deployment.yaml
kubectl apply -f deployment/k8s/frontend-deployment.yaml
kubectl apply -f deployment/k8s/ingress.yaml
```

### 4. Verify Deployment

```bash
# Check deployment status
./scripts/deploy-k8s.sh production status

# Or manually check
kubectl get all -n pgb4
kubectl get ingress -n pgb4
```

## Deployment Script Usage

The `deploy-k8s.sh` script provides comprehensive deployment automation:

```bash
# Deploy to production
./scripts/deploy-k8s.sh production deploy --image-tag v1.2.3

# Deploy to staging with dry-run
./scripts/deploy-k8s.sh staging deploy --dry-run --verbose

# Update existing deployment
./scripts/deploy-k8s.sh production update --image-tag v1.2.4

# Scale deployments
./scripts/deploy-k8s.sh production scale 3

# Rollback deployment
./scripts/deploy-k8s.sh production rollback --force

# Check status
./scripts/deploy-k8s.sh production status

# View logs
./scripts/deploy-k8s.sh production logs backend
```

### Script Options

- `--namespace NAME`: Kubernetes namespace (default: pgb4)
- `--context NAME`: Kubernetes context to use
- `--image-tag TAG`: Docker image tag (default: latest)
- `--dry-run`: Show what would be done without executing
- `--verbose`: Enable verbose output
- `--force`: Force deployment without confirmation
- `--timeout SECONDS`: Deployment timeout (default: 300)

## Environment Configuration

### Production Environment

```bash
# Production deployment with specific image tag
./scripts/deploy-k8s.sh production deploy \
  --image-tag v1.0.0 \
  --namespace pgb4 \
  --timeout 600
```

### Staging Environment

```bash
# Staging deployment
./scripts/deploy-k8s.sh staging deploy \
  --image-tag develop \
  --namespace pgb4-staging
```

### Development Environment

```bash
# Development deployment
./scripts/deploy-k8s.sh development deploy \
  --image-tag latest \
  --namespace pgb4-dev
```

## Configuration Customization

### Environment Variables

Key environment variables can be customized in `configmap.yaml`:

```yaml
data:
  database.url: "jdbc:mysql://your-mysql-host:3306/pgb4"
  database.pool.size: "20"
  logging.level: "WARN"
  backend.url: "http://pgb4-backend-service:8080"
```

### Resource Limits

Adjust resource requests and limits in deployment files:

```yaml
resources:
  requests:
    memory: "1Gi"
    cpu: "500m"
  limits:
    memory: "2Gi"
    cpu: "1000m"
```

### Scaling

Configure replica counts:

```yaml
spec:
  replicas: 3  # Adjust based on your needs
```

## Ingress Configuration

### Domain Configuration

Update `ingress.yaml` with your actual domain names:

```yaml
rules:
- host: your-domain.com
  http:
    paths:
    - path: /
      pathType: Prefix
      backend:
        service:
          name: pgb4-frontend-service
          port:
            number: 80
```

### TLS/SSL Configuration

The ingress is configured for automatic TLS certificate management using cert-manager:

```yaml
tls:
- hosts:
  - your-domain.com
  secretName: pgb4-tls-secret
```

## Monitoring and Logging

### Health Checks

The application includes comprehensive health checks:

- **Liveness Probe**: `/actuator/health/liveness`
- **Readiness Probe**: `/actuator/health/readiness`
- **Startup Probe**: `/actuator/health/liveness`

### Metrics

Prometheus metrics are available at:
- Backend: `/actuator/prometheus`
- Frontend: Nginx metrics via ingress annotations

### Logging

View application logs:

```bash
# Backend logs
kubectl logs -l app=pgb4-backend -n pgb4 -f

# Frontend logs
kubectl logs -l app=pgb4-frontend -n pgb4 -f

# All logs
./scripts/deploy-k8s.sh production logs
```

## Troubleshooting

### Common Issues

1. **Pods not starting**:
   ```bash
   kubectl describe pod <pod-name> -n pgb4
   kubectl logs <pod-name> -n pgb4
   ```

2. **Database connection issues**:
   - Check database URL in ConfigMap
   - Verify database credentials in Secrets
   - Ensure database is accessible from cluster

3. **Image pull errors**:
   - Verify image names and tags
   - Check image registry access
   - Ensure images are pushed to registry

4. **Ingress not working**:
   - Verify ingress controller is installed
   - Check DNS configuration
   - Validate TLS certificates

### Debug Commands

```bash
# Check all resources
kubectl get all -n pgb4

# Describe problematic resources
kubectl describe deployment pgb4-backend -n pgb4
kubectl describe pod <pod-name> -n pgb4

# Check events
kubectl get events -n pgb4 --sort-by='.lastTimestamp'

# Port forward for local testing
kubectl port-forward svc/pgb4-backend-service 8080:8080 -n pgb4
kubectl port-forward svc/pgb4-frontend-service 8080:80 -n pgb4
```

## Security Considerations

1. **Secrets Management**: Use external secret management systems in production
2. **Network Policies**: Configured to restrict pod-to-pod communication
3. **RBAC**: Minimal permissions granted to service accounts
4. **Container Security**: Runs as non-root user with read-only filesystem
5. **TLS**: All external communication encrypted

## Backup and Recovery

### Database Backup

```bash
# Create database backup job
kubectl create job --from=cronjob/mysql-backup mysql-backup-manual -n pgb4
```

### Configuration Backup

```bash
# Backup all configurations
kubectl get all,configmap,secret,ingress -n pgb4 -o yaml > pgb4-backup.yaml
```

## Maintenance

### Updates

```bash
# Rolling update with new image
./scripts/deploy-k8s.sh production update --image-tag v1.2.4

# Check rollout status
kubectl rollout status deployment/pgb4-backend -n pgb4
kubectl rollout status deployment/pgb4-frontend -n pgb4
```

### Rollback

```bash
# Rollback to previous version
./scripts/deploy-k8s.sh production rollback

# Or manually
kubectl rollout undo deployment/pgb4-backend -n pgb4
kubectl rollout undo deployment/pgb4-frontend -n pgb4
```

### Scaling

```bash
# Scale up for high traffic
./scripts/deploy-k8s.sh production scale 5

# Scale down during low traffic
./scripts/deploy-k8s.sh production scale 2
```

## Support

For issues and questions:
1. Check the troubleshooting section above
2. Review Kubernetes events and logs
3. Consult the main project documentation
4. Contact the development team

## License

This deployment configuration is part of the PGB4 Message Board project.