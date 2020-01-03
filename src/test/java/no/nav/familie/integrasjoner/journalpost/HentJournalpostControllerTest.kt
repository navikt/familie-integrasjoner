package no.nav.familie.integrasjoner.journalpost

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.security.token.support.test.JwtTokenGenerator
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockserver.junit.MockServerRule
import org.mockserver.model.Header
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.slf4j.LoggerFactory
import org.springframework.boot.test.web.client.exchange
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.util.UriComponentsBuilder
import java.nio.charset.StandardCharsets
import java.nio.file.Files

@ActiveProfiles("integrasjonstest", "mock-sts", "mock-innsyn")
class HentJournalpostControllerTest : OppslagSpringRunnerTest() {

    private val testLogger = LoggerFactory.getLogger(HentJournalpostController::class.java) as Logger
    @get:Rule
    val mockServerRule = MockServerRule(this, MOCK_SERVER_PORT)
    private lateinit var uriHentSaksnummer: String

    @Before
    fun setUp() {
        testLogger.addAppender(listAppender)
        headers.setBearerAuth(JwtTokenGenerator.signedJWTAsString("testbruker"))
        uriHentSaksnummer = UriComponentsBuilder.fromHttpUrl(localhost(JOURNALPOST_BASE_URL) + "/sak")
                .queryParam("journalpostId", JOURNALPOST_ID).toUriString()
    }

