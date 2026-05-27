# Build stage
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy Maven project files
COPY pom.xml .
COPY .mvn ./.mvn
COPY mvnw* ./

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds)
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port defined in application.properties (8081)
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]