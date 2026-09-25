# ============================
# BUILD STAGE
# ============================
FROM maven:3.9.11-eclipse-temurin-17 AS builder

WORKDIR /workspace

# 1. Install parent POM non-recursively (-N) so child modules are not scanned
COPY pom.xml .
RUN mvn -B -ntp install -N

# 2. Build and install shared module
COPY shared/pom.xml shared/pom.xml
COPY shared/src shared/src
RUN mvn -B -ntp -f shared/pom.xml clean install -DskipTests

# 3. Build notification-service
COPY notification-service/pom.xml notification-service/pom.xml
COPY notification-service/src notification-service/src
RUN mvn -B -ntp -f notification-service/pom.xml clean package -DskipTests

# 4. Stage executable fat JAR (avoiding hardcoded version names)
RUN cp notification-service/target/*.jar /tmp/app.jar


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

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]

