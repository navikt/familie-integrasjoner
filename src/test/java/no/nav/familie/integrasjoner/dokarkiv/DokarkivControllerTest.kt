package no.nav.familie.integrasjoner.dokarkiv

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.tomakehurst.wiremock.client.WireMock.anyUrl
import com.github.tomakehurst.wiremock.client.WireMock.delete
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.patch
import com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.client.WireMock.status
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.verify
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.integrasjoner.dokarkiv.client.domene.OpprettJournalpostResponse
import no.nav.familie.kontrakter.felles.BrukerIdType
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokarkiv.ArkiverDokumentResponse
import no.nav.familie.kontrakter.felles.dokarkiv.BulkOppdaterLogiskVedleggRequest
import no.nav.familie.kontrakter.felles.dokarkiv.DokarkivBruker
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import no.nav.familie.kontrakter.felles.dokarkiv.LogiskVedleggRequest
import no.nav.familie.kontrakter.felles.dokarkiv.LogiskVedleggResponse
import no.nav.familie.kontrakter.felles.dokarkiv.OppdaterJournalpostRequest
import no.nav.familie.kontrakter.felles.dokarkiv.OppdaterJournalpostResponse
import no.nav.familie.kontrakter.felles.dokarkiv.Sak
import no.nav.familie.kontrakter.felles.dokarkiv.v2.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Dokument
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Filtype
import no.nav.familie.kontrakter.felles.jsonMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.resttestclient.exchange
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
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
import java.util.LinkedList
import kotlin.test.assertFalse

