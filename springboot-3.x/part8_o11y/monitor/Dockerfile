# 使用 OpenJDK 17 作為基礎映像
FROM openjdk:17-jdk-slim

# 設定工作目錄
WORKDIR /app

# 複製 Gradle 构建文件並下載依賴
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle
RUN ./gradlew build -x test --no-daemon

# 複製源代碼並構建應用程式
COPY src src
RUN ./gradlew build -x test --no-daemon

# 暴露應用程式的端口
EXPOSE 8080

# 啟動 Spring Boot 應用程式
CMD ["java", "-jar", "build/libs/your-app-name.jar"] # 替換為實際的 .jar 名稱
