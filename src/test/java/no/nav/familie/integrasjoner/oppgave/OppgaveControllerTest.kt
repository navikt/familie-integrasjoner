package no.nav.familie.integrasjoner.oppgave

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import com.github.tomakehurst.wiremock.client.WireMock.*
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.integrasjoner.config.ApiExceptionHandler
import no.nav.familie.integrasjoner.oppgave.domene.FinnOppgaveResponseDto
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.felles.oppgave.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.test.web.client.exchange
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.time.LocalDate

@ActiveProfiles("integrasjonstest", "mock-sts")
@TestPropertySource(properties = ["OPPGAVE_URL=http://localhost:28085"])
@AutoConfigureWireMock(port = 28085)
class OppgaveControllerTest : OppslagSpringRunnerTest() {

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
        stubFor(get(GET_OPPGAVER_URL).willReturn(ok()))

        val oppgave = Oppgave(aktoerId = "1234567891011",
                              journalpostId = "1",
                              eksisterendeOppgaveId = null,
                              beskrivelse = "test NPE",
                              tema = Tema.KON)

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
        stubFor(get(GET_OPPGAVER_URL).willReturn(status(404)))

        val oppgave = Oppgave(aktoerId = "1234567891011",
                              journalpostId = "1",
                              eksisterendeOppgaveId = null,
                              beskrivelse = "test RestClientException",
                              tema = Tema.KON)

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
        stubFor(get(GET_OPPGAVER_URL).willReturn(okJson(gyldigOppgaveResponse("tom_response.json"))))


        val oppgave = Oppgave(aktoerId = "1234567891011",
                              journalpostId = "1",
                              eksisterendeOppgaveId = null,
                              beskrivelse = "test oppgave ikke funnet",
                              tema = Tema.KON)

        val response: ResponseEntity<Ressurs<Map<String, Long>>> = restTemplate.exchange(localhost(OPPDATER_OPPGAVE_URL),
                                                                                         HttpMethod.POST,
                                                                                         HttpEntity(oppgave, headers))

        assertThat(loggingEvents)
                .extracting<String, RuntimeException> { obj: ILoggingEvent -> obj.formattedMessage }
                .anyMatch {
                    it.contains("[oppgave][Ingen oppgaver funnet for http://localhost:28085/api/v1/oppgaver" +
                                "?aktoerId=1234567891011&tema=KON&oppgavetype=BEH_SAK&journalpostId=1]")
                }
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `skal ignorere oppdatering hvis oppgave er ferdigstilt`() {
        stubFor(get(GET_OPPGAVER_URL).willReturn(okJson(gyldigOppgaveResponse("ferdigstilt_oppgave.json"))))

        val oppgave = Oppgave(aktoerId = "1234567891011",
                              journalpostId = "1",
                              eksisterendeOppgaveId = null,
                              beskrivelse = "test oppgave ikke funnet",
                              tema = null)

        val response: ResponseEntity<Ressurs<Map<String, Long>>> = restTemplate.exchange(localhost(OPPDATER_OPPGAVE_URL),
                                                                                         HttpMethod.POST,
                                                                                         HttpEntity(oppgave, headers))

        assertThat(loggingEvents).extracting<String, RuntimeException> { obj: ILoggingEvent -> obj.formattedMessage }
                .anyMatch {
                    it.contains("Ignorerer oppdatering av oppgave som er ferdigstilt for aktørId=1234567891011 " +
                                "journalpostId=123456789 oppgaveId=${OPPGAVE_ID}")
                }
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }


    @Test
    fun `skal oppdatere oppgave med ekstra beskrivelse, returnere oppgaveid og 200 OK`() {

        stubFor(get(GET_OPPGAVER_URL).willReturn(okJson(gyldigOppgaveResponse("oppgave.json"))))

        stubFor(patch(urlEqualTo("/api/v1/oppgaver/${OPPGAVE_ID}"))
                        .withRequestBody(matchingJsonPath("$.[?(@.beskrivelse == 'Behandle sak$EKSTRA_BESKRIVELSE')]"))
                        .willReturn(aResponse()
                                            .withStatus(201)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody(gyldigOppgaveResponse("ferdigstilt_oppgave.json"))))

        val oppgave = Oppgave(aktoerId = "1234567891011",
                              journalpostId = "1",
                              eksisterendeOppgaveId = null,
                              beskrivelse = EKSTRA_BESKRIVELSE,
                              tema = null)

        val response: ResponseEntity<Ressurs<OppgaveResponse>> =
                restTemplate.exchange(localhost(OPPDATER_OPPGAVE_URL),
                                      HttpMethod.POST,
                                      HttpEntity(oppgave, headers))
        assertThat(response.body?.data?.oppgaveId).isEqualTo(OPPGAVE_ID)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }


