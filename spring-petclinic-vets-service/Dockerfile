# Sử dụng một image Java base (OpenJDK) để build ứng dụng
FROM openjdk:17-jdk-slim AS build

# Thiết lập thư mục làm việc
WORKDIR /app

# Copy file pom.xml và tải dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy toàn bộ mã nguồn vào container
COPY src /app/src

# Build ứng dụng Spring Boot
RUN mvn clean package -DskipTests

# Image chính để chạy ứng dụng
FROM openjdk:17-jdk-slim

# Thiết lập thư mục làm việc
WORKDIR /app

# Copy file JAR đã build từ container build vào container chính
COPY --from=build /app/target/spring-petclinic-vets-service-*.jar /app/spring-petclinic-vets-service.jar

# Expose cổng mà dịch vụ sẽ chạy trên đó
EXPOSE 8080

# Lệnh để chạy ứng dụng Spring Boot
ENTRYPOINT ["java", "-jar", "spring-petclinic-vets-service.jar"]
