package no.nav.familie.integrasjoner.dokarkiv

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.dokarkiv.*
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockserver.junit.MockServerRule
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse.response
import org.slf4j.LoggerFactory
import org.springframework.boot.test.web.client.exchange
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*
import kotlin.test.assertFalse
import no.nav.familie.kontrakter.felles.arkivering.ArkiverDokumentRequest as DeprecatedArkiverDokumentRequest

@ActiveProfiles(profiles = ["integrasjonstest", "mock-sts", "mock-aktor", "mock-personopplysninger", "mock-pdl"])
class DokarkivControllerTest : OppslagSpringRunnerTest() {

    @get:Rule
    val mockServerRule = MockServerRule(this, MOCK_SERVER_PORT)

    @Before fun setUp() {
        headers.setBearerAuth(lokalTestToken)
        objectMapper.registerModule(KotlinModule())

        (LoggerFactory.getLogger("secureLogger") as Logger)
                .addAppender(listAppender)
    }

    @Test
    fun `skal returnere bad request hvis ingen dokumenter`() {
        val body = DeprecatedArkiverDokumentRequest("fnr",
                                                    false,
                                                    LinkedList())

        val responseDeprecated: ResponseEntity<Ressurs<ArkiverDokumentResponse>> =
                restTemplate.exchange(localhost(DOKARKIV_URL_V2),
                                      HttpMethod.POST,
                                      HttpEntity(body, headers))

        assertThat(responseDeprecated.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(responseDeprecated.body?.status).isEqualTo(Ressurs.Status.FEILET)
        assertThat(responseDeprecated.body?.melding).contains("dokumenter=must not be empty")
    }

    @Test
    fun `v3 skal returnere bad request hvis ingen hoveddokumenter`() {
        val body = ArkiverDokumentRequest("fnr",
                                          false,
                                          LinkedList())

        val responseDeprecated: ResponseEntity<Ressurs<ArkiverDokumentResponse>> =
                restTemplate.exchange(localhost(DOKARKIV_URL_V3),
                                      HttpMethod.POST,
                                      HttpEntity(body, headers))

        assertThat(responseDeprecated.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(responseDeprecated.body?.status).isEqualTo(Ressurs.Status.FEILET)
        assertThat(responseDeprecated.body?.melding).contains("hoveddokumentvarianter=must not be empty")
    }

    @Test
    fun `skal midlertidig journalføre dokument`() {
        mockServerRule.client
                .`when`(HttpRequest
                                .request()
                                .withMethod("POST")
                                .withPath("/rest/journalpostapi/v1/journalpost")
                                .withQueryStringParameter("forsoekFerdigstill", "false"))
                .respond(response()
                                 .withHeader("Content-Type", "application/json;charset=UTF-8")
                                 .withBody(gyldigDokarkivResponse()))
        val body = DeprecatedArkiverDokumentRequest("FNR",
                                                    false,
                                                    listOf(HOVEDDOKUMENT))

        val response: ResponseEntity<Ressurs<ArkiverDokumentResponse>> =
                restTemplate.exchange(localhost(DOKARKIV_URL_V2),
                                      HttpMethod.POST,
                                      HttpEntity(body, headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(response.body?.data?.journalpostId).isEqualTo("12345678")
        assertFalse(response.body?.data?.ferdigstilt!!)
    }

    @Test
    fun `v3 skal midlertidig journalføre dokument`() {
        mockServerRule.client
                .`when`(HttpRequest
                                .request()
                                .withMethod("POST")
                                .withPath("/rest/journalpostapi/v1/journalpost")
                                .withQueryStringParameter("forsoekFerdigstill", "false"))
                .respond(response()
                                 .withHeader("Content-Type", "application/json;charset=UTF-8")
                                 .withBody(gyldigDokarkivResponse()))
        val body = ArkiverDokumentRequest("FNR",
                                          false,
                                          listOf(HOVEDDOKUMENT))

        val response: ResponseEntity<Ressurs<ArkiverDokumentResponse>> =
                restTemplate.exchange(localhost(DOKARKIV_URL_V3),
                                      HttpMethod.POST,
                                      HttpEntity(body, headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(response.body?.data?.journalpostId).isEqualTo("12345678")
        assertFalse(response.body?.data?.ferdigstilt!!)
    }

    @Test
    fun `skal midlertidig journalføre dokument med vedlegg`() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withPath("/rest/journalpostapi/v1/journalpost")
                                .withQueryStringParameter("forsoekFerdigstill", "false"))
                .respond(response()
                                 .withHeader("Content-Type", "application/json")
                                 .withBody(gyldigDokarkivResponse()))
        val body = ArkiverDokumentRequest("FNR",
                                          false,
                                          listOf(HOVEDDOKUMENT),
                                          listOf(VEDLEGG))

        val response: ResponseEntity<Ressurs<ArkiverDokumentResponse>> =
                restTemplate.exchange(localhost(DOKARKIV_URL_V3),
                                      HttpMethod.POST,
                                      HttpEntity(body, headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(response.body?.data?.journalpostId).isEqualTo("12345678")
        assertFalse(response.body?.data?.ferdigstilt!!)
    }

    @Test
    fun `dokarkiv returnerer 401`() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withPath("/rest/journalpostapi/v1/journalpost")
                                .withQueryStringParameter("forsoekFerdigstill", "false"))
                .respond(response()
                                 .withStatusCode(401)
                                 .withHeader("Content-Type", "application/json;charset=UTF-8")
                                 .withBody("Tekst fra body"))
        val body = DeprecatedArkiverDokumentRequest("FNR",
                                                    false,
                                                    listOf(Dokument("foo".toByteArray(),
                                                                    FilType.PDFA,
                                                                    null,
                                                                    null,
                                                                    "KONTANTSTØTTE_SØKNAD")))

        val responseDeprecated: ResponseEntity<Ressurs<ArkiverDokumentResponse>> =
                restTemplate.exchange(localhost(DOKARKIV_URL_V2),
                                      HttpMethod.POST,
                                      HttpEntity(body, headers))

        assertThat(responseDeprecated.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(responseDeprecated.body?.status).isEqualTo(Ressurs.Status.FEILET)
        assertThat(responseDeprecated.body?.melding).contains("Unauthorized")
    }

    @Test
    fun `oppdaterJournalpost returnerer OK`() {
        val journalpostId = "12345678"
        mockServerRule.client
                .`when`(HttpRequest
                                .request()
                                .withMethod("PUT")
                                .withPath("/rest/journalpostapi/v1/journalpost/$journalpostId"))
                .respond(response()
                                 .withHeader("Content-Type", "application/json;charset=UTF-8")
                                 .withBody(gyldigDokarkivResponse()))

        val body = OppdaterJournalpostRequest(bruker = DokarkivBruker(IdType.FNR, "12345678910"),
                                              tema = "tema",
                                              sak = Sak("11111111", "fagsaksystem"))

        val response: ResponseEntity<Ressurs<OppdaterJournalpostResponse>> =
                restTemplate.exchange(localhost("$DOKARKIV_URL_V2/12345678"),
                                      HttpMethod.PUT,
                                      HttpEntity(body, headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(response.body?.data?.journalpostId).isEqualTo("12345678")
    }

    @Test
    fun `dokarkiv skal logge detaljert feilmelding til secureLogger ved HttpServerErrorExcetion`() {
        val journalpostId = "12345678"
        mockServerRule.client
                .`when`(HttpRequest
                                .request()
                                .withMethod("PUT")
                                .withPath("/rest/journalpostapi/v1/journalpost/$journalpostId"))
                .respond(response().withStatusCode(500)
                                 .withHeader("Content-Type", "application/json;charset=UTF-8")
                                 .withBody(gyldigDokarkivResponse(500)))

        val body = OppdaterJournalpostRequest(bruker = DokarkivBruker(IdType.FNR, "12345678910"),
                                              tema = "tema",
                                              sak = Sak("11111111", "fagsaksystem"))

        val response: ResponseEntity<Ressurs<OppdaterJournalpostResponse>> =
                restTemplate.exchange(localhost("$DOKARKIV_URL_V2/12345678"),
                                      HttpMethod.PUT,
                                      HttpEntity(body, headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        assertThat(loggingEvents)
                .extracting<String, RuntimeException> { obj: ILoggingEvent -> obj.formattedMessage }
                .anyMatch { message -> message.contains("Fant ikke person med ident: 12345678910") }
    }

    @Test
    fun `ferdigstill returnerer ok`() {
        mockServerRule.client
                .`when`(HttpRequest
                                .request()
                                .withMethod("PATCH")
                                .withPath("/rest/journalpostapi/v1/journalpost/123/ferdigstill"))
                .respond(response().withStatusCode(200).withBody("Journalpost ferdigstilt"))

        val response: ResponseEntity<Ressurs<Map<String, String>>> =
                restTemplate.exchange(localhost("$DOKARKIV_URL_V2/123/ferdigstill?journalfoerendeEnhet=9999"),
                                      HttpMethod.PUT,
                                      HttpEntity(null, headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
    }

    @Test
    fun `ferdigstill returnerer 400 hvis ikke mulig ferdigstill`() {
        mockServerRule.client
                .`when`(HttpRequest
                                .request()
                                .withMethod("PATCH")
                                .withPath("/rest/journalpostapi/v1/journalpost/123/ferdigstill"))
                .respond(response().withStatusCode(400))

        val response: ResponseEntity<Ressurs<Map<String, String>>> =
                restTemplate.exchange(localhost("$DOKARKIV_URL_V2/123/ferdigstill?journalfoerendeEnhet=9999"),
                                      HttpMethod.PUT,
                                      HttpEntity(null, headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        assertThat(response.body?.melding).contains("Kan ikke ferdigstille journalpost 123")
    }


    @Test
    fun `skal opprette logisk vedlegg`() {
        mockServerRule.client
                .`when`(HttpRequest
                                .request()
                                .withMethod("POST")
                                .withPath("/rest/journalpostapi/v1/dokumentInfo/321/logiskVedlegg/"))
                .respond(response()
                                 .withStatusCode(200)
                                 .withHeader("Content-Type", "application/json;charset=UTF-8")
                                 .withBody(objectMapper.writeValueAsString(
                                         LogiskVedleggResponse(21L))))

        val response: ResponseEntity<Ressurs<LogiskVedleggResponse>> =
                restTemplate.exchange(localhost("$DOKARKIV_URL/dokument/321/logiskVedlegg"),
                                      HttpMethod.POST,
                                      HttpEntity(LogiskVedleggRequest("Ny tittel"), headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.data?.logiskVedleggId).isEqualTo(21L)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
    }

    @Test
    fun `skal returnere feil hvis man ikke kan opprette logisk vedlegg`() {
        mockServerRule.client
                .`when`(HttpRequest
                                .request()
                                .withMethod("POST")
                                .withPath("/rest/journalpostapi/v1/dokumentInfo/321/logiskVedlegg/"))
                .respond(response()
                                 .withStatusCode(404)
                                 .withBody("melding fra klient"))

        val response: ResponseEntity<Ressurs<LogiskVedleggResponse>> =
                restTemplate.exchange(localhost("$DOKARKIV_URL/dokument/321/logiskVedlegg"),
                                      HttpMethod.POST,
                                      HttpEntity(LogiskVedleggRequest("Ny tittel"), headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(response.body?.melding).contains("melding fra klient")
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
    }


    @Test
    fun `skal slette logisk vedlegg`() {
        mockServerRule.client
                .`when`(HttpRequest
                                .request()
                                .withMethod("DELETE")
                                .withPath("/rest/journalpostapi/v1/dokumentInfo/321/logiskVedlegg/432"))
                .respond(response()
                                 .withStatusCode(200))

        val response: ResponseEntity<Ressurs<LogiskVedleggResponse>> =
                restTemplate.exchange(localhost("$DOKARKIV_URL/dokument/321/logiskVedlegg/432"),
                                      HttpMethod.DELETE,
                                      HttpEntity(LogiskVedleggRequest("Ny tittel"), headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.data?.logiskVedleggId).isEqualTo(432L)
        assertThat(response.body?.melding).contains("logisk vedlegg slettet")
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
    }

    @Test
    fun `skal returnere feil hvis man ikke kan slette logisk vedlegg`() {
        mockServerRule.client
                .`when`(HttpRequest
                                .request()
                                .withMethod("DELETE")
                                .withPath("/rest/journalpostapi/v1/dokumentInfo/321/logiskVedlegg/432"))
                .respond(response()
                                 .withStatusCode(404)
                                 .withBody("sletting feilet"))

        val response: ResponseEntity<Ressurs<LogiskVedleggResponse>> =
                restTemplate.exchange(localhost("$DOKARKIV_URL/dokument/321/logiskVedlegg/432"),
                                      HttpMethod.DELETE,
                                      HttpEntity(LogiskVedleggRequest("Ny tittel"), headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(response.body?.melding).contains("sletting feilet")
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
    }

    @Throws(IOException::class) private fun gyldigDokarkivResponse(statusKode: Int? = null): String {
        return Files.readString(ClassPathResource("dokarkiv/gyldig${statusKode ?: ""}response.json").file.toPath(),
                                StandardCharsets.UTF_8)
    }

    companion object {
        private const val MOCK_SERVER_PORT = 18321
        private const val DOKARKIV_URL = "/api/arkiv"
        private const val DOKARKIV_URL_V2 = "${DOKARKIV_URL}/v2/"
        private const val DOKARKIV_URL_V3 = "${DOKARKIV_URL}/v3/"

        private val HOVEDDOKUMENT = Dokument("foo".toByteArray(),
                                             FilType.PDFA,
                                             "filnavn",
                                             null,
                                             "KONTANTSTØTTE_SØKNAD")
        private val VEDLEGG = Dokument("foo".toByteArray(),
                                       FilType.PDFA,
                                       "filnavn",
                                       "Vedlegg",
                                       "KONTANTSTØTTE_SØKNAD_VEDLEGG")
    }
}