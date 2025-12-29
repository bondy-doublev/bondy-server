FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/upload-service-0.0.1.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
