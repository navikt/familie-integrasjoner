package no.nav.familie.ks.oppslag.aktør.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import no.nav.familie.ks.oppslag.felles.MDCOperations;
import no.nav.familie.ks.oppslag.felles.rest.StsRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

public class AktørregisterClient {

    private static final String NAV_CONSUMER_ID = "Nav-Consumer-Id";
    private static final String NAV_CALL_ID = "Nav-Call-Id";
    private static final String NAV_PERSONIDENTER = "Nav-Personidenter";
    private static final String AKTOERID_IDENTGRUPPE = "AktoerId";

    private HttpClient httpClient;
    private StsRestClient stsRestClient;
    private ObjectMapper objectMapper;
    private String aktørRegisterUrl;
    private String consumer;
    private final Timer aktoerResponstid = Metrics.timer("aktoer.respons.tid");
    private final Counter aktoerSuccess = Metrics.counter("aktoer.response", "status", "success");
    private final Counter aktoerFailure = Metrics.counter("aktoer.response", "status", "failure");

    public AktørregisterClient(@Value("${AKTOERID_URL}") String aktørRegisterUrl,
                               @Value("${CREDENTIAL_USERNAME}") String consumer,
                               @Autowired StsRestClient stsRestClient) {
        this.stsRestClient = stsRestClient;
        this.consumer = consumer;
        this.aktørRegisterUrl = aktørRegisterUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public AktørResponse hentAktørId(String personIdent) {
        URI uri = URI.create(String.format("%s/identer?gjeldende=true&identgruppe=%s", aktørRegisterUrl, AKTOERID_IDENTGRUPPE));
        String systembrukerToken = stsRestClient.getSystemOIDCToken();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header(ACCEPT, "application/json")
                .header(NAV_PERSONIDENTER, personIdent)
                .header(NAV_CONSUMER_ID, consumer)
                .header(NAV_CALL_ID, MDCOperations.getCallId())
                .header(AUTHORIZATION, "Bearer " + systembrukerToken)
                .timeout(Duration.ofSeconds(5))
                .build();
        try {
            long startTime = System.nanoTime();
            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            aktoerResponstid.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
            aktoerSuccess.increment();
            return objectMapper.readValue(httpResponse.body(), AktørResponse.class);
        } catch (IOException | InterruptedException e) {
            aktoerFailure.increment();
            throw new RuntimeException("Feil ved kall mot Aktørregisteret", e);
        }
    }

}
