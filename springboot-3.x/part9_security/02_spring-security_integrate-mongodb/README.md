
docs: https://chikuwa-tech-study.blogspot.com/2023/11/spring-boot-security-authentication-integrating-with-mongodb-database.html
code: https://github.com/ntub46010/SpringBootTutorial/tree/Ch17-2-v2


## 17.1 預設的 spring securtiy

啟動後，自動產生一個帳號 `user` 以及密碼，如下：

```bash
❯ gradle clean bootRun

> Task :bootRun

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.0)

2025-02-15T09:51:14.852+08:00  INFO 5763 --- [           main] com.gtcafe.asimov.Main                   : Starting Main using Java 17.0.14 with PID 5763 (/Users/rickhwang/Repos/rickhwang/github/thinking-in-java/springboot-3.x/part9_security/01_spring-security/build/classes/java/main started by rickhwang in /Users/rickhwang/Repos/rickhwang/github/thinking-in-java/springboot-3.x/part9_security/01_spring-security)
2025-02-15T09:51:14.853+08:00  INFO 5763 --- [           main] com.gtcafe.asimov.Main                   : No active profile set, falling back to 1 default profile: "default"
2025-02-15T09:51:15.203+08:00  INFO 5763 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
2025-02-15T09:51:15.207+08:00  INFO 5763 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2025-02-15T09:51:15.207+08:00  INFO 5763 --- [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.16]
2025-02-15T09:51:15.230+08:00  INFO 5763 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2025-02-15T09:51:15.230+08:00  INFO 5763 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 356 ms
2025-02-15T09:51:15.352+08:00  WARN 5763 --- [           main] .s.s.UserDetailsServiceAutoConfiguration : 

Using generated security password: bbe34c39-2f91-4b26-aff1-cc4b3343a531

This generated password is for development use only. Your security configuration must be updated before running your application in production.
```

// 預設沒有登入，無法瀏覽
http://localhost:8080/home


// spring-security 自動產生 login web from
http://localhost:8080/
http://localhost:8080/login

出現一個 web form, 填入帳號: `user`, 密碼: `bbe34c39-2f91-4b26-aff1-cc4b3343a531`

// 預設沒有登入，無法瀏覽
http://localhost:8080/home

// spring-security 自動產生
http://localhost:8080/logout



## 2 SecurityConfig & UserDetail



{
    "username": "user1",
    "password": "111",
    "authorities": ["STUDENT"]
}

{
    "username": "user2",
    "password": "222",
    "authorities": ["TEACHER"]
}

{
    "username": "user3",
    "password": "333",
    "authorities": ["TEACHER", "ADMIN"]
}

