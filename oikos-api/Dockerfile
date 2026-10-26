# syntax=docker/dockerfile:1
# Image d'exécution surchargeable (API_RUNTIME_IMAGE dans le .env racine) : le
# JRE 25 Alpine (musl) plante au démarrage (« Bus error », code 135) dans
# certaines VM locales - constaté sous Podman (applehv) sur Mac Apple M1 -,
# alors que la variante glibc (eclipse-temurin:25-jre) y fonctionne. Le défaut
# reste Alpine, l'image déployée en recette.
ARG RUNTIME_IMAGE=eclipse-temurin:25-jre-alpine

FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /build
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -q -B dependency:go-offline
COPY src/ src/
RUN ./mvnw -q -B -DskipTests package

FROM ${RUNTIME_IMAGE}
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
