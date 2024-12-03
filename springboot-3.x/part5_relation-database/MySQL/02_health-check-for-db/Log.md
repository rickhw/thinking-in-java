

~$ curl localhost:8181/actuator/health
{"status":"UP"}                                                                                                                               
~$ curl localhost:8181/api/health
{"application":"Spring Boot Application","version":"1.0.0","status":"UP"}

❯ docker ps
CONTAINER ID   IMAGE                        COMMAND                  CREATED              STATUS              PORTS                                                   NAMES
35974c8e634e   phpmyadmin/phpmyadmin        "/docker-entrypoint.…"   About a minute ago   Up About a minute   0.0.0.0:8080->80/tcp, [::]:8080->80/tcp                 phpmyadmin
923eab47418b   02_health-check-for-db-app   "java -jar /app/app.…"   About a minute ago   Up About a minute   8181/tcp, 0.0.0.0:8181->8080/tcp, [::]:8181->8080/tcp   springboot-app
ad8513a29255   mysql:8.0                    "docker-entrypoint.s…"   About a minute ago   Up About a minute   0.0.0.0:3306->3306/tcp, :::3306->3306/tcp, 33060/tcp    mysqldb

❯ docker stop mysqldb
mysqldb

❯ curl localhost:8181/api/health
{"application":"Spring Boot Application","version":"1.0.0","status":"UP"}%                                                                       

❯ curl localhost:8181/actuator/health
{"status":"DOWN"}%                                                                                                                               
