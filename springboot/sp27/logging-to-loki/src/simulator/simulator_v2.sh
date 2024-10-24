#!/bin/bash

TARGET_HOST=$1


# Generate a random number of requests between 1 and 10
RANDOM_COUNT=$(shuf -i 1-20 -n 1)

# Function to make the API call
make_api_call() {
  # Generate a random number between -500 and 1000 for the value
  RANDOM_VALUE=$(( (RANDOM % 101) - 50))

  # URL of the API
  API_URL="http://${TARGET_HOST}/operate?value=$RANDOM_VALUE"

  # Make the API call
  response=$(curl -s $API_URL)

  # Print the response
  echo "API Response: $i value=$RANDOM_VALUE, result=$response" >> log_$(date +"%Y%m%d-%H").txt
}

# Loop to send the request RANDOM_COUNT times
for ((i=1; i<=RANDOM_COUNT; i++))
do
  make_api_call $i
  # Generate a random sleep time between 1 and 5 seconds
  SLEEP_TIME=$(shuf -i 1-5 -n 1)
  sleep $SLEEP_TIME
done