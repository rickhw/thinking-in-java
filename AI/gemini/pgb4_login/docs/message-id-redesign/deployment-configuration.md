# Message ID Redesign - Deployment and Configuration Guide

## Overview

This document provides deployment and configuration instructions for the Message ID redesign feature. The system has been updated to use 36-character string IDs instead of Long integer IDs for messages.

## Configuration Changes

### Application Properties

The following configuration properties should be reviewed and updated as needed:

#### Database Configuration

**File:** `backend/src/main/resources/application.properties`

```properties
# Spring Datasource (No changes required)
spring.datasource.url=jdbc:mysql://localhost:3306/pgb
spring.datasource.username=root
spring.datasource.password=medusa
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Properties (No changes required)
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Async Thread Pool (No changes required)
spring.task.execution.pool.core-size=5
spring.task.execution.pool.max-size=10
spring.task.execution.pool.queue-capacity=25

# Message ID Generator Configuration (New)
# These properties can be added to customize ID generation behavior
messageboard.id-generator.machine-id=001
messageboard.id-generator.enable-uniqueness-check=true
messageboard.id-generator.max-retry-attempts=3
```

#### Test Environment Configuration

**File:** `backend/src/test/resources/application-test.properties`

```properties
# Test Database Configuration
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA Test Configuration
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false

# Message ID Generator Test Configuration
messageboard.id-generator.machine-id=TEST
messageboard.id-generator.enable-uniqueness-check=false
messageboard.id-generator.max-retry-attempts=1
```

### Database Schema Updates

#### Required Database Changes

The deployment requires the following database schema changes:

1. **Message Table ID Column Update:**
   ```sql
   ALTER TABLE messages MODIFY COLUMN id VARCHAR(36) NOT NULL;
   ```

2. **Index Updates:**
   ```sql
   -- Recreate primary key index
   ALTER TABLE messages DROP PRIMARY KEY;
   ALTER TABLE messages ADD PRIMARY KEY (id);
   
   -- Update other indexes if they reference the id column
   DROP INDEX IF EXISTS idx_messages_id;
   CREATE INDEX idx_messages_id ON messages(id);
   ```

#### Migration Script Location

The migration scripts are located in:
- `backend/src/main/resources/db/migration/V2__migrate_message_id_to_varchar.sql`
- `backend/src/main/resources/db/migration/V3__optimize_message_id_indexes.sql`

### Frontend Configuration

#### Environment Variables

**File:** `frontend/.env.development`

```env
# API Base URL (No changes required)
VITE_API_BASE_URL=http://localhost:8080/api/v1

# Message ID Validation (New)
VITE_ENABLE_CLIENT_SIDE_ID_VALIDATION=true
VITE_MESSAGE_ID_PATTERN=^[A-Z0-9]{8}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{12}$
```

**File:** `frontend/.env.production`

```env
# Production API Base URL
VITE_API_BASE_URL=https://your-production-domain.com/api/v1

# Message ID Validation
VITE_ENABLE_CLIENT_SIDE_ID_VALIDATION=true
VITE_MESSAGE_ID_PATTERN=^[A-Z0-9]{8}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{12}$
```

## Deployment Steps

### Pre-Deployment Checklist

1. **Database Backup:**
   ```bash
   mysqldump -u root -p pgb > backup_before_message_id_migration_$(date +%Y%m%d_%H%M%S).sql
   ```

2. **Test Environment Validation:**
   - Ensure all tests pass in the test environment
   - Verify migration scripts work correctly
   - Test API endpoints with new ID format

3. **Dependencies Check:**
   - Verify all required dependencies are installed
   - Check Java version compatibility (Java 17+)
   - Ensure MySQL version supports VARCHAR(36) primary keys

### Backend Deployment

#### Step 1: Stop the Application

```bash
# If using systemd
sudo systemctl stop messageboard-backend

# If using Docker
docker stop messageboard-backend

# If running directly
pkill -f "java.*messageboard"
```

#### Step 2: Database Migration

