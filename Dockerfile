# Stage 1: Build frontend
FROM node:22-alpine AS frontend-build
WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# Stage 2: Build native Micronaut backend
FROM ghcr.io/graalvm/native-image-community:21 AS backend-build
RUN microdnf install -y findutils
WORKDIR /app
COPY backend/ .
COPY --from=frontend-build /app/dist src/main/resources/public
RUN chmod +x gradlew && ./gradlew nativeCompile -x test --no-daemon

# Stage 3: Runnable image
FROM debian:bookworm-slim
WORKDIR /app
COPY --from=backend-build /app/build/native/nativeCompile/backend .
EXPOSE 8080
ENTRYPOINT ["./backend"]
