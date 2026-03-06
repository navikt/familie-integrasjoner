package no.nav.familie.integrasjoner.journalpost

import ch.qos.logback.classic.Logger
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.status
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.integrasjoner.baks.søknad.lagBarnetrygdSøknad
import no.nav.familie.integrasjoner.felles.graphqlCompatible
import no.nav.familie.integrasjoner.felles.graphqlQuery
import no.nav.familie.integrasjoner.journalpost.internal.SafJournalpostRequest
import no.nav.familie.integrasjoner.journalpost.internal.SafRequestForBruker
import no.nav.familie.integrasjoner.personopplysning.internal.PdlPersonRequest
import no.nav.familie.integrasjoner.personopplysning.internal.PdlPersonRequestVariables
import no.nav.familie.kontrakter.felles.BrukerIdType
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.journalpost.Bruker
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.JournalposterForBrukerRequest
import no.nav.familie.kontrakter.felles.journalpost.Journalposttype
import no.nav.familie.kontrakter.felles.journalpost.Journalstatus
import no.nav.familie.kontrakter.felles.journalpost.TilgangsstyrtJournalpost
import no.nav.familie.kontrakter.felles.journalpost.Utsendingsmåte
import no.nav.familie.kontrakter.felles.journalpost.VarselType
import no.nav.familie.kontrakter.felles.jsonMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.resttestclient.exchange
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.web.util.UriComponentsBuilder
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock
import java.time.LocalDateTime

@EnableWireMock(
    ConfigureWireMock(name = "HentJournalpostControllerTest", port = 28085),
)
@TestPropertySource(properties = ["SAF_URL=http://localhost:28085"])
@ActiveProfiles("integrasjonstest", "mock-sts", "mock-oauth", "mock-egenansatt-false")
class HentJournalpostControllerTest : OppslagSpringRunnerTest() {
    private val testLogger = LoggerFactory.getLogger(HentJournalpostController::class.java) as Logger

    private lateinit var uriHentSaksnummer: String
    private lateinit var uriHentJournalpost: String
    private lateinit var uriHentTilgangsstyrtJournalpost: String
    private lateinit var uriHentDokument: String

    @BeforeEach
    fun setUp() {
        // Wiremock reset is automatic per test
        testLogger.addAppender(listAppender)
        headers.setBearerAuth(lagToken("testbruker"))
        uriHentSaksnummer =
            UriComponentsBuilder
                .fromUriString(localhost(JOURNALPOST_BASE_URL) + "/sak")
                .queryParam("journalpostId", JOURNALPOST_ID)
                .toUriString()
        uriHentJournalpost =
            UriComponentsBuilder
                .fromUriString(localhost(JOURNALPOST_BASE_URL))
                .queryParam("journalpostId", JOURNALPOST_ID)
                .toUriString()
        uriHentTilgangsstyrtJournalpost =
            UriComponentsBuilder
                .fromUriString(localhost(JOURNALPOST_BASE_URL) + "/tilgangsstyrt/baks")
                .queryParam("journalpostId", JOURNALPOST_ID)
                .toUriString()
        uriHentDokument = localhost(JOURNALPOST_BASE_URL) + "/hentdokument/$JOURNALPOST_ID/$DOKUMENTINFO_ID"
    }

