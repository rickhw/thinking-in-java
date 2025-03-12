
ref: 
- https://ithelp.ithome.com.tw/articles/10307445
- https://www.baeldung.com/spring-boot-actuators


Show all: http://localhost:8080/actuator

http://localhost:8080/actuator/health
http://localhost:8080/actuator/info
http://localhost:8080/actuator/beans
http://localhost:8080/actuator/conditions
http://localhost:8080/actuator/env
http://localhost:8080/actuator/mappings
http://localhost:8080/actuator/loggers
http://localhost:8080/actuator/threaddump



http://localhost:8080/actuator/metrics
http://localhost:8080/actuator/metrics/http.server.requests
http://localhost:8080/actuator/metrics/http.server.requests?tag=status:404

# not work

http://localhost:8080/actuator/httptrace 
http://localhost:8080/actuator/heapdump
http://localhost:8080/actuator/logfile