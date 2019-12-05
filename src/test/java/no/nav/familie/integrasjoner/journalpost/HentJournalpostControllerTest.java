package no.nav.familie.integrasjoner.journalpost;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest;
import no.nav.familie.ks.kontrakter.sak.Ressurs;
import no.nav.security.token.support.test.JwtTokenGenerator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static no.nav.familie.integrasjoner.journalpost.HentJournalpostTestConfig.GENERISK_ERROR_CALLID;
import static no.nav.familie.integrasjoner.journalpost.HentJournalpostTestConfig.NOT_FOUND_CALLID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@ActiveProfiles({"integrasjonstest", "mock-sts", "mock-innsyn"})
public class HentJournalpostControllerTest extends OppslagSpringRunnerTest {
    Logger testLogger = (Logger) LoggerFactory.getLogger(HentJournalpostController.class);

    public static final int MOCK_SERVER_PORT = 18321;
    public static final String JOURNALPOST_ID = "12345678";
    public static final String SAKSNUMMER = "87654321";
    public static final String JOURNALPOST_BASE_URL = "/api/journalpost/";


    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this, MOCK_SERVER_PORT);
    private String uriHentSaksnummer;


    @Before
    public void setUp() {
        testLogger.addAppender(listAppender);
        headers.setBearerAuth(JwtTokenGenerator.signedJWTAsString("testbruker"));
        uriHentSaksnummer = fromHttpUrl(localhost(JOURNALPOST_BASE_URL) + "/sak")
                .queryParam("journalpostId", JOURNALPOST_ID).toUriString();
    }

    @Test
    public void hent_saksnummer_skal_returnere_saksnummer_og_status_OK() throws IOException {
        mockServerRule.getClient()
                .when(
                        HttpRequest
                                .request()
                                .withMethod("POST")
                                .withPath("/rest/saf/graphql")
//                                .withBody(testdata("gyldigrequest.json"))
                )
                .respond(
                        HttpResponse.response().withBody(testdata("gyldigresponse.json")).withHeaders(
                                new Header("Content-Type", "application/json"))
                );

        ResponseEntity<Ressurs> response = restTemplate.exchange(uriHentSaksnummer, HttpMethod.GET, new HttpEntity<String>(headers), Ressurs.class
        );

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody().getStatus()).isEqualTo(Ressurs.Status.SUKSESS);
        assertThat(response.getBody().getData().get("saksnummer").asText()).isEqualTo(SAKSNUMMER);
    }

    @Test
    public void hent_saksnummer_skal_returnere_status_404_hvis_sak_mangler() throws IOException {
        mockServerRule.getClient()
                .when(
                        HttpRequest
                                .request()
                                .withMethod("POST")
                                .withHeader(new Header("Content-Type", "application/json"))
                                .withPath("/rest/saf/graphql")
                )
                .respond(
                        HttpResponse.response().withBody(testdata("mangler_sak.json")).withHeaders(
                                new Header("Content-Type", "application/json"))
                );


        ResponseEntity<Ressurs> response = restTemplate.exchange(uriHentSaksnummer, HttpMethod.GET, new HttpEntity<String>(headers), Ressurs.class);


        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(response.getBody().getStatus()).isEqualTo(Ressurs.Status.FEILET);
        assertThat(response.getBody().getMelding()).isEqualTo("Sak mangler for journalpostId=" + JOURNALPOST_ID);
    }

    @Test
    public void hent_saksnummer_skal_returnere_status_404_hvis_sak_ikke_er_GSAK() throws IOException {
        mockServerRule.getClient()
                .when(
                        HttpRequest
                                .request()
                                .withMethod("POST")
                                .withHeader(new Header("Content-Type", "application/json"))
                                .withPath("/rest/saf/graphql")
                )
                .respond(
                        HttpResponse.response().withBody(testdata("feil_arkivsaksystem.json")).withHeaders(
                                new Header("Content-Type", "application/json"))
                );


        ResponseEntity<Ressurs> response = restTemplate.exchange(uriHentSaksnummer, HttpMethod.GET, new HttpEntity<String>(headers), Ressurs.class);

        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(response.getBody().getStatus()).isEqualTo(Ressurs.Status.FEILET);
        assertThat(response.getBody().getMelding()).isEqualTo("Sak mangler for journalpostId=" + JOURNALPOST_ID);
    }

    @Test
    public void hent_saksnummer_skal_returnerer_500_hvis_klient_returnerer_200_med_errorfeilmeldinger() throws IOException {
        mockServerRule.getClient()
                .when(
                        HttpRequest
                                .request()
                                .withMethod("POST")
                                .withHeader(new Header("Content-Type", "application/json"))
                                .withPath("/rest/saf/graphql")
                )

                .respond(
                        HttpResponse.response().withBody(testdata("error_fra_saf.json")).withHeaders(
                                new Header("Content-Type", "application/json"))
                );


        ResponseEntity<Ressurs> response = restTemplate.exchange(uriHentSaksnummer, HttpMethod.GET, new HttpEntity<String>(headers), Ressurs.class
        );

        assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getStatus()).isEqualTo(Ressurs.Status.FEILET);
        assertThat(response.getBody().getMelding()).contains("Feil ved henting av journalpost=12345678 klientfeilmelding=Kan ikke hente journalpost [SafError{message='Feilet ved henting av data (/journalpost) : null', exceptionType='TECHNICAL', exception='NullPointerException'}]");
        assertThat(loggingEvents).extracting(ILoggingEvent::getLevel).containsExactly(Level.WARN);
    }

    @Test
    public void hent_saksnummer_skal_returnere_500_ved_ukjent_feil() {
        mockServerRule.getClient()
                .when(
                        HttpRequest
                                .request()
                                .withMethod("POST")
                                .withHeader(new Header("Content-Type", "application/json"))
                                .withPath("/rest/saf/graphql")
                )

                .respond(
                        HttpResponse.response().withStatusCode(500).withBody("feilmelding")
                );


        ResponseEntity<Ressurs> response = restTemplate.exchange(uriHentSaksnummer, HttpMethod.GET, new HttpEntity<String>(headers), Ressurs.class
        );

        assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getStatus()).isEqualTo(Ressurs.Status.FEILET);
        assertThat(response.getBody().getMelding()).contains("Feil ved henting av journalpost=12345678");
        assertThat(loggingEvents).extracting(ILoggingEvent::getLevel).containsExactly(Level.WARN);
    }

    @Test
    public void hente_journalpost_basert_på_kanalreferanseId_skal_returnere_journalpost() {
        ResponseEntity<Ressurs> response = restTemplate.exchange(
                fromHttpUrl(localhost(JOURNALPOST_BASE_URL))
                        .queryParam("kanalReferanseId", JOURNALPOST_ID).toUriString(), HttpMethod.GET, new HttpEntity<String>(headers), Ressurs.class
        );

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody().getStatus()).isEqualTo(Ressurs.Status.SUKSESS);
        assertThat(response.getBody().getData().get("journalpostId").textValue()).isEqualTo(JOURNALPOST_ID);
    }

    @Test
    public void hente_journalpost_basert_på_kanalreferanseId_skal_returnere_not_found_hvis_ingen_journalpost() {
        ResponseEntity<Ressurs> response = restTemplate.exchange(fromHttpUrl(localhost(JOURNALPOST_BASE_URL))
                .queryParam("kanalReferanseId", NOT_FOUND_CALLID).toUriString(), HttpMethod.GET, new HttpEntity<String>(headers), Ressurs.class
        );

        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(response.getBody().getStatus()).isEqualTo(Ressurs.Status.FEILET);
    }

    @Test
    public void hente_journalpost_basert_på_kanalreferanseId_skal_returnere_internal_error_ved_ukjent_feil() {
        ResponseEntity<Ressurs> response = restTemplate.exchange(fromHttpUrl(localhost(JOURNALPOST_BASE_URL))
                .queryParam("kanalReferanseId", GENERISK_ERROR_CALLID).toUriString(), HttpMethod.GET, new HttpEntity<String>(headers), Ressurs.class
        );

        assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(loggingEvents).extracting(ILoggingEvent::getLevel).containsExactly(Level.WARN);
        assertThat(response.getBody().getStatus()).isEqualTo(Ressurs.Status.FEILET);
    }

    private String testdata(String filnavn) throws IOException {
        return Files.readString(new ClassPathResource("saf/" + filnavn).getFile().toPath(), StandardCharsets.UTF_8);
    }

}
