# Multi-stage Dockerfile for production-ready Spring Boot app

# Build stage
FROM maven:3.9.4-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy Maven files for dependency caching
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean install -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-jammy AS runtime

# Install curl for health checks
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# Create app user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/project3-*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM optimization for containers
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 