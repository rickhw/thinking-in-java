#!/bin/bash

# A script to generate random messages for 10 test users (50-100 messages per user)

API_URL="http://localhost:8080/api/v1/messages"

# Define the 10 test users
users=("alice" "bob" "charlie" "diana" "eve" "frank" "grace" "henry" "iris" "jack")

# Sample message templates for more realistic content
message_templates=(
  "今天天氣真不錯！☀️"
  "剛看完一部很棒的電影，推薦給大家 🎬"
  "週末計劃去爬山，有人要一起嗎？🏔️"
  "新的咖啡店開張了，咖啡很香醇 ☕"
  "學習新技術真的很有趣！💻"
  "今天的晚餐特別美味 🍽️"
  "讀了一本好書，收穫很多 📚"
  "運動後感覺精神百倍！💪"
  "和朋友聚會總是很開心 👥"
  "工作上遇到了有趣的挑戰 🚀"
  "音樂會的演出太精彩了！🎵"
  "旅行中發現了美麗的風景 🌅"
  "嘗試了新的料理食譜 👨‍🍳"
  "寵物今天特別可愛 🐱"
  "完成了一個重要的專案 ✅"
  "春天的花朵開始綻放了 🌸"
  "和家人度過了溫馨的時光 ❤️"
  "學會了一項新技能 🎯"
  "今天的日落特別美麗 🌇"
  "感謝生活中的小確幸 🙏"
)

# Additional content variations
topics=("工作" "生活" "學習" "旅行" "美食" "運動" "音樂" "電影" "書籍" "科技")
emotions=("開心" "興奮" "滿足" "感動" "驚喜" "平靜" "充實" "溫暖" "樂觀" "感恩")

total_messages=0

echo "=== 開始為 10 個用戶生成訊息 ==="
echo ""

for user in "${users[@]}"; do
  # Generate random number of messages between 50-100 for each user
  num_messages=$((RANDOM % 51 + 50))  # 50 to 100 messages
  
  echo "為用戶 '$user' 生成 $num_messages 條訊息..."
  
  for ((i=1; i<=num_messages; i++)); do
    # Choose random message type
    message_type=$((RANDOM % 3))
    
    case $message_type in
      0)
        # Use template message
        template_index=$((RANDOM % ${#message_templates[@]}))
        content="${message_templates[$template_index]}"
        ;;
      1)
        # Generate topic-based message
        topic_index=$((RANDOM % ${#topics[@]}))
        emotion_index=$((RANDOM % ${#emotions[@]}))
        content="今天在${topics[$topic_index]}方面感到很${emotions[$emotion_index]}！分享一下心得。"
        ;;
      2)
        # Generate random thought
        random_num=$((RANDOM % 1000))
        content="隨想 #$random_num：生活中總有意想不到的驚喜，保持好奇心很重要。"
        ;;
    esac
    
    # Add some variation with timestamps or numbers
    if [ $((RANDOM % 5)) -eq 0 ]; then
      content="$content [$(date +%H:%M)]"
    fi
    
    # Construct JSON payload
    JSON_PAYLOAD=$(printf '{"userId": "%s", "content": "%s"}' "$user" "$content")
    
    # Send POST request
    response=$(curl -s -X POST -H "Content-Type: application/json" -d "$JSON_PAYLOAD" "$API_URL")
    
    # Show progress every 10 messages
    if [ $((i % 10)) -eq 0 ]; then
      echo "  [$i/$num_messages] 已發布 $i 條訊息..."
    fi
    
    # Small delay to avoid overwhelming the server
    sleep 0.02
  done
  
  total_messages=$((total_messages + num_messages))
  echo "✅ 用戶 '$user' 完成，共發布 $num_messages 條訊息"
  echo ""
done

echo "=== 訊息生成完成 ==="
echo "總共為 10 個用戶生成了 $total_messages 條訊息"
echo ""
echo "統計摘要："
for user in "${users[@]}"; do
  echo "• $user: 50-100 條隨機訊息"
done
echo ""
echo "現在可以登入系統查看這些測試資料！"
