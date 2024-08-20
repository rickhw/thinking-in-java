

## Use Cases

- An new service register.
    - config API for all tenants
    - define the rate limit for comp or per API

- [ ] multi-tenancy
  - Per Kind RateLimit
  - Per API/** RateLimit
- [ ] multi sites
- [ ] custom authorization and process


---

## PoC

- [ ] custom authorization
    - forward-auth: https://apisix.apache.org/docs/apisix/plugins/forward-auth/
- [ ] custom http return code and status.
    - Consider using the response-rewrite plugin. https://github.com/apache/apisix/issues/10387
    - https://apisix.apache.org/zh/docs/apisix/plugins/response-rewrite/
- [ ] import openapi


---

## Persona

- 01. api: API Client
- 02. srvmin: Service Provider Manager
- 03. ptmin: Platform Manager
- 04. apimin: API Manager
