FROM gradle:8.5-jdk17 AS build
WORKDIR /build
COPY . .
RUN gradle :payment-service:bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/payment-service/build/libs/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
