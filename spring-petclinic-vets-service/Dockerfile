# Stage 1: Build
FROM maven:3.8.4-openjdk-11-slim AS builder

WORKDIR /app

# Copy toàn bộ source code vào image
COPY . .

# Build project từ root (chứa pom.xml cha)
RUN mvn clean install -DskipTests -U --no-transfer-progress

# Stage 2: Runtime
FROM openjdk:11-jre-slim

WORKDIR /app

# Copy file JAR đã build từ stage trước
COPY --from=builder /app/spring-petclinic-vets-service/target/*.jar app.jar

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
