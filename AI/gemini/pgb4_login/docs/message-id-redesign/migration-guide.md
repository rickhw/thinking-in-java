# Message ID Redesign - Migration Guide and Operation Manual

## Overview

This comprehensive guide provides step-by-step instructions for migrating from the old Long-based message ID system to the new 36-character string-based ID system. This migration is a breaking change that requires careful planning and execution.

## Migration Strategy

### Migration Approach

The migration follows a **Big Bang** approach due to the breaking nature of the ID format change. This means:

1. **Downtime Required:** The system will be temporarily unavailable during migration
2. **Data Transformation:** All existing message IDs will be converted to the new format
3. **Client Updates:** All client applications must be updated simultaneously

### Migration Timeline

**Estimated Total Downtime:** 2-4 hours (depending on data volume)

1. **Preparation Phase:** 1-2 weeks before migration
2. **Migration Execution:** 2-4 hours maintenance window
3. **Verification Phase:** 1-2 hours post-migration
4. **Monitoring Phase:** 24-48 hours post-migration

## Pre-Migration Preparation

### 1. Environment Setup

#### Test Environment Preparation

```bash
# 1. Create test environment backup
mysqldump -u root -p pgb_test > test_backup_$(date +%Y%m%d_%H%M%S).sql

# 2. Run migration in test environment
cd backend
./gradlew flywayMigrate -Dspring.profiles.active=test

# 3. Verify test migration
./gradlew test -Dspring.profiles.active=test
```

#### Production Environment Preparation

```bash
# 1. Create production backup
mysqldump -u root -p pgb > prod_backup_$(date +%Y%m%d_%H%M%S).sql

# 2. Verify backup integrity
mysql -u root -p pgb_backup < prod_backup_*.sql

# 3. Calculate migration time estimate
mysql -u root -p pgb -e "SELECT COUNT(*) as message_count FROM messages;"
```

### 2. Code Deployment Preparation

#### Backend Preparation

```bash
# 1. Build production-ready JAR
cd backend
./gradlew clean build -Dspring.profiles.active=prod

# 2. Verify JAR integrity
java -jar build/libs/messageboard-*.jar --spring.profiles.active=prod --spring.boot.run.arguments=--server.port=8081 &
sleep 30
curl -f http://localhost:8081/actuator/health
pkill -f "java.*messageboard"
```

#### Frontend Preparation

```bash
# 1. Build production frontend
cd frontend
npm ci
npm run build

# 2. Verify build integrity
npm run preview &
sleep 10
curl -f http://localhost:4173
pkill -f "vite preview"
```

### 3. Communication Plan

#### Stakeholder Notification

**2 weeks before migration:**
- Send migration announcement to all stakeholders
- Schedule maintenance window
- Provide migration timeline and expected downtime

**1 week before migration:**
- Send reminder notification
- Confirm maintenance window
- Share rollback procedures

**1 day before migration:**
- Send final reminder
- Confirm all preparation steps completed
- Brief migration team on procedures

## Migration Execution

### Phase 1: System Shutdown (15 minutes)

#### Step 1: Stop Frontend Services

```bash
# Stop nginx/web server
sudo systemctl stop nginx

# Or if using PM2
pm2 stop frontend

# Verify frontend is down
curl -f http://your-domain.com || echo "Frontend successfully stopped"
```

#### Step 2: Stop Backend Services

```bash
# Stop Spring Boot application
sudo systemctl stop messageboard-backend

# Or if using Docker
docker stop messageboard-backend

# Verify backend is down
curl -f http://localhost:8080/api/v1/messages || echo "Backend successfully stopped"
```

#### Step 3: Verify No Active Connections

```sql
-- Check for active database connections
SELECT * FROM information_schema.PROCESSLIST 
WHERE DB = 'pgb' AND COMMAND != 'Sleep';

-- Kill any remaining connections if necessary
-- KILL <connection_id>;
```

### Phase 2: Database Migration (60-120 minutes)

#### Step 1: Create Migration Backup

