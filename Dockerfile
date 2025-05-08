# Use a base image with Java
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the pom.xml file
COPY pom.xml .

# Download the project dependencies
RUN mvn dependency:go-offline -B

# Copy the entire project
COPY . .

# Build the project (run Maven)
RUN mvn clean package -DskipTests

# Expose the port for your application
EXPOSE 8080

# Command to run the application (change if needed)
CMD ["java", "-jar", "target/spring-petclinic.jar"]
