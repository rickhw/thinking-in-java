# Deployment Configuration

This directory contains deployment configurations for the pgb4_login project.

## Directory Structure

- `systemd/` - SystemD service configuration files for traditional Linux server deployment
- `docker/` - Docker configuration files including Dockerfiles and docker-compose.yml
- `k8s/` - Kubernetes deployment configurations including deployments, services, and ingress

## Related Directories

- `../scripts/` - Automation scripts for build and deployment processes
- `../config/` - Environment-specific configuration files

## Usage

Each subdirectory contains specific deployment configurations for different deployment targets:

1. **SystemD Deployment**: Use configurations in `systemd/` for deploying to Linux servers
2. **Docker Deployment**: Use configurations in `docker/` for containerized deployment
3. **Kubernetes Deployment**: Use configurations in `k8s/` for K8s cluster deployment