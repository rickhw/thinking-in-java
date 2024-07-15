

- [ ] custom header


## [admin] register a api

```bash
curl -i "http://127.0.0.1:9180/apisix/admin/routes?api_key=zaq12wsx" -X PUT -d '
{
  "id": "v2alpha-vm-all",
  "uris": [ "/v2alpha/vm", "/v2alpha/vm/", "/v2alpha/vm/*" ],
  "upstream": {
    "type": "roundrobin",
    "nodes": {
      "192.168.25.50:9083": 1
    }
  }
}'

curl -i "http://127.0.0.1:9180/apisix/admin/routes?api_key=zaq12wsx" -X PUT -d '
{
  "id": "v2alpha-vm-instances",
  "uris": [ "/v2alpha/vm/instances", "/v2alpha/vm/instances/", "/v2alpha/vm/instances/*" ],
  "upstream": {
    "type": "roundrobin",
    "nodes": {
      "192.168.25.50:9083": 1
    }
  }
}'


curl http://127.0.0.1:9080/v2alpha/vm
curl http://127.0.0.1:9080/v2alpha/vm/instance-types
```



---

## Q&A

- 如何針對 upstream 做 auth by http header?