```bash
# Connect to MySQL
mysql -u root -p pgb

# Run migration scripts (if not using Flyway)
source backend/src/main/resources/db/migration/V2__migrate_message_id_to_varchar.sql;
source backend/src/main/resources/db/migration/V3__optimize_message_id_indexes.sql;
```

#### Step 3: Deploy New Backend Version

```bash
# Build the application
cd backend
./gradlew clean build

# Deploy the JAR file
cp build/libs/messageboard-*.jar /opt/messageboard/
```

#### Step 4: Start the Application

```bash
# If using systemd
sudo systemctl start messageboard-backend

# If using Docker
docker start messageboard-backend

# If running directly
cd /opt/messageboard
java -jar messageboard-*.jar
```

### Frontend Deployment

#### Step 1: Build Frontend

```bash
cd frontend
npm install
npm run build
```

#### Step 2: Deploy Static Files

```bash
# Copy build files to web server
cp -r dist/* /var/www/html/messageboard/

# Or if using nginx
cp -r dist/* /usr/share/nginx/html/messageboard/
```

### Post-Deployment Verification

#### Backend Health Check

```bash
# Check application status
curl -f http://localhost:8080/api/v1/messages || echo "Backend health check failed"

# Test new message creation
curl -X POST http://localhost:8080/api/v1/messages \
  -H "Content-Type: application/json" \
  -d '{"userId":"test-user","content":"Test message with new ID format"}'
```

#### Frontend Health Check

```bash
# Check if frontend is accessible
curl -f http://localhost:3000 || echo "Frontend health check failed"
```

#### Database Verification

```sql
-- Check message table structure
DESCRIBE messages;

-- Verify new ID format in existing data
SELECT id, LENGTH(id), id REGEXP '^[A-Z0-9]{8}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{12}$' as valid_format 
FROM messages 
LIMIT 5;
```

## Rollback Procedures

### Database Rollback

If rollback is necessary, use the rollback script:

```sql
-- File: backend/src/main/resources/db/migration/rollback/rollback_V2__revert_message_id_to_bigint.sql
-- This script reverts the message ID changes back to BIGINT format
```

### Application Rollback

```bash
# Stop current version
sudo systemctl stop messageboard-backend

# Deploy previous version
cp /opt/messageboard/backup/messageboard-previous.jar /opt/messageboard/messageboard.jar

# Start previous version
sudo systemctl start messageboard-backend
```

## Monitoring and Logging

### Key Metrics to Monitor

1. **ID Generation Performance:**
   - ID generation time
   - Uniqueness check failures
   - Retry attempts

2. **Database Performance:**
   - Query response times for message lookups
   - Index usage statistics
   - Lock contention on message table

3. **API Response Times:**
   - Message CRUD operation latencies
   - Error rates for invalid ID format requests

### Log Configuration

Add the following to `application.properties` for enhanced logging:

```properties
# Message ID Generator Logging
logging.level.com.gtcafe.messageboard.service.MessageIdGenerator=DEBUG

# Database Query Logging
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# Performance Monitoring Logging
logging.level.com.gtcafe.messageboard.service.PerformanceMonitoringService=INFO
```

## Troubleshooting

### Common Issues

1. **Invalid ID Format Errors:**
   - Check client-side ID validation
   - Verify API request format
   - Review error logs for pattern matching failures

2. **Database Migration Issues:**
   - Ensure sufficient disk space for table alteration
   - Check for foreign key constraints
   - Verify MySQL version compatibility

3. **Performance Degradation:**
   - Monitor index usage
   - Check query execution plans
   - Review ID generation performance metrics

### Support Contacts

- **Database Issues:** DBA Team
- **Application Issues:** Backend Development Team
- **Frontend Issues:** Frontend Development Team
- **Infrastructure Issues:** DevOps Team

## Security Considerations

1. **ID Predictability:** The new ID format reduces predictability compared to sequential integers
2. **URL Security:** Longer IDs make URL guessing more difficult
3. **Database Security:** Ensure proper access controls on the messages table
4. **API Security:** Validate all incoming message IDs to prevent injection attacks