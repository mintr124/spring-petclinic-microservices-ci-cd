# Sử dụng image cơ bản với Maven và OpenJDK
FROM maven:3.8.4-openjdk-11-slim as builder

# Thiết lập thư mục làm việc trong container
WORKDIR /app

# Sao chép file pom.xml và các files liên quan vào container
COPY pom.xml ./

# Cài đặt các phụ thuộc của Maven (offline, nếu cần)
RUN mvn clean dependency:resolve -DskipTests -U

# Sao chép toàn bộ mã nguồn vào container
COPY src /app/src

# Build ứng dụng mà không cần tải lại dependencies
RUN mvn clean package -DskipTests

# Đưa ra image chứa app đã build
FROM openjdk:11-jre-slim

WORKDIR /app
COPY --from=builder /app/target/spring-petclinic-vets-service-*.jar /app/spring-petclinic-vets-service.jar

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "spring-petclinic-vets-service.jar"]

EXPOSE 8080
