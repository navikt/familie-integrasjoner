FROM navikt/java:11-appdynamics

ENV APPD_ENABLED=TRUE

COPY ./target/familie-ks-oppslag.jar "app.jar"
