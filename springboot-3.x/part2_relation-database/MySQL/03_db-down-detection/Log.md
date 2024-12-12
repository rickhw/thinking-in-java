

~$ curl localhost:8181/actuator/health
{"status":"UP"}                                                                                                                               
~$ curl localhost:8181/api/health
{"application":"Spring Boot Application","version":"1.0.0","status":"UP"}

~$ docker ps

~$ docker stop mysqldb

~$ curl localhost:8181/api/health

~$ curl localhost:8181/actuator/health
           
---

❯ curl localhost:8181/actuator/health
{"status":"UP"}%                                                                                                                                 
❯ curl localhost:8181/api/health
{"application":"Spring Boot Application","version":"1.0.0","status":"UP"}%                                                                       
❯ docker ps
CONTAINER ID   IMAGE                      COMMAND                  CREATED          STATUS          PORTS                                                   NAMES
d0c38892599d   03_db-down-detection-app   "java -jar /app/app.…"   35 seconds ago   Up 35 seconds   8181/tcp, 0.0.0.0:8181->8080/tcp, [::]:8181->8080/tcp   springboot-app
ea2b8e386eb6   phpmyadmin/phpmyadmin      "/docker-entrypoint.…"   6 minutes ago    Up 35 seconds   0.0.0.0:8080->80/tcp, [::]:8080->80/tcp                 phpmyadmin
a409ef3e3d12   mysql:8.0                  "docker-entrypoint.s…"   6 minutes ago    Up 35 seconds   0.0.0.0:3306->3306/tcp, :::3306->3306/tcp, 33060/tcp    mysqldb
❯ docker stop mysqldb
mysqldb
❯ curl localhost:8181/api/health
{"application":"Spring Boot Application","version":"1.0.0","status":"DOWN"}%                                                                     
❯ curl localhost:8181/actuator/health
{"status":"DOWN"}%           