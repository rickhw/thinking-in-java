#!/bin/bash

if [ $# -lt 2 ]; then
  echo "Usage: $0 <server_url> <file_path>"
  echo "Example: $0 http://localhost:8080/api/encryption/encrypt ./genfile_1G_01-05-09-06.bin"
  exit 1
fi

SERVER_URL=$1
FILE_PATH=$2

# 確保檔案存在
if [ ! -f "$FILE_PATH" ]; then
  echo "File not found: $FILE_PATH"
  exit 1
fi


#START_TIME=$(date +%s%3N) # 毫秒精度
START_TIME=$(date +%s%3) # 毫秒精度
echo "started at: ${START_TIME}"

curl -F "file=@${FILE_PATH}" \
    ${SERVER_URL} \
    -o encrypted_file.enc


# 記錄上傳完成時間
END_TIME=$(date +%s%3)
echo "finished at: ${END_TIME}"

# 計算總耗時
TOTAL_TIME=$((END_TIME - START_TIME))
echo "Total upload time: ${TOTAL_TIME} ms"