FROM maven:3.9.11-eclipse-temurin-21-alpine AS build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk
COPY --from=build /target/*.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]