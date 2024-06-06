
- Gradle: 8.3, 8.7
- Java 17 (OpenJDK)
- Spring Boot 2.7.3



---

grafana GQL:

```bash
pattern `{ "level":"%level", "class":"%logger{36}", "thread":"%thread", "message": "%message", "requestId": "%X{X-Request-ID}" }"`

```

---
## ref

- https://piotrminkowski.com/2023/07/05/logging-in-spring-boot-with-loki/

