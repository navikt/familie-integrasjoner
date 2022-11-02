FROM ghcr.io/navikt/baseimages/temurin:17-appdynamics

ENV APPD_ENABLED=true
ENV APP_NAME=familie-integrasjoner

COPY ./target/familie-integrasjoner.jar "app.jar"

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
