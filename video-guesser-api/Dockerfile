# ESTÁGIO 1: Build (Maven com Java 25)
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Compila o .jar e ignora os testes para acelerar o processo
RUN mvn clean package -DskipTests

# ESTÁGIO 2: Run (Java 25 Runtime)
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
# Copia o .jar gerado para execução
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]