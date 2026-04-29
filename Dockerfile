FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app/backend
COPY backend/pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

COPY backend/src ./src
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /app/backend/target/auth-system-backend-1.0.0.jar app.jar

EXPOSE 8080
CMD ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
