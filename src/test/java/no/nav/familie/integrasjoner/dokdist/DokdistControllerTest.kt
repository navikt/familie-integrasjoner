package no.nav.familie.integrasjoner.dokdist

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.dokdist.DistribuerJournalpostRequest
import no.nav.familie.kontrakter.felles.dokdist.Distribusjonstidspunkt
import no.nav.familie.kontrakter.felles.dokdist.Distribusjonstype
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.resttestclient.exchange
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files

@ActiveProfiles("integrasjonstest", "mock-oauth")
@TestPropertySource(properties = ["DOKDIST_URL=http://localhost:28085"])
@EnableWireMock(
    ConfigureWireMock(name = "DokdistControllerTest", port = 28085),
)
class DokdistControllerTest : OppslagSpringRunnerTest() {
    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
    }

    @Test
    fun `dokdist returnerer OK uten kjernetid og distribusjonstidspunkt`() {
        mockGodkjentKallMotDokDist()

        val body2 =
            """
            {
                "journalpostId": "$JOURNALPOST_ID",
                "bestillendeFagsystem": "BA",
                "dokumentProdApp": "ba-sak"
            }
            """.trimIndent()
        headers.set("Content-Type", "application/json")
        val response: ResponseEntity<Ressurs<String>> =
            restTemplate.exchange(
                localhost(DOKDIST_URL),
                HttpMethod.POST,
                HttpEntity(body2, headers),
            )

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        Assertions.assertThat(response.body?.data).contains("1234567")
    }

    @Test
    fun `dokdist returnerer OK med distribusjonstype`() {
        mockGodkjentKallMotDokDist()

        val body = DistribuerJournalpostRequest(JOURNALPOST_ID, Fagsystem.BA, "ba-sak", Distribusjonstype.VIKTIG, Distribusjonstidspunkt.KJERNETID)
        val response: ResponseEntity<Ressurs<String>> =
            restTemplate.exchange(
                localhost(DOKDIST_URL),
                HttpMethod.POST,
                HttpEntity(body, headers),
            )

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        Assertions.assertThat(response.body?.data).contains("1234567")
    }

    @Test
    fun `dokdist returnerer OK`() {
        mockGodkjentKallMotDokDist()

        val body = DistribuerJournalpostRequest(JOURNALPOST_ID, Fagsystem.BA, "ba-sak", null, Distribusjonstidspunkt.KJERNETID)
        val response: ResponseEntity<Ressurs<String>> =
            restTemplate.exchange(
                localhost(DOKDIST_URL),
                HttpMethod.POST,
                HttpEntity(body, headers),
            )

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        Assertions.assertThat(response.body?.data).contains("1234567")
    }

    private fun mockGodkjentKallMotDokDist() {
        stubFor(
            post(urlEqualTo("/rest/v1/distribuerjournalpost"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json;charset=UTF-8")
                        .withBody("{\"bestillingsId\": \"1234567\"}"),
                ),
        )
    }

    @Test
    fun `dokdist returnerer OK uten bestillingsId`() {
        stubFor(
            post(urlEqualTo("/rest/v1/distribuerjournalpost"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withBody(""),
                ),
        )

        val body = DistribuerJournalpostRequest(JOURNALPOST_ID, Fagsystem.BA, "ba-sak", null)
        val response: ResponseEntity<Ressurs<String>> =
            restTemplate.exchange(
                localhost(DOKDIST_URL),
                HttpMethod.POST,
                HttpEntity(body, headers),
            )

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        Assertions.assertThat(response.body?.melding).contains("BestillingsId var null")
    }

    @Test
    fun `dokdist returnerer 400`() {
        stubFor(
            post(urlEqualTo("/rest/v1/distribuerjournalpost"))
                .willReturn(
                    aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody(badRequestResponse()),
                ),
        )

        val body = DistribuerJournalpostRequest(JOURNALPOST_ID, Fagsystem.BA, "ba-sak", null)
        val response: ResponseEntity<Ressurs<String>> =
            restTemplate.exchange(
                localhost(DOKDIST_URL),
                HttpMethod.POST,
                HttpEntity(body, headers),
            )

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        Assertions
            .assertThat(response.body?.melding)
            .contains("validering av distribusjonsforespørsel for journalpostId=453492547 feilet, feilmelding=")
    }

    @Throws(IOException::class)
    private fun badRequestResponse(): String = Files.readString(ClassPathResource("dokdist/badrequest.json").file.toPath(), StandardCharsets.UTF_8)

    companion object {
        private const val DOKDIST_URL = "/api/dist/v1"
        private const val JOURNALPOST_ID = "453492547"
    }
}
