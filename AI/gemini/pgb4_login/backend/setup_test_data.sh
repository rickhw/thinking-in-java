#!/bin/bash

# 整合腳本：創建測試用戶並生成訊息資料

echo "🚀 開始設置測試資料..."
echo "=================================="
echo ""

# 檢查後端服務是否運行
echo "🔍 檢查後端服務狀態..."
if ! curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "❌ 後端服務未運行！"
    echo "請先啟動後端服務："
    echo "  cd backend && ./gradlew bootRun"
    exit 1
fi
echo "✅ 後端服務正在運行"
echo ""

# 詢問用戶是否要清除現有資料
read -p "⚠️  是否要清除現有的測試資料？(y/N): " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🗑️  清除現有資料功能需要手動執行 SQL 或重啟服務"
    echo "   建議：重新啟動後端服務以清空記憶體資料"
    echo ""
fi

# 步驟 1: 創建測試用戶
echo "📝 步驟 1: 創建測試用戶"
echo "------------------------"
if [ -f "./generate_users.sh" ]; then
    chmod +x ./generate_users.sh
    ./generate_users.sh
    if [ $? -eq 0 ]; then
        echo "✅ 用戶創建完成"
    else
        echo "❌ 用戶創建失敗"
        exit 1
    fi
else
    echo "❌ 找不到 generate_users.sh 腳本"
    exit 1
fi

echo ""
echo "⏳ 等待 3 秒讓服務處理用戶創建..."
sleep 3

# 步驟 2: 生成測試訊息
echo "💬 步驟 2: 生成測試訊息"
echo "------------------------"
if [ -f "./generate_posts.sh" ]; then
    chmod +x ./generate_posts.sh
    ./generate_posts.sh
    if [ $? -eq 0 ]; then
        echo "✅ 訊息生成完成"
    else
        echo "❌ 訊息生成失敗"
        exit 1
    fi
else
    echo "❌ 找不到 generate_posts.sh 腳本"
    exit 1
fi

echo ""
echo "🎉 測試資料設置完成！"
echo "=================================="
echo ""
echo "📊 資料摘要："
echo "• 用戶數量: 10 個"
echo "• 每用戶訊息: 50-100 條（隨機）"
echo "• 預估總訊息: 500-1000 條"
echo ""
echo "🔑 測試帳號（統一密碼：password123）："
echo "alice, bob, charlie, diana, eve"
echo "frank, grace, henry, iris, jack"
echo ""
echo "🌐 現在可以訪問前端應用："
echo "http://localhost:5174"
echo ""
echo "💡 建議測試流程："
echo "1. 使用任一測試帳號登入"
echo "2. 瀏覽首頁查看所有訊息"
echo "3. 點擊用戶名查看個別用戶訊息"
echo "4. 測試發布、編輯、刪除功能"