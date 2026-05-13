# Build stage - standalone build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy only user-service files
COPY pom.xml .
COPY src ./src

# Download dependencies and build (repackage will create executable jar)
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the built executable jar (the repackaged one)
COPY --from=build /app/target/user-service-*.jar app.jar

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]