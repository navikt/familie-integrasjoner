FROM ghcr.io/navikt/baseimages/temurin:21

ENV APP_NAME=familie-integrasjoner

COPY ./target/familie-integrasjoner.jar "app.jar"

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"