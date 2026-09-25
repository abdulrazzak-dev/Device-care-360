# ============================
# BUILD STAGE
# ============================
FROM maven:3.9.11-eclipse-temurin-17 AS builder

WORKDIR /workspace

# Copy POM descriptors for dependency caching
COPY pom.xml .
COPY shared/pom.xml shared/pom.xml
COPY user-service/pom.xml user-service/pom.xml

# Download dependencies offline (cached layer)
RUN mvn -B -ntp dependency:go-offline \
    -pl user-service \
    -am

# Copy source code
COPY shared/src shared/src
COPY user-service/src user-service/src

# Build shared library and user-service
RUN mvn -B -ntp clean package \
    -pl user-service \
    -am \
    -DskipTests

# Copy generated executable fat JAR to temporary location
RUN cp user-service/target/*.jar /tmp/app.jar


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

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
