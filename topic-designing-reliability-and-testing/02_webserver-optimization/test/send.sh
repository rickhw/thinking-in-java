#!/bin/bash

SERVER_URL=http://localhost:8080/api/encryption/encrypt
# SERVER_URL=http://localhost:8080/api/encryption/upload
FILE_PATH=./genfile_100M.bin
LOOP_COUNT=$1
RPS=$2

rm -rf *.enc

# 根據指定的迴圈數進行請求
for ((i = 1; i <= LOOP_COUNT; i++)); do

    for ((j = 1; j <= $RPS; j++)); do
        # # 紀錄當前請求的開始時間
        # REQUEST_START_TIME=$(date +%s)

        # 發送 curl 請求
        curl -F "file=@${FILE_PATH}" ${SERVER_URL} -o result_${i}_${j}.enc &

        # # 記錄當前請求的結束時間
        # REQUEST_END_TIME=$(date +%s)
        # echo "Request ${i} completed at: ${REQUEST_END_TIME}"

        # # 計算每次請求的耗時
        # REQUEST_TIME=$((REQUEST_END_TIME - REQUEST_START_TIME))
        # echo "Request ${i} upload time: ${REQUEST_TIME} s"
    done

    # 等待 1 秒，然後再次發送請求
    sleep 1
done
