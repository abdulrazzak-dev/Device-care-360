# ============================
# BUILD STAGE
# ============================
FROM maven:3.9.11-eclipse-temurin-17 AS builder

WORKDIR /workspace

# 1. Copy POM files
COPY pom.xml .
COPY shared/pom.xml shared/pom.xml
COPY booking-service/pom.xml booking-service/pom.xml

# 2. Install parent POM non-recursively into local Maven cache (~/.m2)
RUN mvn -B -ntp install -N

# 3. Copy source code
COPY shared/src shared/src
COPY booking-service/src booking-service/src

# 4. Build and install shared library into local cache
RUN mvn -B -ntp -f shared/pom.xml clean install -DskipTests

# 5. Build and package the microservice
RUN mvn -B -ntp -f booking-service/pom.xml clean package -DskipTests

# 6. Copy generated executable fat JAR to temporary location
RUN cp booking-service/target/*.jar /tmp/app.jar


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

EXPOSE 8087

ENTRYPOINT ["java", "-jar", "app.jar"]
