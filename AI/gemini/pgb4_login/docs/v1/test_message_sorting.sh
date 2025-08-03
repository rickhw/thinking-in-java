#!/bin/bash

# 測試訊息排序功能的腳本

API_URL="http://localhost:8080/api/v1"

echo "=== 測試訊息排序功能 ==="
echo ""

# 創建幾條測試訊息，間隔一秒以確保時間戳不同
echo "1. 創建測試訊息..."

echo "創建第一條訊息..."
curl -s -X POST -H "Content-Type: application/json" \
  -d '{"userId": "testuser", "content": "第一條訊息 - 應該在最後"}' \
  "$API_URL/messages"
echo ""

sleep 1

echo "創建第二條訊息..."
curl -s -X POST -H "Content-Type: application/json" \
  -d '{"userId": "testuser", "content": "第二條訊息 - 應該在中間"}' \
  "$API_URL/messages"
echo ""

sleep 1

echo "創建第三條訊息..."
curl -s -X POST -H "Content-Type: application/json" \
  -d '{"userId": "testuser", "content": "第三條訊息 - 應該在最前面"}' \
  "$API_URL/messages"
echo ""

echo "等待非同步處理完成..."
sleep 3

echo ""
echo "2. 測試所有訊息的排序..."
echo "獲取所有訊息（應該按創建時間倒序排列）："
curl -s "$API_URL/messages?page=0&size=10" | jq '.content[] | {id, userId, content, createdAt}' | head -20

echo ""
echo "3. 測試特定用戶訊息的排序..."
echo "獲取 testuser 的訊息（應該按創建時間倒序排列）："
curl -s "$API_URL/users/testuser/messages?page=0&size=10" | jq '.content[] | {id, userId, content, createdAt}' | head -20

echo ""
echo "=== 測試完成 ==="
echo "如果排序正確，最新的訊息（第三條）應該顯示在最前面"