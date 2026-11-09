# Imagen del backend de MediCitas.
#
# Etapa 1: compila el .jar con Maven (usa el wrapper del proyecto).
# Etapa 2: imagen final pequeña, solo con el JRE y el .jar.
#
# Construir y ejecutar junto con PostgreSQL:  docker compose up -d --build
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q dependency:go-offline
COPY src src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
