#!/bin/bash

URL="http://localhost:8080/hello"
TOTAL_REQUESTS=100    # 要送幾個 request
SLEEP_MS=10          # 每次 request 間隔 (毫秒)

for ((i=1;i<=TOTAL_REQUESTS;i++))
do
  curl -X GET "$URL" &
  echo "Sent request $i"
  sleep $(echo "$SLEEP_MS/1000" | bc -l)   # 毫秒轉成秒 sleep
done

wait
echo "All requests sent."
