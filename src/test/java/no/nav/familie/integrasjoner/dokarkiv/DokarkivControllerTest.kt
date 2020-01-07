package no.nav.familie.integrasjoner.dokarkiv

import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.integrasjoner.dokarkiv.api.*
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockserver.junit.MockServerRule
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
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

@ActiveProfiles(profiles = ["integrasjonstest", "mock-sts", "mock-aktor", "mock-personopplysninger"])
class DokarkivControllerTest : OppslagSpringRunnerTest() {

    @get:Rule
    val mockServerRule = MockServerRule(this, MOCK_SERVER_PORT)

    @Before fun setUp() {
        headers.setBearerAuth(lokalTestToken)
        objectMapper.registerModule(KotlinModule())
    }

    @Test
    fun `skal returnere bad request hvis fNr mangler`() {
        val body = ArkiverDokumentRequest(null,
                                          FULLT_NAVN,
                                          false,
                                          listOf(Dokument("foo".toByteArray(),
                                                          FilType.PDFA,
                                                          null,
                                                          DokumentType.KONTANTSTØTTE_SØKNAD)))

        val response: ResponseEntity<Ressurs<ArkiverDokumentResponse>> = restTemplate.exchange(localhost(DOKARKIV_URL),
                                                                                               HttpMethod.POST,
                                                                                               HttpEntity(body, headers))

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        Assertions.assertThat(response.body?.melding).contains("fnr=must not be blank")
    }

    @Test
    fun `skal returnere bad request hvis ingen dokumenter`() {
        val body = ArkiverDokumentRequest("fnr",
                                          "Foobar",
                                          false,
                                          LinkedList())

        val response: ResponseEntity<Ressurs<ArkiverDokumentResponse>> = restTemplate.exchange(localhost(DOKARKIV_URL),
                                                                                               HttpMethod.POST,
                                                                                               HttpEntity(body, headers))

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        Assertions.assertThat(response.body?.melding).contains("dokumenter=must not be empty")
    }

    @Test
    fun `skal midlertidig journalføre dokument`() {
        mockServerRule.client
                .`when`(HttpRequest
                                .request()
                                .withMethod("POST")
                                .withPath("/rest/journalpostapi/v1/journalpost")
                                .withQueryStringParameter("foersoekFerdigstill", "false"))
                .respond(HttpResponse.response().withBody(gyldigDokarkivResponse()))
        val body = ArkiverDokumentRequest("FNR",
                                          FULLT_NAVN,
                                          false,
                                          listOf(HOVEDDOKUMENT))

        val response: ResponseEntity<Ressurs<ArkiverDokumentResponse>> = restTemplate.exchange(localhost(DOKARKIV_URL),
                                                                                               HttpMethod.POST,
                                                                                               HttpEntity(body, headers))

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        Assertions.assertThat(response.body?.data?.journalpostId).isEqualTo("12345678")
        Assertions.assertThat(response.body?.data?.isFerdigstilt).isFalse()
    }

    @Test
    fun `skal midlertidig journalføre dokument med vedlegg`() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withPath("/rest/journalpostapi/v1/journalpost")
                                .withQueryStringParameter("foersoekFerdigstill", "false"))
                .respond(HttpResponse.response().withBody(gyldigDokarkivResponse()))
        val body = ArkiverDokumentRequest("FNR",
                                          FULLT_NAVN,
                                          false,
                                          listOf(HOVEDDOKUMENT, VEDLEGG))

        val response: ResponseEntity<Ressurs<ArkiverDokumentResponse>> = restTemplate.exchange(localhost(DOKARKIV_URL),
                                                                                               HttpMethod.POST,
                                                                                               HttpEntity(body, headers))

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        Assertions.assertThat(response.body?.data?.journalpostId).isEqualTo("12345678")
        Assertions.assertThat(response.body?.data?.isFerdigstilt).isFalse()
    }

    @Test
    fun `dokarkiv returnerer 401`() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withPath("/rest/journalpostapi/v1/journalpost")
                                .withQueryStringParameter("foersoekFerdigstill", "false"))
                .respond(HttpResponse.response().withStatusCode(401).withBody("Tekst fra body"))
        val body = ArkiverDokumentRequest("FNR",
                                          "Foobar",
                                          false,
                                          listOf(Dokument("foo".toByteArray(),
                                                          FilType.PDFA,
                                                          null,
                                                          DokumentType.KONTANTSTØTTE_SØKNAD)))

        val response: ResponseEntity<Ressurs<ArkiverDokumentResponse>> = restTemplate.exchange(localhost(DOKARKIV_URL),
                                                                                               HttpMethod.POST,
                                                                                               HttpEntity(body, headers))

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        Assertions.assertThat(response.body?.melding).contains("Feilresponse fra dokarkiv-tjenesten 401 Tekst fra body")
    }

    @Test
    fun `ferdigstill returnerer ok`() {
        mockServerRule.client
                .`when`(HttpRequest
                                .request()
                                .withMethod("PATCH")
                                .withPath("/rest/journalpostapi/v1/journalpost/123/ferdigstill"))
                .respond(HttpResponse.response().withStatusCode(200))

        val response: ResponseEntity<Ressurs<Map<String, String>>> =
                restTemplate.exchange(localhost("$DOKARKIV_URL/123/ferdigstill?journalfoerendeEnhet=9999"),
                                      HttpMethod.PUT,
                                      HttpEntity(null, headers))

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
    }

    @Test
    fun `ferdigstill returnerer 400 hvis ikke mulig ferdigstill`() {
        mockServerRule.client
                .`when`(HttpRequest
                                .request()
                                .withMethod("PATCH")
                                .withPath("/rest/journalpostapi/v1/journalpost/123/ferdigstill"))
                .respond(HttpResponse.response().withStatusCode(400))

        val response: ResponseEntity<Ressurs<Map<String, String>>> =
                restTemplate.exchange(localhost("$DOKARKIV_URL/123/ferdigstill?journalfoerendeEnhet=9999"),
                                      HttpMethod.PUT,
                                      HttpEntity(null, headers))

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        Assertions.assertThat(response.body?.melding).contains("Kan ikke ferdigstille journalpost 123")
    }

    @Throws(IOException::class) private fun gyldigDokarkivResponse(): String {
        return Files.readString(ClassPathResource("dokarkiv/gyldigresponse.json").file.toPath(),
                                StandardCharsets.UTF_8)
    }

    companion object {
        private const val MOCK_SERVER_PORT = 18321
        private const val FULLT_NAVN = "Foo Bar"
        private const val DOKARKIV_URL = "/api/arkiv/v1"
        private val HOVEDDOKUMENT = Dokument("foo".toByteArray(),
                                             FilType.PDFA,
                                             "filnavn",
                                             DokumentType.KONTANTSTØTTE_SØKNAD)
        private val VEDLEGG = Dokument("foo".toByteArray(),
                                       FilType.PDFA,
                                       "filnavn",
                                       DokumentType.KONTANTSTØTTE_SØKNAD_VEDLEGG)
    }
}