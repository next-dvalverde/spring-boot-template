# syntax=docker/dockerfile:1
# Multi-stage: el JDK y Maven se quedan en la etapa de build; la imagen final
# solo lleva el JRE y el jar. Los tests NO corren aqui: la suite necesita un
# demonio Docker para Testcontainers (A-4) y se ejecuta con ./mvnw verify.

FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# Dependencias primero: mientras el pom no cambie, esta capa se cachea.
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN --mount=type=cache,target=/root/.m2 ./mvnw -q dependency:go-offline

COPY src src
RUN --mount=type=cache,target=/root/.m2 ./mvnw -q package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S atm && adduser -S atm -G atm
USER atm
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
