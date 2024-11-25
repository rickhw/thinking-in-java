

## General Log

log4j.xml

```xml
  <appender name="ConsoleJsonLine" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>{ "timestamp":"%d{yyyy-MM-dd'T'HH:mm:ss.SSS}", "scheme": "%X{Protocol}", "method": "%X{Method}", "uri": "%X{RequestURI}", "clientIp": "%X{ClientIP}", "level":"%level", "message": "%message", "thread":"%thread", "class":"%logger{36}", "requestId": "%X{R-Request-Id}" }%n</pattern>
    </encoder>
  </appender>
```


output:

```json
{ "timestamp":"2024-05-13T19:32:54.871", "scheme": "http", "method": "GET", "uri": "/", "clientIp": "0:0:0:0:0:0:0:1", "level":"INFO", "message": "Entry Controller", "thread":"http-nio-8080-exec-1", "class":"c.g.r.b.controller.EntryController", "requestId": "a65fa626-0dcd-4a94-b55a-5638a68d9c62" }
```


