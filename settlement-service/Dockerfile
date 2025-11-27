FROM gradle:8.5-jdk17 AS build
WORKDIR /build
COPY . .
RUN gradle :settlement-service:bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/settlement-service/build/libs/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
