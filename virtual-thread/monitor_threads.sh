#!/bin/bash

# 啟動 Java 程式並取得其 PID
echo "Starting Java ThreadPerformanceTest..."
java ThreadPerformanceTest &  # 啟動 Java 程式 (後台運行)
JAVA_PID=$!  # 取得 Java 處理程序的 PID

# 等待 2 秒讓程式啟動
sleep 2

# 取得當前 timestamp 並作為檔案名
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
FILENAME="thread_monitor_$TIMESTAMP.log"

# 顯示 PID
echo "Java Process PID: $JAVA_PID"
echo "Saving thread details to $FILENAME"

# 將作業系統的 thread 監控資訊寫入檔案
{
    echo "Java Process PID: $JAVA_PID"
    echo "----- Thread Details (ps) -----"
    ps -eLf | grep "$JAVA_PID"
    echo "----- Resource Usage (top snapshot) -----"
    top -b -n 1 -p "$JAVA_PID"  # 一次性輸出 top 結果
} > "$FILENAME"

echo "Thread information has been written to $FILENAME"
