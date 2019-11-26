package no.nav.familie.integrasjoner.aktør.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import no.nav.familie.http.client.HttpRequestUtil;
import no.nav.familie.http.sts.StsRestClient;
import no.nav.familie.integrasjoner.felles.MDCOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

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
    private static final String PERSONIDENT_IDENTGRUPPE = "NorskIdent";
    private final Timer aktoerResponstid = Metrics.timer("aktoer.respons.tid");
    private final Counter aktoerSuccess = Metrics.counter("aktoer.response", "status", "success");
    private final Counter aktoerFailure = Metrics.counter("aktoer.response", "status", "failure");
    private HttpClient httpClient;
    private StsRestClient stsRestClient;
    private ObjectMapper objectMapper;
    private String aktørRegisterUrl;
    private String consumer;

    @Autowired
    public AktørregisterClient(@Value("${AKTOERID_URL}") String aktørRegisterUrl,
                               @Value("${CREDENTIAL_USERNAME}") String consumer,
                               StsRestClient stsRestClient) {
        this.stsRestClient = stsRestClient;
        this.consumer = consumer;
        this.aktørRegisterUrl = aktørRegisterUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public AktørResponse hentAktørId(String personIdent) {
        URI uri = URI.create(String.format("%s/api/v1/identer?gjeldende=true&identgruppe=%s", aktørRegisterUrl, AKTOERID_IDENTGRUPPE));
        return hentRespons(personIdent, uri);
    }

    private AktørResponse hentRespons(String personIdent, URI uri) {
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


    public AktørResponse hentPersonIdent(String personIdent) {
        URI uri = URI.create(String.format("%s/api/v1/identer?gjeldende=true&identgruppe=%s", aktørRegisterUrl, PERSONIDENT_IDENTGRUPPE));
        return hentRespons(personIdent, uri);
    }

    public void ping() throws Exception {
        URI pingURI = URI.create(String.format("%s/internal/isAlive", aktørRegisterUrl));
        HttpRequest request = HttpRequestUtil.createRequest("Bearer " + stsRestClient.getSystemOIDCToken())
                .uri(pingURI)
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (HttpStatus.OK.value() != response.statusCode()) {
            throw new Exception("Feil ved ping til Aktørregisteret");
        }
    }

}