```bash
# Create pre-migration backup
mysqldump -u root -p pgb > migration_backup_$(date +%Y%m%d_%H%M%S).sql

# Verify backup size and integrity
ls -lh migration_backup_*.sql
mysql -u root -p pgb_verify < migration_backup_*.sql
```

#### Step 2: Execute Database Schema Changes

```sql
-- Connect to production database
mysql -u root -p pgb

-- Start transaction for safety
START TRANSACTION;

-- Step 2a: Add new ID column
ALTER TABLE messages ADD COLUMN new_id VARCHAR(36) NULL;

-- Step 2b: Create index on new column
CREATE INDEX idx_messages_new_id ON messages(new_id);

-- Commit schema changes
COMMIT;
```

#### Step 3: Generate New IDs for Existing Data

```sql
-- Use the migration utility to generate new IDs
-- This step is handled by the Java migration utility

-- Verify the migration utility is ready
SELECT COUNT(*) as total_messages FROM messages WHERE new_id IS NULL;
```

```bash
# Run the ID migration utility
cd migration
./gradlew run

# Monitor migration progress
tail -f logs/migration.log
```

#### Step 4: Verify Data Migration

```sql
-- Check that all messages have new IDs
SELECT COUNT(*) as messages_without_new_id FROM messages WHERE new_id IS NULL;

-- Verify new ID format
SELECT new_id, 
       LENGTH(new_id) as id_length,
       new_id REGEXP '^[A-Z0-9]{8}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{12}$' as valid_format
FROM messages 
LIMIT 10;

-- Check for duplicate new IDs
SELECT new_id, COUNT(*) as count 
FROM messages 
GROUP BY new_id 
HAVING COUNT(*) > 1;
```

#### Step 5: Switch to New ID Column

```sql
-- Start transaction for final schema changes
START TRANSACTION;

-- Drop old primary key
ALTER TABLE messages DROP PRIMARY KEY;

-- Drop old id column
ALTER TABLE messages DROP COLUMN id;

-- Rename new_id to id
ALTER TABLE messages CHANGE COLUMN new_id id VARCHAR(36) NOT NULL;

-- Add new primary key
ALTER TABLE messages ADD PRIMARY KEY (id);

-- Update indexes
DROP INDEX idx_messages_new_id ON messages;
CREATE INDEX idx_messages_id ON messages(id);
CREATE INDEX idx_messages_user_id ON messages(user_id);
CREATE INDEX idx_messages_created_at ON messages(created_at);

-- Commit final changes
COMMIT;
```

### Phase 3: Application Deployment (30-45 minutes)

#### Step 1: Deploy Backend

```bash
# Deploy new backend version
cp build/libs/messageboard-*.jar /opt/messageboard/messageboard.jar

# Update configuration if needed
cp backend/src/main/resources/application.properties /opt/messageboard/

# Start backend service
sudo systemctl start messageboard-backend

# Wait for startup
sleep 60

# Verify backend health
curl -f http://localhost:8080/actuator/health
```

#### Step 2: Deploy Frontend

```bash
# Deploy new frontend version
rm -rf /var/www/html/messageboard/*
cp -r frontend/dist/* /var/www/html/messageboard/

# Start frontend service
sudo systemctl start nginx

# Verify frontend accessibility
curl -f http://your-domain.com
```

### Phase 4: Verification and Testing (30-60 minutes)

#### Step 1: API Endpoint Testing

```bash
# Test message creation
curl -X POST http://localhost:8080/api/v1/messages \
  -H "Content-Type: application/json" \
  -d '{"userId":"test-user","content":"Migration test message"}' \
  -w "\nHTTP Status: %{http_code}\n"

# Test message retrieval (use ID from creation response)
MESSAGE_ID="A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6"  # Replace with actual ID
curl -f http://localhost:8080/api/v1/messages/$MESSAGE_ID

# Test message listing
curl -f http://localhost:8080/api/v1/messages?page=0&size=5

# Test message update
curl -X PUT http://localhost:8080/api/v1/messages/$MESSAGE_ID \
  -H "Content-Type: application/json" \
  -d '{"content":"Updated migration test message"}' \
  -w "\nHTTP Status: %{http_code}\n"
```

