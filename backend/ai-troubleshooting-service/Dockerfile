# syntax=docker/dockerfile:1
# ============================
# BUILD STAGE
# ============================
FROM maven:3.9.11-eclipse-temurin-17 AS builder

WORKDIR /workspace

# Configure bounded retries and backoff for Maven Resolver (Aether) and Wagon on HTTP 429/503
ENV MAVEN_OPTS="-Daether.connector.http.retryHandler.count=5 \
    -Daether.connector.http.retryHandler.interval=3000 \
    -Daether.connector.http.retryHandler.intervalMax=15000 \
    -Daether.connector.http.retryHandler.serviceUnavailable=429,503 \
    -Dmaven.wagon.http.retryHandler.count=5 \
    -Dmaven.wagon.http.retryHandler.requestSentCount=5"

# 1. Copy parent aggregator POM and all module POMs to allow reactor resolution
COPY pom.xml .
COPY shared/pom.xml shared/pom.xml
COPY config-server/pom.xml config-server/pom.xml
COPY discovery-service/pom.xml discovery-service/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY user-service/pom.xml user-service/pom.xml
COPY device-service/pom.xml device-service/pom.xml
COPY ai-troubleshooting-service/pom.xml ai-troubleshooting-service/pom.xml
COPY repair-guide-service/pom.xml repair-guide-service/pom.xml
COPY technician-service/pom.xml technician-service/pom.xml
COPY booking-service/pom.xml booking-service/pom.xml
COPY payment-service/pom.xml payment-service/pom.xml
COPY review-service/pom.xml review-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml
COPY admin-service/pom.xml admin-service/pom.xml

# 2. Copy source code for the required reactor projects (shared library and AI microservice)
COPY shared/src shared/src
COPY ai-troubleshooting-service/src ai-troubleshooting-service/src

# 3. Execute single-pass reactor build:
# -pl ai-troubleshooting-service: targets the AI troubleshooting microservice
# -am (--also-make): automatically builds upstream reactor dependencies (com.devicecare360:shared)
# package: produces the fat JAR without running 'clean' (avoiding unnecessary maven-clean-plugin download)
# Cache mount preserves /root/.m2 across builds
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp -f pom.xml -pl ai-troubleshooting-service -am package -DskipTests

# 4. Copy generated executable fat JAR to runtime staging path
RUN cp ai-troubleshooting-service/target/ai-troubleshooting-service-*.jar /tmp/app.jar


# ============================
# RUNTIME STAGE
# ============================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create unprivileged non-root user and group
RUN addgroup -S spring && adduser -S spring -G spring

# Copy only the executable JAR from builder stage
COPY --from=builder /tmp/app.jar app.jar

# Set ownership to unprivileged user
RUN chown -R spring:spring /app

USER spring

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]
