
`application.properties` 與 `application.yml` 可以互換.


## prefix: spring

```yaml
spring:
  main:
    # see: https://stackoverflow.com/questions/26585959/how-to-disable-spring-boot-logo-in-stdout
    banner-mode: off 

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

  rabbitmq:
    host: localhost
    port: 5672
    username: root
    password: password

  data:
    redis:
      database: 0
      host: localhost
      port: 6379
      # password: password
      # timeout: 60000
```

## prefix: server

```yaml
server:
  port: ${APP_PORT:8080}
  servlet:
    context-path: /demo

  # Disable Whitelabel Error Page
  # see: https://websparrow.org/spring/how-to-resolve-whitelabel-error-page-in-spring-boot
  error:
    whitelabel:
      enabled: false
```

