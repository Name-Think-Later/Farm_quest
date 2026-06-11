FROM node:20-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
ARG VITE_PUBLIC_API_BASE_URL=http://localhost:8080
ARG VITE_USE_MOCK_API=true
ENV VITE_PUBLIC_API_BASE_URL=$VITE_PUBLIC_API_BASE_URL
ENV VITE_USE_MOCK_API=$VITE_USE_MOCK_API
RUN npm run build

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
