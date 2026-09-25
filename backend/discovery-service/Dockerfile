# ============================
# BUILD STAGE
# ============================
FROM maven:3.9.11-eclipse-temurin-17 AS builder

WORKDIR /workspace

# 1. Install parent POM non-recursively (-N) so child modules are not scanned
COPY pom.xml .
RUN mvn -B -ntp install -N

# 2. Build discovery-service
COPY discovery-service/pom.xml discovery-service/pom.xml
COPY discovery-service/src discovery-service/src
RUN mvn -B -ntp -f discovery-service/pom.xml clean package -DskipTests

# 3. Stage executable fat JAR (avoiding hardcoded version names)
RUN cp discovery-service/target/*.jar /tmp/app.jar


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

