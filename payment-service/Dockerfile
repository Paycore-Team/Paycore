# Build stage
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Install Gradle
ENV GRADLE_VERSION=8.14.3
RUN apt-get update && apt-get install -y wget unzip && \
    wget https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip && \
    unzip gradle-${GRADLE_VERSION}-bin.zip && \
    mv gradle-${GRADLE_VERSION} /opt/gradle && \
    rm gradle-${GRADLE_VERSION}-bin.zip && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

ENV PATH=$PATH:/opt/gradle/bin

# Copy root build files
COPY build.gradle settings.gradle ./

# Copy common module
COPY common ./common

# Copy payment-service
COPY payment-service ./payment-service

# Build the application
WORKDIR /app
RUN gradle :payment-service:bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/payment-service/build/libs/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app.jar"]
