#!/bin/bash

# 檢查是否提供檔案大小參數
if [ -z "$1" ]; then
  echo "Usage: $0 <file_size>"
  echo "Example: $0 1G or $0 500M"
  exit 1
fi

# 取得檔案大小參數
FILE_SIZE=$1

# 生成檔案名稱：genfile_<size>_<timestamp>.bin
TIMESTAMP=$(date +"%m-%d-%H-%M") # 格式: MM-dd-hh-mm
OUTPUT_FILE="genfile_${FILE_SIZE}_${TIMESTAMP}.bin"

# 解析檔案大小
SIZE_UNIT=${FILE_SIZE: -1} # 最後一個字母作為單位 (K, M, G)
SIZE_VALUE=${FILE_SIZE%?}  # 去掉最後一個字母作為數值

# 確認單位合法
if [[ ! "$SIZE_UNIT" =~ [KMG] ]]; then
  echo "Invalid size unit. Please use K, M, or G (e.g., 1G, 500M)."
  exit 1
fi

# 將大小轉換成字節
case "$SIZE_UNIT" in
  K) BLOCK_SIZE=1024 ;;
  M) BLOCK_SIZE=$((1024 * 1024)) ;;
  G) BLOCK_SIZE=$((1024 * 1024 * 1024)) ;;
esac

# 計算要生成的總塊數 (count)
TOTAL_SIZE=$((SIZE_VALUE * BLOCK_SIZE))
BLOCK_COUNT=$((TOTAL_SIZE / 1024)) # 使用每塊 1KB 的大小

# 生成檔案
echo "Generating file: $OUTPUT_FILE with size: $FILE_SIZE..."
dd if=/dev/urandom of=$OUTPUT_FILE bs=1024 count=$BLOCK_COUNT status=progress

echo "File generated: $OUTPUT_FILE (Size: $FILE_SIZE)"
