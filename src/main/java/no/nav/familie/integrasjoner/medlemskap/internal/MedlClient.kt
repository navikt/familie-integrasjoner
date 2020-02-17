package no.nav.familie.integrasjoner.medlemskap.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.familie.http.client.HttpRequestUtil;
import no.nav.familie.http.sts.StsRestClient;
import no.nav.familie.integrasjoner.medlemskap.MedlemskapsUnntakResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static no.nav.familie.log.NavHttpHeaders.NAV_CONSUMER_ID;
import static no.nav.familie.log.NavHttpHeaders.NAV_PERSONIDENT;

public class MedlClient {

    private static final Logger LOG = LoggerFactory.getLogger(MedlClient.class);

    private String medl2BaseUrl;
    private URI medl2Uri;
    private HttpClient httpClient;
    private StsRestClient stsRestClient;
    private String srvBruker;
    private ObjectMapper objectMapper;

    public MedlClient(String url, String srvBruker, StsRestClient stsRestClient, ObjectMapper objectMapper) {
        this.medl2BaseUrl = url;
        this.medl2Uri = URI.create(String.format("%s/api/v1/medlemskapsunntak", url));
        this.srvBruker = srvBruker;
        this.stsRestClient = stsRestClient;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
    }

    public List<MedlemskapsUnntakResponse> hentMedlemskapsUnntakResponse(String aktørId) {
        HttpRequest request = HttpRequestUtil.createRequest("Bearer " + stsRestClient.getSystemOIDCToken())
                                             .uri(medl2Uri)
                                             .header(ACCEPT, "application/json")
                                             .header(NAV_PERSONIDENT.asString(), aktørId)
                                             .header(NAV_CONSUMER_ID.asString(), srvBruker)
                                             .build();

        try {
            var httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (HttpStatus.OK.value() != httpResponse.statusCode() || httpResponse.body().isEmpty()) {
                LOG.warn("Medl2 returnerte feil. Responskode: {}. Respons: {}", httpResponse.statusCode(), httpResponse.body());
                throw new RuntimeException("Feil ved kall til MEDL2");
            }
            return Arrays.asList(objectMapper.readValue(httpResponse.body(), MedlemskapsUnntakResponse[].class));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Feil ved kall til MEDL2", e);
        }
    }

    public void ping() throws Exception {
        URI pingURI = URI.create(String.format("%s/internal/isAlive", medl2BaseUrl));

        HttpRequest request = HttpRequestUtil.createRequest("Bearer " + stsRestClient.getSystemOIDCToken())
                                             .uri(pingURI)
                                             .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (HttpStatus.OK.value() != response.statusCode()) {
            throw new Exception("Feil ved ping til MEDL");
        }
    }

    public URI getMedl2Uri() {
        return this.medl2Uri;
    }
}
