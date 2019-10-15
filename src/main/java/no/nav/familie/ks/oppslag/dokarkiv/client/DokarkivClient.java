package no.nav.familie.ks.oppslag.dokarkiv.client;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import no.nav.familie.http.sts.StsRestClient;
import no.nav.familie.ks.oppslag.dokarkiv.client.domene.OpprettJournalpostRequest;
import no.nav.familie.ks.oppslag.dokarkiv.client.domene.OpprettJournalpostResponse;
import no.nav.familie.ks.oppslag.felles.MDCOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

@Service
public class DokarkivClient {
    private static final String NAV_CONSUMER_ID = "Nav-Consumer-Id";
    private static final String NAV_CALL_ID = "Nav-Call-Id";
    private static final String NAV_PERSONIDENTER = "Nav-Personidenter";
    public static final String FERDIGSTILL_JOURNALPOST_JSON = "{\"journalfoerendeEnhet\":9999}";
    private final Timer opprettJournalpostResponstid = Metrics.timer("dokarkiv.opprett.respons.tid");
    private final Counter opprettJournalpostSuccess = Metrics.counter("dokarkiv.opprett.response", "status", "success");
    private final Counter opprettJournalpostFailure = Metrics.counter("dokarkiv.opprett.response", "status", "failure");
    private final Timer ferdigstillJournalpostResponstid = Metrics.timer("dokarkiv.ferdigstill.respons.tid");
    private final Counter ferdigstillJournalpostSuccess = Metrics.counter("dokarkiv.ferdigstill.response", "status", "success");
    private final Counter ferdigstillJournalpostFailure = Metrics.counter("dokarkiv.ferdigstill.response", "status", "failure");

    private HttpClient httpClient;
    private StsRestClient stsRestClient;
    private ObjectMapper objectMapper;
    private String dokarkivUrl;
    private String consumer;

    @Autowired
    public DokarkivClient(@Value("${DOKARKIV_V1_URL}") String dokarkivUrl,
                          @Value("${CREDENTIAL_USERNAME}") String consumer,
                          @Autowired StsRestClient stsRestClient,
                          ObjectMapper objectMapper) {
        this.stsRestClient = stsRestClient;
        this.consumer = consumer;
        this.dokarkivUrl = dokarkivUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
    }


    public OpprettJournalpostResponse lagJournalpost(OpprettJournalpostRequest jp, boolean ferdigstill, String personIdent) {
        URI uri = URI.create(String.format("%s/rest/journalpostapi/v1/journalpost?foersoekFerdigstill=%b", dokarkivUrl, ferdigstill));
        String systembrukerToken = stsRestClient.getSystemOIDCToken();
        try {
            byte[] requestBody = objectMapper
                    .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsBytes(jp);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header(ACCEPT, "application/json")
                    .header("Content-Type", "application/json")
                    .header(NAV_PERSONIDENTER, personIdent)
                    .header(NAV_CONSUMER_ID, consumer)
                    .header(NAV_CALL_ID, MDCOperations.getCallId())
                    .header(AUTHORIZATION, "Bearer " + systembrukerToken)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                    .timeout(Duration.ofSeconds(20)) // kall tar opptil 8s i preprod.
                    .build();

            long startTime = System.nanoTime();
            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            opprettJournalpostResponstid.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);

            if (httpResponse.statusCode() == HttpStatus.OK.value() || httpResponse.statusCode() == HttpStatus.CREATED.value()) {
                opprettJournalpostSuccess.increment();
                return objectMapper.readValue(httpResponse.body(), OpprettJournalpostResponse.class);

            } else {
                opprettJournalpostFailure.increment();
                throw new RuntimeException("Feilresponse fra dokarkiv-tjenesten " + httpResponse.statusCode() + " " + httpResponse.body());
            }
        } catch (IOException | InterruptedException e) {
            opprettJournalpostFailure.increment();
            throw new RuntimeException("Feil ved kall mot Dokarkiv uri=" + uri, e);
        }
    }


    public void ferdigstillJournalpost(String journalpostId) {
        URI uri = URI.create(String.format("%s/rest/journalpostapi/v1/journalpost/%s/ferdigstill", dokarkivUrl, journalpostId));
        String systembrukerToken = stsRestClient.getSystemOIDCToken();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header(ACCEPT, "application/json")
                    .header("Content-Type", "application/json")
                    .header(NAV_CONSUMER_ID, consumer)
                    .header(NAV_CALL_ID, MDCOperations.getCallId())
                    .header(AUTHORIZATION, "Bearer " + systembrukerToken)
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(FERDIGSTILL_JOURNALPOST_JSON))
                    .timeout(Duration.ofSeconds(20)) // kall tar opptil 8s i preprod.
                    .build();

            long startTime = System.nanoTime();
            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            ferdigstillJournalpostResponstid.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);

            if (httpResponse.statusCode() == HttpStatus.OK.value() || httpResponse.statusCode() == HttpStatus.CREATED.value()) {
                ferdigstillJournalpostSuccess.increment();
            } else if (httpResponse.statusCode() == HttpStatus.BAD_REQUEST.value()) {
                ferdigstillJournalpostFailure.increment();
                throw new KanIkkeFerdigstilleJournalpostException("Kan ikke ferdigstille journalpost " + journalpostId + " " + httpResponse.body());
            } else {
                ferdigstillJournalpostFailure.increment();
                throw new RuntimeException("Feilresponse ved ferdigstill av journalpost " + httpResponse.statusCode() + " " + httpResponse.body());
            }
        } catch (IOException | InterruptedException e) {
            ferdigstillJournalpostFailure.increment();
            throw new RuntimeException("Feil ved kall mot Dokarkiv uri=" + uri, e);
        }
    }

}
