
`application.properties` 與 `application.yml` 可以互換.


## prefix: spring

```yaml
spring:
  application:
    name: ${APP_NAME:unnamed}
  datasource:
    url: jdbc:hsqldb:file:testdb
    username: sa
    password:
    dirver-class-name: org.hsqldb.jdbc.JDBCDriver
    hikari:
      auto-commit: false
      connection-timeout: 3000
      validation-timeout: 3000
      max-lifetime: 60000
      maximum-pool-size: 20
      minimum-idle: 1
```

## prefix: server

```yaml
server:
  port: ${APP_PORT:8080}
```


## 修改 banner

