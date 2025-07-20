FROM openjdk:17-jdk-slim

WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle ./
COPY settings.gradle ./

COPY src ./src

RUN ./gradlew bootJar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "build/libs/gbook-0.0.1-SNAPSHOT.jar"]
