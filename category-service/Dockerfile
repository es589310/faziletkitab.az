# 1. Temel imaj: Java 17 kullanıyoruz
FROM openjdk:17-jdk-slim

# 2. Çalışma dizinini belirtiyoruz
WORKDIR /app

# 3. Gradle ile oluşturulan JAR dosyasını kopyalıyoruz
COPY build/libs/category-service-0.0.1-SNAPSHOT.jar app.jar

# 4. Container çalıştığında uygulamayı başlatıyoruz
ENTRYPOINT ["java", "-jar", "app.jar"]
