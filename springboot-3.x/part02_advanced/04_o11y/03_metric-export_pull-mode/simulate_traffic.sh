#!/bin/bash

# Task API endpoints
API_BASE_URL="http://localhost:8080/api/tasks"
TASK_IDS=(1 2 3 4 5 6 7 8 9 0) # 預設存在的 Task IDs，可根據實際情況調整

# 隨機產生 JSON Payload
generate_payload() {
  echo "{\"title\":\"Random Task $(date +%s%N)\",\"description\":\"This is a randomly generated task\"}"
}

# 隨機執行 API 請求
simulate_requests() {
  # 隨機決定請求數量
  local request_count=$((RANDOM % 2000 + 1))
  echo "Sending $request_count requests..."

  for ((i = 1; i <= request_count; i++)); do
    # 隨機選擇要執行的 API
    case $((RANDOM % 5)) in
      0)
        echo "Creating a task..."
        curl -X POST "$API_BASE_URL" \
          -H "Content-Type: application/json" \
          -d "$(generate_payload)" -s -o /dev/null
        ;;
      1)
        echo "Fetching all tasks..."
        curl "$API_BASE_URL" -s -o /dev/null
        ;;
      2)
        echo "Fetching a specific task..."
        TASK_ID=${TASK_IDS[$((RANDOM % ${#TASK_IDS[@]}))]}
        curl "$API_BASE_URL/$TASK_ID" -s -o /dev/null
        ;;
      3)
        echo "Updating a task status..."
        TASK_ID=${TASK_IDS[$((RANDOM % ${#TASK_IDS[@]}))]}
        curl -X PUT "$API_BASE_URL/$TASK_ID/status" \
          -H "Content-Type: application/json" \
          -d '"COMPLETED"' -s -o /dev/null
        ;;
      4)
        echo "Deleting a task..."
        TASK_ID=${TASK_IDS[$((RANDOM % ${#TASK_IDS[@]}))]}
        curl -X DELETE "$API_BASE_URL/$TASK_ID" -s -o /dev/null
        ;;
    esac

    # 隨機等待間隔 (100ms ~ 5s)
    local delay=$(awk "BEGIN {print $((RANDOM % 4900 + 100)) / 1000}")
    echo "Waiting for $delay seconds..."
    sleep "$delay"
  done
}

# 主程式
simulate_requests
