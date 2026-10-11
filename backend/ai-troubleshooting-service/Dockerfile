# syntax=docker/dockerfile:1.7
# ============================
# BUILD STAGE
# ============================
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /workspace

# Configure bounded retry behavior for Maven Resolver (Aether) and Wagon on HTTP 429/503
ENV MAVEN_OPTS="-Daether.connector.http.retryHandler.count=5 \
    -Daether.connector.http.retryHandler.interval=3000 \
    -Daether.connector.http.retryHandler.intervalMax=30000 \
    -Daether.connector.http.retryHandler.serviceUnavailable=429,503 \
    -Dmaven.wagon.http.retryHandler.count=5 \
    -Dmaven.wagon.http.retryHandler.requestSentCount=5"

# Copy source tree from build context
COPY . .

# Setup build directory and auto-detect context (supports Render Root Directory: 'backend' or repository root)
RUN if [ -f "backend/pom.xml" ]; then \
        echo "Detected repository root build context"; \
        mkdir -p /build && cp -a backend/. /build/; \
    else \
        echo "Detected backend build context"; \
        mkdir -p /build && cp -a /workspace/. /build/; \
    fi

WORKDIR /build

# Execute single-pass reactor build:
# -pl ai-troubleshooting-service : focuses on the AI microservice
# -am (--also-make)              : builds upstream dependencies (com.devicecare360:shared)
# package                         : compiles and packages fat JAR without 'clean'
# Cache mount preserves /root/.m2/repository across builds
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn -B -ntp \
    -f pom.xml \
    -pl ai-troubleshooting-service \
    -am package \
    -DskipTests

# Stage the executable Spring Boot fat JAR (explicitly excluding .jar.original)
RUN set -eu; \
    JAR_FILE=$(find ai-troubleshooting-service/target -maxdepth 1 -name "*.jar" ! -name "*.original" | head -n 1); \
    echo "Staging executable JAR: $JAR_FILE"; \
    test -n "$JAR_FILE"; \
    cp "$JAR_FILE" /workspace/app.jar


# ============================
# RUNTIME STAGE
# ============================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create unprivileged non-root user and group
RUN addgroup -S spring && adduser -S spring -G spring

# Copy only the executable fat JAR from builder stage
COPY --from=builder /workspace/app.jar /app/app.jar

# Set ownership to unprivileged user
RUN chown -R spring:spring /app

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