    @Test
    fun `hent journalpost skal returnere journalpost og status ok`() {
        stubFor(
            post(urlPathEqualTo("/graphql"))
                .withRequestBody(equalTo(gyldigJournalPostIdRequest()))
                .willReturn(okJson(lesFil("saf/gyldigjournalpostresponse.json")).withHeader("Content-Type", "application/json")),
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
        assertThat(response.body?.data?.datoMottatt).isEqualTo(LocalDateTime.of(2024, 3, 26, 1, 0))
    }

    @Test
    fun `hent tilgangsstyrt baks journalpost skal returnere journalpost og status ok`() {
        val uriHentTilgangsstyrtBaksJournalpost =
            UriComponentsBuilder
                .fromUriString(localhost("$JOURNALPOST_BASE_URL/tilgangsstyrt/baks"))
                .queryParam("journalpostId", JOURNALPOST_ID)
                .toUriString()
        stubFor(
            post(urlPathEqualTo("/graphql"))
                .withRequestBody(equalTo(gyldigJournalPostIdRequest()))
                .willReturn(okJson(lesFil("saf/gyldigjournalpostresponse.json")).withHeader("Content-Type", "application/json")),
        )

        val response: ResponseEntity<Ressurs<Journalpost>> =
            restTemplate.exchange(
                uriHentTilgangsstyrtBaksJournalpost,
                HttpMethod.GET,
                HttpEntity<String>(headers),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(response.body?.data?.journalposttype).isEqualTo(Journalposttype.I)
        assertThat(response.body?.data?.journalstatus).isEqualTo(Journalstatus.JOURNALFOERT)
        assertThat(response.body?.data?.datoMottatt).isEqualTo(LocalDateTime.of(2024, 3, 26, 1, 0))
    }

    @Test
    fun `hent journalpostForBruker skal returnere journalposter og status ok`() {
        stubFor(
            post(urlPathEqualTo("/graphql"))
                .withRequestBody(equalTo(jsonMapper.writeValueAsString(gyldigBrukerRequest())))
                .willReturn(okJson(lesFil("saf/gyldigJournalposterResponse.json"))),
        )

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
        assertThat(
            response.body
                ?.data
                ?.first()
                ?.journalposttype,
        ).isEqualTo(Journalposttype.I)
        assertThat(
            response.body
                ?.data
                ?.first()
                ?.journalstatus,
        ).isEqualTo(Journalstatus.JOURNALFOERT)
        assertThat(
            response.body
                ?.data
                ?.first()
                ?.datoMottatt,
        ).isEqualTo(LocalDateTime.parse("2020-01-31T08:00:17"))
        val utsendingsinfo =
            response.body
                ?.data
                ?.find { it.utsendingsinfo != null }
                ?.utsendingsinfo
                ?: error("Finner ikke utsendingsinfo på noen journalposter")
        assertThat(utsendingsinfo.utsendingsmåter).hasSize(1)
        assertThat(utsendingsinfo.utsendingsmåter).contains(Utsendingsmåte.DIGITAL_POST)
        assertThat(utsendingsinfo.digitalpostSendt?.adresse).isEqualTo("0000487236")
        assertThat(utsendingsinfo.fysiskpostSendt).isNull()
        assertThat(utsendingsinfo.varselSendt).hasSize(1)
        assertThat(utsendingsinfo.varselSendt.first().type).isEqualTo(VarselType.SMS)
    }

    @Test
    fun `hentTilgangsstyrteJournalposterForBruker skal returnere tilgangsstyrte journalposter og status ok`() {
        val barnetrygdSøknad = lagBarnetrygdSøknad("12345678910", "12345678911")
        stubFor(
            post(urlPathEqualTo("/graphql"))
                .withRequestBody(equalTo(jsonMapper.writeValueAsString(gyldigBrukerRequest())))
                .willReturn(okJson(lesFil("saf/gyldigJournalposterResponseBarnetrygd.json"))),
        )
        stubFor(
            get(urlPathEqualTo("/soknad/hent-personer-i-digital-soknad/BAR/453492634"))
                .willReturn(okJson(lesFil("mottak/gyldigPersonerIDigitalSøknadResponse.json"))),
        )
        stubFor(
            post(urlPathEqualTo("/rest/pdl/graphql"))
                .withRequestBody(equalTo(jsonMapper.writeValueAsString(gyldigPdlPersonRequest("12345678910"))))
                .willReturn(okJson(lesFil("pdl/pdlAdressebeskyttelseResponse.json"))),
        )
        stubFor(
            post(urlPathEqualTo("/rest/pdl/graphql"))
                .withRequestBody(equalTo(jsonMapper.writeValueAsString(gyldigPdlPersonRequest("12345678911"))))
                .willReturn(okJson(lesFil("pdl/pdlAdressebeskyttelseResponse.json"))),
        )
        stubFor(
            get(urlPathEqualTo("/rest/saf/rest/hentdokument/453492634/453871494/ORIGINAL"))
                .willReturn(okJson(jsonMapper.writeValueAsString(barnetrygdSøknad)).withHeader("Content-Type", "application/json")),
        )

        val response: ResponseEntity<Ressurs<List<TilgangsstyrtJournalpost>>> =
            restTemplate.exchange(
                uriHentTilgangsstyrtJournalpost,
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
        assertThat(
            response.body
                ?.data
                ?.first()
                ?.journalpost
                ?.journalpostId,
        ).isEqualTo("453492634")
        assertThat(
            response.body
                ?.data
                ?.first()
                ?.journalpostTilgang
                ?.harTilgang,
        ).isEqualTo(true)
    }

    @Test
    fun `hent dokument skal returnere dokument og status ok`() {
        stubFor(
            get(urlPathEqualTo("/rest/hentdokument/$JOURNALPOST_ID/$DOKUMENTINFO_ID/ARKIV"))
                .willReturn(status(200).withBody("pdf".toByteArray()).withHeader("Content-Type", "application/pdf")),
        )

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
    fun `hent tilgangsstyrt baks dokument skal returnere dokument og status ok`() {
        val uriHentTilgangsstyrtBaksDokument = localhost(JOURNALPOST_BASE_URL) + "/hentdokument/tilgangsstyrt/baks/$JOURNALPOST_ID/$DOKUMENTINFO_ID"
        stubFor(
            post(urlPathEqualTo("/graphql"))
                .withRequestBody(equalTo(gyldigJournalPostIdRequest()))
                .willReturn(okJson(lesFil("saf/gyldigjournalpostresponse.json")).withHeader("Content-Type", "application/json")),
        )
        stubFor(
            get(urlPathEqualTo("/rest/hentdokument/$JOURNALPOST_ID/$DOKUMENTINFO_ID/ARKIV"))
                .willReturn(status(200).withBody("pdf".toByteArray()).withHeader("Content-Type", "application/pdf")),
        )

        val response: ResponseEntity<Ressurs<ByteArray>> =
            restTemplate.exchange(
                uriHentTilgangsstyrtBaksDokument,
                HttpMethod.GET,
                HttpEntity<String>(headers),
            )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
    }

    @Test
    fun `skal returnere 403 FORBIDDEN med status IKKE_TILGANG hvis man ikke har tilgang til SAF ressurs`() {
        stubFor(
            post(urlPathEqualTo("/graphql"))
                .withRequestBody(equalTo(gyldigJournalPostIdRequest()))
                .willReturn(okJson(lesFil("saf/forbidden.json")).withHeader("Content-Type", "application/json")),
        )

        val response: ResponseEntity<Ressurs<Journalpost>> =
            restTemplate.exchange(
                uriHentJournalpost,
                HttpMethod.GET,
                HttpEntity<String>(headers),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.IKKE_TILGANG)
    }

    @Test
    fun `skal returnere 404 NOT FOUND med status FEILET hvis man ikke finner SAF ressurs`() {
        stubFor(
            post(urlPathEqualTo("/graphql"))
                .withRequestBody(equalTo(gyldigJournalPostIdRequest()))
                .willReturn(okJson(lesFil("saf/not_found.json")).withHeader("Content-Type", "application/json")),
        )

        val response: ResponseEntity<Ressurs<Journalpost>> =
            restTemplate.exchange(
                uriHentJournalpost,
                HttpMethod.GET,
                HttpEntity<String>(headers),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
    }

    private fun gyldigJournalPostIdRequest(): String =
        lesFil("saf/gyldigJournalpostIdRequest.json")
            .replace(
                "GRAPHQL-PLACEHOLDER",
                lesFil("saf/journalpostForId.graphql").graphqlCompatible(),
            )

    private fun gyldigBrukerRequest(): SafJournalpostRequest =
        SafJournalpostRequest(
            SafRequestForBruker(
                brukerId = Bruker("12345678901", BrukerIdType.FNR),
                antall = 10,
                tema = listOf(Tema.BAR),
                journalposttype = listOf(Journalposttype.I),
                journalstatus = emptyList(),
            ),
            graphqlQuery("/saf/journalposterForBruker.graphql"),
        )

    private fun gyldigPdlPersonRequest(ident: String): PdlPersonRequest =
        PdlPersonRequest(
            variables = PdlPersonRequestVariables(ident),
            query = graphqlQuery("/pdl/adressebeskyttelse.graphql"),
        )

    private fun lesFil(path: String): String = ClassPathResource(path).url.readText()

    companion object {
        const val JOURNALPOST_ID = "12345678"
        const val DOKUMENTINFO_ID = "123456789"
        const val JOURNALPOST_BASE_URL = "/api/journalpost"
    }
}
