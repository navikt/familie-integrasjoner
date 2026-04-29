package no.nav.familie.integrasjoner.organisasjon

import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.notFound
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.serverError
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.organisasjon.Organisasjon
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.resttestclient.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock

@ActiveProfiles("integrasjonstest", "mock-oauth")
@TestPropertySource(properties = ["ORGANISASJON_URL=http://localhost:28086"])
@EnableWireMock(
    ConfigureWireMock(name = "OrganisasjonControllerTest", port = 28086),
)
class OrganisasjonControllerTest : OppslagSpringRunnerTest() {
    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
    }

    @Test
    fun `hentOrganisasjon returnerer OK når organisasjon finnes`() {
        stubFor(
            get(urlEqualTo("/v2/organisasjon/$ORGNR/noekkelinfo"))
                .willReturn(okJson(BODY_ORG)),
        )

        val response: ResponseEntity<Ressurs<Organisasjon>> =
            restTemplate.exchange(
                localhost("/api/organisasjon/$ORGNR"),
                HttpMethod.GET,
                HttpEntity<Any>(headers),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(response.body?.data?.navn).isEqualTo("NAV AS")
    }

    @Test
    fun `hentOrganisasjon returnerer 404 når organisasjon ikke finnes i EREG`() {
        stubFor(
            get(urlEqualTo("/v2/organisasjon/$ORGNR/noekkelinfo"))
                .willReturn(notFound().withBody(BODY_ORG_IKKE_FUNNET)),
        )

        val response: ResponseEntity<Ressurs<Organisasjon>> =
            restTemplate.exchange(
                localhost("/api/organisasjon/$ORGNR"),
                HttpMethod.GET,
                HttpEntity<Any>(headers),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        assertThat(response.body?.melding).contains("Ingen organisasjon med oppgitt organisasjonsnummer ble funnet")
    }

    @Test
    fun `hentOrganisasjon returnerer 500 ved andre feil mot EREG`() {
        stubFor(
            get(urlEqualTo("/v2/organisasjon/$ORGNR/noekkelinfo"))
                .willReturn(serverError()),
        )

        val response: ResponseEntity<Ressurs<Organisasjon>> =
            restTemplate.exchange(
                localhost("/api/organisasjon/$ORGNR"),
                HttpMethod.GET,
                HttpEntity<Any>(headers),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
    }

    companion object {
        private const val ORGNR = "123456789"

        private val BODY_ORG =
            """
            {
              "navn": { "sammensattnavn": "NAV AS" }
            }
            """.trimIndent()

        private val BODY_ORG_IKKE_FUNNET =
            """
            {
              "melding": "Ingen organisasjon med organisasjonsnummer $ORGNR ble funnet"
            }
            """.trimIndent()
    }
}
