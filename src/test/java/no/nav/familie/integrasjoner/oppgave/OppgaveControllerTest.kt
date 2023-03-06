package no.nav.familie.integrasjoner.oppgave

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.exactly
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.patch
import com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.status
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.verify
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.integrasjoner.config.ApiExceptionHandler
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.felles.oppgave.FinnMappeResponseDto
import no.nav.familie.kontrakter.felles.oppgave.IdentGruppe
import no.nav.familie.kontrakter.felles.oppgave.MappeDto
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.kontrakter.felles.oppgave.OppgaveIdentV2
import no.nav.familie.kontrakter.felles.oppgave.OppgaveResponse
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgaveRequest
import no.nav.familie.kontrakter.felles.oppgave.StatusEnum
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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

@ActiveProfiles("integrasjonstest", "mock-oauth")
@TestPropertySource(properties = ["OPPGAVE_URL=http://localhost:28085"])
@AutoConfigureWireMock(port = 28085)
class OppgaveControllerTest : OppslagSpringRunnerTest() {

    @BeforeEach
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
    fun `finnMapper med gyldig query returnerer mapper uten tema som skal filtreres bort`() {
        stubFor(
            get(GET_MAPPER_URL)
                .willReturn(
                    okJson(
                        objectMapper.writeValueAsString(
                            FinnMappeResponseDto(
                                3,
                                listOf(
                                    MappeDto(
                                        1,
                                        "112",
                                        "4489",
                                    ),
                                    MappeDto(
                                        2,
                                        "132",
                                        "4489",
                                    ),
                                    MappeDto(
                                        id = 3,
                                        navn = "123",
                                        enhetsnr = "4489",
                                        tema = "PEN",
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
        )

        val response: ResponseEntity<Ressurs<FinnMappeResponseDto>> =
            restTemplate.exchange(
                localhost("/api/oppgave/mappe/sok?enhetsnr=1234567891011&opprettetFom=dcssdf&limit=50"),
                HttpMethod.GET,
                HttpEntity(null, headers),
            )

        assertThat(response.body?.data?.antallTreffTotalt).isEqualTo(2)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `skal logge stack trace og returnere internal server error ved IllegalStateException`() {
        stubFor(get(GET_OPPGAVER_URL).willReturn(ok()))

        val oppgave = Oppgave(
            aktoerId = "1234567891011",
            journalpostId = "1",
            beskrivelse = "test NPE",
            tema = Tema.KON,
        )

        val response: ResponseEntity<Ressurs<Map<String, Long>>> = restTemplate.exchange(
            localhost(OPPDATER_OPPGAVE_URL),
            HttpMethod.POST,
            HttpEntity(oppgave, headers),
        )

        assertThat(loggingEvents)
            .extracting<String, RuntimeException> { obj: ILoggingEvent -> obj.formattedMessage }
            .anyMatch { s: String -> s.contains("Exception : java.lang.IllegalStateException") }
        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @Test
    fun `skal logge og returnere internal server error ved restClientException`() {
        stubFor(get(GET_OPPGAVER_URL).willReturn(status(404)))

        val oppgave = Oppgave(
            aktoerId = "1234567891011",
            journalpostId = "1",
            beskrivelse = "test RestClientException",
            tema = Tema.KON,
        )

        val response: ResponseEntity<Ressurs<Map<String, Long>>> = restTemplate.exchange(
            localhost(OPPDATER_OPPGAVE_URL),
            HttpMethod.POST,
            HttpEntity(oppgave, headers),
        )

        assertThat(loggingEvents)
            .extracting<String, RuntimeException> { obj: ILoggingEvent -> obj.formattedMessage }
            .anyMatch { it.contains("HttpClientErrorException") }
        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @Test
    fun `skal logge og returnere not found ved oppgaveIkkeFunnetException`() {
        stubFor(get(GET_OPPGAVER_URL).willReturn(okJson(gyldigOppgaveResponse("tom_response.json"))))

        val oppgave = Oppgave(
            aktoerId = "1234567891011",
            journalpostId = "1",
            beskrivelse = "test oppgave ikke funnet",
            tema = Tema.KON,
        )

        val response: ResponseEntity<Ressurs<Map<String, Long>>> = restTemplate.exchange(
            localhost(OPPDATER_OPPGAVE_URL),
            HttpMethod.POST,
            HttpEntity(oppgave, headers),
        )

        assertThat(loggingEvents)
            .extracting<String, RuntimeException> { obj: ILoggingEvent -> obj.formattedMessage }
            .anyMatch {
                it.contains(
                    "[oppgave][Ingen oppgaver funnet for http://localhost:28085/api/v1/oppgaver" +
                        "?aktoerId=1234567891011&tema=KON&oppgavetype=BEH_SAK&journalpostId=1&statuskategori=AAPEN]",
                )
            }
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `skal ignorere oppdatering hvis oppgave er ferdigstilt`() {
        stubFor(get(GET_OPPGAVER_URL).willReturn(okJson(gyldigOppgaveResponse("ferdigstilt_oppgave.json"))))

        val oppgave = Oppgave(
            aktoerId = "1234567891011",
            journalpostId = "1",
            beskrivelse = "test oppgave ikke funnet",
            tema = null,
        )

        val response: ResponseEntity<Ressurs<Map<String, Long>>> = restTemplate.exchange(
            localhost(OPPDATER_OPPGAVE_URL),
            HttpMethod.POST,
            HttpEntity(oppgave, headers),
        )

        assertThat(loggingEvents).extracting<String, RuntimeException> { obj: ILoggingEvent -> obj.formattedMessage }
            .anyMatch {
                it.contains(
                    "Ignorerer oppdatering av oppgave som er ferdigstilt for aktørId=1234567891011 " +
                        "journalpostId=123456789 oppgaveId=$OPPGAVE_ID",
                )
            }
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `skal oppdatere oppgave med ekstra beskrivelse, returnere oppgaveid og 200 OK`() {
        stubFor(get(GET_OPPGAVER_URL).willReturn(okJson(gyldigOppgaveResponse("oppgave.json"))))

        stubFor(
            patch(urlEqualTo("/api/v1/oppgaver/$OPPGAVE_ID"))
                .withRequestBody(matchingJsonPath("$.[?(@.beskrivelse == 'Behandle sak$EKSTRA_BESKRIVELSE')]"))
                .willReturn(
                    aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(gyldigOppgaveResponse("ferdigstilt_oppgave.json")),
                ),
        )

        val oppgave = Oppgave(
            aktoerId = "1234567891011",
            journalpostId = "1",
            beskrivelse = EKSTRA_BESKRIVELSE,
            tema = null,
        )

        val response: ResponseEntity<Ressurs<OppgaveResponse>> =
            restTemplate.exchange(
                localhost(OPPDATER_OPPGAVE_URL),
                HttpMethod.POST,
                HttpEntity(oppgave, headers),
            )
        assertThat(response.body?.data?.oppgaveId).isEqualTo(OPPGAVE_ID)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `skal opprette oppgave med mappeId, returnere oppgaveid og 201 Created`() {
        stubFor(post("/api/v1/oppgaver").willReturn(okJson(objectMapper.writeValueAsString(Oppgave(id = OPPGAVE_ID)))))

        val opprettOppgave = OpprettOppgaveRequest(
            ident = OppgaveIdentV2(ident = "123456789012", gruppe = IdentGruppe.AKTOERID),
            fristFerdigstillelse = LocalDate.now().plusDays(3),
            behandlingstema = "behandlingstema",
            enhetsnummer = "enhetsnummer",
            tema = Tema.ENF,
            oppgavetype = Oppgavetype.BehandleSak,
            mappeId = 1234L,
            saksId = "saksid",
            beskrivelse = "Oppgavetekst",
        )
        val response: ResponseEntity<Ressurs<OppgaveResponse>> =
            restTemplate.exchange(
                localhost(OPPRETT_OPPGAVE_URL_V2),
                HttpMethod.POST,
                HttpEntity(opprettOppgave, headers),
            )

        assertThat(response.body?.data?.oppgaveId).isEqualTo(OPPGAVE_ID)
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
    }

    @Test
    fun `skal opprette oppgave uten ident, returnere oppgaveid og 201 Created`() {
        stubFor(post("/api/v1/oppgaver").willReturn(okJson(objectMapper.writeValueAsString(Oppgave(id = OPPGAVE_ID)))))

        val opprettOppgave = OpprettOppgaveRequest(
            ident = null,
            fristFerdigstillelse = LocalDate.now().plusDays(3),
            behandlingstema = "behandlingstema",
            enhetsnummer = "enhetsnummer",
            tema = Tema.BAR,
            oppgavetype = Oppgavetype.BehandleSak,
            saksId = "saksid",
            beskrivelse = "Oppgavetekst",
        )
        val response: ResponseEntity<Ressurs<OppgaveResponse>> =
            restTemplate.exchange(
                localhost(OPPRETT_OPPGAVE_URL_V2),
                HttpMethod.POST,
                HttpEntity(opprettOppgave, headers),
            )

        assertThat(response.body?.data?.oppgaveId).isEqualTo(OPPGAVE_ID)
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
    }

    @Test
    fun `kall mot oppgave ved opprett feiler med bad request, tjenesten vår returernerer 500 og med info om feil i response `() {
        stubFor(
            post("/api/v1/oppgaver")
                .willReturn(
                    aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("body"),
                ),
        )
        val opprettOppgave = OpprettOppgaveRequest(
            ident = OppgaveIdentV2(ident = "123456789012", gruppe = IdentGruppe.AKTOERID),
            fristFerdigstillelse = LocalDate.now().plusDays(3),
            behandlingstema = "behandlingstema",
            enhetsnummer = "enhetsnummer",
            tema = Tema.BAR,
            oppgavetype = Oppgavetype.BehandleSak,
            saksId = "saksid",
            beskrivelse = "Oppgavetekst",
        )
        val response: ResponseEntity<Ressurs<OppgaveResponse>> =
            restTemplate.exchange(
                localhost(OPPRETT_OPPGAVE_URL_V2),
                HttpMethod.POST,
                HttpEntity(opprettOppgave, headers),
            )
        assertThat(response.body?.melding)
            .contains("Feil ved oppretting av oppgave for 123456789012. Response fra oppgave = body")
        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @Test
    fun `Ferdigstilling av oppgave som er alt ferdigstillt skal logge og returnerer 200 OK`() {
        stubFor(
            get("/api/v1/oppgaver/123").willReturn(
                okJson(
                    objectMapper.writeValueAsString(
                        Oppgave(
                            id = 123,
                            status = StatusEnum.FERDIGSTILT,
                        ),
                    ),
                ),
            ),
        )

        verify(exactly(0), patchRequestedFor(urlEqualTo("/api/v1/oppgaver/123")))

        val response: ResponseEntity<Ressurs<OppgaveResponse>> =
            restTemplate.exchange(
                localhost("/api/oppgave/123/ferdigstill"),
                HttpMethod.PATCH,
                HttpEntity(null, headers),
            )

        assertThat(response.body?.data?.oppgaveId).isEqualTo(123)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `Ferdigstilling av oppgave som er feilregistrert skal generere en oppslagsfeil`() {
        stubFor(
            get("/api/v1/oppgaver/$OPPGAVE_ID").willReturn(
                okJson(
                    objectMapper.writeValueAsString(Oppgave(id = OPPGAVE_ID, status = StatusEnum.FEILREGISTRERT)),
                ),
            ),
        )

        val response: ResponseEntity<Ressurs<OppgaveResponse>> =
            restTemplate.exchange(
                localhost("/api/oppgave/$OPPGAVE_ID/ferdigstill"),
                HttpMethod.PATCH,
                HttpEntity(null, headers),
            )

        assertThat(response.body?.melding).contains("Oppgave har status feilregistrert og kan ikke oppdateres")
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `Ferdigstilling av oppgave som er i status opprettet skal gjøre et patch kall mot oppgave med status FERDIGSTILL`() {
        stubFor(
            get("/api/v1/oppgaver/$OPPGAVE_ID").willReturn(
                okJson(
                    objectMapper.writeValueAsString(Oppgave(id = OPPGAVE_ID, status = StatusEnum.OPPRETTET)),
                ),
            ),
        )

        stubFor(
            patch(urlEqualTo("/api/v1/oppgaver/$OPPGAVE_ID"))
                .withRequestBody(matchingJsonPath("$.[?(@.status == 'FERDIGSTILT')]"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            objectMapper.writeValueAsBytes(
                                Oppgave(
                                    id = OPPGAVE_ID,
                                    status = StatusEnum.FERDIGSTILT,
                                ),
                            ),
                        ),
                ),
        )

        val response: ResponseEntity<Ressurs<OppgaveResponse>> =
            restTemplate.exchange(
                localhost("/api/oppgave/$OPPGAVE_ID/ferdigstill"),
                HttpMethod.PATCH,
                HttpEntity(null, headers),
            )

        assertThat(response.body?.melding).contains("ferdigstill OK")
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `fordelOppgave skal tilordne oppgave til saksbehandler når saksbehandler er satt på requesten`() {
        val saksbehandlerId = "Z999999"
        stubFor(get(GET_OPPGAVE_URL).willReturn(okJson(objectMapper.writeValueAsString(Oppgave(id = OPPGAVE_ID)))))
        stubFor(
            patch(urlEqualTo("/api/v1/oppgaver/$OPPGAVE_ID"))
                .willReturn(
                    aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(gyldigOppgaveResponse("oppgave.json")),
                ),
        )

        val response: ResponseEntity<Ressurs<OppgaveResponse>> = restTemplate.exchange(
            localhost("/api/oppgave/$OPPGAVE_ID/fordel?saksbehandler=$saksbehandlerId"),
            HttpMethod.POST,
            HttpEntity(null, headers),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.melding).isEqualTo("Oppgaven ble tildelt saksbehandler $saksbehandlerId")
        assertThat(response.body?.data).isEqualTo(OppgaveResponse(oppgaveId = OPPGAVE_ID))
    }

    @Test
    fun `fordelOppgave skal tilbakestille tilordning på oppgave når saksbehandler ikke er satt på requesten`() {
        stubFor(get(GET_OPPGAVE_URL).willReturn(okJson(objectMapper.writeValueAsString(Oppgave(id = OPPGAVE_ID)))))
        stubFor(
            patch(urlEqualTo("/api/v1/oppgaver/$OPPGAVE_ID"))
                .willReturn(
                    aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(gyldigOppgaveResponse("oppgave.json")),
                ),
        )

        val response: ResponseEntity<Ressurs<OppgaveResponse>> = restTemplate.exchange(
            localhost("/api/oppgave/$OPPGAVE_ID/fordel"),
            HttpMethod.POST,
            HttpEntity(null, headers),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.melding).isEqualTo("Fordeling på oppgaven ble tilbakestilt")
        assertThat(response.body?.data).isEqualTo(OppgaveResponse(oppgaveId = OPPGAVE_ID))
    }

    @Test
    fun `fordelOppgave skal returnere feil når oppgaven er ferdigstilt`() {
        stubFor(
            get(GET_OPPGAVE_URL).willReturn(
                okJson(
                    objectMapper.writeValueAsString(Oppgave(id = OPPGAVE_ID, status = StatusEnum.FERDIGSTILT)),
                ),
            ),
        )
        stubFor(
            patch(urlEqualTo("/api/v1/oppgaver/$OPPGAVE_ID"))
                .willReturn(
                    aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(gyldigOppgaveResponse("oppgave.json")),
                ),
        )

        val response: ResponseEntity<Ressurs<OppgaveResponse>> = restTemplate.exchange(
            localhost("/api/oppgave/$OPPGAVE_ID/fordel?saksbehandler=Z999999"),
            HttpMethod.POST,
            HttpEntity(null, headers),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body?.melding).isEqualTo("[Oppgave.fordel][Kan ikke fordele oppgave med id $OPPGAVE_ID som allerede er ferdigstilt]")
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

    @Test
    fun `Endre enhet på mappe skal endre enhet og sette mappe til null hvis fjernMappeFraOppgave-flagg satt til true`() {
        stubFor(get("/api/v1/oppgaver/$OPPGAVE_ID").willReturn(okJson(gyldigOppgaveResponse("hentOppgave.json"))))

        stubFor(
            patch(urlEqualTo("/api/v1/oppgaver/$OPPGAVE_ID"))
                .withRequestBody(
                    WireMock.equalToJson("""{"id":315488374,"tildeltEnhetsnr": "4833","versjon":1,"mappeId":null}"""),
                )
                .willReturn(
                    aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(gyldigOppgaveResponse("ferdigstilt_oppgave.json")),
                ),
        )

        val oppgave = Oppgave(
            aktoerId = "1234567891011",
            journalpostId = "1",
            beskrivelse = EKSTRA_BESKRIVELSE,
            tema = null,
        )

        val response: ResponseEntity<Ressurs<OppgaveResponse>> =
            restTemplate.exchange(
                localhost("$OPPGAVE_URL/$OPPGAVE_ID/enhet/4833?fjernMappeFraOppgave=true"),
                HttpMethod.PATCH,
                HttpEntity(oppgave, headers),
            )
        assertThat(response.body?.data?.oppgaveId).isEqualTo(OPPGAVE_ID)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `Endre enhet på mappe skal endre enhet og beholde mappe hvis fjernMappeFraOppgave-flagg satt til false`() {
        stubFor(get("/api/v1/oppgaver/$OPPGAVE_ID").willReturn(okJson(gyldigOppgaveResponse("hentOppgave.json"))))

        stubFor(
            patch(urlEqualTo("/api/v1/oppgaver/$OPPGAVE_ID"))
                .withRequestBody(
                    WireMock.equalToJson("""{"id":315488374,"tildeltEnhetsnr": "4833","versjon":1,"mappeId":1234}"""),
                )
                .willReturn(
                    aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(gyldigOppgaveResponse("ferdigstilt_oppgave.json")),
                ),
        )

        val oppgave = Oppgave(
            aktoerId = "1234567891011",
            journalpostId = "1",
            beskrivelse = EKSTRA_BESKRIVELSE,
            tema = null,
        )

        val response: ResponseEntity<Ressurs<OppgaveResponse>> =
            restTemplate.exchange(
                localhost("$OPPGAVE_URL/$OPPGAVE_ID/enhet/4833?fjernMappeFraOppgave=false"),
                HttpMethod.PATCH,
                HttpEntity(oppgave, headers),
            )
        assertThat(response.body?.data?.oppgaveId).isEqualTo(OPPGAVE_ID)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `Endre enhet på mappe skal feile når oppgave returnerer bad request`() {
        stubFor(get("/api/v1/oppgaver/$OPPGAVE_ID").willReturn(okJson(gyldigOppgaveResponse("hentOppgave.json"))))

        stubFor(
            patch(urlEqualTo("/api/v1/oppgaver/$OPPGAVE_ID"))
                .withRequestBody(
                    WireMock.equalToJson("""{"id":315488374,"tildeltEnhetsnr": "4833","versjon":1,"mappeId":1234}"""),
                )
                .willReturn(
                    aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(""""{uuid":"123","feilmelding":"Mappe finnes ikke for enhet"} """),
                ),
        )

        val oppgave = Oppgave(
            aktoerId = "1234567891011",
            journalpostId = "1",
            beskrivelse = EKSTRA_BESKRIVELSE,
            tema = null,
        )

        val response: ResponseEntity<Ressurs<OppgaveResponse>> =
            restTemplate.exchange(
                localhost("$OPPGAVE_URL/$OPPGAVE_ID/enhet/4833?fjernMappeFraOppgave=false"),
                HttpMethod.PATCH,
                HttpEntity(oppgave, headers),
            )
        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(response.body?.melding).contains("[Oppgave.byttEnhet][Feil ved bytte av enhet for oppgave for $OPPGAVE_ID")
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
    }

    @Test
    fun `Skal returnere 409 dersom man oppdaterer oppgave med feil versjon`() {
        stubFor(get("/api/v1/oppgaver/$OPPGAVE_ID").willReturn(okJson(gyldigOppgaveResponse("hentOppgave.json"))))
        stubFor(
            patch(urlEqualTo("/api/v1/oppgaver/$OPPGAVE_ID"))
                .willReturn(
                    aResponse()
                        .withStatus(409)
                        .withHeader("Content-Type", "application/json")
                        .withBody(""""{uuid":"123","feilmelding":"Versjonskonflikt ved forespørsel om endring av oppgave med id"} """),
                ),
        )

        val oppgave = Oppgave(
            aktoerId = "1234567891011",
            journalpostId = "1",
            beskrivelse = EKSTRA_BESKRIVELSE,
            tema = null,
        )

        val response: ResponseEntity<Ressurs<OppgaveResponse>> =
            restTemplate.exchange(
                localhost("$OPPGAVE_URL/$OPPGAVE_ID/fordel/?saksbehandler=test123&versjon=1"),
                HttpMethod.POST,
                HttpEntity(oppgave, headers),
            )
        assertThat(response.statusCode).isEqualTo(HttpStatus.CONFLICT)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
    }

    private fun gyldigOppgaveResponse(filnavn: String): String {
        return Files.readString(
            ClassPathResource("oppgave/$filnavn").file.toPath(),
            StandardCharsets.UTF_8,
        )
    }

    companion object {

        private const val OPPGAVE_URL = "/api/oppgave/"
        private const val OPPRETT_OPPGAVE_URL_V2 = "/api/oppgave/opprett"
        private const val OPPDATER_OPPGAVE_URL = "$OPPGAVE_URL/oppdater"
        private const val OPPGAVE_ID = 315488374L
        private const val GET_OPPGAVER_URL =
            "/api/v1/oppgaver?aktoerId=1234567891011&tema=KON&oppgavetype=BEH_SAK&journalpostId=1&statuskategori=AAPEN"
        private const val GET_MAPPER_URL =
            "/api/v1/mapper?enhetsnr=1234567891011&opprettetFom=dcssdf&limit=50"
        private const val GET_OPPGAVE_URL = "/api/v1/oppgaver/$OPPGAVE_ID"
        private const val EKSTRA_BESKRIVELSE = " Ekstra beskrivelse"
    }
}
