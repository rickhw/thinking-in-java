

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
