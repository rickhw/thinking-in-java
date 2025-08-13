#!/bin/bash

# MySQL Cluster 監控腳本

set -e

echo "📊 MySQL Cluster 狀態監控"
echo "========================="

# 檢查容器狀態
echo ""
echo "🐳 容器運行狀態："
echo "----------------"
docker ps --filter "name=mysql\|maxscale\|phpmyadmin" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# 檢查 MaxScale 狀態
echo ""
echo "⚖️ MaxScale 服務器狀態："
echo "----------------------"
if docker exec maxscale maxctrl list servers 2>/dev/null; then
    echo ""
    echo "📈 MaxScale 服務狀態："
    echo "--------------------"
    docker exec maxscale maxctrl list services 2>/dev/null
else
    echo "❌ MaxScale 無法連接或未啟動"
fi

# 檢查 MySQL 複製狀態
echo ""
echo "🔄 MySQL 複製狀態："
echo "------------------"

echo "Master 狀態："
if docker exec mysql-master mysql -u root -prootpassword -e "SHOW MASTER STATUS;" 2>/dev/null; then
    echo "✅ Master 正常"
else
    echo "❌ Master 連接失敗"
fi

echo ""
echo "Slave1 狀態："
if docker exec mysql-slave1 mysql -u root -prootpassword -e "SHOW SLAVE STATUS\G" 2>/dev/null | grep -E "(Slave_IO_Running|Slave_SQL_Running|Seconds_Behind_Master)" | head -3; then
    echo "✅ Slave1 狀態已顯示"
else
    echo "❌ Slave1 連接失敗"
fi

echo ""
echo "Slave2 狀態："
if docker exec mysql-slave2 mysql -u root -prootpassword -e "SHOW SLAVE STATUS\G" 2>/dev/null | grep -E "(Slave_IO_Running|Slave_SQL_Running|Seconds_Behind_Master)" | head -3; then
    echo "✅ Slave2 狀態已顯示"
else
    echo "❌ Slave2 連接失敗"
fi

# 檢查數據一致性
echo ""
echo "📋 數據一致性檢查："
echo "------------------"

# 在 master 創建測試表（如果不存在）
docker exec mysql-master mysql -u root -prootpassword testdb -e "
CREATE TABLE IF NOT EXISTS cluster_monitor (
    id INT AUTO_INCREMENT PRIMARY KEY,
    node VARCHAR(50),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    message VARCHAR(255)
);
" 2>/dev/null || echo "⚠️ 無法在 Master 創建測試表"

# 檢查各節點的數據
echo "Master 數據行數："
docker exec mysql-master mysql -u root -prootpassword testdb -e "SELECT COUNT(*) as count FROM cluster_monitor;" 2>/dev/null || echo "❌ Master 查詢失敗"

echo "Slave1 數據行數："
docker exec mysql-slave1 mysql -u root -prootpassword testdb -e "SELECT COUNT(*) as count FROM cluster_monitor;" 2>/dev/null || echo "❌ Slave1 查詢失敗"

echo "Slave2 數據行數："
docker exec mysql-slave2 mysql -u root -prootpassword testdb -e "SELECT COUNT(*) as count FROM cluster_monitor;" 2>/dev/null || echo "❌ Slave2 查詢失敗"

# Web 界面訪問信息
echo ""
echo "🌐 Web 管理界面："
echo "----------------"
echo "Master phpMyAdmin:   http://localhost:8080"
echo "Slave1 phpMyAdmin:   http://localhost:8081"
echo "Slave2 phpMyAdmin:   http://localhost:8082"
echo "MaxScale Admin:      http://localhost:8989"

echo ""
echo "🔄 監控完成！使用 'watch -n 5 ./scripts/monitor-cluster.sh' 可以持續監控"