FROM node:20-alpine

# Crée et définit le répertoire de travail
WORKDIR /app

# Copie les fichiers de configuration de dépendances
COPY package*.json ./

# Installe les dépendances
RUN npm ci

# Copie tout le reste du code de l'application
COPY . .

# Construit l'application Next.js (production)
RUN npm run build

# Expose le port 3000
EXPOSE 3000

# Lance le serveur en mode production
CMD ["npm", "start"]
