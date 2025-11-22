FROM openjdk:21-slim
WORKDIR /app

# Copier le pom.xml et télécharger les dépendances d'abord
COPY pom.xml .
RUN apt-get update && apt-get install -y maven
RUN mvn dependency:go-offline -B

# Copier le code source et compiler
COPY src ./src
RUN mvn clean package -DskipTests

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/pouleAPI-0.0.1-SNAPSHOT.jar"]