#!/bin/bash

# A script to generate 10 test users for the message board API.

API_URL="http://localhost:8080/api/v1/users/register"

# Define 10 test users with consistent password
users=(
  '{"username": "alice", "password": "123", "email": "alice@example.com"}'
  '{"username": "bob", "password": "123", "email": "bob@example.com"}'
  '{"username": "charlie", "password": "123", "email": "charlie@example.com"}'
  '{"username": "diana", "password": "123", "email": "diana@example.com"}'
  '{"username": "eve", "password": "123", "email": "eve@example.com"}'
  '{"username": "frank", "password": "123", "email": "frank@example.com"}'
  '{"username": "grace", "password": "123", "email": "grace@example.com"}'
  '{"username": "henry", "password": "123", "email": "henry@example.com"}'
  '{"username": "iris", "password": "123", "email": "iris@example.com"}'
  '{"username": "jack", "password": "123", "email": "jack@example.com"}'
)

echo "=== 創建 10 個測試用戶 ==="
echo ""

for i in "${!users[@]}"; do
  user="${users[$i]}"
  user_num=$((i + 1))
  
  echo "[$user_num/10] Creating user..."
  response=$(curl -s -X POST -H "Content-Type: application/json" -d "$user" "$API_URL")
  
  # Extract username from JSON for display
  username=$(echo "$user" | grep -o '"username": *"[^"]*"' | cut -d'"' -f4)
  
  if [[ $response == *"id"* ]]; then
    echo "✅ User '$username' created successfully"
  else
    echo "❌ Failed to create user '$username'"
    echo "Response: $response"
  fi
  
  sleep 0.1
done

echo ""
echo "=== 用戶創建完成 ==="
echo "總共創建了 10 個測試用戶，統一密碼：123"
echo ""
echo "可用的測試帳號："
echo "• alice / 123"
echo "• bob / 123" 
echo "• charlie / 123"
echo "• diana / 123"
echo "• eve / 123"
echo "• frank / 123"
echo "• grace / 123"
echo "• henry / 123"
echo "• iris / 123"
echo "• jack / 123"