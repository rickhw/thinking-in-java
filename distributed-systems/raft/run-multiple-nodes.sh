#!/bin/bash

echo "🟢 啟動 3 個 Raft 節點"

./gradlew :raft-node:bootRun --args='--server.port=8080 --RAFT_ID=node1' > /tmp/raft-node1.log 2>&1 &
./gradlew :raft-node:bootRun --args='--server.port=8081 --RAFT_ID=node2' > /tmp/raft-node2.log 2>&1 &
./gradlew :raft-node:bootRun --args='--server.port=8082 --RAFT_ID=node3' > /tmp/raft-node3.log 2>&1 &

echo "✅ 所有節點已啟動，請檢查 /tmp/raft-nodeX.log 以確認運行狀況"
