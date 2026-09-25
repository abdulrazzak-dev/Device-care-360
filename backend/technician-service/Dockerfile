# ============================
# BUILD STAGE
# ============================
FROM maven:3.9.11-eclipse-temurin-17 AS builder

WORKDIR /workspace

# Copy POM descriptors for dependency caching
COPY pom.xml .
COPY backend/pom.xml backend/pom.xml
COPY backend/shared/pom.xml backend/shared/pom.xml
COPY backend/technician-service/pom.xml backend/technician-service/pom.xml

# Download dependencies offline (cached layer)
RUN mvn -B -ntp dependency:go-offline \
    -pl backend/technician-service \
    -am

# Copy source code
COPY backend/shared/src backend/shared/src
COPY backend/technician-service/src backend/technician-service/src

# Build shared library and technician-service
RUN mvn -B -ntp clean package \
    -pl backend/technician-service \
    -am \
    -DskipTests

# Copy generated executable fat JAR to temporary location
RUN cp backend/technician-service/target/*.jar /tmp/app.jar


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

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "app.jar"]
