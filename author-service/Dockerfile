FROM amazoncorretto:17
WORKDIR /app
COPY build/libs/author-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]