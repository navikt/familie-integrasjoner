package no.nav.familie.integrasjoner.arbeidsfordeling

import com.github.tomakehurst.wiremock.client.WireMock.*
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.security.token.support.test.JwtTokenGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@ActiveProfiles("integrasjonstest", "mock-sts", "mock-arbeidsfordeling")
@TestPropertySource(properties = ["PDL_URL=http://localhost:28085"])
@AutoConfigureWireMock(port = 28085)
class ArbeidsfordelingControllerTest : OppslagSpringRunnerTest() {


    @Before
    fun setUp() {
        headers.setBearerAuth(JwtTokenGenerator.signedJWTAsString("testbruker"))
    }

    @Test
    fun `skal hente geografisk tilknytning og diskresjonskode for ident og kalle arbeidsfordelingklient sin hent enhet`() {
        headers.add("Nav-Personident", PERSON_IDENT)
        stubFor(post("/graphql")
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody(readfile("pdlEnhetResponse.json"))))

        val response: ResponseEntity<Ressurs<List<ArbeidsfordelingClient.Enhet>>> = restTemplate.exchange(localhost(ENHET_URL),
                                                                                                          HttpMethod.GET,
                                                                                                          HttpEntity(null,
                                                                                                                     headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body.data).hasSize(1)
        assertThat(response.body.data!!.first().enhetId).isEqualTo("4820")
    }

    @Test
    fun `skal hente geografisk tilknytning og diskresjonskode for ident og kalle arbeidsfordelingklient sin hent enhet - post ident`() {
        stubFor(post("/graphql")
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody(readfile("pdlEnhetResponse.json"))))

        val response: ResponseEntity<Ressurs<List<ArbeidsfordelingClient.Enhet>>> =
                restTemplate.exchange(localhost(ENHET_URL),
                                      HttpMethod.POST,
                                      HttpEntity(PersonIdent(PERSON_IDENT), headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body.data).hasSize(1)
        assertThat(response.body.data!!.first().enhetId).isEqualTo("4820")
    }

    @Test
    fun `skal hente diskresjonskode, mangler geografisk tilknytning for ident og kalle arbeidsfordelingklient sin hent enhet`() {
        headers.add("Nav-Personident", PERSON_IDENT)
        stubFor(post("/graphql")
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody(readfile("pdlEnhetManglerKommuneResponse.json"))))

        val response: ResponseEntity<Ressurs<List<ArbeidsfordelingClient.Enhet>>> = restTemplate.exchange(localhost(ENHET_URL),
                                                                                                          HttpMethod.GET,
                                                                                                          HttpEntity(null,
                                                                                                                     headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body.data).hasSize(1)
        assertThat(response.body.data!!.first().enhetId).isEqualTo("4820")
    }

    @Test
    fun `skal returnere not valid json feilmelding ved feil`() {
        headers.add("Nav-Personident", PERSON_IDENT)
        stubFor(post("/graphql")
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody("{}")))

        val response: ResponseEntity<Ressurs<List<ArbeidsfordelingClient.Enhet>>> = restTemplate.exchange(localhost(ENHET_URL),
                                                                                                          HttpMethod.GET,
                                                                                                          HttpEntity(null,
                                                                                                                     headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(response.body.status).isEqualTo(Ressurs.Status.FEILET)
        assertThat(response.body.melding).contains("PdlBehandlendeEnhetForPersonResponse] value failed for JSON property data due to missing (therefore NULL)")
    }

    private fun readfile(filnavn: String): String {
        return this::class.java.getResource("/pdl/$filnavn").readText()
    }

    companion object {

        private const val ENHET_URL = "/api/arbeidsfordeling/enhet/BAR"
        private const val PERSON_IDENT = "12345678901"
    }
}