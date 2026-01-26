FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copier le pom.xml et télécharger les dépendances d'abord
COPY pom.xml .
RUN apt-get update && apt-get install -y maven
RUN mvn dependency:go-offline -B

# Copier le code source et compiler
COPY src ./src
RUN mvn clean package -DskipTests

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "target/KernelService-0.0.1-SNAPSHOT.jar"]
