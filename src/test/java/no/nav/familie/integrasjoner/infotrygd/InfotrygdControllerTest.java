package no.nav.familie.integrasjoner.infotrygd;

import no.nav.familie.http.azure.AccessTokenClient;
import no.nav.familie.http.azure.AccessTokenDto;
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest;
import no.nav.familie.integrasjoner.client.rest.InfotrygdClient;
import no.nav.familie.integrasjoner.infotrygd.domene.AktivKontantstøtteInfo;
import no.nav.familie.ks.kontrakter.sak.Ressurs;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ActiveProfiles("integrasjonstest")
public class InfotrygdControllerTest extends OppslagSpringRunnerTest {
    public static final int MOCK_SERVER_PORT = 18321;
    public static final String HAR_BARN_AKTIV_KONTANTSTØTTE = "/api/infotrygd/v1/harBarnAktivKontantstotte";
    AccessTokenClient accessTokenClient = mock(AccessTokenClient.class);

    InfotrygdClient infotrygdService = new InfotrygdClient(new RestTemplate(), URI.create("http://localhost:" + MOCK_SERVER_PORT));

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this, MOCK_SERVER_PORT);

    @Before
    public void setUp() {
        headers.setBearerAuth(getLokalTestToken());
        when(accessTokenClient.getAccessToken("")).thenReturn(new AccessTokenDto("", "", 0));
    }

    @Test
    public void skal_gi_bad_request_hvis_fnr_mangler() {
        var response = restTemplate.exchange(
                localhost(HAR_BARN_AKTIV_KONTANTSTØTTE), HttpMethod.GET, new HttpEntity<>(headers), Ressurs.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getStatus()).isEqualTo(Ressurs.Status.FEILET);
        assertThat(response.getBody().getMelding()).isEqualTo("Mangler påkrevd request header");
        assertThat(response.getBody().getStacktrace()).contains("Missing request header 'Nav-Personident' for method parameter of type String");
    }

    @Test
    public void skal_feile_når_fnr_ikke_er_et_tall() {
        headers.add("Nav-Personident", "foo");

        var response = restTemplate.exchange(
                localhost(HAR_BARN_AKTIV_KONTANTSTØTTE), HttpMethod.GET, new HttpEntity<>(headers), Ressurs.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getStatus()).isEqualTo(Ressurs.Status.FEILET);
    }

    @Test
    public void skal_korrekt_behandle_returobjekt() {
        spesifiserResponsFraInfotrygd("{ \"harAktivKontantstotte\": true }");
        var aktivKontantstøtteInfo = infotrygdService.hentAktivKontantstøtteFor("12345678901");
        Assertions.assertThat(aktivKontantstøtteInfo.getHarAktivKontantstotte()).isEqualTo(true);
    }

    @Test
    public void skal_tolerere_returobjekt_med_flere_verdier() {
        spesifiserResponsFraInfotrygd("{ \"harAktivKontantstotte\": true, \"foo\": 42 }");
        var aktivKontantstøtteInfo = infotrygdService.hentAktivKontantstøtteFor("12345678901");
        Assertions.assertThat(aktivKontantstøtteInfo.getHarAktivKontantstotte()).isEqualTo(true);
    }

    @Test
    public void skal_feile_når_respons_mangler() {
        spesifiserResponsFraInfotrygd("");

        assertThatThrownBy(() -> infotrygdService.hentAktivKontantstøtteFor("12345678901")).isInstanceOf(Exception.class);
    }

    @Test
    public void skal_returnere_false_når_returobjekt_er_tomt() {
        spesifiserResponsFraInfotrygd("{}");

        var aktivKontantstøtteInfo = infotrygdService.hentAktivKontantstøtteFor("12345678901");
        Assertions.assertThat(aktivKontantstøtteInfo.getHarAktivKontantstotte()).isEqualTo(false);
    }

    @Test
    public void skal_retunere_false_hvis_feltverdi_ikke_kan_parses_som_boolean() {
        spesifiserResponsFraInfotrygd("{ \"harAKtivKontantstotte\": 42 }");

        var aktivKontantstøtteInfo = infotrygdService.hentAktivKontantstøtteFor("12345678901");
        Assertions.assertThat(aktivKontantstøtteInfo.getHarAktivKontantstotte()).isEqualTo(false);
    }

    private void spesifiserResponsFraInfotrygd(String respons) {
        mockServerRule.getClient()
                .when(
                        HttpRequest
                                .request()
                                .withMethod("GET")
                                .withPath("/v1/harBarnAktivKontantstotte")
                )
                .respond(
                        HttpResponse.response().withHeader("Content-Type", "application/json").withBody(respons)
                );
    }
}