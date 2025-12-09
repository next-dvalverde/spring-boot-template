FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src

RUN mvn -B package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar bankApp.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "bankApp.jar"]