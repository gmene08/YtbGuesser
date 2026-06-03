# ESTÁGIO 1: Build (Compilação do Angular)
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
# Utiliza o build de produção do Angular
RUN npm run build --configuration=production

# ESTÁGIO 2: Servidor Web (Nginx)
FROM nginx:alpine
# Copia a pasta gerada pelo Angular para o servidor web (ajuste 'videoguesser-ui' se o nome do projeto for outro)
COPY --from=build /app/dist/videoguesser-ui/browser /usr/share/nginx/html

# Copia a configuração de roteamento e proxy
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]