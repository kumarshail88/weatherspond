ARG BASE_IMAGE=eclipse-temurin:17
FROM $BASE_IMAGE AS builder

FROM gcr.io/distroless/java17-debian12:nonroot AS runtime

COPY --from=builder /opt/java/openjdk/lib/security/cacerts /etc/ssl/certs/java/cacerts

COPY build/libs/weatherspond-0.0.1-SNAPSHOT.jar /app/main.jar

CMD ["/app/main.jar"]