    @Test
    fun `hent saksnummer skal returnere saksnummer og status ok`() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withPath("/rest/saf/graphql")
                                .withBody(testdata("gyldigrequest.json")))
                .respond(HttpResponse.response().withBody(testdata("gyldigresponse.json"))
                                 .withHeaders(Header("Content-Type", "application/json")))

        val response: ResponseEntity<Ressurs<Map<String, String>>> = restTemplate.exchange(uriHentSaksnummer,
                                                                                           HttpMethod.GET,
                                                                                           HttpEntity<String>(headers))

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        Assertions.assertThat(response.body?.data?.get("saksnummer")).isEqualTo(SAKSNUMMER)
    }

    @Test
    fun `hent saksnummer skal returnere status 404 hvis sak mangler`() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withHeader(Header("Content-Type", "application/json"))
                                .withPath("/rest/saf/graphql"))
                .respond(HttpResponse.response().withBody(testdata("mangler_sak.json"))
                                 .withHeaders(Header("Content-Type", "application/json")))

        val response: ResponseEntity<Ressurs<Map<String, String>>> = restTemplate.exchange(uriHentSaksnummer,
                                                                                           HttpMethod.GET,
                                                                                           HttpEntity<String>(headers))

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        Assertions.assertThat(response.body?.melding).isEqualTo("Sak mangler for journalpostId=$JOURNALPOST_ID")
    }

    @Test
    fun `hent saksnummer skal returnere status 404 hvis sak ikke er gsak`() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withHeader(Header("Content-Type", "application/json"))
                                .withPath("/rest/saf/graphql"))
                .respond(HttpResponse.response().withBody(testdata("feil_arkivsaksystem.json"))
                                 .withHeaders(Header("Content-Type", "application/json")))

        val response: ResponseEntity<Ressurs<Map<String, String>>> = restTemplate.exchange(uriHentSaksnummer,
                                                                                           HttpMethod.GET,
                                                                                           HttpEntity<String>(headers))

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        Assertions.assertThat(response.body?.melding).isEqualTo("Sak mangler for journalpostId=$JOURNALPOST_ID")
    }

    @Test
    fun `hent saksnummer skal returnerer 500 hvis klient returnerer 200 med feilmeldinger`() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withHeader(Header("Content-Type", "application/json"))
                                .withPath("/rest/saf/graphql"))
                .respond(HttpResponse.response().withBody(testdata("error_fra_saf.json"))
                                 .withHeaders(Header("Content-Type", "application/json")))

        val response: ResponseEntity<Ressurs<Map<String, String>>> = restTemplate.exchange(uriHentSaksnummer,
                                                                                           HttpMethod.GET,
                                                                                           HttpEntity<String>(headers))

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        Assertions.assertThat(response.body?.melding)
                .contains("Feil ved henting av journalpost=12345678 klientfeilmelding=Kan ikke hente journalpost " +
                          "[SafError{message='Feilet ved henting av data (/journalpost) : null', " +
                          "exceptionType='TECHNICAL', exception='NullPointerException'}]")
        Assertions.assertThat(loggingEvents)
                .extracting<Level, RuntimeException> { obj: ILoggingEvent -> obj.level }
                .containsExactly(Level.WARN)
    }

    @Test
    fun `hent saksnummer skal returnere 500 ved ukjent feil`() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withHeader(Header("Content-Type", "application/json"))
                                .withPath("/rest/saf/graphql"))
                .respond(HttpResponse.response().withStatusCode(500).withBody("feilmelding"))

        val response: ResponseEntity<Ressurs<Map<String, String>>> = restTemplate.exchange(uriHentSaksnummer,
                                                                                           HttpMethod.GET,
                                                                                           HttpEntity<String>(headers))

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        Assertions.assertThat(response.body?.melding)
                .contains("Feil ved henting av journalpost=12345678 statuscode=500 INTERNAL_SERVER_ERROR body=feilmelding")
        Assertions.assertThat(loggingEvents)
                .extracting<Level, RuntimeException> { obj: ILoggingEvent -> obj.level }
                .containsExactly(Level.WARN)
    }

    @Test
    fun `hente journalpost basert på kanalreferanseId skal returnere journalpost`() {
        val response: ResponseEntity<Ressurs<Map<String, String>>> =
                restTemplate.exchange(UriComponentsBuilder.fromHttpUrl(localhost(JOURNALPOST_BASE_URL))
                                              .queryParam("kanalReferanseId", JOURNALPOST_ID).toUriString(),
                                      HttpMethod.GET,
                                      HttpEntity<String>(headers))

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        Assertions.assertThat(response.body?.data?.get("journalpostId")).isEqualTo(JOURNALPOST_ID)
    }

    @Test
    fun `hente journalpost basert på kanalreferanseId skal returnere not found hvis ingen journalpost`() {
        val response: ResponseEntity<Ressurs<Map<String, String>>> =
                restTemplate.exchange(UriComponentsBuilder.fromHttpUrl(localhost(JOURNALPOST_BASE_URL))
                                              .queryParam("kanalReferanseId",
                                                          HentJournalpostTestConfig.NOT_FOUND_CALLID)
                                              .toUriString(),
                                      HttpMethod.GET,
                                      HttpEntity<String>(headers))

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
    }

    @Test
    fun `hente journalpost basert på kanalreferanseId skal returnere internal error ved ukjent feil`() {
        val response: ResponseEntity<Ressurs<Map<String, String>>> =
                restTemplate.exchange(UriComponentsBuilder.fromHttpUrl(localhost(JOURNALPOST_BASE_URL))
                                              .queryParam("kanalReferanseId",
                                                          HentJournalpostTestConfig.GENERISK_ERROR_CALLID)
                                              .toUriString(),
                                      HttpMethod.GET,
                                      HttpEntity<String>(headers))

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        Assertions.assertThat(loggingEvents)
                .extracting<Level, RuntimeException> { obj: ILoggingEvent -> obj.level }
                .containsExactly(Level.WARN)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
    }

    private fun testdata(filnavn: String): String {
        return Files.readString(ClassPathResource("saf/$filnavn").file.toPath(), StandardCharsets.UTF_8)
    }

    companion object {
        const val MOCK_SERVER_PORT = 18321
        const val JOURNALPOST_ID = "12345678"
        const val SAKSNUMMER = "87654321"
        const val JOURNALPOST_BASE_URL = "/api/journalpost/"
    }
}