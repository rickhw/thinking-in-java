

---
## Create Dummy File

```bash
# Let's create a heuristic file for our server to send
# We'll create a random file of size 1GB
dd if=/dev/urandom of=1G.bin bs=1G count=1
dd if=/dev/urandom of=500M.bin bs=500M count=1
dd if=/dev/urandom of=250M.bin bs=250M count=1
dd if=/dev/urandom of=100M.bin bs=100M count=1
dd if=/dev/urandom of=50M.bin bs=50M count=1
dd if=/dev/urandom of=10M.bin bs=10M count=1
```

```bash
# The below commands only works on Mac OS
# Check the size of the file generated in bytes
stat -f "%z" python/public/stream/data/heuristics.bin
# 1073741824

# Let's check the md5 of the file
md5 -r python/public/stream/data/heuristics.bin | awk '{print $1}'
# c5b8959732d3359791bcd06ca5a92dc2
```


```bash
# For linux, you can use the below code
# Check the size of the file generated in bytes
stat -c "%s" python/public/stream/data/heuristics.bin

# Check the md5 of the file
md5sum python/public/stream/data/heuristics.bin | awk '{print $1}'
```

---
## Upload

curl -i -X POST \
  -H "Content-Type: multipart/form-data" \
  -F "data=@/Users/rickhwang/Temp/10M.bin" \
  http://35.89.145.54:8080/upload


---
## Download

```bash
$ curl -o filename.ext -v http://localhost:8080/download
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
  0     0    0     0    0     0      0      0 --:--:-- --:--:-- --:--:--     0*   Trying [::1]:8080...
* Connected to localhost (::1) port 8080
> GET /download HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/8.4.0
> Accept: */*
>
< HTTP/1.1 200
< Content-Disposition: form-data; name="attachment"; filename="346811976_786838622803063_3390886969870428845_n.jpg"
< Accept-Ranges: bytes
< Content-Type: application/octet-stream
< Content-Length: 71657
< Date: Tue, 09 Apr 2024 12:10:04 GMT
<
{ [16130 bytes data]
100 71657  100 71657    0     0  9501k      0 --:--:-- --:--:-- --:--:-- 9996k
* Connection #0 to host localhost left intact
```


---

## Ref

- https://shubham.codes/blog/2023-05-26-chunked-file-transfer-protocol-rest-api/