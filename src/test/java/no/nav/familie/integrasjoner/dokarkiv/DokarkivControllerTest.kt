package no.nav.familie.integrasjoner.dokarkiv

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.kontrakter.felles.BrukerIdType
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokarkiv.ArkiverDokumentResponse
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
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockserver.integration.ClientAndServer
import org.mockserver.junit.jupiter.MockServerExtension
import org.mockserver.junit.jupiter.MockServerSettings
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.JsonBody.json
import org.slf4j.LoggerFactory
import org.springframework.boot.test.web.client.exchange
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.LinkedList
import kotlin.test.assertFalse

@ActiveProfiles(profiles = ["integrasjonstest", "mock-sts", "mock-aktor", "mock-personopplysninger", "mock-pdl"])
@ExtendWith(MockServerExtension::class)
@MockServerSettings(ports = [OppslagSpringRunnerTest.MOCK_SERVER_PORT])
class DokarkivControllerTest(private val client: ClientAndServer) : OppslagSpringRunnerTest() {

    @BeforeEach
    fun setUp() {
        client.reset()
        headers.setBearerAuth(lokalTestToken)
        objectMapper.registerModule(KotlinModule())

        (LoggerFactory.getLogger("secureLogger") as Logger)
            .addAppender(listAppender)
    }

    @Test
    fun `skal returnere bad request hvis ingen hoveddokumenter`() {
        val body = ArkiverDokumentRequest(
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
        client.`when`(
            request()
                .withMethod("POST")
                .withPath("/rest/journalpostapi/v1/journalpost")
                .withQueryStringParameter("forsoekFerdigstill", "false"),
        )
            .respond(response().withBody(json(gyldigDokarkivResponse())))
        val body = ArkiverDokumentRequest(
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
        client.`when`(
            request()
                .withMethod("POST")
                .withPath("/rest/journalpostapi/v1/journalpost")
                .withQueryStringParameter("forsoekFerdigstill", "false"),
        )
            .respond(response().withBody(json(gyldigDokarkivResponse())))
        val body = ArkiverDokumentRequest(
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
        client.verify(request().withHeader("Nav-User-Id", "k123123"))
    }

    @Test
    fun `skal returnere 409 ved 409 response fra dokarkiv`() {
        client.`when`(
            request()
                .withMethod("POST")
                .withPath("/rest/journalpostapi/v1/journalpost")
                .withQueryStringParameter("forsoekFerdigstill", "false"),
        )
            .respond(
                response()
                    .withStatusCode(409)
                    .withHeader("Content-Type", "application/json;charset=UTF-8")
                    .withBody("Tekst fra body"),
            )
        val body = ArkiverDokumentRequest(
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
        assertThat(response.body?.melding).isEqualTo("[Dokarkiv][Feil ved opprettelse av journalpost ][org.springframework.web.client.HttpClientErrorException\$Conflict]")
    }

    @Test
    fun `skal midlertidig journalføre dokument med vedlegg`() {
        client.`when`(
            request()
                .withMethod("POST")
                .withPath("/rest/journalpostapi/v1/journalpost")
                .withQueryStringParameter("forsoekFerdigstill", "false"),
        )
            .respond(response().withBody(json(gyldigDokarkivResponse())))
        val body = ArkiverDokumentRequest(
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
        client.`when`(
            request()
                .withMethod("POST")
                .withPath("/rest/journalpostapi/v1/journalpost")
                .withQueryStringParameter("forsoekFerdigstill", "false"),
        )
            .respond(
                response()
                    .withStatusCode(401)
                    .withHeader("Content-Type", "application/json;charset=UTF-8")
                    .withBody("Tekst fra body"),
            )
        val body = ArkiverDokumentRequest(
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
        client.`when`(
            request()
                .withMethod("PUT")
                .withPath("/rest/journalpostapi/v1/journalpost/$journalpostId"),
        )
            .respond(response().withBody(json(gyldigDokarkivResponse())))

        val body = OppdaterJournalpostRequest(
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
        client.`when`(
            request()
                .withMethod("PUT")
                .withPath("/rest/journalpostapi/v1/journalpost/$journalpostId"),
        )
            .respond(
                response().withStatusCode(500)
                    .withHeader("Content-Type", "application/json;charset=UTF-8")
                    .withBody(gyldigDokarkivResponse(500)),
            )

        val body = OppdaterJournalpostRequest(
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
        client.`when`(
            request()
                .withMethod("PATCH")
                .withPath("/rest/journalpostapi/v1/journalpost/123/ferdigstill"),
        )
            .respond(response().withStatusCode(200).withBody("Journalpost ferdigstilt"))

        val response: ResponseEntity<Ressurs<Map<String, String>>> =
            restTemplate.exchange(
                localhost("$DOKARKIV_URL_V2/123/ferdigstill?journalfoerendeEnhet=9999"),
                HttpMethod.PUT,
                HttpEntity(null, headersWithNavUserId()),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        client.verify(request().withHeader("Nav-User-Id", "k123123"))
    }

    @Test
    fun `ferdigstill returnerer 400 hvis ikke mulig ferdigstill`() {
        client.`when`(
            request()
                .withMethod("PATCH")
                .withPath("/rest/journalpostapi/v1/journalpost/123/ferdigstill"),
        )
            .respond(response().withStatusCode(400))

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
        client.`when`(
            request()
                .withMethod("POST")
                .withPath("/rest/journalpostapi/v1/dokumentInfo/321/logiskVedlegg/"),
        )
            .respond(
                response()
                    .withStatusCode(200)
                    .withBody(json(objectMapper.writeValueAsString(LogiskVedleggResponse(21L)))),
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
        client.`when`(
            request()
                .withMethod("POST")
                .withPath("/rest/journalpostapi/v1/dokumentInfo/321/logiskVedlegg/"),
        )
            .respond(
                response()
                    .withStatusCode(404)
                    .withBody("melding fra klient"),
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
        client.`when`(
            request()
                .withMethod("DELETE")
                .withPath("/rest/journalpostapi/v1/dokumentInfo/321/logiskVedlegg/432"),
        )
            .respond(
                response()
                    .withStatusCode(200),
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
    fun `skal returnere feil hvis man ikke kan slette logisk vedlegg`() {
        client.`when`(
            request()
                .withMethod("DELETE")
                .withPath("/rest/journalpostapi/v1/dokumentInfo/321/logiskVedlegg/432"),
        )
            .respond(
                response()
                    .withStatusCode(404)
                    .withBody("sletting feilet"),
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
    private fun gyldigDokarkivResponse(statusKode: Int? = null): String {
        return Files.readString(
            ClassPathResource("dokarkiv/gyldig${statusKode ?: ""}response.json").file.toPath(),
            StandardCharsets.UTF_8,
        )
    }

    private fun headersWithNavUserId(): HttpHeaders {
        return headers.apply {
            add("Nav-User-Id", NAV_USER_ID_VALUE)
        }
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
