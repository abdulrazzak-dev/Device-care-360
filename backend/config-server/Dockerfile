# ============================
# BUILD STAGE
# ============================
FROM maven:3.9.11-eclipse-temurin-17 AS builder

WORKDIR /workspace

# Copy POM descriptors for dependency caching
COPY pom.xml .
COPY config-server/pom.xml config-server/pom.xml

# Download dependencies offline (cached layer)
RUN mvn -B -ntp dependency:go-offline \
    -pl config-server \
    -am

# Copy source code
COPY config-server/src config-server/src

# Build config-server
RUN mvn -B -ntp clean package \
    -pl config-server \
    -am \
    -DskipTests

# Copy generated executable fat JAR to temporary location
RUN cp config-server/target/*.jar /tmp/app.jar


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

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "app.jar"]
