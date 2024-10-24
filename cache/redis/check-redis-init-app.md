同样的方式，你可以在应用启动时检查 Redis 的连接，并处理连接失败的情况。与 MySQL 类似，你可以编写一个 Redis 健康检查类，并在 Spring Boot 启动时进行检测。

### 实现步骤

1. **创建 Redis 连接检查类**。
2. **使用 `ApplicationRunner` 或 `CommandLineRunner` 进行启动时检查**。
3. **处理连接异常并终止应用**。

### 示例代码

#### 1. Redis 健康检查类

你可以使用 `Lettuce` 或 `Jedis` 作为 Redis 客户端。这里以 `Lettuce` 为例：

```java
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.stereotype.Component;

@Component
public class RedisHealthCheck {

    private final RedisClient redisClient;

    public RedisHealthCheck(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    public void checkConnection() throws Exception {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            String pong = syncCommands.ping();
            if (!"PONG".equals(pong)) {
                throw new Exception("Failed to connect to Redis.");
            }
        }
    }
}
```

#### 2. 使用 `ApplicationRunner` 实现 Redis 启动检查

```java
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRedisCheck implements ApplicationRunner {

    private final RedisHealthCheck redisHealthCheck;

    public StartupRedisCheck(RedisHealthCheck redisHealthCheck) {
        this.redisHealthCheck = redisHealthCheck;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            redisHealthCheck.checkConnection();
            System.out.println("Redis connection successful.");
        } catch (Exception e) {
            System.err.println("Failed to connect to Redis: " + e.getMessage());
            System.exit(1); // Terminate the application if Redis is not reachable
        }
    }
}
```

#### 3. Application 配置

确保在 `application.properties` 中配置 Redis 连接信息：

```properties
spring.redis.host=localhost
spring.redis.port=6379
```

并在配置文件中启用 Lettuce 作为 Redis 客户端：

```properties
spring.redis.lettuce.pool.enabled=true
```

### 工作原理

- 在应用启动时，`StartupRedisCheck` 调用 `RedisHealthCheck` 来检查 Redis 连接。
- 如果连接失败，程序会捕获异常并通过 `System.exit(1)` 停止应用，并显示错误信息。
- 如果 Redis 连接成功，应用将继续启动。

这种方法可以确保在 Redis 不可用时及时获取反馈并停止应用，避免潜在的运行错误。