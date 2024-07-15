

## 1-3.1 [admin] Rate Limit on a REST api.

```bash
curl -i "http://127.0.0.1:9180/apisix/admin/routes/getting-started-ip?api_key=edd1c9f034335f136f87ad84b625c8f1" -X PATCH -d '
{
  "plugins": {
    "limit-count": {
        "count": 2,
        "time_window": 10,
        "rejected_code": 429
     }
  }
}'
```

response

```json
HTTP/1.1 200 OK
Date: Sun, 03 Mar 2024 09:11:29 GMT
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
        "uri": "/ip",
        "create_time": 1709448316,
        "status": 1,
        "update_time": 1712052789,
        "priority": 0,
        "id": "getting-started-ip",
        "upstream": {
            "pass_host": "pass",
            "hash_on": "vars",
            "scheme": "http",
            "type": "roundrobin",
            "nodes": {
                "httpbin.org:80": 1
            }
        },
        "plugins": {
            "limit-count": {
                "allow_degradation": false,
                "show_limit_quota_header": true,
                "time_window": 10,
                "count": 2,
                "key": "remote_addr",
                "key_type": "var",
                "rejected_code": 429,
                "policy": "local"
            }
        }
    }
}
```




