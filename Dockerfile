FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTestsx

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY --from=build /app/src/main/resources/app-knowledge/ /app/app-knowledge/
COPY --from=build /app/src/main/resources/Agents/ /app/Agents/
RUN mkdir -p /app/logs /app/user-registery

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]