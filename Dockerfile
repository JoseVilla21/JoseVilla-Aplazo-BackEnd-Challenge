FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY target/aplazo-backend-challenge-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]