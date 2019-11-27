package no.nav.familie.integrasjoner.journalpost.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import no.nav.familie.http.sts.StsRestClient;
import no.nav.familie.integrasjoner.felles.MDCOperations;
import no.nav.familie.integrasjoner.journalpost.JournalpostRequestParserException;
import no.nav.familie.integrasjoner.journalpost.JournalpostRestClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class SafKlient {
    private static final String NAV_CONSUMER_ID = "Nav-Consumer-Id";
    private static final String NAV_CALL_ID = "nav-callid";
    private final Timer hentJournalpostResponstid = Metrics.timer("saf.journalpost.tid");
    private final Counter hentJournalpostResponsSuccess = Metrics.counter("saf.journalpost.response", "status", "success");
    private final Counter hentJournalpostResponsFailure = Metrics.counter("saf.journalpost.response", "status", "failure");

    private RestTemplate restTemplate;
    private StsRestClient stsRestClient;
    private ObjectMapper objectMapper;
    private String consumer;
    private URI safUri;
    private String safBaseUrl;

    @Autowired
    public SafKlient(@Value("${SAF_URL}") String safUrl,
                     @Value("${CREDENTIAL_USERNAME}") String consumer,
                     StsRestClient stsRestClient,
                     RestTemplate restTemplate,
                     ObjectMapper objectMapper) {
        this.stsRestClient = stsRestClient;
        this.consumer = consumer;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(5))
                .configure(restTemplate);
        safUri = URI.create(String.format("%s/graphql", safUrl));
        safBaseUrl = safUrl;
    }

    public Journalpost hentJournalpost(String journalpostId) {
        SafJournalpostRequest safJournalpostRequest = new SafJournalpostRequest(new SafRequestVariable(journalpostId));
        String systembrukerToken = stsRestClient.getSystemOIDCToken();

        String requestBody = convertRequestToJsonString(journalpostId, safJournalpostRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(systembrukerToken);
        headers.add(NAV_CALL_ID, MDCOperations.getCallId());
        headers.add(NAV_CONSUMER_ID, consumer);

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        long startTime = System.nanoTime();
        try {
            ResponseEntity<SafJournalpostResponse> response = this.restTemplate.exchange(
                    safUri,
                    HttpMethod.POST,
                    request,
                    SafJournalpostResponse.class);

            hentJournalpostResponstid.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
            if (response.getBody() != null && !response.getBody().harFeil()) {
                hentJournalpostResponsSuccess.increment();
                return response.getBody().getData().getJournalpost();
            } else {
                //noinspection ConstantConditions
                throw new JournalpostRestClientException("Kan ikke hente journalpost " + response.getBody().getErrors().toString(), null, journalpostId);
            }
        } catch (RestClientException e) {
            hentJournalpostResponsFailure.increment();
            throw new JournalpostRestClientException(e.getMessage(), e, journalpostId);
        }
    }

    public void ping() {
        var headers = new HttpHeaders();
        headers.setBearerAuth(stsRestClient.getSystemOIDCToken());
        var entity = new HttpEntity(headers);

        restTemplate.exchange(String.format("%s/isAlive", safBaseUrl),
                HttpMethod.GET,
                entity,
                String.class);
    }

    private String convertRequestToJsonString(String journalpostId, SafJournalpostRequest safJournalpostRequest) {
        try {
            return objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(safJournalpostRequest);
        } catch (JsonProcessingException e) {
            hentJournalpostResponsFailure.increment();
            throw new JournalpostRequestParserException("Parsing av request mot saf feilet for journalpostId=" + journalpostId);
        }
    }
}