    @Test
    fun `skal opprette oppgave, returnere oppgaveid og 201 Created`() {

        stubFor(post("/api/v1/oppgaver").willReturn(okJson(objectMapper.writeValueAsString(Oppgave(id = OPPGAVE_ID)))))

        val opprettOppgave = OpprettOppgave(
                ident = OppgaveIdent(ident = "123456789012", type = IdentType.Aktør),
                fristFerdigstillelse = LocalDate.now().plusDays(3),
                behandlingstema = "behandlingstema",
                enhetsnummer = "enhetsnummer",
                tema = Tema.BAR,
                oppgavetype = Oppgavetype.BehandleSak,
                saksId = "saksid",
                beskrivelse = "Oppgavetekst"
        )
        val response: ResponseEntity<Ressurs<OppgaveResponse>> =
                restTemplate.exchange(localhost(OPPGAVE_URL),
                                      HttpMethod.POST,
                                      HttpEntity(opprettOppgave, headers))

        assertThat(response.body?.data?.oppgaveId).isEqualTo(OPPGAVE_ID)
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
    }

    @Test
    fun `kall mot oppgave ved opprett feiler med bad request, tjenesten vår returernerer 500 og med info om feil i response `() {

        stubFor(post("/api/v1/oppgaver")
                        .willReturn(aResponse()
                                            .withStatus(400)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody("body")))
        val opprettOppgave = OpprettOppgave(
                ident = OppgaveIdent(ident = "123456789012", type = IdentType.Aktør),
                fristFerdigstillelse = LocalDate.now().plusDays(3),
                behandlingstema = "behandlingstema",
                enhetsnummer = "enhetsnummer",
                tema = Tema.BAR,
                oppgavetype = Oppgavetype.BehandleSak,
                saksId = "saksid",
                beskrivelse = "Oppgavetekst"
        )
        val response: ResponseEntity<Ressurs<OppgaveResponse>> =
                restTemplate.exchange(localhost(OPPGAVE_URL),
                                      HttpMethod.POST,
                                      HttpEntity(opprettOppgave, headers))
        assertThat(response.body?.melding).contains("Feil ved oppretting av oppgave for 123456789012. Response fra oppgave = body")
        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
    }


