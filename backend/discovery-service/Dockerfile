# ============================
# BUILD STAGE
# ============================
FROM maven:3.9.11-eclipse-temurin-17 AS builder

WORKDIR /workspace

# Copy POM descriptors for dependency caching
COPY pom.xml .
COPY backend/pom.xml backend/pom.xml
COPY backend/discovery-service/pom.xml backend/discovery-service/pom.xml

# Download dependencies offline (cached layer)
RUN mvn -B -ntp dependency:go-offline \
    -pl backend/discovery-service \
    -am

# Copy source code
COPY backend/discovery-service/src backend/discovery-service/src

# Build discovery-service
RUN mvn -B -ntp clean package \
    -pl backend/discovery-service \
    -am \
    -DskipTests

# Copy generated executable fat JAR to temporary location
RUN cp backend/discovery-service/target/*.jar /tmp/app.jar


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

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]
