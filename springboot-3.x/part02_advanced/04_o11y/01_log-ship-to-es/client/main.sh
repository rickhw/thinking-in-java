#!/bin/bash

# API 端點
URL="http://localhost:8080/api/logs"

# 事件列表
EVENTS=(
    '{"event": "login", "user": "user1"}'
    '{"event": "logout", "user": "user2"}'
    '{"event": "purchase", "user": "user3", "amount": "30"}'
)

# 無窮迴圈
while true; do
    # 從事件中隨機選擇
    PAYLOAD=${EVENTS[$RANDOM % ${#EVENTS[@]}]}
    
    # 發送 POST 請求
    RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -d "$PAYLOAD" "$URL")
    
    # 輸出請求和回應
    echo "Sent: $PAYLOAD, Response: $RESPONSE"
    
    # 隨機暫停 1-3 秒
    SLEEP_TIME=$((RANDOM % 1 + 1))
    sleep "$SLEEP_TIME"
done
