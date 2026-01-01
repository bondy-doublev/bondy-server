FROM eclipse-temurin:21-jre

WORKDIR /app

# tạo thư mục uploads để Spring có thể lưu file
RUN mkdir -p /app/uploads

COPY target/upload-service-0.0.1.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
