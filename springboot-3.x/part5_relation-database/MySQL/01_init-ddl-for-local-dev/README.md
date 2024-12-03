## Prompt

在 java17, springboot 3.4, gradle, 針對 mysql 初始資料的需求, 幫我寫一個完整範例, 滿足以下:

1. 初始化的 init-ddl.sql, init-data.sql 放在 src/main/resources 裡
2. 透過 docker-compose 準備開發環境, 裡面有 api server (springboot) 和 mysql + phpmyadmin 3 個 container
3. docker-compose 啟動的時候會自動把 init-ddl.sql, init-data.sql  依序匯入到 mysql 指定的 database
4. springboot app 啟動後，在 CommandRunner 的介面，顯示目前已經連線到 database

---

## Claude

我將為您建立一個完整的範例專案，滿足您的所有需求。我會逐步展示每個檔案的內容。

```yaml
# docker-compose.yml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    container_name: mysqldb
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: myapp
      MYSQL_USER: myuser
      MYSQL_PASSWORD: mypass
    ports:
      - "3306:3306"
    volumes:
      - ./src/main/resources/init-ddl.sql:/docker-entrypoint-initdb.d/1-init-ddl.sql
      - ./src/main/resources/init-data.sql:/docker-entrypoint-initdb.d/2-init-data.sql

  phpmyadmin:
    image: phpmyadmin/phpmyadmin
    container_name: phpmyadmin
    environment:
      PMA_HOST: mysql
      PMA_PORT: 3306
    ports:
      - "8080:80"
    depends_on:
      - mysql

  app:
    build: .
    container_name: springboot-app
    ports:
      - "8181:8181"
    depends_on:
      - mysql
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/myapp
      - SPRING_DATASOURCE_USERNAME=myuser
      - SPRING_DATASOURCE_PASSWORD=mypass

```

```sql
# src/main/resources/init-ddl.sql
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

# src/main/resources/init-data.sql
INSERT INTO users (username, email) VALUES 
('john_doe', 'john@example.com'),
('jane_smith', 'jane@example.com');

```

```properties
# src/main/resources/application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/myapp
spring.datasource.username=myuser
spring.datasource.password=mypass
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

```

```java
# Dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8181
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

```

```java
package com.example.myapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}

```

```java
package com.example.myapp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
            System.out.println("成功連線到資料庫！目前使用者數量：" + userCount);
        } catch (Exception e) {
            System.err.println("無法連線到資料庫：" + e.getMessage());
        }
    }
}

```

```groovy
// build.gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.1'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'

java {
    sourceCompatibility = '17'
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-jdbc'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    runtimeOnly 'com.mysql:mysql-connector-j'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
    useJUnitPlatform()
}

bootJar {
    archiveFileName = 'app.jar'
}

```

完整專案架構如下：
```
my-spring-mysql-app/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           └── myapp/
│       │               ├── MyApplication.java
│       │               └── DatabaseInitializationRunner.java
│       └── resources/
│           ├── init-ddl.sql
│           ├── init-data.sql
│           └── application.properties
├── docker-compose.yml
├── Dockerfile
└── build.gradle
```

使用步驟：

1. 確保已安裝 Docker 和 Docker Compose
2. 執行 `./gradlew build` 編譯專案
3. 執行 `docker-compose up --build`

特點：
- 使用 Java 17 和 Spring Boot 3.4
- MySQL 8.0
- 自動初始化資料庫
- 透過 CommandLineRunner 確認資料庫連線
- PhpMyAdmin 用於資料庫管理

專案將自動：
1. 建立 users 資料表
2. 插入兩筆初始資料
3. 啟動 Spring Boot 應用程式
4. 連線並顯示使用者數量

需要額外注意的是，要確保 Gradle Wrapper 已正確設定，並給予 docker-compose 和相關 shell 檔案執行權限。