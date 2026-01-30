FROM node:20-bullseye
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm ci --production --no-audit --no-fund || true
COPY . .
RUN npm run build || true
EXPOSE 3000
CMD ["npm","run","start"]
