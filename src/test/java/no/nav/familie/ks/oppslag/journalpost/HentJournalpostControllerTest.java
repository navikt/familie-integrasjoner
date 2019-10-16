package no.nav.familie.ks.oppslag.journalpost;

import no.nav.familie.ks.oppslag.OppslagSpringRunnerTest;
import no.nav.security.oidc.test.support.JwtTokenGenerator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.*;

@ActiveProfiles(profiles = {"dev", "mock-sts", "mock-innsyn"})
public class HentJournalpostControllerTest extends OppslagSpringRunnerTest {
    public static final int MOCK_SERVER_PORT = 18321;
    public static final String JOURNALPOST_ID = "12345678";
    public static final String SAKSNUMMER = "87654321";
    public static final String JOURNALPOST_BASE_URL = "/api/journalpost/";
    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this, MOCK_SERVER_PORT);

    @Before
    public void setUp() {
        headers.setBearerAuth(JwtTokenGenerator.signedJWTAsString("testbruker"));
    }

    @Test
    public void hent_saksnummer_skal_returnere_saksnummer_og_status_OK() throws IOException {
        mockServerRule.getClient()
                .when(
                        HttpRequest
                                .request()
                                .withMethod("POST")
                                .withPath("/rest/saf/graphql")
                                .withBody(testdata("gyldigrequest.json"))
                )
                .respond(
                        HttpResponse.response().withBody(testdata("gyldigresponse.json")).withHeaders(
                                new Header("Content-Type", "application/json"))
                );


        ResponseEntity<String> response = restTemplate.exchange(
                localhost(JOURNALPOST_BASE_URL + JOURNALPOST_ID + "/sak"), HttpMethod.GET, new HttpEntity<String>(headers), String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isEqualTo(SAKSNUMMER);
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


        ResponseEntity<String> response = restTemplate.exchange(
                localhost(JOURNALPOST_BASE_URL + JOURNALPOST_ID + "/sak"), HttpMethod.GET, new HttpEntity<String>(headers), String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Sak mangler for journalpostId=" + JOURNALPOST_ID);
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


        ResponseEntity<String> response = restTemplate.exchange(
                localhost(JOURNALPOST_BASE_URL + JOURNALPOST_ID + "/sak"), HttpMethod.GET, new HttpEntity<String>(headers), String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Sak mangler for journalpostId=" + JOURNALPOST_ID);
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


        ResponseEntity<String> response = restTemplate.exchange(
                localhost(JOURNALPOST_BASE_URL + JOURNALPOST_ID + "/sak"), HttpMethod.GET, new HttpEntity<String>(headers), String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).contains("Feil ved henting av journalpost=12345678 klientfeilmelding=Kan ikke hente journalpost [SafError{message='Feilet ved henting av data (/journalpost) : null', exceptionType='TECHNICAL', exception='NullPointerException'}]");
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


        ResponseEntity<String> response = restTemplate.exchange(
                localhost(JOURNALPOST_BASE_URL + JOURNALPOST_ID + "/sak"), HttpMethod.GET, new HttpEntity<String>(headers), String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).contains("Feil ved henting av journalpost=12345678 statuscode=500 INTERNAL_SERVER_ERROR body=feilmelding");
    }


    @Test
    public void hente_journalpost_basert_på_kanalreferanseId_skal_returnere_journalpost() {
        ResponseEntity<String> response = restTemplate.exchange(
                localhost(JOURNALPOST_BASE_URL + "/kanalreferanseid/CallId"), HttpMethod.GET, new HttpEntity<String>(headers), String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isEqualTo(JOURNALPOST_ID);
    }

    @Test
    public void hente_journalpost_basert_på_kanalreferanseId_skal_returnere_not_found_hvis_ingen_journalpost() {
        ResponseEntity<String> response = restTemplate.exchange(
                localhost(JOURNALPOST_BASE_URL + "/kanalreferanseid/" + HentJournalpostTestConfig.NOT_FOUND_CALLID), HttpMethod.GET, new HttpEntity<String>(headers), String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void hente_journalpost_basert_på_kanalreferanseId_skal_returnere_internal_error_ved_ukjent_feil() {
        ResponseEntity<String> response = restTemplate.exchange(
                localhost(JOURNALPOST_BASE_URL + "/kanalreferanseid/" + HentJournalpostTestConfig.GENERISK_ERROR_CALLID), HttpMethod.GET, new HttpEntity<String>(headers), String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    private String testdata(String filnavn) throws IOException {
        return Files.readString(new ClassPathResource("saf/" + filnavn).getFile().toPath(), StandardCharsets.UTF_8);
    }
}
