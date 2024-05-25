https://www.baeldung.com/spring-boot-console-app


## Description

Springboot 2.7 Console 的專案樣板 (Project Template)



- App 開始
- App end hook
    - ApplicationContext is closed gracefully on exit. Create bean (or beans) that implements DisposableBean or has method with @PreDestroy annotation. This bean will be invoked on app shutdown.


---
## PreDestroy and 

Like @Resource, the @PostConstruct and @PreDestroy annotation types were a part of the standard Java libraries from JDK 6 to 8. However, the entire javax.annotation package got separated from the core Java modules in JDK 9 and eventually removed in JDK 11. As of Jakarta EE 9, the package lives in jakarta.annotation now. If needed, the jakarta.annotation-api artifact needs to be obtained via Maven Central now, simply to be added to the application’s classpath like any other library.