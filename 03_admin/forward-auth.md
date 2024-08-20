

---
## Forward-auth

```bash
curl -X PUT 'http://127.0.0.1:9180/apisix/admin/routes/auth' \
    -H 'X-API-KEY: zaq12wsx' \
    -H 'Content-Type: application/json' \
    -d "@forward-auth-data.json"
```


```json
{
    "uri": "/auth",
    "plugins": {
        "serverless-pre-function": {
            "phase": "rewrite",
            "functions": [
                "return function (conf, ctx)
                    local core = require(\"apisix.core\");
                    local authorization = core.request.header(ctx, \"Authorization\");
                    if authorization == \"123\" then
                        core.response.exit(200);
                    elseif authorization == \"321\" then
                        core.response.set_header(\"X-User-ID\", \"i-am-user\");
                        core.response.exit(200);
                    else core.response.set_header(\"Location\", \"http://example.com/auth\");
                        core.response.exit(403);
                    end
                end"
            ]
        }
    }
}
```
