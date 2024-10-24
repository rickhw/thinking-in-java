
## Prompt

在 java 17, springboot 3.2 的環境，啟動 application 時，我想增加一段 exception handler 處理當 database (以 mysql 為例), 則停下來，然後顯示必要的訊息。

## ChatGPT

要在 Java 17 和 Spring Boot 3.2 的環境中处理数据库连接失败的情况，你可以使用 Spring Boot 提供的 `ApplicationRunner` 或 `CommandLineRunner` 来在应用启动时执行数据库连接的检查。如果连接失败，可以通过自定义异常处理程序捕获并输出相关信息。

### 实现步骤

1. **创建一个数据库检查类**：
   编写一个类来检查数据库连接是否成功。

2. **使用 `ApplicationRunner` 或 `CommandLineRunner`**：
   利用 Spring Boot 的 `ApplicationRunner` 或 `CommandLineRunner` 在应用启动时执行检查。

3. **自定义异常处理**：
   捕获数据库连接异常并输出错误信息，终止应用启动。

### 示例代码

#### 1. 数据库检查类

```java
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DatabaseHealthCheck {

    private final DataSource dataSource;

    public DatabaseHealthCheck(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void checkConnection() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            if (!connection.isValid(5)) {
                throw new SQLException("Database connection is not valid.");
            }
        }
    }
}
```

#### 2. 使用 `ApplicationRunner` 实现启动时的数据库连接检查

```java
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupDatabaseCheck implements ApplicationRunner {

    private final DatabaseHealthCheck databaseHealthCheck;

    public StartupDatabaseCheck(DatabaseHealthCheck databaseHealthCheck) {
        this.databaseHealthCheck = databaseHealthCheck;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            databaseHealthCheck.checkConnection();
            System.out.println("Database connection successful.");
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database: " + e.getMessage());
            System.exit(1); // Terminate the application if the database is not reachable
        }
    }
}
```

#### 3. Application 配置

确保你的 `application.properties` 文件里已经配置了 MySQL 的连接信息：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/your_database
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

### 工作原理

- 当应用启动时，`StartupDatabaseCheck` 会调用 `DatabaseHealthCheck` 进行数据库连接检查。
- 如果数据库连接失败，会捕获 `SQLException` 并输出相关信息，同时通过 `System.exit(1)` 终止应用启动。
- 如果连接成功，应用将继续启动。

这样，当 MySQL 数据库不可用时，你可以快速获得错误信息并终止应用启动，避免后续的错误和不稳定的行为。