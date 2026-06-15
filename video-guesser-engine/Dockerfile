# 1. Usa a versão mais leve do Node (Alpine Linux)
FROM node:18-alpine

# 2. Cria a pasta de trabalho dentro do container
WORKDIR /app

# 3. Copia apenas os arquivos de dependências primeiro (otimiza o cache do Docker)
COPY package*.json ./

# 4. Instala os pacotes (Express, Socket.io, Redis, etc)
RUN npm install

# 5. Copia todo o resto do seu código (src, server.js) para dentro do container
COPY . .

# 6. Expõe a porta que o seu server.js usa
EXPOSE 3000

# 7. O comando que liga o motor!
CMD ["node", "server.js"]