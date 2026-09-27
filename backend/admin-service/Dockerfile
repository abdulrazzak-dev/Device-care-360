# ============================
# BUILD STAGE
# ============================
FROM maven:3.9.11-eclipse-temurin-17 AS builder

WORKDIR /workspace

# 1. Install parent POM non-recursively (-N) so uncopied child modules are not evaluated
COPY pom.xml .
RUN mvn -B -ntp install -N

# 2. Copy and install shared library dependency
COPY shared/pom.xml shared/pom.xml
COPY shared/src shared/src
RUN mvn -B -ntp -f shared/pom.xml install -DskipTests

# 3. Copy and package admin-service
COPY admin-service/pom.xml admin-service/pom.xml
COPY admin-service/src admin-service/src
RUN mvn -B -ntp -f admin-service/pom.xml package -DskipTests

# 4. Stage executable fat JAR deterministically (excluding .original artifacts)
RUN find admin-service/target/ -maxdepth 1 -name "*.jar" ! -name "*original*" -exec cp {} /tmp/app.jar \;


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

EXPOSE 8091

ENTRYPOINT ["java", "-jar", "app.jar"]


