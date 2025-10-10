# ===========================
# Stage 1: Build the project
# ===========================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and download dependencies first (for caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Now copy the source code and build the jar
COPY src ./src
RUN mvn clean package -DskipTests

# ===========================
# Stage 2: Run the application
# ===========================
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy jar file from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port (Render uses PORT env var, so don't hardcode)
EXPOSE 2002

# Use environment variable PORT if provided by Render
ENTRYPOINT ["java", "-jar", "app.jar"]
