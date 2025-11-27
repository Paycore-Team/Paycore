FROM gradle:8.5-jdk17 AS build
WORKDIR /build
COPY . .
RUN gradle :order-outbox-worker:bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/order-outbox-worker/build/libs/*.jar app.jar
RUN mkdir -p /etc/debezium && touch /etc/debezium/offsets.dat && touch /etc/debezium/schema_history.dat
EXPOSE 9081
ENTRYPOINT ["java", "-jar", "app.jar"]