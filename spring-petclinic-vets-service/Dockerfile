# Sử dụng image của OpenJDK làm base image
FROM openjdk:17-jdk-slim as build

# Đặt thư mục làm việc trong container
WORKDIR /app

# Sao chép file pom.xml và tải các phụ thuộc
COPY pom.xml .
RUN mvn dependency:go-offline

# Sao chép toàn bộ mã nguồn vào container
COPY src ./src

# Build ứng dụng bằng Maven
RUN mvn clean package -DskipTests

# Sử dụng image nhẹ để chạy ứng dụng
FROM openjdk:17-jdk-slim

# Đặt thư mục làm việc trong container
WORKDIR /app

# Sao chép file JAR đã build từ build stage vào container
COPY --from=build /app/target/vets-service.jar /app/vets-service.jar

# Cấu hình container để chạy ứng dụng
ENTRYPOINT ["java", "-jar", "/app/vets-service.jar"]

# Cổng mà ứng dụng sẽ chạy trên container
EXPOSE 8080
