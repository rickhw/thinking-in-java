# MySQL Cluster with MaxScale

這個配置提供了一個完整的 MySQL 集群環境，包含：
- 1 個 MySQL Master 節點
- 2 個 MySQL Slave 節點  
- 1 個 MaxScale 代理服務器

## 架構說明

```
                    ┌─────────────┐
                    │  MaxScale   │
                    │   Proxy     │
                    └─────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
   ┌─────────┐       ┌─────────┐       ┌─────────┐
   │ Master  │────── │ Slave1  │       │ Slave2  │
   │  :3306  │       │  :3307  │       │  :3308  │
   └─────────┘       └─────────┘       └─────────┘
```

## 快速開始

### 1. 啟動 MySQL 集群
```bash
# 啟動 MySQL 節點
docker-compose -f docker-compose.mysql-only.yml up -d

# 等待服務啟動完成
sleep 30

# 設置複製關係
./scripts/setup-mysql-cluster.sh
```

### 2. 啟動 MaxScale
```bash
# 啟動 MaxScale 代理
docker-compose -f docker-compose.maxscale.yml up -d

# 檢查 MaxScale 狀態
docker exec maxscale maxctrl list servers
```

### 3. 測試集群
```bash
# 測試 MaxScale 連接
mysql -h 127.0.0.1 -P 4006 -u testuser -ptestpass testdb

# 手動測試複製
docker exec mysql-master mysql -u root -prootpassword testdb -e "
CREATE TABLE test_table (id INT, message VARCHAR(100));
INSERT INTO test_table VALUES (1, 'Hello World');
"

# 檢查 slave 是否同步
docker exec mysql-slave1 mysql -u root -prootpassword testdb -e "SELECT * FROM test_table;"
```

## 連接信息

| 服務 | 端口 | 用途 | 連接字符串 |
|------|------|------|------------|
| MySQL Master | 3306 | 讀寫 | `mysql -h 127.0.0.1 -P 3306 -u testuser -ptestpass testdb` |
| MySQL Slave1 | 3307 | 只讀 | `mysql -h 127.0.0.1 -P 3307 -u testuser -ptestpass testdb` |
| MySQL Slave2 | 3308 | 只讀 | `mysql -h 127.0.0.1 -P 3308 -u testuser -ptestpass testdb` |
| MaxScale R/W | 4006 | 讀寫分離 | `mysql -h 127.0.0.1 -P 4006 -u testuser -ptestpass testdb` |
| MaxScale RO | 4008 | 只讀 | `mysql -h 127.0.0.1 -P 4008 -u testuser -ptestpass testdb` |
| MaxScale Admin | 8989 | 管理界面 | http://localhost:8989 (admin/mariadb) |

## 測試場景

### 1. 數據同步測試
```bash
# 在 master 插入數據
docker exec mysql-master mysql -u root -prootpassword testdb -e "
INSERT INTO test_replication (message) VALUES ('Test sync');
"

# 檢查 slave 是否同步
docker exec mysql-slave1 mysql -u root -prootpassword testdb -e "
SELECT * FROM test_replication ORDER BY id DESC LIMIT 1;
"
```

### 2. 故障轉移測試
```bash
# 停止 master
docker stop mysql-master

# 檢查 MaxScale 狀態
docker exec maxscale maxctrl list servers

# 重啟 master
docker start mysql-master
```

### 3. 負載均衡測試
```bash
# 通過 MaxScale 讀寫分離端口連接
mysql -h 127.0.0.1 -P 4006 -u testuser -ptestpass testdb

# 寫操作會路由到 master
INSERT INTO test_replication (message) VALUES ('Load balance test');

# 讀操作會路由到 slave
SELECT * FROM test_replication;
```

## 監控命令

### MaxScale 狀態
```bash
# 查看服務器狀態
docker exec maxscale maxctrl list servers

# 查看服務狀態
docker exec maxscale maxctrl list services

# 查看監控狀態
docker exec maxscale maxctrl list monitors
```

### MySQL 複製狀態
```bash
# Master 狀態
docker exec mysql-master mysql -u root -prootpassword -e "SHOW MASTER STATUS;"

# Slave 狀態
docker exec mysql-slave1 mysql -u root -prootpassword -e "SHOW SLAVE STATUS\G"
docker exec mysql-slave2 mysql -u root -prootpassword -e "SHOW SLAVE STATUS\G"
```

## 故障排除

### 複製問題
```bash
# 重置 slave
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
```

### MaxScale 問題
```bash
# 重啟 MaxScale
docker restart maxscale

# 查看 MaxScale 日誌
docker logs maxscale
```

## 清理環境

```bash
# 停止並刪除容器
docker-compose -f docker-compose.mysql-cluster.yml down

# 刪除數據卷（注意：這會刪除所有數據）
docker volume rm $(docker volume ls -q | grep mysql)
```

## 配置文件說明

- `docker-compose.mysql-cluster.yml`: 主要的 Docker Compose 配置
- `config/maxscale/maxscale.cnf`: MaxScale 配置文件
- `scripts/setup-mysql-cluster.sh`: 集群初始化腳本
- `scripts/test-mysql-cluster.sh`: 基本測試腳本
- `scripts/failover-test.sh`: 故障轉移測試腳本

這個配置可以讓你完整地測試 MySQL 主從複製、數據同步、故障轉移和負載均衡等功能。
---


## 當前部署狀態

### ✅ 已完成
- MySQL Master 節點運行正常 (端口 3306)
- MySQL Slave 節點運行正常 (端口 3307, 3308)
- MaxScale 代理運行正常 (端口 4006, 4008, 8989)
- MaxScale 能正確檢測所有 MySQL 節點狀態
- 基本的複製用戶和 MaxScale 用戶已創建

### ⚠️ 需要注意
- MySQL 複製可能需要手動重置 (GTID 衝突)
- MaxScale 的客戶端認證需要進一步調整
- 建議先通過直接端口連接測試各個節點

### 🔧 故障排除
如果遇到複製問題，可以重置 slave：
```bash
docker exec mysql-slave1 mysql -u root -prootpassword -e "
STOP SLAVE;
RESET SLAVE ALL;
RESET MASTER;
CHANGE MASTER TO
  MASTER_HOST='mysql-master',
  MASTER_USER='replication',
  MASTER_PASSWORD='replication_password',
  MASTER_AUTO_POSITION=1;
START SLAVE;
"
```

### 📊 監控命令
```bash
# 檢查 MaxScale 狀態
docker exec maxscale maxctrl list servers

# 檢查複製狀態
docker exec mysql-slave1 mysql -u root -prootpassword -e "SHOW SLAVE STATUS\G"

# 檢查容器狀態
docker ps
```

這個配置為你提供了一個完整的 MySQL 集群環境，可以用來模擬各種數據同步和故障轉移場景。