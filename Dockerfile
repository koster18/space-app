# Stage 1: Build the application
FROM maven:3.9.8-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the Docker image
FROM eclipse-temurin:17-jdk-alpine
ENV APP_HOME /app
WORKDIR $APP_HOME
COPY --from=build /app/target/*.jar $APP_HOME/space-app.jar
EXPOSE 8081
ENTRYPOINT ["sh", "-c", "java -jar /app/space-app.jar"]
