# SystemD Service Configuration for PGB4 Message Board

This directory contains SystemD service configuration files for deploying the PGB4 Message Board application on Linux servers.

## Files Overview

- `pgb4-backend.service` - SystemD service for the Spring Boot backend
- `pgb4-frontend.service` - SystemD service for the Nginx frontend
- `pgb4.target` - SystemD target to manage both services together
- `pgb4-backend.env` - Environment variables template for backend
- `pgb4-frontend.env` - Environment variables template for frontend

## Prerequisites

1. **System Requirements:**
   - Linux system with SystemD
   - Java 17 or higher
   - Nginx web server
   - MySQL database server

2. **User Setup:**
   ```bash
   # Create application user
   sudo useradd -r -s /bin/false pgb4
   sudo useradd -r -s /bin/false nginx  # if not exists
   ```

3. **Directory Structure:**
   ```bash
   sudo mkdir -p /opt/pgb4/{backend,frontend}
   sudo mkdir -p /var/log/pgb4
   sudo chown pgb4:pgb4 /opt/pgb4/backend /var/log/pgb4
   sudo chown nginx:nginx /opt/pgb4/frontend
   ```

## Installation

1. **Copy service files:**
   ```bash
   sudo cp pgb4-backend.service /etc/systemd/system/
   sudo cp pgb4-frontend.service /etc/systemd/system/
   sudo cp pgb4.target /etc/systemd/system/
   ```

2. **Configure environment variables:**
   ```bash
   # Edit environment files with your specific configuration
   sudo mkdir -p /etc/systemd/system/pgb4-backend.service.d
   sudo mkdir -p /etc/systemd/system/pgb4-frontend.service.d
   
   # Create override files based on .env templates
   sudo cp pgb4-backend.env /etc/systemd/system/pgb4-backend.service.d/override.conf
   sudo cp pgb4-frontend.env /etc/systemd/system/pgb4-frontend.service.d/override.conf
   ```

3. **Deploy application files:**
   ```bash
   # Backend JAR file
   sudo cp /path/to/pgb4-backend.jar /opt/pgb4/backend/
   
   # Frontend static files
   sudo cp -r /path/to/frontend/dist/* /opt/pgb4/frontend/
   sudo cp /path/to/frontend/nginx.conf /opt/pgb4/frontend/
   ```

4. **Reload SystemD and enable services:**
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl enable pgb4-backend.service
   sudo systemctl enable pgb4-frontend.service
   sudo systemctl enable pgb4.target
   ```

## Service Management

### Start Services
```bash
# Start individual services
sudo systemctl start pgb4-backend
sudo systemctl start pgb4-frontend

# Or start both using target
sudo systemctl start pgb4.target
```

### Stop Services
```bash
# Stop individual services
sudo systemctl stop pgb4-backend
sudo systemctl stop pgb4-frontend

# Or stop both using target
sudo systemctl stop pgb4.target
```

### Check Status
```bash
# Check individual service status
sudo systemctl status pgb4-backend
sudo systemctl status pgb4-frontend

# Check target status
sudo systemctl status pgb4.target

# View logs
sudo journalctl -u pgb4-backend -f
sudo journalctl -u pgb4-frontend -f
```

### Restart Services
```bash
# Restart individual services
sudo systemctl restart pgb4-backend
sudo systemctl restart pgb4-frontend

# Restart both
sudo systemctl restart pgb4.target
```

## Configuration

### Backend Configuration
Edit `/etc/systemd/system/pgb4-backend.service.d/override.conf`:

```ini
[Service]
Environment=DB_URL=jdbc:mysql://your-db-host:3306/pgb4_messageboard
Environment=DB_USERNAME=your_db_user
Environment=DB_PASSWORD=your_db_password
Environment=JWT_SECRET=your_jwt_secret
```

### Frontend Configuration
Edit `/etc/systemd/system/pgb4-frontend.service.d/override.conf`:

```ini
[Service]
Environment=BACKEND_URL=http://localhost:8080
Environment=FRONTEND_PORT=80
```

## Security Features

- Services run as non-root users (`pgb4` and `nginx`)
- Restricted file system access using `ProtectSystem=strict`
- Private temporary directories
- No new privileges allowed

## Monitoring and Logging

- All services log to systemd journal
- Backend logs: `journalctl -u pgb4-backend`
- Frontend logs: `journalctl -u pgb4-frontend`
- Application logs: `/var/log/pgb4/backend.log`
- Nginx logs: `/var/log/nginx/pgb4-*.log`

## Troubleshooting

### Common Issues

1. **Service fails to start:**
   ```bash
   sudo systemctl status pgb4-backend
   sudo journalctl -u pgb4-backend --no-pager
   ```

2. **Port conflicts:**
   ```bash
   sudo netstat -tlnp | grep :8080
   sudo netstat -tlnp | grep :80
   ```

3. **Permission issues:**
   ```bash
   sudo chown -R pgb4:pgb4 /opt/pgb4/backend
   sudo chown -R nginx:nginx /opt/pgb4/frontend
   ```

4. **Database connection issues:**
   - Check database server status
   - Verify connection credentials
   - Check firewall rules

### Health Checks

The services include built-in health checks:
- Backend: `curl http://localhost:8080/api/health`
- Frontend: `curl http://localhost/health`

## Automatic Startup

Services are configured to:
- Start automatically on system boot
- Restart automatically on failure
- Respect service dependencies (frontend waits for backend)