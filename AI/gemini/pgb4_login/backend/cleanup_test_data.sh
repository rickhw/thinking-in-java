#!/bin/bash

# 清理測試資料腳本

echo "🧹 清理測試資料"
echo "=================================="
echo ""

# 檢查後端服務是否運行
echo "🔍 檢查後端服務狀態..."
if ! curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "❌ 後端服務未運行！"
    echo "如果要清理資料，請先啟動後端服務"
    exit 1
fi
echo "✅ 後端服務正在運行"
echo ""

# 警告用戶
echo "⚠️  警告：此操作將刪除所有測試資料！"
echo "包括："
echo "• 所有測試用戶帳號"
echo "• 所有測試訊息"
echo "• 所有任務記錄"
echo ""

read -p "確定要繼續嗎？(輸入 'YES' 確認): " -r
if [[ $REPLY != "YES" ]]; then
    echo "❌ 操作已取消"
    exit 0
fi

echo ""
echo "🗑️  開始清理資料..."

# 定義測試用戶列表
test_users=("alice" "bob" "charlie" "diana" "eve" "frank" "grace" "henry" "iris" "jack")

# 由於沒有直接的刪除 API，我們提供幾種清理方法的說明
echo ""
echo "📋 資料清理方法："
echo ""
echo "方法 1: 重啟後端服務（推薦）"
echo "-----------------------------"
echo "如果使用 H2 記憶體資料庫："
echo "1. 停止後端服務 (Ctrl+C)"
echo "2. 重新啟動: ./gradlew bootRun"
echo "3. 所有資料將自動清空"
echo ""

echo "方法 2: 手動 SQL 清理"
echo "--------------------"
echo "如果使用 MySQL 持久化資料庫："
echo "連接到資料庫並執行："
echo "  DELETE FROM messages;"
echo "  DELETE FROM users;"
echo "  DELETE FROM tasks;"
echo ""

echo "方法 3: 使用資料庫管理工具"
echo "-------------------------"
echo "使用 phpMyAdmin, MySQL Workbench 等工具"
echo "手動清空相關資料表"
echo ""

# 嘗試通過 API 獲取一些統計資訊
echo "📊 當前資料統計："
echo "----------------"

# 獲取訊息總數（通過分頁 API）
messages_response=$(curl -s "http://localhost:8080/api/v1/messages?page=0&size=1")
if [[ $messages_response == *"totalElements"* ]]; then
    total_messages=$(echo "$messages_response" | grep -o '"totalElements":[0-9]*' | cut -d':' -f2)
    echo "• 總訊息數: $total_messages"
else
    echo "• 無法獲取訊息統計"
fi

# 檢查測試用戶是否存在
echo "• 測試用戶檢查:"
for user in "${test_users[@]}"; do
    user_messages=$(curl -s "http://localhost:8080/api/v1/users/$user/messages?page=0&size=1")
    if [[ $user_messages == *"totalElements"* ]]; then
        user_msg_count=$(echo "$user_messages" | grep -o '"totalElements":[0-9]*' | cut -d':' -f2)
        echo "  - $user: $user_msg_count 條訊息"
    else
        echo "  - $user: 用戶不存在或無訊息"
    fi
done

echo ""
echo "💡 建議："
echo "如果要完全重新開始，最簡單的方法是重啟後端服務"
echo "然後重新執行 ./setup_test_data.sh"