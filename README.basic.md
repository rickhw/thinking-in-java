
- [ ] multi-tenancy
  - Per Kind RateLimit
  - Per API/** RateLimit

- [ ] multi sites
- [ ] custom authorization
- [ ] custom http return code and status.
    - Consider using the response-rewrite plugin. https://github.com/apache/apisix/issues/10387
    - https://apisix.apache.org/zh/docs/apisix/plugins/response-rewrite/
- [ ] import openapi
- [ ] custom authorization and process


```bash
docker-compose -p saas up -d

curl -i "http://127.0.0.1:9180/apisix/admin/routes?api_key=zaq12wsx" -X PUT -d '
{
  "id": "getting-started-ip",
  "uri": "/ip",
  "upstream": {
    "type": "roundrobin",
    "nodes": {
      "httpbin.org:80": 1
    }
  }
}'

curl http://127.0.0.1:9080/ip
```



## Debug

```bash
docker logs -f saas-apisix-1

2024/04/04 01:43:29 [error] 64#64: *101571 connect() failed (111: Connection refused) while connecting to upstream, client: 192.168.214.1, server: _, request: "GET /core HTTP/1.1", upstream: "http://127.0.0.1:9081/core", host: "127.0.0.1:9080"
192.168.214.1 - - [04/Apr/2024:01:43:29 +0000] 127.0.0.1:9080 "GET /core HTTP/1.1" 502 229 0.000 "-" "curl/8.4.0" 127.0.0.1:9081 502 0.000 "http://127.0.0.1:9080"
http://127.0.0.1:9081/core192.168.214.1 - - [04/Apr/2024:01:44:24 +0000] 127.0.0.1:9180 "PUT /apisix/admin/routes?api_key=zaq12wsx HTTP/1.1" 200 317 0.009 "-" "curl/8.4.0" - - - "http://127.0.0.1:9180"



```

## 1. comp#A=core

```bash
curl -i "http://127.0.0.1:9180/apisix/admin/routes?api_key=zaq12wsx" -X PUT -d '
{
  "id": "core",
  "uri": "/core",
  "methods": ["GET"],
  "upstream": {
    "type": "roundrobin",
    "nodes": {
      "192.168.25.50:9081": 1
    },
    "scheme": "http"
  }
}'


curl -i "http://127.0.0.1:9180/apisix/admin/routes?api_key=zaq12wsx" -X PUT -d '
{
  "id": "core",
  "uris": ["/core", "/core/", "/core/*"],
  "methods": ["GET"],
  "upstream": {
    "type": "roundrobin",
    "nodes": {
      "192.168.25.50:9081": 1
    },
    "scheme": "http"
  }
}'


curl -i "http://127.0.0.1:9180/apisix/admin/routes?api_key=zaq12wsx" -X PUT -d '
{
  "id": "v2alpha-core",
  "uris": ["/v2alpha/core", "/v2alpha/core/", "/v2alpha/core/*"],
  "methods": ["GET"],
  "upstream": {
    "type": "roundrobin",
    "nodes": {
      "192.168.25.50:9081": 1
    },
    "scheme": "http"
  }
}'

curl -i "http://127.0.0.1:9180/apisix/admin/routes?api_key=zaq12wsx"

curl -i "http://127.0.0.1:9180/apisix/admin/routes/v2alpha-core?api_key=zaq12wsx" -X DELETE
curl -i "http://127.0.0.1:9180/apisix/admin/routes/comp-dns?api_key=zaq12wsx" -X DELETE
curl -i "http://127.0.0.1:9180/apisix/admin/routes/comp-vm?api_key=zaq12wsx" -X DELETE
curl -i "http://127.0.0.1:9180/apisix/admin/routes/getting-started-ip?api_key=zaq12wsx" -X DELETE




curl http://127.0.0.1:9080/core
curl http://127.0.0.1:9080/v2alpha/core


```

## 2. comp#dns

```bash
curl -i "http://127.0.0.1:9180/apisix/admin/routes?api_key=zaq12wsx" -X PUT -d '
{
  "id": "comp-dns",
  "uris": ["/v2alpha/dns", "/v2alpha/dns/", "/v2alpha/dns/*"],
  "upstream": {
    "type": "roundrobin",
    "nodes": {
      "192.168.25.50:9082": 1
    }
  }
}'

curl http://127.0.0.1:9080/v2alpha/dns

```

## 3. comp#vm

```bash
curl -i "http://127.0.0.1:9180/apisix/admin/routes?api_key=zaq12wsx" -X PUT -d '
{
  "id": "comp-vm",
  "uri": "/v2alpha/vm/instance/*",
  "upstream": {
    "type": "roundrobin",
    "nodes": {
      "192.168.25.50:9083": 1
    }
  }
}'

```

### 1.1 [consumer] verify the api: /pi

```bash
## origin
$ curl http://127.0.0.1:9081
hello core

$ curl http://127.0.0.1:9082
hello dns

$ curl http://127.0.0.1:9083
hello vm

$ curl http://127.0.0.1:9080/v2alpha/core/

{
  "origin": "192.168.237.1, 1.34.185.77"
}
```


### 1.2 [admin] get all register api and route

```bash title="01_query-route.json"
curl -i "http://127.0.0.1:9180/apisix/admin/routes?api_key=edd1c9f034335f136f87ad84b625c8f1"

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


## 2. [admin] create a apikey key to consumer (username)

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


### 2.1 [admin] query consumer

```json
curl http://127.0.0.1:9180/apisix/admin/consumers?api_key=edd1c9f034335f136f87ad84b625c8f1

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



## 3. [admin] bind api key and consumer to api

```bash title="02_bind-to-apikey.json"
curl -i "http://127.0.0.1:9180/apisix/admin/routes/getting-started-ip?api_key=edd1c9f034335f136f87ad84b625c8f1" \
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


### 3.1 [consumer] verify

```bash
$ curl -i "http://127.0.0.1:9080/ip"
HTTP/1.1 401 Unauthorized
Date: Sun, 03 Mar 2024 09:09:03 GMT
Content-Type: text/plain; charset=utf-8
Transfer-Encoding: chunked
Connection: keep-alive
Server: APISIX/3.8.0

{"message":"Missing API key found in request"}


$ curl -i "http://127.0.0.1:9080/ip" -H "apikey: 9527-1235-3234-abcd"

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