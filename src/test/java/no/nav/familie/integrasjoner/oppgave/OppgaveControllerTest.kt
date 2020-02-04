package no.nav.familie.integrasjoner.oppgave

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.integrasjoner.config.ApiExceptionHandler
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import org.assertj.core.api.Assertions.assertThat
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
import java.nio.charset.StandardCharsets
import java.nio.file.Files

@ActiveProfiles("integrasjonstest", "mock-sts")
class OppgaveControllerTest : OppslagSpringRunnerTest() {

    @get:Rule
    var mockServerRule = MockServerRule(this, MOCK_SERVER_PORT)

    @Before
    fun setup() {
        val oppgaveControllerLogger =
                LoggerFactory.getLogger(OppgaveController::class.java) as Logger
        val oppgaveServiceLogger =
                LoggerFactory.getLogger(OppgaveService::class.java) as Logger
        val exceptionHandler =
                LoggerFactory.getLogger(ApiExceptionHandler::class.java) as Logger
        oppgaveControllerLogger.addAppender(listAppender)
        oppgaveServiceLogger.addAppender(listAppender)
        exceptionHandler.addAppender(listAppender)
        headers.setBearerAuth(lokalTestToken)
    }

    @Test
    fun `skal logge stack trace og returnere internal server error ved IllegalStateException`() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("GET")
                                .withPath("/api/v1/oppgaver"))
                .respond(HttpResponse.response())
        val oppgave = Oppgave("1234567891011", "1", null, "test NPE")

        val response: ResponseEntity<Ressurs<Map<String, Long>>> = restTemplate.exchange(localhost(OPPDATER_OPPGAVE_URL),
                                                                                         HttpMethod.POST,
                                                                                         HttpEntity(oppgave, headers))

        assertThat(loggingEvents)
                .extracting<String, RuntimeException> { obj: ILoggingEvent -> obj.formattedMessage }
                .anyMatch { s: String -> s.contains("Exception : java.lang.IllegalStateException") }
        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @Test
    fun `skal logge og returnere internal server error ved restClientException`() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("GET")
                                .withPath("/api/v1/oppgaver"))
                .respond(HttpResponse.notFoundResponse())
        val oppgave = Oppgave("1234567891011", "1", null, "test RestClientException")

        val response: ResponseEntity<Ressurs<Map<String, Long>>> = restTemplate.exchange(localhost(OPPDATER_OPPGAVE_URL),
                                                                                         HttpMethod.POST,
                                                                                         HttpEntity(oppgave, headers))

        assertThat(loggingEvents)
                .extracting<String, RuntimeException> { obj: ILoggingEvent -> obj.formattedMessage }
                .anyMatch { it.contains("HttpClientErrorException") }
        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @Test
    fun `skal logge og returnere not found ved oppgaveIkkeFunnetException`() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("GET")
                                .withPath("/api/v1/oppgaver"))
                .respond(HttpResponse.response().withBody(gyldigOppgaveResponse("tom_response.json"))
                                 .withHeaders(Header("Content-Type", "application/json")))
        val oppgave = Oppgave("1234567891011", "1", null, "test oppgave ikke funnet")

        val response: ResponseEntity<Ressurs<Map<String, Long>>> = restTemplate.exchange(localhost(OPPDATER_OPPGAVE_URL),
                                                                                         HttpMethod.POST,
                                                                                         HttpEntity(oppgave, headers))

        assertThat(loggingEvents)
                .extracting<String, RuntimeException> { obj: ILoggingEvent -> obj.formattedMessage }
                .anyMatch {
                    it.contains("[oppgave][Ingen oppgaver funnet for http://localhost:18321/api/v1/oppgaver" +
                                "?aktoerId=1234567891011&tema=KON&oppgavetype=BEH_SAK&journalpostId=1]")
                }
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `skal ignorere oppdatering hvis oppgave er ferdigstilt`() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("GET")
                                .withPath("/api/v1/oppgaver"))
                .respond(HttpResponse.response().withBody(gyldigOppgaveResponse("ferdigstilt_oppgave.json"))
                                 .withHeaders(Header("Content-Type", "application/json")))
        val oppgave = Oppgave("1234567891011", "1", null, "test oppgave ikke funnet")

        val response: ResponseEntity<Ressurs<Map<String, Long>>> = restTemplate.exchange(localhost(OPPDATER_OPPGAVE_URL),
                                                                                         HttpMethod.POST,
                                                                                         HttpEntity(oppgave, headers))

        assertThat(loggingEvents).extracting<String, RuntimeException> { obj: ILoggingEvent -> obj.formattedMessage }
                .anyMatch {
                    it.contains("Ignorerer oppdatering av oppgave som er ferdigstilt for aktørId=1234567891011 " +
                                "journalpostId=123456789 oppgaveId=315488374")
                }
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    private fun gyldigOppgaveResponse(filnavn: String): String {
        return Files.readString(ClassPathResource("oppgave/$filnavn").file.toPath(),
                                StandardCharsets.UTF_8)
    }

    companion object {
        private const val OPPDATER_OPPGAVE_URL = "/api/oppgave/oppdater"
        private const val MOCK_SERVER_PORT = 18321
    }
}