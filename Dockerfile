FROM eclipse-temurin:21-jdk

WORKDIR /app

# copy jar sau khi build
COPY target/app-1.0.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]