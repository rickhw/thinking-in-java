#!/bin/bash

# MySQL 複製測試腳本

set -e

echo "🧪 MySQL 複製功能測試"
echo "===================="

# 在 Master 插入測試數據
echo ""
echo "📝 在 Master 節點插入測試數據..."
TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')
TEST_MESSAGE="Test data inserted at $TIMESTAMP"

docker exec mysql-master mysql -u root -prootpassword testdb -e "
CREATE TABLE IF NOT EXISTS replication_test (
    id INT AUTO_INCREMENT PRIMARY KEY,
    node VARCHAR(50),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    message VARCHAR(255)
);

INSERT INTO replication_test (node, message) VALUES ('master', '$TEST_MESSAGE');
"

echo "✅ 測試數據已插入 Master"

# 等待複製
echo ""
echo "⏳ 等待數據複製到 Slave 節點..."
sleep 3

# 檢查 Slave1
echo ""
echo "🔍 檢查 Slave1 數據同步："
echo "------------------------"
SLAVE1_DATA=$(docker exec mysql-slave1 mysql -u root -prootpassword testdb -e "
SELECT * FROM replication_test ORDER BY id DESC LIMIT 1;
" 2>/dev/null || echo "ERROR")

if [[ "$SLAVE1_DATA" == *"$TEST_MESSAGE"* ]]; then
    echo "✅ Slave1 數據同步成功"
    echo "$SLAVE1_DATA"
else
    echo "❌ Slave1 數據同步失敗"
    echo "Slave1 複製狀態："
    docker exec mysql-slave1 mysql -u root -prootpassword -e "SHOW SLAVE STATUS\G" | grep -E "(Slave_IO_Running|Slave_SQL_Running|Last_Error)"
fi

# 檢查 Slave2
echo ""
echo "🔍 檢查 Slave2 數據同步："
echo "------------------------"
SLAVE2_DATA=$(docker exec mysql-slave2 mysql -u root -prootpassword testdb -e "
SELECT * FROM replication_test ORDER BY id DESC LIMIT 1;
" 2>/dev/null || echo "ERROR")

if [[ "$SLAVE2_DATA" == *"$TEST_MESSAGE"* ]]; then
    echo "✅ Slave2 數據同步成功"
    echo "$SLAVE2_DATA"
else
    echo "❌ Slave2 數據同步失敗"
    echo "Slave2 複製狀態："
    docker exec mysql-slave2 mysql -u root -prootpassword -e "SHOW SLAVE STATUS\G" | grep -E "(Slave_IO_Running|Slave_SQL_Running|Last_Error)"
fi

# 統計各節點數據量
echo ""
echo "📊 各節點數據統計："
echo "------------------"
echo "Master 總記錄數："
docker exec mysql-master mysql -u root -prootpassword testdb -e "SELECT COUNT(*) as total_records FROM replication_test;" 2>/dev/null || echo "查詢失敗"

echo "Slave1 總記錄數："
docker exec mysql-slave1 mysql -u root -prootpassword testdb -e "SELECT COUNT(*) as total_records FROM replication_test;" 2>/dev/null || echo "查詢失敗"

echo "Slave2 總記錄數："
docker exec mysql-slave2 mysql -u root -prootpassword testdb -e "SELECT COUNT(*) as total_records FROM replication_test;" 2>/dev/null || echo "查詢失敗"

# 測試 MaxScale 連接
echo ""
echo "⚖️ 測試 MaxScale 連接："
echo "----------------------"
if docker run --rm --network mysql-cluster_mysql-cluster mysql:8.0 mysql -h maxscale -P 4006 -u root -prootpassword testdb -e "SELECT 'MaxScale connection works!' as status;" 2>/dev/null; then
    echo "✅ MaxScale 連接成功"
else
    echo "❌ MaxScale 連接失敗，嘗試使用 testuser"
    if docker run --rm --network mysql-cluster_mysql-cluster mysql:8.0 mysql -h maxscale -P 4006 -u testuser -ptestpass testdb -e "SELECT 'MaxScale testuser connection works!' as status;" 2>/dev/null; then
        echo "✅ MaxScale testuser 連接成功"
    else
        echo "❌ MaxScale 所有用戶連接都失敗"
    fi
fi

echo ""
echo "🎯 測試完成！"
echo "============"
echo ""
echo "💡 提示："
echo "- 如果複製失敗，可以運行 './scripts/setup-mysql-cluster.sh' 重新設置"
echo "- 使用 phpMyAdmin 可以直觀查看各節點的數據："
echo "  * Master:  http://localhost:8080"
echo "  * Slave1:  http://localhost:8081"
echo "  * Slave2:  http://localhost:8082"