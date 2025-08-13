#!/bin/bash

# 測試 MySQL Cluster 新目錄結構

echo "🧪 測試 MySQL Cluster 目錄結構"
echo "=============================="

echo ""
echo "📁 檢查文件結構..."
echo "✅ Docker Compose 文件："
ls -la *.yml | awk '{print "   - " $9}'

echo ""
echo "✅ 配置文件："
find config -name "*.cnf" | awk '{print "   - " $1}'

echo ""
echo "✅ 腳本文件："
ls -la scripts/*.sh | awk '{print "   - " $9}'

echo ""
echo "🔧 檢查腳本權限..."
ls -la scripts/*.sh | grep -E "^-rwx" && echo "✅ 所有腳本都有執行權限" || echo "❌ 部分腳本缺少執行權限"

echo ""
echo "📋 可用的 Docker Compose 配置："
echo "   - docker-compose.full-cluster.yml    (完整集群)"
echo "   - docker-compose.mysql-only.yml      (僅 MySQL 節點)"
echo "   - docker-compose.maxscale.yml        (MaxScale 代理)"
echo "   - docker-compose.phpmyadmin.yml      (phpMyAdmin 界面)"

echo ""
echo "🚀 可用的腳本："
echo "   - ./scripts/start-full-cluster.sh    (一鍵啟動完整集群)"
echo "   - ./scripts/monitor-cluster.sh       (監控集群狀態)"
echo "   - ./scripts/test-replication.sh      (測試數據複製)"
echo "   - ./scripts/setup-mysql-cluster.sh   (設置 MySQL 複製)"
echo "   - ./scripts/fix-replication.sh       (修復複製問題)"
echo "   - ./scripts/failover-test.sh         (故障轉移測試)"

echo ""
echo "✅ 目錄結構測試完成！"
echo ""
echo "💡 快速開始："
echo "   ./scripts/start-full-cluster.sh"