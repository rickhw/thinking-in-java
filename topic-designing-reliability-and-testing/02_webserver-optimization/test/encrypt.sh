#!/bin/bash

if [ $# -lt 3 ]; then
  echo "Usage: $0 <server_url> <file_path> <loop_count>"
  echo "Example: $0 http://localhost:8080/api/encryption/encrypt ./genfile_1G.bin 10"
  exit 1
fi

SERVER_URL=$1
FILE_PATH=$2
LOOP_COUNT=$3

# 確保檔案存在
if [ ! -f "$FILE_PATH" ]; then
  echo "File not found: $FILE_PATH"
  exit 1
fi

# 設置每秒發送請求的次數
REQUEST_INTERVAL=1 # 每秒一次

ulimit -n 65535

# 取得當前時間戳
START_TIME=$(date +%s) # 毫秒精度
echo "Started at: ${START_TIME}"

# 根據指定的迴圈數進行請求
for ((i = 1; i <= LOOP_COUNT; i++)); do
  # 紀錄當前請求的開始時間
  REQUEST_START_TIME=$(date +%s)

  # 發送 curl 請求
  curl -F "file=@${FILE_PATH}" ${SERVER_URL} -o encrypted_file.enc

  # 記錄當前請求的結束時間
  REQUEST_END_TIME=$(date +%s)
  echo "Request ${i} completed at: ${REQUEST_END_TIME}"

  # 計算每次請求的耗時
  REQUEST_TIME=$((REQUEST_END_TIME - REQUEST_START_TIME))
  echo "Request ${i} upload time: ${REQUEST_TIME} s"

  # 等待 1 秒，然後再次發送請求
  sleep 1
done

# 記錄總的上傳時間
END_TIME=$(date +%s)
echo "Finished at: ${END_TIME}"

# 計算總耗時
TOTAL_TIME=$((END_TIME - START_TIME))
echo "Total upload time: ${TOTAL_TIME} ms"
