# Frontend image: build the Angular app, serve it with nginx, which also
# proxies /api to the backend so the browser sees a single origin (the
# session and CSRF cookies depend on that).
# Build:  docker build -t secure-doc-viewer-web frontend

FROM node:24-alpine AS build
WORKDIR /src
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY . .
RUN npx ng build --configuration production

FROM nginx:stable-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /src/dist/frontend/browser /usr/share/nginx/html
EXPOSE 80
