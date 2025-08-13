#!/bin/bash

# MySQL 複製修復腳本

set -e

echo "🔧 修復 MySQL 複製問題"
echo "===================="

echo ""
echo "📋 停止所有 Slave 複製..."
docker exec mysql-slave1 mysql -u root -prootpassword -e "STOP SLAVE;" 2>/dev/null || true
docker exec mysql-slave2 mysql -u root -prootpassword -e "STOP SLAVE;" 2>/dev/null || true

echo ""
echo "🗑️ 重置 Slave 配置..."
docker exec mysql-slave1 mysql -u root -prootpassword -e "
RESET SLAVE ALL;
RESET MASTER;
" 2>/dev/null || true

docker exec mysql-slave2 mysql -u root -prootpassword -e "
RESET SLAVE ALL;
RESET MASTER;
" 2>/dev/null || true

echo ""
echo "🔄 重置 Master binlog..."
docker exec mysql-master mysql -u root -prootpassword -e "
RESET MASTER;
" 2>/dev/null || true

echo ""
echo "⏳ 等待服務穩定..."
sleep 5

echo ""
echo "🔗 重新配置複製關係..."
docker exec mysql-slave1 mysql -u root -prootpassword -e "
CHANGE MASTER TO
  MASTER_HOST='mysql-master',
  MASTER_USER='replication',
  MASTER_PASSWORD='replication_password',
  MASTER_AUTO_POSITION=1;
START SLAVE;
" 2>/dev/null || echo "❌ Slave1 配置失敗"

docker exec mysql-slave2 mysql -u root -prootpassword -e "
CHANGE MASTER TO
  MASTER_HOST='mysql-master',
  MASTER_USER='replication',
  MASTER_PASSWORD='replication_password',
  MASTER_AUTO_POSITION=1;
START SLAVE;
" 2>/dev/null || echo "❌ Slave2 配置失敗"

echo ""
echo "⏳ 等待複製啟動..."
sleep 10

echo ""
echo "📊 檢查複製狀態..."
echo "Slave1 狀態："
docker exec mysql-slave1 mysql -u root -prootpassword -e "SHOW SLAVE STATUS\G" 2>/dev/null | grep -E "(Slave_IO_Running|Slave_SQL_Running|Last_Error)" | head -3

echo ""
echo "Slave2 狀態："
docker exec mysql-slave2 mysql -u root -prootpassword -e "SHOW SLAVE STATUS\G" 2>/dev/null | grep -E "(Slave_IO_Running|Slave_SQL_Running|Last_Error)" | head -3

echo ""
echo "🧪 測試複製功能..."
TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')
docker exec mysql-master mysql -u root -prootpassword testdb -e "
CREATE TABLE IF NOT EXISTS replication_fix_test (
    id INT AUTO_INCREMENT PRIMARY KEY,
    message VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO replication_fix_test (message) VALUES ('Replication test at $TIMESTAMP');
" 2>/dev/null || echo "❌ Master 測試失敗"

echo ""
echo "⏳ 等待數據同步..."
sleep 5

echo ""
echo "🔍 檢查數據同步結果..."
echo "Master 數據："
docker exec mysql-master mysql -u root -prootpassword testdb -e "SELECT * FROM replication_fix_test ORDER BY id DESC LIMIT 1;" 2>/dev/null || echo "❌ Master 查詢失敗"

echo ""
echo "Slave1 數據："
docker exec mysql-slave1 mysql -u root -prootpassword testdb -e "SELECT * FROM replication_fix_test ORDER BY id DESC LIMIT 1;" 2>/dev/null || echo "❌ Slave1 查詢失敗"

echo ""
echo "Slave2 數據："
docker exec mysql-slave2 mysql -u root -prootpassword testdb -e "SELECT * FROM replication_fix_test ORDER BY id DESC LIMIT 1;" 2>/dev/null || echo "❌ Slave2 查詢失敗"

echo ""
echo "✅ 複製修復完成！"
echo "================"
echo ""
echo "💡 如果複製仍有問題，可能需要："
echo "1. 檢查網絡連接"
echo "2. 檢查用戶權限"
echo "3. 查看詳細錯誤日誌"
echo ""
echo "🌐 使用 phpMyAdmin 查看結果："
echo "- Master:  http://localhost:8080"
echo "- Slave1:  http://localhost:8081"
echo "- Slave2:  http://localhost:8082"