#### Step 2: Frontend Testing

```bash
# Test frontend pages
curl -f http://your-domain.com
curl -f http://your-domain.com/messages
curl -f http://your-domain.com/messages/$MESSAGE_ID
```

#### Step 3: Database Integrity Check

```sql
-- Verify message count matches pre-migration
SELECT COUNT(*) as post_migration_count FROM messages;

-- Check ID format consistency
SELECT 
  COUNT(*) as total_messages,
  SUM(CASE WHEN LENGTH(id) = 36 THEN 1 ELSE 0 END) as correct_length,
  SUM(CASE WHEN id REGEXP '^[A-Z0-9]{8}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{12}$' THEN 1 ELSE 0 END) as valid_format
FROM messages;

-- Check for any data corruption
SELECT id, user_id, content, created_at, updated_at 
FROM messages 
WHERE user_id IS NULL OR content IS NULL OR created_at IS NULL
LIMIT 10;
```

## Post-Migration Operations

### Immediate Post-Migration (First 2 hours)

#### 1. System Monitoring

```bash
# Monitor application logs
tail -f /opt/messageboard/logs/application.log

# Monitor system resources
htop
iostat -x 1

# Monitor database performance
mysql -u root -p pgb -e "SHOW PROCESSLIST;"
```

#### 2. Performance Verification

```bash
# Run performance tests
cd backend
./gradlew test --tests "*PerformanceTest*"

# Check response times
for i in {1..10}; do
  time curl -s http://localhost:8080/api/v1/messages > /dev/null
done
```

#### 3. Error Monitoring

```bash
# Check for application errors
grep -i error /opt/messageboard/logs/application.log | tail -20

# Check for database errors
mysql -u root -p pgb -e "SHOW ENGINE INNODB STATUS\G" | grep -A 10 "LATEST DETECTED DEADLOCK"
```

### Extended Monitoring (First 48 hours)

#### 1. User Experience Monitoring

- Monitor user feedback channels
- Check error rates in application logs
- Verify frontend functionality across different browsers
- Monitor API response times and error rates

#### 2. Database Performance Monitoring

```sql
-- Monitor query performance
SELECT 
  ROUND(AVG_TIMER_WAIT/1000000000000,6) as avg_exec_time_sec,
  COUNT_STAR as exec_count,
  DIGEST_TEXT
FROM performance_schema.events_statements_summary_by_digest 
WHERE DIGEST_TEXT LIKE '%messages%'
ORDER BY AVG_TIMER_WAIT DESC
LIMIT 10;

-- Monitor index usage
SELECT 
  OBJECT_NAME,
  INDEX_NAME,
  COUNT_FETCH,
  COUNT_INSERT,
  COUNT_UPDATE,
  COUNT_DELETE
FROM performance_schema.table_io_waits_summary_by_index_usage
WHERE OBJECT_SCHEMA = 'pgb' AND OBJECT_NAME = 'messages';
```

## Rollback Procedures

### When to Rollback

Initiate rollback if any of the following occur within 2 hours of migration:

1. **Critical System Errors:** Application fails to start or crashes repeatedly
2. **Data Corruption:** Evidence of data loss or corruption
3. **Performance Degradation:** Response times increase by >50%
4. **User Impact:** >10% of users unable to access the system

### Rollback Execution

#### Step 1: Stop Current Services

```bash
# Stop all services
sudo systemctl stop messageboard-backend
sudo systemctl stop nginx
```

#### Step 2: Restore Database

```bash
# Drop current database
mysql -u root -p -e "DROP DATABASE pgb;"

# Recreate database
mysql -u root -p -e "CREATE DATABASE pgb;"

# Restore from backup
mysql -u root -p pgb < migration_backup_*.sql

# Verify restoration
mysql -u root -p pgb -e "SELECT COUNT(*) FROM messages;"
```

#### Step 3: Deploy Previous Version

