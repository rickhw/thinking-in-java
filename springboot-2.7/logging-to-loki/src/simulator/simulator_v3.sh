#!/bin/bash

TARGET_HOST=$1
ES_HOST=$2

# Generate a random number of requests between 1 and 20
RANDOM_COUNT=$(shuf -i 1-20 -n 1)

# Function to make the API call
make_api_call() {
  # Generate a random number between -50 and 50 for the value
  RANDOM_VALUE=$(( (RANDOM % 101) - 50))

  # URL of the API
  API_URL="http://${TARGET_HOST}/operate?value=$RANDOM_VALUE"

  # Make the API call
  response=$(curl -s $API_URL)

  # Get current timestamp
  TIMESTAMP=$(date --utc +%Y-%m-%dT%H:%M:%S.%3NZ)
  ES_INDEX=$(date --utc +%Y-%m-%d)

  # Create a log entry
  log_entry="API Response: $i value=$RANDOM_VALUE, result=$response"
  echo $log_entry >> log_$(date +"%Y%m%d-%H").txt

  # JSON payload for Elasticsearch
  json_payload=$(cat <<EOF
{
  "timestamp": "$TIMESTAMP",
  "level": "INFO",
  "message": "$log_entry",
  "logger": "client_simulation",
  "thread": "main",
  "api": "${API_URL}",
  "consumedUnit": "${RANDOM_VALUE}",
  "response": ${response}
}
EOF
)

  # URL of the Elasticsearch API
  ES_URL="http://${ES_HOST}/logs-${ES_INDEX}/_doc"

  # Send the log to Elasticsearch
  curl -s -X POST $ES_URL -H 'Content-Type: application/json' -d "$json_payload" > /dev/null
}

# Loop to send the request RANDOM_COUNT times
for ((i=1; i<=RANDOM_COUNT; i++))
do
  make_api_call $i
  # Generate a random sleep time between 1 and 5 seconds
  SLEEP_TIME=$(shuf -i 1-5 -n 1)
  sleep $SLEEP_TIME
done
