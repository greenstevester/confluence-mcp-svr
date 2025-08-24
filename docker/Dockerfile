# Multi-stage Dockerfile for Spring Boot Confluence MCP Server
# Optimized for Java 21 and Spring Boot 3.5.4

# Stage 1: Build stage with Gradle
FROM eclipse-temurin:21-jdk-alpine AS builder

# Install required build tools
RUN apk add --no-cache bash

# Set working directory
WORKDIR /app

# Copy gradle wrapper and configuration files first (for better caching)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Download dependencies (this layer will be cached if dependencies don't change)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build the application
RUN ./gradlew bootJar --no-daemon && \
    java -Djarmode=layertools -jar build/libs/*.jar extract

# Stage 2: Runtime stage with layered JAR
FROM eclipse-temurin:21-jre-alpine AS runtime

# Install useful tools and create non-root user
RUN apk add --no-cache \
    curl \
    jq \
    tini && \
    addgroup -g 1000 spring && \
    adduser -D -s /bin/sh -u 1000 -G spring spring

# Set working directory
WORKDIR /app

# Copy application layers from builder stage
# Order matters for caching - least frequently changed first
COPY --from=builder --chown=spring:spring /app/dependencies/ ./
COPY --from=builder --chown=spring:spring /app/spring-boot-loader/ ./
COPY --from=builder --chown=spring:spring /app/snapshot-dependencies/ ./
COPY --from=builder --chown=spring:spring /app/application/ ./

# Create directories for logs and temp files
RUN mkdir -p /app/logs /app/temp && \
    chown -R spring:spring /app/logs /app/temp

# Switch to non-root user
USER spring:spring

# Expose the application port
EXPOSE 8081

# JVM options optimized for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -XX:+ExitOnOutOfMemoryError \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.backgroundpreinitializer.ignore=true \
    -Djava.awt.headless=true"

# Spring Boot configuration
ENV SPRING_PROFILES_ACTIVE=default
ENV SERVER_PORT=8081
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS

# Health check configuration
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:${SERVER_PORT}/actuator/health || exit 1

# Use tini to handle signals properly
ENTRYPOINT ["tini", "--"]

# Run the Spring Boot application
CMD ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]