FROM amazoncorretto:17
WORKDIR /app
COPY build/libs/image-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]