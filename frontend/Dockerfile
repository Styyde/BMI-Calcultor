# Étape 1: Build
FROM node:20-alpine AS builder

WORKDIR /app

# Copier les fichiers de dépendances
COPY package*.json ./
COPY package-lock.json ./

# Installer les dépendances
RUN npm ci && npm cache clean --force

# Copier tout le code source
COPY . .

# Build l'application
RUN npm run build

# Étape 2: Production avec Nginx
FROM nginx:alpine

# Installer curl pour healthcheck
RUN apk add --no-cache curl

# Copier la configuration Nginx (créez ce fichier)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copier les fichiers build
COPY --from=builder /app/dist /usr/share/nginx/html

# Exposer le port
EXPOSE 80

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost/ || exit 1

# Démarrer nginx
CMD ["nginx", "-g", "daemon off;"]