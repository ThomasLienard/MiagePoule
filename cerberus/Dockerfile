# ---- Image Cerberus personnalisée ----
FROM tomcat:9.0-jdk17

LABEL maintainer="Maureen maureen.coffre@gmail.com"
LABEL description="Cerberus Testing custom image"

# Copie du WAR officiel dans le répertoire webapps de Tomcat
COPY cerberus-core-4.20.war /usr/local/tomcat/webapps/cerberus.war

# Exposition du port HTTP de Tomcat
EXPOSE 8080

# Démarrage de Tomcat
CMD ["catalina.sh", "run"]
