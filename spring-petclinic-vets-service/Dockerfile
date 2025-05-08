# Sử dụng Maven với JDK 11 để build ứng dụng
FROM maven:3.8.4-openjdk-11-slim AS builder

# Set working directory
WORKDIR /app

# Sao chép pom.xml vào thư mục làm việc
COPY pom.xml ./

# Thực thi mvn validate thay vì dependency:resolve
RUN mvn validate -DskipTests -U --no-transfer-progress

# Sao chép toàn bộ mã nguồn vào container
COPY src ./src

# Build ứng dụng, bỏ qua test
RUN mvn clean install -DskipTests

# Sử dụng image openjdk để chạy ứng dụng
FROM openjdk:11-jre-slim

# Set working directory cho container chính
WORKDIR /app

# Sao chép ứng dụng đã build từ builder container
COPY --from=builder /app/target/spring-petclinic-vets-service-*.jar ./app.jar

# Expose cổng mà ứng dụng sẽ chạy
EXPOSE 8080

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
