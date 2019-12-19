package no.nav.familie.integrasjoner.dokarkiv

import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.integrasjoner.dokarkiv.api.ArkiverDokumentRequest
import no.nav.familie.integrasjoner.dokarkiv.api.Dokument
import no.nav.familie.integrasjoner.dokarkiv.api.DokumentType
import no.nav.familie.integrasjoner.dokarkiv.api.FilType
import no.nav.familie.ks.kontrakter.objectMapper
import no.nav.familie.ks.kontrakter.sak.Ressurs
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockserver.junit.MockServerRule
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
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
        headers.set("Content-Type", "application/json")

        objectMapper.registerModule(KotlinModule())

    }

    @Test
    fun skal_returnere_Bad_Request_hvis_fNr_mangler() {
        val body = ArkiverDokumentRequest("",
                                          false,
                                          listOf(Dokument("foo".toByteArray(),
                                                          FilType.PDFA,
                                                          null,
                                                          DokumentType.KONTANTSTØTTE_SØKNAD)))

        val response = restTemplate.exchange(localhost(DOKARKIV_URL),
                                             HttpMethod.POST,
                                             HttpEntity(body, headers),
                                             Ressurs::class.java)

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        Assertions.assertThat(response.body?.melding).contains("fnr=must not be blank")
    }

    @Test
    fun skal_returnere_Bad_Request_hvis_ingen_dokumenter() {
        val body = ArkiverDokumentRequest("fnr", false, LinkedList())

        val response = restTemplate.exchange(localhost(DOKARKIV_URL),
                                             HttpMethod.POST,
                                             HttpEntity(body, headers),
                                             Ressurs::class.java)

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        Assertions.assertThat(response.body?.melding).contains("dokumenter=must not be empty")
    }

    @Test @Throws(IOException::class)
    fun skal_midlertidig_journalføre_dokument() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withPath("/rest/journalpostapi/v1/journalpost")
                                .withQueryStringParameter("foersoekFerdigstill", "false"))
                .respond(HttpResponse.response().withBody(gyldigDokarkivResponse()))
        val body = ArkiverDokumentRequest("FNR",
                                          false,
                                          listOf(HOVEDDOKUMENT))

        val response =
                restTemplate.exchange(localhost(DOKARKIV_URL),
                                      HttpMethod.POST,
                                      HttpEntity(body, headers),
                                      Ressurs::class.java)

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        Assertions.assertThat(response.body?.data!!["journalpostId"].textValue()).isEqualTo("12345678")
        Assertions.assertThat(response.body?.data!!["ferdigstilt"].booleanValue()).isFalse()
    }

    @Test @Throws(IOException::class) fun skal_midlertidig_journalføre_dokument_med_vedlegg() {
        mockServerRule.client
                .`when`(HttpRequest
                                .request()
                                .withMethod("POST")
                                .withPath("/rest/journalpostapi/v1/journalpost")
                                .withQueryStringParameter("foersoekFerdigstill", "false"))
                .respond(HttpResponse.response().withBody(gyldigDokarkivResponse()))
        val body = ArkiverDokumentRequest("FNR",
                                          false,
                                          listOf(HOVEDDOKUMENT, VEDLEGG))

        val response =
                restTemplate.exchange(localhost(DOKARKIV_URL),
                                      HttpMethod.POST,
                                      HttpEntity(body, headers),
                                      Ressurs::class.java)

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        Assertions.assertThat(response.body?.data!!["journalpostId"].textValue()).isEqualTo("12345678")
        Assertions.assertThat(response.body?.data!!["ferdigstilt"].booleanValue()).isFalse()
    }

    @Test @Throws(IOException::class) fun dokarkiv_returnerer_401() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withPath("/rest/journalpostapi/v1/journalpost")
                                .withQueryStringParameter("foersoekFerdigstill", "false"))
                .respond(HttpResponse.response().withStatusCode(401).withBody("Tekst fra body"))
        val body = ArkiverDokumentRequest("FNR",
                                          false,
                                          listOf(Dokument("foo".toByteArray(),
                                                          FilType.PDFA,
                                                          null,
                                                          DokumentType.KONTANTSTØTTE_SØKNAD)))

        val response = restTemplate.exchange(localhost(DOKARKIV_URL),
                                             HttpMethod.POST,
                                             HttpEntity(body, headers),
                                             Ressurs::class.java)

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        Assertions.assertThat(response.body?.melding).contains("Feilresponse fra dokarkiv-tjenesten 401 Tekst fra body")
    }

    @Test @Throws(IOException::class) fun ferdigstill_returnerer_OK() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("PATCH")
                                .withPath("/rest/journalpostapi/v1/journalpost/123/ferdigstill"))
                .respond(HttpResponse.response().withStatusCode(200))

        val response =
                restTemplate.exchange(localhost("$DOKARKIV_URL/123/ferdigstill?journalfoerendeEnhet=9999"),
                                      HttpMethod.PUT,
                                      HttpEntity<Any?>(null, headers),
                                      Ressurs::class.java)

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
    }

    @Test @Throws(IOException::class) fun ferdigstill_returnerer_400_hvis_ikke_mulig_ferdigstill() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("PATCH")
                                .withPath("/rest/journalpostapi/v1/journalpost/123/ferdigstill"))
                .respond(HttpResponse.response().withStatusCode(400))

        val response =
                restTemplate.exchange(localhost("$DOKARKIV_URL/123/ferdigstill?journalfoerendeEnhet=9999"),
                                      HttpMethod.PUT,
                                      HttpEntity<Any?>(null, headers),
                                      Ressurs::class.java)

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