# Build stage
FROM maven:3.8.4-openjdk-11-slim AS builder

# Làm việc tại thư mục /app
WORKDIR /app

# Sao chép toàn bộ project microservices
COPY . .

# Build toàn bộ dự án và skip test
RUN mvn clean install -DskipTests -U --no-transfer-progress

# Run stage
FROM openjdk:11-jre-slim

# Làm việc tại thư mục /app
WORKDIR /app

# Copy file jar của vets-service từ builder
COPY --from=builder /app/spring-petclinic-vets-service/target/spring-petclinic-vets-service-*.jar ./app.jar

# Mở cổng 8080
EXPOSE 8080

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
