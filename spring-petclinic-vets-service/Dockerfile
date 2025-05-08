# Sử dụng image cơ bản với JDK
FROM maven:3.8.4-openjdk-11-slim as builder

# Thiết lập thư mục làm việc trong container
WORKDIR /app

# Sao chép file pom.xml
COPY pom.xml .

# Bỏ qua bước 'dependency:go-offline' và build trực tiếp
RUN mvn clean package -DskipTests

# Sao chép toàn bộ mã nguồn vào container
COPY src /app/src

# Đưa ra image chứa app đã build
FROM openjdk:11-jre-slim

WORKDIR /app
COPY --from=builder /app/target/your-app.jar /app/your-app.jar

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "your-app.jar"]

EXPOSE 8080
