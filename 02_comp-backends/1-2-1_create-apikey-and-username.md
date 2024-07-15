
##  [admin] create a apikey key to consumer (username)

```bash
curl -i "http://127.0.0.1:9180/apisix/admin/consumers?api_key=edd1c9f034335f136f87ad84b625c8f1" \
  -X PUT \
  -d '
{
  "username": "t12345678",
  "plugins": {
    "key-auth": {
      "key": "9527-1235-3234-abcd"
    }
  }
}'
```

response

```json
HTTP/1.1 201 Created
Date: Sun, 03 Mar 2024 09:08:02 GMT
Content-Type: application/json
Transfer-Encoding: chunked
Connection: keep-alive
Server: APISIX/3.8.0
Access-Control-Allow-Origin: *
Access-Control-Allow-Credentials: true
Access-Control-Expose-Headers: *
Access-Control-Max-Age: 3600
X-API-VERSION: v3

{
    "key": "/apisix/consumers/t12345678",
    "value": {
        "plugins": {
            "key-auth": {
                "key": "9527-1235-3234-abcd"
            }
        },
        "username": "t12345678",
        "create_time": 1712053397,
        "update_time": 1712053397
    }
}
```

