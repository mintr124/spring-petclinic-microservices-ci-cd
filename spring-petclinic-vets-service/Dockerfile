# Base image with Maven and JDK 11
FROM maven:3.8.4-openjdk-11-slim AS builder

WORKDIR /app

# Sao chép toàn bộ project vào container
COPY . .

# Build toàn bộ project (bao gồm parent và các module con)
RUN mvn clean install -DskipTests -U --no-transfer-progress

# Image run time
FROM openjdk:11-jre-slim

WORKDIR /app

# Copy đúng jar của vets-service
COPY --from=builder /app/spring-petclinic-vets-service/target/spring-petclinic-vets-service-*.jar ./app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
