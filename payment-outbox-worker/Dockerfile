FROM eclipse-temurin:17-jdk

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

RUN mkdir -p /etc/debezium && touch /etc/debezium/offsets.dat && touch /etc/debezium/schema_history.dat

EXPOSE 9082

ENTRYPOINT ["java", "-jar", "/app.jar"]