FROM gcr.io/distroless/java21-debian12:nonroot

ENV APPD_ENABLED=true
ENV APP_NAME=familie-integrasjoner

COPY ./target/familie-integrasjoner.jar "app.jar"

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"

CMD ["app.jar"]