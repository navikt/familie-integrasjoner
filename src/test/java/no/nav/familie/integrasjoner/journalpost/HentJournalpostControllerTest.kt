package no.nav.familie.integrasjoner.journalpost

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.integrasjoner.felles.graphqlCompatible
import no.nav.familie.integrasjoner.felles.graphqlQuery
import no.nav.familie.integrasjoner.journalpost.internal.SafJournalpostRequest
import no.nav.familie.integrasjoner.journalpost.internal.SafRequestForBruker
import no.nav.familie.kontrakter.felles.BrukerIdType
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.journalpost.Bruker
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.JournalposterForBrukerRequest
import no.nav.familie.kontrakter.felles.journalpost.Journalposttype
import no.nav.familie.kontrakter.felles.journalpost.Journalstatus
import no.nav.familie.kontrakter.felles.journalpost.Utsendingsmåte
import no.nav.familie.kontrakter.felles.journalpost.VarselType
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockserver.integration.ClientAndServer
import org.mockserver.junit.jupiter.MockServerExtension
import org.mockserver.junit.jupiter.MockServerSettings
import org.mockserver.model.Header
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.JsonBody.json
import org.slf4j.LoggerFactory
import org.springframework.boot.test.web.client.exchange
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.util.UriComponentsBuilder
import java.time.LocalDateTime

@ActiveProfiles("integrasjonstest", "mock-sts", "mock-oauth")
@ExtendWith(MockServerExtension::class)
@MockServerSettings(ports = [OppslagSpringRunnerTest.MOCK_SERVER_PORT])
class HentJournalpostControllerTest(val client: ClientAndServer) : OppslagSpringRunnerTest() {
    private val testLogger = LoggerFactory.getLogger(HentJournalpostController::class.java) as Logger

    private lateinit var uriHentSaksnummer: String
    private lateinit var uriHentJournalpost: String
    private lateinit var uriHentDokument: String

    @BeforeEach
    fun setUp() {
        client.reset()
        testLogger.addAppender(listAppender)
        headers.setBearerAuth(lagToken("testbruker"))
        uriHentSaksnummer =
            UriComponentsBuilder.fromHttpUrl(localhost(JOURNALPOST_BASE_URL) + "/sak")
                .queryParam("journalpostId", JOURNALPOST_ID).toUriString()
        uriHentJournalpost =
            UriComponentsBuilder.fromHttpUrl(localhost(JOURNALPOST_BASE_URL))
                .queryParam("journalpostId", JOURNALPOST_ID).toUriString()
        uriHentDokument = localhost(JOURNALPOST_BASE_URL) + "/hentdokument/$JOURNALPOST_ID/$DOKUMENTINFO_ID"
    }

