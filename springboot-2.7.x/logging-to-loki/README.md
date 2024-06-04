
- Gradle: 8.3, 8.7
- Java 17 (OpenJDK)
- Spring Boot 2.7.3



---
## ref

- https://piotrminkowski.com/2023/07/05/logging-in-spring-boot-with-loki/

---


```bash
❯ gradle run
> Task :run

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.7.3)

2024-06-02 15:05:59.391  INFO 52188 --- [           main] com.beginnertechies.googleapi.Main       : Starting Main using Java 17.0.8 on iStar.local with PID 52188 (/Users/rickhwang/Repos/rickhwang/github/java-lab/race-condition/sp27/build/classes/java/main started by rickhwang in /Users/rickhwang/Repos/rickhwang/github/java-lab/race-condition/sp27)
2024-06-02 15:05:59.392  INFO 52188 --- [           main] com.beginnertechies.googleapi.Main       : No active profile set, falling back to 1 default profile: "default"
2024-06-02 15:05:59.895  INFO 52188 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8092 (http)
2024-06-02 15:05:59.900  INFO 52188 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2024-06-02 15:05:59.900  INFO 52188 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.65]
2024-06-02 15:05:59.947  INFO 52188 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2024-06-02 15:05:59.947  INFO 52188 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 523 ms
Init counter, value: [0]
2024-06-02 15:06:00.155  INFO 52188 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8092 (http) with context path ''
2024-06-02 15:06:00.162  INFO 52188 --- [           main] com.beginnertechies.googleapi.Main       : Started Main in 0.995 seconds (JVM running for 1.231)
<=========----> 75% EXECUTING [14s]
> :run

```