package no.nav.familie.integrasjoner.oppgave

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import com.github.tomakehurst.wiremock.client.WireMock.*
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.integrasjoner.config.ApiExceptionHandler
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
import org.springframework.web.util.UriComponentsBuilder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.time.LocalDate
import java.time.LocalDateTime

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
    fun `skal opprette oppgave uten ident, returnere oppgaveid og 201 Created`() {

        stubFor(post("/api/v1/oppgaver").willReturn(okJson(objectMapper.writeValueAsString(Oppgave(id = OPPGAVE_ID)))))

        val opprettOppgave = OpprettOppgave(
                ident = null,
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
        assertThat(response.body?.melding)
                .contains("Feil ved oppretting av oppgave for 123456789012. Response fra oppgave = body")
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
    fun `finnOppgaverV3 skal fungere ved retur av 0 oppgaver`() {
        val finnOppgaveRequest = FinnOppgaveRequest(Tema.BAR)
        val url = UriComponentsBuilder.fromHttpUrl(localhost("/api/oppgave/v3"))
                .queryParams(finnOppgaveRequest.toQueryParams()).build().toUri()
        stubFor(get("/api/v1/oppgaver?statuskategori=AAPEN&tema=BAR&sorteringsfelt=OPPRETTET_TIDSPUNKT" +
                    "&sorteringsrekkefolge=DESC&limit=50&offset=0")
                        .willReturn(okJson(gyldigOppgaveResponse("tom_response.json"))))

        val response: ResponseEntity<Ressurs<FinnOppgaveResponseDto>> =
                restTemplate.exchange(url,
                                      HttpMethod.GET,
                                      HttpEntity(DeprecatedFinnOppgaveRequest("BAR"), headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.data?.oppgaver).isEmpty()
    }

    @Test
    fun `finnOppgaverV3 skal fungere ved retur av 1 oppgave`() {
        val finnOppgaveRequest = FinnOppgaveRequest(Tema.BAR)
        val url = UriComponentsBuilder.fromHttpUrl(localhost("/api/oppgave/v3"))
                .queryParams(finnOppgaveRequest.toQueryParams()).build().toUri()
        stubFor(get("/api/v1/oppgaver?statuskategori=AAPEN&tema=BAR&sorteringsfelt=OPPRETTET_TIDSPUNKT" +
                    "&sorteringsrekkefolge=DESC&limit=50&offset=0")
                        .willReturn(okJson(gyldigOppgaveResponse("oppgave.json"))))

        val response: ResponseEntity<Ressurs<FinnOppgaveResponseDto>> =
                restTemplate.exchange(url,
                                      HttpMethod.GET,
                                      HttpEntity(DeprecatedFinnOppgaveRequest("BAR"), headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.data?.oppgaver).hasSize(1)
    }

    @Test
    fun `finnOppgaverV3 skal fungere ved retur av 51 oppgaver`() {
        val finnOppgaveRequest = FinnOppgaveRequest(Tema.BAR)
        val url = UriComponentsBuilder.fromHttpUrl(localhost("/api/oppgave/v3"))
                .queryParams(finnOppgaveRequest.toQueryParams()).build().toUri()
        val oppgaver50stk = FinnOppgaveResponseDto(51, List(50) { Oppgave() })
        val oppgaver1stk = FinnOppgaveResponseDto(51, List(1) { Oppgave() })

        stubFor(get("/api/v1/oppgaver?statuskategori=AAPEN&tema=BAR&sorteringsfelt=OPPRETTET_TIDSPUNKT" +
                    "&sorteringsrekkefolge=DESC&limit=50&offset=0")
                        .willReturn(okJson(objectMapper.writeValueAsString(oppgaver50stk))))

        stubFor(get("/api/v1/oppgaver?statuskategori=AAPEN&tema=BAR&sorteringsfelt=OPPRETTET_TIDSPUNKT" +
                    "&sorteringsrekkefolge=DESC&limit=1&offset=50")
                        .willReturn(okJson(objectMapper.writeValueAsString(oppgaver1stk))))

        val response: ResponseEntity<Ressurs<FinnOppgaveResponseDto>> =
                restTemplate.exchange(url,
                                      HttpMethod.GET,
                                      HttpEntity(DeprecatedFinnOppgaveRequest("BAR"), headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.data?.oppgaver).hasSize(51)
    }

    @Test
    fun `finnOppgaverV3 skal fungere begrenset til 16 oppgaver`() {
        val finnOppgaveRequest = FinnOppgaveRequest(tema = Tema.BAR, limit = 16)
        val url = UriComponentsBuilder.fromHttpUrl(localhost("/api/oppgave/v3"))
                .queryParams(finnOppgaveRequest.toQueryParams()).build().toUri()
        val oppgaver16stk = FinnOppgaveResponseDto(51, List(16) { Oppgave() })

        stubFor(get("/api/v1/oppgaver?statuskategori=AAPEN&tema=BAR&sorteringsfelt=OPPRETTET_TIDSPUNKT" +
                    "&sorteringsrekkefolge=DESC&limit=16&offset=0")
                        .willReturn(okJson(objectMapper.writeValueAsString(oppgaver16stk))))


        val response: ResponseEntity<Ressurs<FinnOppgaveResponseDto>> =
                restTemplate.exchange(url,
                                      HttpMethod.GET,
                                      HttpEntity(DeprecatedFinnOppgaveRequest("BAR"), headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.data?.oppgaver).hasSize(16)
    }

    @Test
    fun `finnOppgaverV3 skal fungere begrenset til 58 oppgaver`() {
        val finnOppgaveRequest = FinnOppgaveRequest(tema = Tema.BAR, limit = 58)
        val url = UriComponentsBuilder.fromHttpUrl(localhost("/api/oppgave/v3"))
                .queryParams(finnOppgaveRequest.toQueryParams()).build().toUri()
        val oppgaver50stk = FinnOppgaveResponseDto(116, List(50) { Oppgave() })
        val oppgaver8stk = FinnOppgaveResponseDto(116, List(8) { Oppgave() })

        stubFor(get("/api/v1/oppgaver?statuskategori=AAPEN&tema=BAR&sorteringsfelt=OPPRETTET_TIDSPUNKT" +
                    "&sorteringsrekkefolge=DESC&limit=50&offset=0")
                        .willReturn(okJson(objectMapper.writeValueAsString(oppgaver50stk))))

        stubFor(get("/api/v1/oppgaver?statuskategori=AAPEN&tema=BAR&sorteringsfelt=OPPRETTET_TIDSPUNKT" +
                    "&sorteringsrekkefolge=DESC&limit=8&offset=50")
                        .willReturn(okJson(objectMapper.writeValueAsString(oppgaver8stk))))

        val response: ResponseEntity<Ressurs<FinnOppgaveResponseDto>> =
                restTemplate.exchange(url,
                                      HttpMethod.GET,
                                      HttpEntity(DeprecatedFinnOppgaveRequest("BAR"), headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.data?.oppgaver).hasSize(58)
    }

    @Test
    fun `finnOppgaverV3 deserialiserer komplett objekt riktig`() {
        val finnOppgaveRequest = FinnOppgaveRequest(tema = Tema.BAR,
                                                    behandlingstema = Behandlingstema.Barnetrygd,
                                                    oppgavetype = Oppgavetype.BehandleSak,
                                                    enhet = "enhet",
                                                    saksbehandler = "sb",
                                                    aktørId = null,
                                                    saksreferanse = "123rfadsfasdfkdsafkjsdafjk",
                                                    journalpostId = "jpId",
                                                    tilordnetRessurs = "tilordnetRessurs",
                                                    tildeltRessurs = true,
                                                    opprettetFomTidspunkt = LocalDateTime.of(2015, 10, 1, 12, 10),
                                                    opprettetTomTidspunkt = LocalDateTime.of(2016, 11, 2, 13, 11),
                                                    fristFomDato = LocalDate.of(2017, 12, 3),
                                                    fristTomDato = LocalDate.of(2018, 1, 4),
                                                    aktivFomDato = LocalDate.of(2019, 2, 5),
                                                    aktivTomDato = LocalDate.of(2020, 3, 6),
                                                    enhetsmappe = Enhetsmappe.SøknaderKlarTilBehandling,
                                                    offset = 16,
                                                    limit = 46)
        val url = UriComponentsBuilder.fromHttpUrl(localhost("/api/oppgave/v3"))
                .queryParams(finnOppgaveRequest.toQueryParams()).build().toUri()
        stubFor(get("/api/v1/oppgaver?statuskategori=AAPEN&tema=BAR&sorteringsfelt=OPPRETTET_TIDSPUNKT" +
                    "&sorteringsrekkefolge=DESC&limit=46&offset=16&behandlingstema=ab0270&oppgavetype=BEH_SAK" +
                    "&tildeltEnhetsnr=enhet&tildeltRessurs=true&tilordnetRessurs=tilordnetRessurs&journalpostId=jpId" +
                    "&saksreferanse=123rfadsfasdfkdsafkjsdafjk" +
                    "&opprettetFom=2015-10-01T12:10:00&opprettetTom=2016-11-02T13:11:00&fristFom=2017-12-03" +
                    "&fristTom=2018-01-04&aktivDatoFom=2019-02-05&aktivDatoTom=2020-03-06&mappeId=100000035")
                        .willReturn(okJson(gyldigOppgaveResponse("oppgave.json"))))

        val response: ResponseEntity<Ressurs<FinnOppgaveResponseDto>> =
                restTemplate.exchange(url,
                                      HttpMethod.GET,
                                      HttpEntity(DeprecatedFinnOppgaveRequest("BAR"), headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.data?.oppgaver).hasSize(1)
    }

    @Test
    fun `fordelOppgave skal tilordne oppgave til saksbehandler når saksbehandler er satt på requesten`() {
        val saksbehandlerId = "Z999999"
        stubFor(get(GET_OPPGAVE_URL).willReturn(okJson(objectMapper.writeValueAsString(Oppgave(id = OPPGAVE_ID)))))
        stubFor(patch(urlEqualTo("/api/v1/oppgaver/${OPPGAVE_ID}"))
                        .willReturn(aResponse()
                                            .withStatus(201)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody(gyldigOppgaveResponse("oppgave.json"))))

        val response: ResponseEntity<Ressurs<OppgaveResponse>> = restTemplate.exchange(
                localhost("/api/oppgave/$OPPGAVE_ID/fordel?saksbehandler=$saksbehandlerId"),
                HttpMethod.POST,
                HttpEntity(null, headers)
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.melding).isEqualTo("Oppgaven ble tildelt saksbehandler $saksbehandlerId")
        assertThat(response.body?.data).isEqualTo(OppgaveResponse(oppgaveId = OPPGAVE_ID))
    }

    @Test
    fun `fordelOppgave skal tilbakestille tilordning på oppgave når saksbehandler ikke er satt på requesten`() {
        stubFor(get(GET_OPPGAVE_URL).willReturn(okJson(objectMapper.writeValueAsString(Oppgave(id = OPPGAVE_ID)))))
        stubFor(patch(urlEqualTo("/api/v1/oppgaver/${OPPGAVE_ID}"))
                        .willReturn(aResponse()
                                            .withStatus(201)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody(gyldigOppgaveResponse("oppgave.json"))))

        val response: ResponseEntity<Ressurs<OppgaveResponse>> = restTemplate.exchange(
                localhost("/api/oppgave/$OPPGAVE_ID/fordel"),
                HttpMethod.POST,
                HttpEntity(null, headers)
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.melding).isEqualTo("Fordeling på oppgaven ble tilbakestilt")
        assertThat(response.body?.data).isEqualTo(OppgaveResponse(oppgaveId = OPPGAVE_ID))
    }

    @Test
    fun `fordelOppgave skal returnere feil når oppgaven er ferdigstilt`() {
        stubFor(get(GET_OPPGAVE_URL).willReturn(okJson(
                objectMapper.writeValueAsString(Oppgave(id = OPPGAVE_ID, status = StatusEnum.FERDIGSTILT))
        )))
        stubFor(patch(urlEqualTo("/api/v1/oppgaver/${OPPGAVE_ID}"))
                        .willReturn(aResponse()
                                            .withStatus(201)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody(gyldigOppgaveResponse("oppgave.json"))))

        val response: ResponseEntity<Ressurs<OppgaveResponse>> = restTemplate.exchange(
                localhost("/api/oppgave/$OPPGAVE_ID/fordel?saksbehandler=Z999999"),
                HttpMethod.POST,
                HttpEntity(null, headers)
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body?.melding).isEqualTo("Kan ikke fordele oppgave med id $OPPGAVE_ID som allerede er ferdigstilt")
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