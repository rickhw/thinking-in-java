# 項目說明

這個項目包含多個不同的組件和示例。

## 📁 目錄結構

### mysql-cluster/
完整的 MySQL 集群環境，包含：
- 1 個 MySQL Master 節點
- 2 個 MySQL Slave 節點  
- MaxScale 代理服務器
- phpMyAdmin Web 管理界面

**快速開始：**
```bash
cd mysql-cluster
./scripts/start-full-cluster.sh
```

**Web 界面：**
- Master phpMyAdmin: http://localhost:8080
- Slave1 phpMyAdmin: http://localhost:8081
- Slave2 phpMyAdmin: http://localhost:8082
- MaxScale Admin: http://localhost:8989

詳細說明請查看 [mysql-cluster/README.md](mysql-cluster/README.md)

### 其他文件
- `docker-compose.yml` - 主要的應用服務
- `docker-compose.prod.yml` - 生產環境配置
- `docker-compose.override.yml` - 開發環境覆蓋配置

## 🚀 使用說明

1. **MySQL 集群測試**：進入 `mysql-cluster/` 目錄並按照 README 操作
2. **主應用**：使用根目錄的 docker-compose 文件

## 📋 功能特性

- ✅ MySQL 主從複製集群
- ✅ MaxScale 讀寫分離和故障轉移
- ✅ phpMyAdmin Web 管理界面
- ✅ 自動化部署和監控腳本
- ✅ 完整的測試和故障轉移場景

## 🔧 技術棧

- **數據庫**: MySQL 8.0
- **代理**: MariaDB MaxScale
- **管理界面**: phpMyAdmin
- **容器化**: Docker & Docker Compose
- **監控**: 自定義腳本

## 📖 更多信息

每個組件都有詳細的 README 文件，請查看相應目錄獲取更多信息。