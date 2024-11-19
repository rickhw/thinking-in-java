#!/bin/bash

TARGET_HOST=$1

# Generate a random number between 0 and 1500
RANDOM_BASE=$((RANDOM % 1501))

# Offset by -500 to get a range of -500 to 1000
RANDOM_VALUE=$((RANDOM_BASE - 500))

# URL of the API
API_URL="http://${TARGET_HOST}/operate?value=$RANDOM_VALUE"

# Make the API call
response=$(curl -s $API_URL)

# Print the response
echo "API Response: $response"