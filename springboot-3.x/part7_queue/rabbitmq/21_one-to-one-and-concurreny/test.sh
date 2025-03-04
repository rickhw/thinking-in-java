#!/bin/bash

# 设置基本 URL
BASE_URL="http://localhost:8080/api/messages"

# 函数：发送正常消息
send_normal_messages() {
    echo "发送正常消息..."
    for i in {1..10}
    do
        response=$(curl -s -X POST -H "Content-Type: application/json" \
            -d "Normal message $i" \
            "$BASE_URL/send")
        echo "$response"
        sleep 0.5
    done
}

# 函数：发送错误消息
send_error_messages() {
    echo "发送错误消息..."
    for i in {1..5}
    do
        response=$(curl -s -X POST -H "Content-Type: application/json" \
            -d "Error message $i" \
            "$BASE_URL/send-error")
        echo "$response"
        sleep 0.5
    done
}

# 主脚本
main() {
    echo "开始测试 RabbitMQ 消息发送..."
    
    # 先发送正常消息
    send_normal_messages
    
    # 再发送错误消息
    send_error_messages
}

# 执行主脚本
main