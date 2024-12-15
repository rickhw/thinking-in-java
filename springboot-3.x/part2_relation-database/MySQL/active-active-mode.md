MySQL 8.0 supports an **Active-Active architecture** through the **Group Replication plugin**. This feature allows you to create highly available clusters where multiple nodes can process write and read requests simultaneously. Group Replication provides built-in conflict detection and resolution, ensuring data consistency across nodes.

Here’s a **docker-compose configuration** for setting up an Active-Active MySQL cluster using Group Replication locally:

### Docker-Compose Configuration
```yaml
version: '3.8'

services:
  mysql1:
    image: mysql:8.0
    container_name: mysql1
    environment:
      MYSQL_ROOT_PASSWORD: rootpass
      MYSQL_ROOT_HOST: '%'
      MYSQL_DATABASE: testdb
    volumes:
      - ./mysql1:/var/lib/mysql
      - ./config/mysql.cnf:/etc/mysql/mysql.conf.d/mysqld.cnf
    ports:
      - "3306:3306"
    networks:
      - mysql-cluster

  mysql2:
    image: mysql:8.0
    container_name: mysql2
    environment:
      MYSQL_ROOT_PASSWORD: rootpass
      MYSQL_ROOT_HOST: '%'
    volumes:
      - ./mysql2:/var/lib/mysql
      - ./config/mysql.cnf:/etc/mysql/mysql.conf.d/mysqld.cnf
    ports:
      - "3307:3306"
    networks:
      - mysql-cluster

networks:
  mysql-cluster:
    driver: bridge
```

### Configuration File (`mysql.cnf`)
Place this file in the `./config/` directory:
```ini
[mysqld]
server-id=1  # For mysql2, use 2 or a unique ID for each instance
log-bin=mysql-bin
binlog_format=ROW
default_authentication_plugin=mysql_native_password
plugin-load-add=group_replication.so
group_replication_start_on_boot=OFF
group_replication_bootstrap_group=OFF
group_replication_group_name="aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"
group_replication_local_address="mysql1:33061"  # Change for other instances
group_replication_group_seeds="mysql1:33061,mysql2:33062"
```

### Steps to Verify
1. Start the containers:
   ```bash
   docker-compose up -d
   ```

2. Log in to each MySQL instance:
   ```bash
   docker exec -it mysql1 mysql -u root -p
   ```

3. Configure Group Replication:
   ```sql
   SET GLOBAL group_replication_bootstrap_group=ON;
   START GROUP_REPLICATION;
   SET GLOBAL group_replication_bootstrap_group=OFF;
   ```

4. Verify the cluster status:
   ```sql
   SELECT * FROM performance_schema.replication_group_members;
   ```

### Validation Process
- Use **different clients** to connect to each MySQL node and perform simultaneous write and read operations.
- Query `replication_group_members` table on each node to confirm synchronization.
- Simulate failure of one node by stopping a container and ensure the other node continues processing writes.

This setup demonstrates MySQL's ability to handle Active-Active workloads with conflict resolution. For production environments, additional considerations like network latency and failure scenarios need to be addressed.

For more details on Group Replication and Active-Active setup, refer to the [MySQL documentation](https://dev.mysql.com/doc/refman/8.0/en/group-replication.html) and AWS RDS support for Group Replication.