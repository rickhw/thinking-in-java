
- [ ] create a key for tenant#x
  - [ ] comp#vm
  - [ ] comp#dns


## [admin] create a apikey key to consumer (username)

```bash
curl -i "http://127.0.0.1:9180/apisix/admin/consumers?api_key=zaq12wsx" \
  -X PUT \
  -d '
{
  "username": "t12345678_vm_apiname",
  "plugins": {
    "key-auth": {
      "key": "9527"
    }
  }
}'

curl -i "http://127.0.0.1:9180/apisix/admin/consumers?api_key=zaq12wsx" \
  -X PUT \
  -d '
{
  "username": "t12345678_dns_apiname",
  "plugins": {
    "key-auth": {
      "key": "9527"
    }
  }
}'
```


### 2.1 [admin] query consumer

```json
curl http://127.0.0.1:9180/apisix/admin/consumers?api_key=zaq12wsx

{
    "list": [
        {
            "createdIndex": 161,
            "key": "/apisix/consumers/t12345678_dns_apiname",
            "value": {
                "create_time": 1712363345,
                "plugins": {
                    "key-auth": {
                        "key": "9527"
                    }
                },
                "username": "t12345678_dns_apiname",
                "update_time": 1712363345
            },
            "modifiedIndex": 161
        },
        {
            "createdIndex": 159,
            "key": "/apisix/consumers/t12345678_vm_apiname",
            "value": {
                "create_time": 1712362982,
                "plugins": {
                    "key-auth": {
                        "key": "9527"
                    }
                },
                "username": "t12345678_vm_apiname",
                "update_time": 1712362982
            },
            "modifiedIndex": 159
        }
    ],
    "total": 2
}
```



## 3. [admin] bind api key and consumer to api

```bash title="02_bind-to-apikey.json"
curl -i "http://127.0.0.1:9180/apisix/admin/routes/comp-vm?api_key=zaq12wsx" \
  -X PATCH \
  -d '
{
  "plugins": {
    "key-auth": {}
  }
}'

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

```


### 3.1 [consumer] verify

```bash
$ curl -i "http://127.0.0.1:9080/v2alpha/vm"
HTTP/1.1 401 Unauthorized
Date: Sun, 03 Mar 2024 09:09:03 GMT
Content-Type: text/plain; charset=utf-8
Transfer-Encoding: chunked
Connection: keep-alive
Server: APISIX/3.8.0

{"message":"Missing API key found in request"}


$ curl -i "http://127.0.0.1:9080/v2alpha/vm" -H "apikey: 9527"

HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 47
Connection: keep-alive
Date: Sun, 03 Mar 2024 09:09:31 GMT
Access-Control-Allow-Origin: *
Access-Control-Allow-Credentials: true
Server: APISIX/3.8.0

{
  "origin": "192.168.207.1, 114.137.70.96"
}
```


---

## 4. [admin] Rate Limit on a REST api.

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

### 4.1 [consumer] verify


```bash
count=$(seq 100 | xargs -I {} curl "http://127.0.0.1:9080/ip" -I -sL | grep "429" | wc -l); echo \"200\": $((100 - $count)), \"429\": $count

"200": 2, "429":       98
```