# syntax=docker/dockerfile:1.4
FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /workspace
COPY pom.xml ./
COPY src ./src
# Use BuildKit cache for Maven to reduce network retries and truncated downloads
# Enable with: DOCKER_BUILDKIT=1
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests package

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=builder /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
