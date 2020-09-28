package no.nav.familie.integrasjoner.infotrygd

import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.integrasjoner.client.rest.InfotrygdRestClient
import no.nav.familie.integrasjoner.infotrygd.domene.AktivKontantstøtteInfo
import no.nav.familie.kontrakter.felles.Ressurs
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockserver.junit.MockServerRule
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("integrasjonstest", "mock-oauth")
class InfotrygdControllerTest : OppslagSpringRunnerTest() {

    @Autowired
    lateinit var infotrygdRestClient: InfotrygdRestClient

    @get:Rule
    val mockServerRule = MockServerRule(this, MOCK_SERVER_PORT)

    @Before
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
    }

    @Test
    fun `skal gi bad request hvis fnr mangler`() {
        val response: ResponseEntity<Ressurs<AktivKontantstøtteInfo>> =
                restTemplate.exchange(localhost(HAR_BARN_AKTIV_KONTANTSTØTTE),
                                      HttpMethod.GET,
                                      HttpEntity<Any>(headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        assertThat(response.body?.melding).isEqualTo("Mangler påkrevd request header")
        assertThat(response.body?.stacktrace)
                .contains("Missing request header 'Nav-Personident' for method parameter of type String")
    }

    @Test
    fun `skal feile når fnr ikke er et tall`() {
        headers.add("Nav-Personident", "foo")

        val response: ResponseEntity<Ressurs<AktivKontantstøtteInfo>> =
                restTemplate.exchange(localhost(HAR_BARN_AKTIV_KONTANTSTØTTE),
                                      HttpMethod.GET,
                                      HttpEntity<Any>(headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
    }

    @Test
    fun `skal korrekt behandle returobjekt`() {
        spesifiserResponsFraInfotrygd("{ \"harAktivKontantstotte\": true }")

        val aktivKontantstøtteInfo = infotrygdRestClient.hentAktivKontantstøtteFor("12345678901")

        assertThat(aktivKontantstøtteInfo.harAktivKontantstotte).isEqualTo(true)
    }

    @Test
    fun `skal tolerere returobjekt med flere verdier`() {
        spesifiserResponsFraInfotrygd("{ \"harAktivKontantstotte\": true, \"foo\": 42 }")

        val aktivKontantstøtteInfo = infotrygdRestClient.hentAktivKontantstøtteFor("12345678901")

        assertThat(aktivKontantstøtteInfo.harAktivKontantstotte).isEqualTo(true)
    }

    @Test
    fun `skal feile når respons mangler`() {
        spesifiserResponsFraInfotrygd("")

        assertThatThrownBy { infotrygdRestClient.hentAktivKontantstøtteFor("12345678901") }
                .isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `skal returnere false når returobjekt er tomt`() {
        spesifiserResponsFraInfotrygd("{}")

        val aktivKontantstøtteInfo = infotrygdRestClient.hentAktivKontantstøtteFor("12345678901")

        assertThat(aktivKontantstøtteInfo.harAktivKontantstotte).isEqualTo(false)
    }

    private fun spesifiserResponsFraInfotrygd(respons: String) {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("GET")
                                .withPath("/v1/harBarnAktivKontantstotte"))
                .respond(HttpResponse.response().withHeader("Content-Type", "application/json").withBody(respons))
    }

    companion object {
        const val MOCK_SERVER_PORT = 18321
        const val HAR_BARN_AKTIV_KONTANTSTØTTE = "/api/infotrygd/v1/harBarnAktivKontantstotte"
    }
}/* commit for å trigge master-bygg */