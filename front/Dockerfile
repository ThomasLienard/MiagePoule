FROM node:22.12.0-alpine

WORKDIR /app

# Copier les fichiers de configuration
COPY package*.json ./

# Installer les dépendances
RUN npm install

# Copier le reste des fichiers
COPY . .

# Construire l'application pour la production
RUN npm run build

# Exposer uniquement le port pour l'application
EXPOSE 3000

# Démarrer le serveur de production
CMD ["npm", "run", "preview"]