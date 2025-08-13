#!/bin/bash

# MySQL Failover Test Script
# This script simulates various failover scenarios

set -e

echo "MySQL Failover Test Script"
echo "=========================="

# Function to check cluster status
check_cluster_status() {
    echo "Checking cluster status..."
    docker exec maxscale maxctrl list servers
    echo ""
}

# Function to insert test data
insert_test_data() {
    local message="$1"
    echo "Inserting test data: $message"
    docker exec mysql-master mysql -u root -prootpassword testdb -e "
    INSERT INTO test_replication (message) VALUES ('$message');
    " 2>/dev/null || echo "Failed to insert data (master might be down)"
    echo ""
}

# Function to check data consistency
check_data_consistency() {
    echo "Checking data consistency across all nodes..."
    
    echo "Master data count:"
    docker exec mysql-master mysql -u root -prootpassword testdb -e "SELECT COUNT(*) as count FROM test_replication;" 2>/dev/null || echo "Master is down"
    
    echo "Slave1 data count:"
    docker exec mysql-slave1 mysql -u root -prootpassword testdb -e "SELECT COUNT(*) as count FROM test_replication;" 2>/dev/null || echo "Slave1 is down"
    
    echo "Slave2 data count:"
    docker exec mysql-slave2 mysql -u root -prootpassword testdb -e "SELECT COUNT(*) as count FROM test_replication;" 2>/dev/null || echo "Slave2 is down"
    echo ""
}

echo "Starting failover tests..."
echo ""

# Initial status
echo "=== Initial Cluster Status ==="
check_cluster_status
insert_test_data "Before failover test"
check_data_consistency

# Test 1: Master failover
echo "=== Test 1: Master Failover ==="
echo "Stopping master node..."
docker stop mysql-master
sleep 10

echo "Cluster status after master failure:"
check_cluster_status

echo "Waiting for failover to complete..."
sleep 20
check_cluster_status

# Test 2: Restart master
echo "=== Test 2: Master Recovery ==="
echo "Restarting master node..."
docker start mysql-master
sleep 30

echo "Cluster status after master recovery:"
check_cluster_status

# Test 3: Slave failover
echo "=== Test 3: Slave Failover ==="
echo "Stopping slave1..."
docker stop mysql-slave1
sleep 10

echo "Cluster status after slave1 failure:"
check_cluster_status

insert_test_data "After slave1 failure"
check_data_consistency

echo "Restarting slave1..."
docker start mysql-slave1
sleep 20

echo "Final cluster status:"
check_cluster_status
check_data_consistency

echo ""
echo "Failover tests completed!"
echo ""
echo "Useful commands for manual testing:"
echo "- Check MaxScale status: docker exec maxscale maxctrl list servers"
echo "- Check replication status: docker exec mysql-slave1 mysql -u root -prootpassword -e \"SHOW SLAVE STATUS\\G\""
echo "- Connect via MaxScale: mysql -h 127.0.0.1 -P 4006 -u testuser -ptestpass testdb"
echo "- MaxScale admin interface: http://localhost:8989 (admin/mariadb)"