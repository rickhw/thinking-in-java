curl -X POST \
    "http://172.31.5.147:9200/logs/_doc" \
    -H 'Content-Type: application/json' \
    -d'
{
  "timestamp": "2024-06-26T14:00:00Z",
  "level": "INFO",
  "message": "This is a test log message",
  "logger": "com.example.demo.DemoApplication",
  "thread": "main"
}'

curl -X DELETE "http://172.31.5.147:9200/logs"

curl -X PUT "http://172.31.5.147:9200/logs-2024-07-02"


curl -X POST "http://172.31.5.147:9200/logs-2024-07-02/_bulk" \
    -H 'Content-Type: application/json' \
    -d'
{ "index" : { "_index" : "logs-2024-07-02" } }
{ "timestamp": "2024-07-02T03:00:00Z", "level": "INFO", "message": "This is a test log message 1", "logger": "com.example.demo.DemoApplication", "thread": "main" }
{ "index" : { "_index" : "logs-2024-07-02" } }
{ "timestamp": "2024-07-02T03:01:00Z", "level": "ERROR", "message": "This is a test log message 2", "logger": "com.example.demo.DemoApplication", "thread": "main" }
'