    @Test
    fun `Ferdigstilling av oppgave som er alt ferdigstillt skal logge og returnerer 200 OK`() {
        stubFor(get("/api/v1/oppgaver/123").willReturn(okJson(
                objectMapper.writeValueAsString(Oppgave(id = 123,
                                                        status = StatusEnum.FERDIGSTILT)))))


        verify(exactly(0), patchRequestedFor(urlEqualTo("/api/v1/oppgaver/123")))

        val response: ResponseEntity<Ressurs<OppgaveResponse>> =
                restTemplate.exchange(localhost("/api/oppgave/123/ferdigstill"),
                                      HttpMethod.PATCH,
                                      HttpEntity(null, headers))

        assertThat(response.body?.data?.oppgaveId).isEqualTo(123)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `Ferdigstilling av oppgave som er feilregistrert skal generere en oppslagsfeil`() {
        stubFor(get("/api/v1/oppgaver/${OPPGAVE_ID}").willReturn(okJson(
                objectMapper.writeValueAsString(Oppgave(id = OPPGAVE_ID, status = StatusEnum.FEILREGISTRERT)))))

        val response: ResponseEntity<Ressurs<OppgaveResponse>> =
                restTemplate.exchange(localhost("/api/oppgave/${OPPGAVE_ID}/ferdigstill"),
                                      HttpMethod.PATCH,
                                      HttpEntity(null, headers))

        assertThat(response.body?.melding).contains("Oppgave har status feilregistrert og kan ikke oppdateres")
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `Ferdigstilling av oppgave som er i status opprettet skal gjøre et patch kall mot oppgave med status FERDIGSTILL`() {
        stubFor(get("/api/v1/oppgaver/${OPPGAVE_ID}").willReturn(okJson(
                objectMapper.writeValueAsString(Oppgave(id = OPPGAVE_ID, status = StatusEnum.OPPRETTET)))))

        stubFor(patch(urlEqualTo("/api/v1/oppgaver/${OPPGAVE_ID}"))
                        .withRequestBody(matchingJsonPath("$.[?(@.status == 'FERDIGSTILT')]"))
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody(objectMapper.writeValueAsBytes(Oppgave(id = OPPGAVE_ID,
                                                                                             status = StatusEnum.FERDIGSTILT)))))

        val response: ResponseEntity<Ressurs<OppgaveResponse>> =
                restTemplate.exchange(localhost("/api/oppgave/${OPPGAVE_ID}/ferdigstill"),
                                      HttpMethod.PATCH,
                                      HttpEntity(null, headers))

        assertThat(response.body?.melding).contains("ferdigstill OK")
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `finnOppgaver skal fungere ved retur av 0 oppgaver`() {
        stubFor(get("/api/v1/oppgaver?statuskategori=AAPEN&aktivDatoTom=${LocalDate.now()}&tema=BAR&limit=50&offset=0")
                        .willReturn(okJson(gyldigOppgaveResponse("tom_response.json"))))

        val response: ResponseEntity<Ressurs<List<Oppgave>>> =
                restTemplate.exchange(localhost("/api/oppgave?tema=BAR"), HttpMethod.GET, HttpEntity(null, headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.data).isEmpty()
    }

    @Test
    fun `finnOppgaver skal fungere ved retur av 1 oppgave`() {
        stubFor(get("/api/v1/oppgaver?statuskategori=AAPEN&aktivDatoTom=${LocalDate.now()}&tema=BAR&limit=50&offset=0")
                        .willReturn(okJson(gyldigOppgaveResponse("oppgave.json"))))

        val response: ResponseEntity<Ressurs<List<Oppgave>>> =
                restTemplate.exchange(localhost("/api/oppgave?tema=BAR"), HttpMethod.GET, HttpEntity(null, headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.data).hasSize(1)
    }

    @Test
    fun `finnOppgaver skal fungere ved retur av 51 oppgaver`() {
        val oppgaver50stk = FinnOppgaveResponseDto(51, List(50) {
            Oppgave()
        })
        val oppgaver1stk = FinnOppgaveResponseDto(51, List(1) {
            Oppgave()
        })

        stubFor(get("/api/v1/oppgaver?statuskategori=AAPEN&aktivDatoTom=${LocalDate.now()}&tema=BAR&limit=50&offset=0")
                        .willReturn(okJson(objectMapper.writeValueAsString(oppgaver50stk))))

        stubFor(get("/api/v1/oppgaver?statuskategori=AAPEN&aktivDatoTom=${LocalDate.now()}&tema=BAR&limit=50&offset=50")
                        .willReturn(okJson(objectMapper.writeValueAsString(oppgaver1stk))))

        val response: ResponseEntity<Ressurs<List<Oppgave>>> =
                restTemplate.exchange(localhost("/api/oppgave?tema=BAR"), HttpMethod.GET, HttpEntity(null, headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.data).hasSize(51)
    }

    @Test
    fun `finnOppgaver skal feile hvis tema ikke er angitt`() {
        val response: ResponseEntity<Ressurs<List<Oppgave>>> =
                restTemplate.exchange(localhost("/api/oppgave"), HttpMethod.GET, HttpEntity(null, headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `Skal hente oppgave basert på id`() {
        stubFor(get(GET_OPPGAVE_URL).willReturn(okJson(objectMapper.writeValueAsString(Oppgave(id = OPPGAVE_ID)))))

        val response: ResponseEntity<Ressurs<Oppgave>> =
                restTemplate.exchange(localhost("/api/oppgave/$OPPGAVE_ID"), HttpMethod.GET, HttpEntity(null, headers))

        println(response.body)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.data?.id).isEqualTo(OPPGAVE_ID)
    }

    private fun gyldigOppgaveResponse(filnavn: String): String {
        return Files.readString(ClassPathResource("oppgave/$filnavn").file.toPath(),
                                StandardCharsets.UTF_8)
    }

    companion object {
        private const val OPPGAVE_URL = "/api/oppgave/"
        private const val OPPDATER_OPPGAVE_URL = "${OPPGAVE_URL}/oppdater"
        private const val OPPGAVE_ID = 315488374L
        private const val GET_OPPGAVER_URL =
                "/api/v1/oppgaver?aktoerId=1234567891011&tema=KON&oppgavetype=BEH_SAK&journalpostId=1"
        private const val GET_OPPGAVE_URL = "/api/v1/oppgaver/$OPPGAVE_ID"
        private const val EKSTRA_BESKRIVELSE = " Ekstra beskrivelse"
    }
}