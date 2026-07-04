FROM eclipse-temurin:17-jre
WORKDIR /app

# Jenkins copie déjà le JAR à la racine, pas besoin de target/
COPY target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]