# ============================
# BUILD STAGE
# ============================
FROM maven:3.9.11-eclipse-temurin-17 AS builder

WORKDIR /workspace

# Copy POM descriptors for dependency caching
COPY pom.xml .
COPY shared/pom.xml shared/pom.xml
COPY payment-service/pom.xml payment-service/pom.xml

# Download dependencies offline (cached layer)
RUN mvn -B -ntp dependency:go-offline \
    -pl payment-service \
    -am

# Copy source code
COPY shared/src shared/src
COPY payment-service/src payment-service/src

# Build shared library and payment-service
RUN mvn -B -ntp clean package \
    -pl payment-service \
    -am \
    -DskipTests

# Copy generated executable fat JAR to temporary location
RUN cp payment-service/target/*.jar /tmp/app.jar


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

EXPOSE 8088

ENTRYPOINT ["java", "-jar", "app.jar"]
