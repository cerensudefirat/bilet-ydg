FROM eclipse-temurin:17-jre
WORKDIR /app
# Jenkins'in derlediği jar dosyasını kopyalar
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]