

## 1.2.2 [admin] query consumer and key

```json
curl http://127.0.0.1:9180/apisix/admin/consumers?api_key=edd1c9f034335f136f87ad84b625c8f1
```

response

```json
{
    "list": [
        {
            "key": "/apisix/consumers/t12345678",
            "value": {
                "plugins": {
                    "key-auth": {
                        "key": "9527-1235-3234-abcd"
                    }
                },
                "username": "t12345678",
                "update_time": 1712053397,
                "create_time": 1712053397
            },
            "modifiedIndex": 17,
            "createdIndex": 17
        }
    ],
    "total": 1
}
```