```bash
# Restore previous backend version
cp /opt/messageboard/backup/messageboard-previous.jar /opt/messageboard/messageboard.jar

# Restore previous frontend version
rm -rf /var/www/html/messageboard/*
cp -r /opt/messageboard/backup/frontend-previous/* /var/www/html/messageboard/

# Start services
sudo systemctl start messageboard-backend
sudo systemctl start nginx
```

#### Step 4: Verify Rollback

```bash
# Test old API format
curl -f http://localhost:8080/api/v1/messages/1  # Using old integer ID

# Test frontend
curl -f http://your-domain.com
```

## Troubleshooting Guide

### Common Issues and Solutions

#### 1. Migration Utility Fails

**Symptoms:**
- Java migration utility throws exceptions
- Some messages don't get new IDs

**Solutions:**
```bash
# Check available memory
free -h

# Increase JVM heap size
export JAVA_OPTS="-Xmx4g -Xms2g"

# Run migration in batches
cd migration
./gradlew run -Dbatch.size=1000
```

#### 2. Database Performance Issues

**Symptoms:**
- Slow query response times
- High CPU usage on database server

**Solutions:**
```sql
-- Analyze table statistics
ANALYZE TABLE messages;

-- Rebuild indexes
ALTER TABLE messages DROP INDEX idx_messages_id;
CREATE INDEX idx_messages_id ON messages(id);

-- Check for table fragmentation
SELECT 
  table_name,
  ROUND(data_length/1024/1024,2) as data_mb,
  ROUND(index_length/1024/1024,2) as index_mb,
  ROUND(data_free/1024/1024,2) as free_mb
FROM information_schema.tables 
WHERE table_schema = 'pgb' AND table_name = 'messages';
```

#### 3. Application Startup Issues

**Symptoms:**
- Spring Boot application fails to start
- Connection pool errors

**Solutions:**
```bash
# Check database connectivity
mysql -u root -p pgb -e "SELECT 1;"

# Verify application configuration
grep -n "spring.datasource" /opt/messageboard/application.properties

# Check for port conflicts
netstat -tulpn | grep :8080

# Review startup logs
tail -100 /opt/messageboard/logs/application.log
```

#### 4. Frontend Issues

**Symptoms:**
- Pages not loading
- API calls failing

**Solutions:**
```bash
# Check nginx configuration
nginx -t

# Verify static files
ls -la /var/www/html/messageboard/

# Check browser console for JavaScript errors
# Test API connectivity from frontend
curl -f http://localhost:8080/api/v1/messages
```

## Success Criteria

### Migration Success Indicators

1. **Functional Success:**
   - All API endpoints respond correctly with new ID format
   - Frontend displays messages with new IDs
   - CRUD operations work for messages
   - No data loss detected

2. **Performance Success:**
   - Response times within 10% of pre-migration levels
   - Database query performance maintained
   - No memory leaks or resource issues

3. **Data Integrity Success:**
   - All messages have valid 36-character IDs
   - No duplicate IDs exist
   - All relationships maintained
   - Backup and restore procedures verified

### Post-Migration Checklist

- [ ] All services running normally
- [ ] API endpoints responding correctly
- [ ] Frontend functionality verified
- [ ] Database performance acceptable
- [ ] No critical errors in logs
- [ ] User feedback positive
- [ ] Monitoring systems updated
- [ ] Documentation updated
- [ ] Team notified of successful migration
- [ ] Backup procedures verified

## Contact Information

### Migration Team Contacts

- **Migration Lead:** [Name] - [Email] - [Phone]
- **Database Administrator:** [Name] - [Email] - [Phone]
- **Backend Developer:** [Name] - [Email] - [Phone]
- **Frontend Developer:** [Name] - [Email] - [Phone]
- **DevOps Engineer:** [Name] - [Email] - [Phone]

### Escalation Procedures

1. **Level 1:** Migration team members
2. **Level 2:** Technical leads and architects
3. **Level 3:** Engineering management
4. **Level 4:** Executive team (for business-critical issues)

### Emergency Contacts

- **24/7 On-call:** [Phone number]
- **Emergency Email:** [Email address]
- **Incident Management:** [System/Process]