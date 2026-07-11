# =========================
# Stage 1: Build
# =========================
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# Cache dependencies first
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copy source and build
COPY src ./src
RUN mvn -B clean package -DskipTests

# =========================
# Stage 2: Runtime
# =========================
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Security: non-root user
RUN addgroup -S forgemind && adduser -S forgemind -G forgemind

# Optional healthcheck tools
RUN apk add --no-cache curl

# Copy jar
COPY --from=build /app/target/*.jar app.jar

# Permissions
RUN chown -R forgemind:forgemind /app
USER forgemind

EXPOSE 8080

# JVM container-friendly flags
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]