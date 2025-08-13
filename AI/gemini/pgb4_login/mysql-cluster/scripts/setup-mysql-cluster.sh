#!/bin/bash

# MySQL Cluster Setup Script
# This script sets up master-slave replication between MySQL nodes

set -e

echo "Setting up MySQL Cluster..."

# Wait for MySQL services to be ready
echo "Waiting for MySQL services to start..."
sleep 30

# Create replication user on master
echo "Creating replication user on master..."
docker exec mysql-master mysql -u root -prootpassword -e "
CREATE USER IF NOT EXISTS 'replication'@'%' IDENTIFIED BY 'replication_password';
GRANT REPLICATION SLAVE ON *.* TO 'replication'@'%';
CREATE USER IF NOT EXISTS 'maxscale'@'%' IDENTIFIED BY 'maxscale_password';
GRANT SELECT ON mysql.user TO 'maxscale'@'%';
GRANT SELECT ON mysql.db TO 'maxscale'@'%';
GRANT SELECT ON mysql.tables_priv TO 'maxscale'@'%';
GRANT SELECT ON mysql.columns_priv TO 'maxscale'@'%';
GRANT SELECT ON mysql.procs_priv TO 'maxscale'@'%';
GRANT SHOW DATABASES ON *.* TO 'maxscale'@'%';
GRANT REPLICATION CLIENT ON *.* TO 'maxscale'@'%';
GRANT REPLICATION SLAVE ON *.* TO 'maxscale'@'%';
FLUSH PRIVILEGES;
"

# Get master status
echo "Getting master status..."
MASTER_STATUS=$(docker exec mysql-master mysql -u root -prootpassword -e "SHOW MASTER STATUS\G")
echo "$MASTER_STATUS"

# Setup slave1
echo "Setting up slave1..."
docker exec mysql-slave1 mysql -u root -prootpassword -e "
STOP SLAVE;
RESET SLAVE ALL;
CHANGE MASTER TO
  MASTER_HOST='mysql-master',
  MASTER_USER='replication',
  MASTER_PASSWORD='replication_password',
  MASTER_AUTO_POSITION=1;
START SLAVE;
"

# Setup slave2
echo "Setting up slave2..."
docker exec mysql-slave2 mysql -u root -prootpassword -e "
STOP SLAVE;
RESET SLAVE ALL;
CHANGE MASTER TO
  MASTER_HOST='mysql-master',
  MASTER_USER='replication',
  MASTER_PASSWORD='replication_password',
  MASTER_AUTO_POSITION=1;
START SLAVE;
"

# Check slave status
echo "Checking slave1 status..."
docker exec mysql-slave1 mysql -u root -prootpassword -e "SHOW SLAVE STATUS\G" | grep -E "(Slave_IO_Running|Slave_SQL_Running|Master_Host)"

echo "Checking slave2 status..."
docker exec mysql-slave2 mysql -u root -prootpassword -e "SHOW SLAVE STATUS\G" | grep -E "(Slave_IO_Running|Slave_SQL_Running|Master_Host)"

echo "MySQL Cluster setup completed!"
echo ""
echo "Connection Information:"
echo "- Master (Read/Write): localhost:3306"
echo "- Slave1 (Read Only): localhost:3307"
echo "- Slave2 (Read Only): localhost:3308"
echo "- MaxScale Read/Write Split: localhost:4006"
echo "- MaxScale Read Only: localhost:4008"
echo "- MaxScale Admin Interface: localhost:8989"
echo ""
echo "Test the setup with:"
echo "mysql -h 127.0.0.1 -P 4006 -u testuser -ptestpass testdb"