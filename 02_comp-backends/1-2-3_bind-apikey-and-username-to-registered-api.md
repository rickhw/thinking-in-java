

## 1.2.3 [admin] bind api key and consumer to api

```bash title="02_bind-to-apikey.json"
curl -i "http://127.0.0.1:9180/apisix/admin/routes/getting-started-ip?api_key=edd1c9f034335f136f87ad84b625c8f1" \
  -X PATCH \
  -d '
{
  "plugins": {
    "key-auth": {}
  }
}'
```

response

```json
HTTP/1.1 200 OK
Date: Sun, 03 Mar 2024 09:08:43 GMT
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
    "key": "/apisix/routes/getting-started-ip",
    "value": {
        "status": 1,
        "upstream": {
            "nodes": {
                "httpbin.org:80": 1
            },
            "type": "roundrobin",
            "scheme": "http",
            "hash_on": "vars",
            "pass_host": "pass"
        },
        "update_time": 1709456923,
        "priority": 0,
        "create_time": 1709448316,
        "uri": "/ip",
        "plugins": {
            "key-auth": {
                "header": "apikey",
                "hide_credentials": false,
                "query": "apikey"
            }
        },
        "id": "getting-started-ip"
    }
}
```

