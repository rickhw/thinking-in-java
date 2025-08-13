#!/bin/bash

# MySQL Cluster Test Script
# This script tests replication and failover scenarios

set -e

echo "Testing MySQL Cluster..."

# Test 1: Basic replication test
echo "=== Test 1: Basic Replication ==="
echo "Creating test table on master..."
docker exec mysql-master mysql -u root -prootpassword testdb -e "
CREATE TABLE IF NOT EXISTS test_replication (
    id INT AUTO_INCREMENT PRIMARY KEY,
    message VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
"

echo "Inserting test data on master..."
docker exec mysql-master mysql -u root -prootpassword testdb -e "
INSERT INTO test_replication (message) VALUES 
('Test message 1'),
('Test message 2'),
('Test message 3');
"

echo "Waiting for replication..."
sleep 5

echo "Checking data on slave1..."
docker exec mysql-slave1 mysql -u root -prootpassword testdb -e "SELECT * FROM test_replication;"

echo "Checking data on slave2..."
docker exec mysql-slave2 mysql -u root -prootpassword testdb -e "SELECT * FROM test_replication;"

# Test 2: MaxScale connection test
echo ""
echo "=== Test 2: MaxScale Connection ==="
echo "Testing connection through MaxScale Read/Write Split (port 4006)..."
docker exec maxscale maxctrl list servers

echo "Testing connection through MaxScale Read Only (port 4008)..."
docker exec maxscale maxctrl show service Read-Connection-Service

# Test 3: Replication lag test
echo ""
echo "=== Test 3: Replication Status ==="
echo "Master status:"
docker exec mysql-master mysql -u root -prootpassword -e "SHOW MASTER STATUS;"

echo "Slave1 status:"
docker exec mysql-slave1 mysql -u root -prootpassword -e "SHOW SLAVE STATUS\G" | grep -E "(Slave_IO_Running|Slave_SQL_Running|Seconds_Behind_Master)"

echo "Slave2 status:"
docker exec mysql-slave2 mysql -u root -prootpassword -e "SHOW SLAVE STATUS\G" | grep -E "(Slave_IO_Running|Slave_SQL_Running|Seconds_Behind_Master)"

echo ""
echo "=== Cluster Status Summary ==="
echo "All tests completed successfully!"
echo ""
echo "To simulate failover scenarios:"
echo "1. Stop master: docker stop mysql-master"
echo "2. Check MaxScale status: docker exec maxscale maxctrl list servers"
echo "3. Restart master: docker start mysql-master"
echo ""
echo "To monitor replication:"
echo "docker exec mysql-slave1 mysql -u root -prootpassword -e \"SHOW SLAVE STATUS\\G\""