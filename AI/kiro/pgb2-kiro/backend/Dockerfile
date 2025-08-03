FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy gradle wrapper and build files
COPY gradle gradle
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .

# Download dependencies
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build application
RUN ./gradlew build --no-daemon -x test

# Run application
EXPOSE 8080
CMD ["java", "-jar", "build/libs/twitter-board-backend-0.0.1-SNAPSHOT.jar"]