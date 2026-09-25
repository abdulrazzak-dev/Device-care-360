# ============================
# BUILD STAGE
# ============================
FROM maven:3.9.11-eclipse-temurin-17 AS builder

WORKDIR /workspace

# Copy POM descriptors for dependency caching
COPY pom.xml .
COPY backend/pom.xml backend/pom.xml
COPY backend/shared/pom.xml backend/shared/pom.xml
COPY backend/repair-guide-service/pom.xml backend/repair-guide-service/pom.xml

# Download dependencies offline (cached layer)
RUN mvn -B -ntp dependency:go-offline \
    -pl backend/repair-guide-service \
    -am

# Copy source code
COPY backend/shared/src backend/shared/src
COPY backend/repair-guide-service/src backend/repair-guide-service/src

# Build shared library and repair-guide-service
RUN mvn -B -ntp clean package \
    -pl backend/repair-guide-service \
    -am \
    -DskipTests

# Copy generated executable fat JAR to temporary location
RUN cp backend/repair-guide-service/target/*.jar /tmp/app.jar


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

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]
