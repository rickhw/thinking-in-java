import requests
import random
import time

url = "http://localhost:8080/api/logs"
events = [
    {"event": "login", "user": "user1"},
    {"event": "logout", "user": "user2"},
    {"event": "purchase", "user": "user3", "amount": "30"},
]

while True:
    payload = random.choice(events)
    response = requests.post(url, json=payload)
    print(f"Sent: {payload}, Response: {response.text}")
    time.sleep(random.randint(1, 3))
