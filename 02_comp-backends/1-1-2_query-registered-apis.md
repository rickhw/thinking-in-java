

### [admin] query all register api and route

```bash title="01_query-route.json"
curl -i "http://127.0.0.1:9180/apisix/admin/routes?api_key=zaq12wsx"
```

response

```json
{
    "list": [
        {
            "key": "/apisix/routes/getting-started-ip",
            "value": {
                "id": "getting-started-ip",
                "create_time": 1712053090,
                "uri": "/ip",
                "priority": 0,
                "update_time": 1712053090,
                "upstream": {
                    "hash_on": "vars",
                    "type": "roundrobin",
                    "pass_host": "pass",
                    "nodes": {
                        "httpbin.org:80": 1
                    },
                    "scheme": "http"
                },
                "status": 1
            },
            "modifiedIndex": 16,
            "createdIndex": 16
        }
    ],
    "total": 1
}
```
