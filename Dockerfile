# Stage 1: Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper & POM
COPY pom.xml ./
COPY src ./src

# Build production jar
RUN ./mvnw clean package -DskipTests 2>/dev/null || mvn clean package -DskipTests

# Stage 2: Minimal Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root system user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /app/target/alumni-linkedin-searcher-*.jar app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
