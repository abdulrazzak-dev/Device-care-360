# ============================
# BUILD STAGE
# ============================
FROM maven:3.9.11-eclipse-temurin-17 AS builder

WORKDIR /workspace

# Copy POM descriptors for dependency caching
COPY pom.xml .
COPY backend/pom.xml backend/pom.xml
COPY backend/shared/pom.xml backend/shared/pom.xml
COPY backend/review-service/pom.xml backend/review-service/pom.xml

# Download dependencies offline (cached layer)
RUN mvn -B -ntp dependency:go-offline \
    -pl backend/review-service \
    -am

# Copy source code
COPY backend/shared/src backend/shared/src
COPY backend/review-service/src backend/review-service/src

# Build shared library and review-service
RUN mvn -B -ntp clean package \
    -pl backend/review-service \
    -am \
    -DskipTests

# Copy generated executable fat JAR to temporary location
RUN cp backend/review-service/target/*.jar /tmp/app.jar


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

EXPOSE 8089

ENTRYPOINT ["java", "-jar", "app.jar"]
