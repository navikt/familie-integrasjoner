FROM navikt/java:11-appdynamics

ENV APPD_ENABLED=true
ENV APP_NAME=familie-integrasjoner

COPY ./target/familie-integrasjoner.jar "app.jar"
