# Stage 1: Build the application
FROM maven:3.8.4-openjdk-11-slim AS builder

# Set the working directory in the container
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml ./ 
RUN mvn clean dependency:resolve -DskipTests -U

# Copy the rest of the application source code
COPY src ./src

# Build the application (skip tests to speed up the build)
RUN mvn clean install -DskipTests

# Stage 2: Build the runtime image
FROM openjdk:11-jre-slim

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the builder stage
COPY --from=builder /app/target/spring-petclinic-microservices-*.jar /app/spring-petclinic.jar

# Expose the port the application will run on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/spring-petclinic.jar"]
