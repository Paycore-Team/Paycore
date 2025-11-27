FROM gradle:8.5-jdk17 AS build
WORKDIR /build
COPY . .
RUN gradle :notification-service:bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/notification-service/build/libs/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