    @Test
    @Disabled("Skrur av siden det er en test på kode som kun KS bruker og den feilet. TODO fiks eller få vekk fra ks-mottak")
    fun `hent saksnummer skal returnere saksnummer og status ok`() {
        client.`when`(
            HttpRequest.request()
                .withMethod("POST")
                .withPath("/rest/saf/graphql")
                .withBody(gyldigJournalPostIdRequest()),
        )
            .respond(
                response().withBody(lesFil("gyldigsakresponse.json"))
                    .withHeaders(Header("Content-Type", "application/json")),
            )

        val response: ResponseEntity<Ressurs<Map<String, String>>> =
            restTemplate.exchange(
                uriHentSaksnummer,
                HttpMethod.GET,
                HttpEntity<String>(headers),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(response.body?.data?.get("saksnummer")).isEqualTo(SAKSNUMMER)
    }

    @Test
    fun `hent journalpost skal returnere journalpost og status ok`() {
        client.`when`(
            HttpRequest.request()
                .withMethod("POST")
                .withPath("/rest/saf/graphql")
                .withBody(gyldigJournalPostIdRequest()),
        )
            .respond(
                response().withBody(json(lesFil("gyldigjournalpostresponse.json")))
                    .withHeaders(Header("Content-Type", "application/json")),
            )

        val response: ResponseEntity<Ressurs<Journalpost>> =
            restTemplate.exchange(
                uriHentJournalpost,
                HttpMethod.GET,
                HttpEntity<String>(headers),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(response.body?.data?.journalposttype).isEqualTo(Journalposttype.I)
        assertThat(response.body?.data?.journalstatus).isEqualTo(Journalstatus.JOURNALFOERT)
        assertThat(response.body?.data?.datoMottatt).isEqualTo(LocalDateTime.of(2020, 3, 26, 1, 0))
    }

    @Test
    fun `hent journalpostForBruker skal returnere journalposter og status ok`() {
        client.`when`(
            HttpRequest.request()
                .withMethod("POST")
                .withPath("/rest/saf/graphql")
                .withBody(objectMapper.writeValueAsString(gyldigBrukerRequest())),
        )
            .respond(response().withBody(json(lesFil("gyldigJournalposterResponse.json"))))

        val response: ResponseEntity<Ressurs<List<Journalpost>>> =
            restTemplate.exchange(
                uriHentJournalpost,
                HttpMethod.POST,
                HttpEntity(
                    JournalposterForBrukerRequest(
                        Bruker("12345678901", BrukerIdType.FNR),
                        10,
                        listOf(Tema.BAR),
                        listOf(Journalposttype.I),
                    ),
                    headers,
                ),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(response.body?.data?.first()?.journalposttype).isEqualTo(Journalposttype.I)
        assertThat(response.body?.data?.first()?.journalstatus).isEqualTo(Journalstatus.JOURNALFOERT)
        assertThat(response.body?.data?.first()?.datoMottatt).isEqualTo(LocalDateTime.parse("2020-01-31T08:00:17"))
        val utsendingsinfo =
            response.body?.data?.find { it.utsendingsinfo != null }?.utsendingsinfo
                ?: error("Finner ikke utsendingsinfo på noen journalposter")
        assertThat(utsendingsinfo.utsendingsmåter).hasSize(1)
        assertThat(utsendingsinfo.utsendingsmåter).contains(Utsendingsmåte.DIGITAL_POST)
        assertThat(utsendingsinfo.digitalpostSendt?.adresse).isEqualTo("0000487236")
        assertThat(utsendingsinfo.fysiskpostSendt).isNull()
        assertThat(utsendingsinfo.varselSendt).hasSize(1)
        assertThat(utsendingsinfo.varselSendt.first().type).isEqualTo(VarselType.SMS)
    }

    @Test
    fun `hent dokument skal returnere dokument og status ok`() {
        client.`when`(
            HttpRequest.request()
                .withMethod("GET")
                .withPath("/rest/saf/rest/hentdokument/$JOURNALPOST_ID/$DOKUMENTINFO_ID/ARKIV"),
        )
            .respond(HttpResponse().withBody("pdf".toByteArray()).withHeaders(Header("Content-Type", "application/pdf")))

        val response: ResponseEntity<Ressurs<ByteArray>> =
            restTemplate.exchange(
                uriHentDokument,
                HttpMethod.GET,
                HttpEntity<String>(headers),
            )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
    }

    @Test
    fun `hent saksnummer skal returnere status 404 hvis sak mangler`() {
        client.`when`(
            HttpRequest.request()
                .withMethod("POST")
                .withHeader(Header("Content-Type", "application/json"))
                .withPath("/rest/saf/graphql"),
        )
            .respond(
                response().withBody(lesFil("mangler_sak.json"))
                    .withHeaders(Header("Content-Type", "application/json")),
            )

        val response: ResponseEntity<Ressurs<Map<String, String>>> =
            restTemplate.exchange(
                uriHentSaksnummer,
                HttpMethod.GET,
                HttpEntity<String>(headers),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        assertThat(response.body?.melding).isEqualTo("Sak mangler for journalpostId=$JOURNALPOST_ID")
    }

    @Test
    fun `hent saksnummer skal returnere status 404 hvis sak ikke er gsak`() {
        client.`when`(
            HttpRequest.request()
                .withMethod("POST")
                .withHeader(Header("Content-Type", "application/json"))
                .withPath("/rest/saf/graphql"),
        )
            .respond(
                response().withBody(lesFil("feil_arkivsaksystem.json"))
                    .withHeaders(Header("Content-Type", "application/json")),
            )

        val response: ResponseEntity<Ressurs<Map<String, String>>> =
            restTemplate.exchange(
                uriHentSaksnummer,
                HttpMethod.GET,
                HttpEntity<String>(headers),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        assertThat(response.body?.melding).isEqualTo("Sak mangler for journalpostId=$JOURNALPOST_ID")
    }

    @Test
    fun `hent saksnummer skal returnerer 500 hvis klient returnerer 200 med feilmeldinger`() {
        client.`when`(
            HttpRequest.request()
                .withMethod("POST")
                .withHeader(Header("Content-Type", "application/json"))
                .withPath("/rest/saf/graphql"),
        )
            .respond(
                response().withBody(lesFil("error_fra_saf.json"))
                    .withHeaders(Header("Content-Type", "application/json")),
            )

        val response: ResponseEntity<Ressurs<Map<String, String>>> =
            restTemplate.exchange(
                uriHentSaksnummer,
                HttpMethod.GET,
                HttpEntity<String>(headers),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        assertThat(response.body?.melding)
            .contains(
                "Feil ved henting av journalpost=12345678 klientfeilmelding=Kan ikke hente journalpost " +
                    "[SafError(message=Feilet ved henting av data (/journalpost) : null, " +
                    "extensions=SafExtension(code=server_error, classification=DataFetchingException))]",
            )
        assertThat(loggingEvents)
            .extracting<Level, RuntimeException> { obj: ILoggingEvent -> obj.level }
            .containsExactly(Level.WARN)
    }

    @Test
    fun `hent saksnummer skal returnere 500 ved ukjent feil`() {
        client.`when`(
            HttpRequest.request()
                .withMethod("POST")
                .withHeader(Header("Content-Type", "application/json"))
                .withPath("/rest/saf/graphql"),
        )
            .respond(response().withStatusCode(500).withBody("feilmelding"))

        val response: ResponseEntity<Ressurs<Map<String, String>>> =
            restTemplate.exchange(
                uriHentSaksnummer,
                HttpMethod.GET,
                HttpEntity<String>(headers),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        assertThat(response.body?.melding)
            .contains("Feil ved henting av journalpost")
        assertThat(loggingEvents)
            .extracting<Level, RuntimeException> { obj: ILoggingEvent -> obj.level }
            .containsExactly(Level.WARN)
    }

    private fun gyldigJournalPostIdRequest(): String {
        return lesFil("gyldigJournalpostIdRequest.json")
            .replace(
                "GRAPHQL-PLACEHOLDER",
                lesFil("journalpostForId.graphql").graphqlCompatible(),
            )
    }

    private fun gyldigBrukerRequest(): SafJournalpostRequest {
        return SafJournalpostRequest(
            SafRequestForBruker(
                brukerId = Bruker("12345678901", BrukerIdType.FNR),
                antall = 10,
                tema = listOf(Tema.BAR),
                journalposttype = listOf(Journalposttype.I),
                journalstatus = emptyList(),
            ),
            graphqlQuery("/saf/journalposterForBruker.graphql"),
        )
    }

    private fun lesFil(filnavn: String): String {
        return ClassPathResource("saf/$filnavn").url.readText()
    }

    companion object {
        const val JOURNALPOST_ID = "12345678"
        const val DOKUMENTINFO_ID = "123456789"
        const val SAKSNUMMER = "87654321"
        const val JOURNALPOST_BASE_URL = "/api/journalpost"
    }
}
