FROM amazoncorretto:17
WORKDIR /app
COPY build/libs/category-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]