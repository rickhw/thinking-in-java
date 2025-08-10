# Multi-stage Dockerfile for Spring Boot backend
# Stage 1: Build stage
FROM openjdk:17-jdk-slim AS builder

# Set working directory
WORKDIR /app

# Copy gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x ./gradlew

# Copy source code
COPY src src

# Build the application
RUN ./gradlew build -x test --no-daemon

# Stage 2: Runtime stage
FROM openjdk:17-jre-slim AS runtime

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r pgb4 && useradd -r -g pgb4 pgb4

# Set working directory
WORKDIR /app

# Create directories for logs and data
RUN mkdir -p /app/logs /app/data && chown -R pgb4:pgb4 /app

# Copy the built JAR from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership of the JAR file
RUN chown pgb4:pgb4 app.jar

# Switch to non-root user
USER pgb4

# Expose the application port
EXPOSE 8080

# Health check configuration
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/health/live || exit 1

# JVM optimization environment variables
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Application environment variables
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]