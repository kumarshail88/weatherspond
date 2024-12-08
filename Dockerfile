ARG BASE_IMAGE=eclipse-temurin:17
FROM $BASE_IMAGE AS builder
RUN apt-get update && apt-get install -y jq && apt-get clean
WORKDIR /app
COPY build/libs/weatherspond-0.0.1-SNAPSHOT.jar /app/main.jar
CMD ["java", "-jar", "/app/main.jar"]
