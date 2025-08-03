#!/bin/bash

# A script to generate test users for the message board API.

API_URL="http://localhost:8080/api/v1/users/register"

# Create some test users
users=(
  '{"username": "rick", "password": "password123", "email": "rick@example.com"}'
  '{"username": "alice", "password": "password123", "email": "alice@example.com"}'
  '{"username": "bob", "password": "password123", "email": "bob@example.com"}'
  '{"username": "charlie", "password": "password123", "email": "charlie@example.com"}'
  '{"username": "diana", "password": "password123", "email": "diana@example.com"}'
)

for user in "${users[@]}"
do
  echo "Creating user: $user"
  curl -s -X POST -H "Content-Type: application/json" -d "$user" "$API_URL"
  echo ""
  sleep 0.1
done

echo "Finished creating test users."
echo "You can now login with:"
echo "Username: rick, Password: password123"
echo "Username: alice, Password: password123"
echo "Username: bob, Password: password123"
echo "Username: charlie, Password: password123"
echo "Username: diana, Password: password123"