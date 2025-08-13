#!/bin/bash

# MySQL Cluster with phpMyAdmin 完整啟動腳本

set -e

echo "🚀 啟動 MySQL Cluster 完整環境..."
echo "=================================="

# 停止現有的容器
echo "📦 清理現有容器..."
docker-compose -f docker-compose.mysql-only.yml down 2>/dev/null || true
docker-compose -f docker-compose.maxscale.yml down 2>/dev/null || true
docker-compose -f docker-compose.phpmyadmin.yml down 2>/dev/null || true

# 啟動完整集群
echo "🔄 啟動完整集群..."
docker-compose -f docker-compose.full-cluster.yml up -d

# 等待 MySQL 服務啟動
echo "⏳ 等待 MySQL 服務啟動..."
sleep 30

# 設置複製關係
echo "🔗 設置 MySQL 複製關係..."
./scripts/setup-mysql-cluster.sh

# 等待 MaxScale 啟動
echo "⏳ 等待 MaxScale 啟動..."
sleep 10

# 檢查服務狀態
echo "📊 檢查服務狀態..."
echo ""
echo "MySQL 節點狀態："
docker ps --filter "name=mysql" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo ""
echo "MaxScale 狀態："
docker exec maxscale maxctrl list servers 2>/dev/null || echo "MaxScale 還在啟動中..."

echo ""
echo "phpMyAdmin 狀態："
docker ps --filter "name=phpmyadmin" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo ""
echo "🎉 集群啟動完成！"
echo "=================="
echo ""
echo "📱 Web 管理界面："
echo "- Master phpMyAdmin:  http://localhost:8080"
echo "- Slave1 phpMyAdmin:  http://localhost:8081"
echo "- Slave2 phpMyAdmin:  http://localhost:8082"
echo "- MaxScale Admin:     http://localhost:8989"
echo ""
echo "🔌 數據庫連接："
echo "- Master (讀寫):      localhost:3306"
echo "- Slave1 (只讀):      localhost:3307"
echo "- Slave2 (只讀):      localhost:3308"
echo "- MaxScale (讀寫分離): localhost:4006"
echo "- MaxScale (只讀):    localhost:4008"
echo ""
echo "🔑 登錄信息："
echo "- MySQL root 密碼:    rootpassword"
echo "- MySQL 用戶:         testuser / testpass"
echo "- MaxScale Admin:     admin / mariadb"
echo ""
echo "💡 使用提示："
echo "1. 通過 phpMyAdmin 可以直觀地查看每個節點的數據"
echo "2. 在 Master 節點創建數據，觀察 Slave 節點的同步情況"
echo "3. 使用 MaxScale Admin 界面監控集群狀態"
echo "4. 可以停止任意節點測試故障轉移"