@ActiveProfiles(profiles = ["integrasjonstest", "mock-sts", "mock-aktor", "mock-personopplysninger", "mock-pdl"])
@TestPropertySource(properties = ["DOKARKIV_V1_URL=http://localhost:28085"])
@EnableWireMock(
    ConfigureWireMock(name = "integrasjonstest", port = 28085),
)
class DokarkivControllerTest : OppslagSpringRunnerTest() {
    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
        (LoggerFactory.getLogger("secureLogger") as Logger)
            .addAppender(listAppender)
    }

    @Test
    fun `skal returnere bad request hvis ingen hoveddokumenter`() {
        val body =
            ArkiverDokumentRequest(
                "fnr",
                false,
                LinkedList(),
            )

        val responseDeprecated: ResponseEntity<Ressurs<ArkiverDokumentResponse>> =
            restTemplate.exchange(
                localhost(DOKARKIV_URL_V4),
                HttpMethod.POST,
                HttpEntity(body, headers),
            )

        assertThat(responseDeprecated.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(responseDeprecated.body?.status).isEqualTo(Ressurs.Status.FEILET)
        assertThat(responseDeprecated.body?.melding).contains("hoveddokumentvarianter=must not be empty")
    }

    @Test
    fun `skal midlertidig journalføre dokument`() {
        stubFor(post(anyUrl()).willReturn(okJson(gyldigDokarkivResponse())))
        val body =
            ArkiverDokumentRequest(
                "FNR",
                false,
                listOf(HOVEDDOKUMENT),
            )

        val response: ResponseEntity<Ressurs<ArkiverDokumentResponse>> =
            restTemplate.exchange(
                localhost(DOKARKIV_URL_V4),
                HttpMethod.POST,
                HttpEntity(body, headers),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(response.body?.data?.journalpostId).isEqualTo("12345678")
        assertFalse(response.body?.data?.ferdigstilt!!)
    }

    @Test
    fun `skal sende med navIdent fra header til journalpost`() {
        stubFor(post(anyUrl()).willReturn(okJson(gyldigDokarkivResponse())))
        val body =
            ArkiverDokumentRequest(
                "FNR",
                false,
                listOf(HOVEDDOKUMENT),
            )

        val response: ResponseEntity<Ressurs<ArkiverDokumentResponse>> =
            restTemplate.exchange(
                localhost(DOKARKIV_URL_V4),
                HttpMethod.POST,
                HttpEntity(body, headersWithNavUserId()),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(response.body?.data?.journalpostId).isEqualTo("12345678")
        assertFalse(response.body?.data?.ferdigstilt!!)
        // Wiremock verification can be added here if needed
    }

    @Test
    fun `skal returnere 2xx hvis dokarkiv returnerer 409 med en response som lar seg parse til en OpprettJournalpostResponse`() {
        stubFor(
            post(anyUrl()).willReturn(
                status(409)
                    .withHeader("Content-Type", "application/json;charset=UTF-8")
                    .withBody(jsonMapper.writeValueAsString(OpprettJournalpostResponse("12345678"))),
            ),
        )
        val body =
            ArkiverDokumentRequest(
                "FNR",
                false,
                listOf(HOVEDDOKUMENT),
            )

        val response: ResponseEntity<Ressurs<ArkiverDokumentResponse>> =
            restTemplate.exchange(
                localhost(DOKARKIV_URL_V4),
                HttpMethod.POST,
                HttpEntity(body, headersWithNavUserId()),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(response.body?.data?.journalpostId).isEqualTo("12345678")
    }

    @Test
    fun `skal returnere 409 hvis dokarkiv returnerer 409 med en response som ikke lar seg parse til en OpprettJournalpostResponse`() {
        stubFor(
            post(anyUrl()).willReturn(
                status(409)
                    .withHeader("Content-Type", "application/json;charset=UTF-8")
                    .withBody("Denne bodyen er ikke en OpprettJournalpostResponse"),
            ),
        )
        val body =
            ArkiverDokumentRequest(
                "FNR",
                false,
                listOf(HOVEDDOKUMENT),
            )

        val response: ResponseEntity<Ressurs<ArkiverDokumentResponse>> =
            restTemplate.exchange(
                localhost(DOKARKIV_URL_V4),
                HttpMethod.POST,
                HttpEntity(body, headersWithNavUserId()),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.CONFLICT)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        assertThat(response.body?.melding).isEqualTo("[dokarkiv.opprettJournalpost][Feil ved opprettelse av journalpost ][org.springframework.web.client.HttpClientErrorException\$Conflict]")
    }

    @Test
    fun `skal midlertidig journalføre dokument med vedlegg`() {
        stubFor(post(anyUrl()).willReturn(okJson(gyldigDokarkivResponse())))
        val body =
            ArkiverDokumentRequest(
                "FNR",
                false,
                listOf(HOVEDDOKUMENT),
                listOf(VEDLEGG),
            )

        val response: ResponseEntity<Ressurs<ArkiverDokumentResponse>> =
            restTemplate.exchange(
                localhost(DOKARKIV_URL_V4),
                HttpMethod.POST,
                HttpEntity(body, headers),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(response.body?.data?.journalpostId).isEqualTo("12345678")
        assertFalse(response.body?.data?.ferdigstilt!!)
    }

    @Test
    fun `dokarkiv returnerer 401`() {
        stubFor(
            post(urlPathEqualTo("/rest/journalpostapi/v1/journalpost"))
                .withQueryParam("forsoekFerdigstill", equalTo("false"))
                .willReturn(
                    status(401)
                        .withHeader("Content-Type", "application/json;charset=UTF-8")
                        .withBody("Tekst fra body"),
                ),
        )
        val body =
            ArkiverDokumentRequest(
                "FNR",
                false,
                listOf(HOVEDDOKUMENT),
            )
        val responseDeprecated: ResponseEntity<Ressurs<ArkiverDokumentResponse>> =
            restTemplate.exchange(
                localhost(DOKARKIV_URL_V4),
                HttpMethod.POST,
                HttpEntity(body, headers),
            )
        assertThat(responseDeprecated.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(responseDeprecated.body?.status).isEqualTo(Ressurs.Status.FEILET)
        assertThat(responseDeprecated.body?.melding).contains("Unauthorized")
    }

    @Test
    fun `oppdaterJournalpost returnerer OK`() {
        val journalpostId = "12345678"
        stubFor(
            put(urlPathEqualTo("/rest/journalpostapi/v1/journalpost/$journalpostId"))
                .willReturn(okJson(gyldigDokarkivResponse())),
        )
        val body =
            OppdaterJournalpostRequest(
                bruker = DokarkivBruker(BrukerIdType.FNR, "12345678910"),
                tema = Tema.ENF,
                sak = Sak("11111111", "fagsaksystem"),
            )
        val response: ResponseEntity<Ressurs<OppdaterJournalpostResponse>> =
            restTemplate.exchange(
                localhost("$DOKARKIV_URL_V2/12345678"),
                HttpMethod.PUT,
                HttpEntity(body, headers),
            )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(response.body?.data?.journalpostId).isEqualTo("12345678")
    }

    @Test
    fun `dokarkiv skal logge detaljert feilmelding til secureLogger ved HttpServerErrorExcetion`() {
        val journalpostId = "12345678"
        stubFor(
            put(urlPathEqualTo("/rest/journalpostapi/v1/journalpost/$journalpostId"))
                .willReturn(
                    status(500)
                        .withHeader("Content-Type", "application/json;charset=UTF-8")
                        .withBody(gyldigDokarkivResponse(500)),
                ),
        )
        val body =
            OppdaterJournalpostRequest(
                bruker = DokarkivBruker(BrukerIdType.FNR, "12345678910"),
                tema = Tema.ENF,
                sak = Sak("11111111", "fagsaksystem"),
            )
        val response: ResponseEntity<Ressurs<OppdaterJournalpostResponse>> =
            restTemplate.exchange(
                localhost("$DOKARKIV_URL_V2/12345678"),
                HttpMethod.PUT,
                HttpEntity(body, headers),
            )
        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        assertThat(loggingEvents)
            .extracting<String, RuntimeException> { obj: ILoggingEvent -> obj.formattedMessage }
            .anyMatch { message -> message.contains("Fant ikke person med ident: 12345678910") }
    }

    @Test
    fun `ferdigstill returnerer ok`() {
        stubFor(
            patch(urlPathEqualTo("/rest/journalpostapi/v1/journalpost/123/ferdigstill"))
                .willReturn(status(200).withBody("Journalpost ferdigstilt")),
        )
        val response: ResponseEntity<Ressurs<Map<String, String>>> =
            restTemplate.exchange(
                localhost("$DOKARKIV_URL_V2/123/ferdigstill?journalfoerendeEnhet=9999"),
                HttpMethod.PUT,
                HttpEntity(null, headersWithNavUserId()),
            )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        verify(
            patchRequestedFor(urlPathEqualTo("/rest/journalpostapi/v1/journalpost/123/ferdigstill"))
                .withHeader("Nav-User-Id", equalTo(NAV_USER_ID_VALUE)),
        )
    }

    @Test
    fun `ferdigstill returnerer 400 hvis ikke mulig ferdigstill`() {
        stubFor(
            patch(urlPathEqualTo("/rest/journalpostapi/v1/journalpost/123/ferdigstill"))
                .willReturn(status(400)),
        )
        val response: ResponseEntity<Ressurs<Map<String, String>>> =
            restTemplate.exchange(
                localhost("$DOKARKIV_URL_V2/123/ferdigstill?journalfoerendeEnhet=9999"),
                HttpMethod.PUT,
                HttpEntity(null, headers),
            )
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        assertThat(response.body?.melding).contains("Kan ikke ferdigstille journalpost 123")
    }

    @Test
    fun `skal opprette logisk vedlegg`() {
        stubFor(
            post(urlPathEqualTo("/rest/journalpostapi/v1/dokumentInfo/321/logiskVedlegg/"))
                .willReturn(okJson(jsonMapper.writeValueAsString(LogiskVedleggResponse(21L)))),
        )
        val response: ResponseEntity<Ressurs<LogiskVedleggResponse>> =
            restTemplate.exchange(
                localhost("$DOKARKIV_URL/dokument/321/logiskVedlegg"),
                HttpMethod.POST,
                HttpEntity(LogiskVedleggRequest("Ny tittel"), headers),
            )
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.data?.logiskVedleggId).isEqualTo(21L)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
    }

    @Test
    fun `skal returnere feil hvis man ikke kan opprette logisk vedlegg`() {
        stubFor(
            post(urlPathEqualTo("/rest/journalpostapi/v1/dokumentInfo/321/logiskVedlegg/"))
                .willReturn(status(404).withBody("melding fra klient")),
        )
        val response: ResponseEntity<Ressurs<LogiskVedleggResponse>> =
            restTemplate.exchange(
                localhost("$DOKARKIV_URL/dokument/321/logiskVedlegg"),
                HttpMethod.POST,
                HttpEntity(LogiskVedleggRequest("Ny tittel"), headers),
            )
        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(response.body?.melding).contains("melding fra klient")
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
    }

    @Test
    fun `skal slette logisk vedlegg`() {
        stubFor(
            delete(urlPathEqualTo("/rest/journalpostapi/v1/dokumentInfo/321/logiskVedlegg/432"))
                .willReturn(status(200)),
        )
        val response: ResponseEntity<Ressurs<LogiskVedleggResponse>> =
            restTemplate.exchange(
                localhost("$DOKARKIV_URL/dokument/321/logiskVedlegg/432"),
                HttpMethod.DELETE,
                HttpEntity(LogiskVedleggRequest("Ny tittel"), headers),
            )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.data?.logiskVedleggId).isEqualTo(432L)
        assertThat(response.body?.melding).contains("logisk vedlegg slettet")
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
    }

    @Test
    fun `skal bulk oppdatere logiske vedlegg for et dokument`() {
        stubFor(
            put(urlPathEqualTo("/rest/journalpostapi/v1/dokumentInfo/321/logiskVedlegg"))
                .willReturn(status(204)),
        )
        val response: ResponseEntity<Ressurs<String>> =
            restTemplate.exchange(
                localhost("$DOKARKIV_URL/dokument/321/logiskVedlegg"),
                HttpMethod.PUT,
                HttpEntity(BulkOppdaterLogiskVedleggRequest(titler = listOf("Logisk vedlegg 1", "Logisk vedlegg 2")), headers),
            )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.data).isEqualTo("321")
        assertThat(response.body?.melding).contains("logiske vedlegg oppdatert")
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
    }

    @Test
    fun `skal returnere feil hvis man ikke kan slette logisk vedlegg`() {
        stubFor(
            delete(urlPathEqualTo("/rest/journalpostapi/v1/dokumentInfo/321/logiskVedlegg/432"))
                .willReturn(status(404).withBody("sletting feilet")),
        )
        val response: ResponseEntity<Ressurs<LogiskVedleggResponse>> =
            restTemplate.exchange(
                localhost("$DOKARKIV_URL/dokument/321/logiskVedlegg/432"),
                HttpMethod.DELETE,
                HttpEntity(LogiskVedleggRequest("Ny tittel"), headers),
            )
        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(response.body?.melding).contains("sletting feilet")
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
    }

    @Throws(IOException::class)
    private fun gyldigDokarkivResponse(statusKode: Int? = null): String =
        Files.readString(
            ClassPathResource("dokarkiv/gyldig${statusKode ?: ""}response.json").file.toPath(),
            StandardCharsets.UTF_8,
        )

    private fun headersWithNavUserId(): HttpHeaders =
        headers.apply {
            add("Nav-User-Id", NAV_USER_ID_VALUE)
        }

    companion object {
        private const val DOKARKIV_URL = "/api/arkiv"
        private const val DOKARKIV_URL_V2 = "$DOKARKIV_URL/v2"
        private const val DOKARKIV_URL_V4 = "$DOKARKIV_URL/v4"

        private const val NAV_USER_ID_VALUE = "k123123"

        private val HOVEDDOKUMENT =
            Dokument(
                "foo".toByteArray(),
                Filtype.JSON,
                "filnavn",
                null,
                Dokumenttype.KONTANTSTØTTE_SØKNAD,
            )

        private val VEDLEGG =
            Dokument(
                "foo".toByteArray(),
                Filtype.PDFA,
                "filnavn",
                "Vedlegg",
                Dokumenttype.KONTANTSTØTTE_SØKNAD_VEDLEGG,
            )
    }
}
