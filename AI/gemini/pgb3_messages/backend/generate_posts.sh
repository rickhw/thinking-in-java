#!/bin/bash

# A script to generate and post 1000 random messages to the message board API.

API_URL="http://localhost:8080/api/v1/messages"

for i in {1..1000}
do
  # Generate random data
  USER_ID="user_$((RANDOM % 100 + 1))" # Random user ID between user_1 and user_100
  CONTENT="This is message number $i from $USER_ID. The content is a random number: $RANDOM."

  # Construct the JSON payload safely. Using printf is more robust than manual quoting.
  JSON_PAYLOAD=$(printf '{"userId": "%s", "content": "%s"}' "$USER_ID" "$CONTENT")

  # Send the POST request using curl
  echo "Posting message $i for $USER_ID..."
  curl -s -X POST -H "Content-Type: application/json" -d "$JSON_PAYLOAD" "$API_URL"

  # Add a small delay to avoid overwhelming the server, 0.01 seconds
  sleep 0.01
done

echo "\nFinished generating 1000 messages."
