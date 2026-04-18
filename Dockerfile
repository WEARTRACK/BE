FROM eclipse-temurin:17-jre
COPY build/libs/weartrack-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
