package no.nav.familie.integrasjoner.personopplysning

import ch.qos.logback.classic.Logger
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.integrasjoner.felles.graphqlCompatible
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING
import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.personopplysning.FinnPersonidenterResponse
import no.nav.familie.kontrakter.felles.personopplysning.Ident
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.resttestclient.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.web.util.UriComponentsBuilder
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock

@ActiveProfiles("integrasjonstest", "mock-personopplysninger", "mock-oauth")
@TestPropertySource(properties = ["PDL_URL=http://localhost:28085"])
@EnableWireMock(
    ConfigureWireMock(name = "personopplysning", port = 28085),
)
class PersonopplysningerControllerTest : OppslagSpringRunnerTest() {
    private val testLogger = LoggerFactory.getLogger(PersonopplysningerControllerTest::class.java) as Logger

    private lateinit var uriHentIdenter: String
    private lateinit var uriHentAktørId: String
    private lateinit var uriHentStrengesteGradering: String

    @BeforeEach
    fun setUp() {
        testLogger.addAppender(listAppender)
        headers
            .apply {
                add("Nav-Personident", "12345678901")
            }.setBearerAuth(lagToken("testbruker"))
        uriHentIdenter = UriComponentsBuilder.fromUriString("${localhost(PDL_BASE_URL)}v1/identer/$TEMA").toUriString()
        uriHentAktørId = UriComponentsBuilder.fromUriString("${localhost(PDL_BASE_URL)}aktorId/$TEMA").toUriString()
        uriHentStrengesteGradering =
            UriComponentsBuilder
                .fromUriString("${localhost(PDL_BASE_URL)}strengeste-adressebeskyttelse-for-person-med-relasjoner")
                .toUriString()
    }

    @Test
    fun `hent identer til en person`() {
        val ident = "12345678901"
        stubFor(
            post(urlEqualTo("/graphql"))
                .withHeader(
                    "Tema",
                    com.github.tomakehurst.wiremock.client.WireMock
                        .equalTo(TEMA),
                ).willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(readfile("pdlIdenterResponse.json")),
                ),
        )
        val response =
            restTemplate.exchange<Ressurs<FinnPersonidenterResponse>>(
                uriHentIdenter,
                HttpMethod.POST,
                HttpEntity(Ident(ident), headers),
            )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(response.body?.data!!.identer).hasSize(1)
        assertThat(
            response.body
                ?.data!!
                .identer
                .first()
                .personIdent,
        ).isEqualTo(ident)
    }

    @Test
    fun `hent strengeste adressebeskyttelsegradering for person med relasjoner`() {
        val ident = "12345678901"
        val barnsIdent = "12345678910"
        mockPdlKall(ident, barnsIdent, "UGRADERT", "STRENGT_FORTROLIG")
        val response =
            restTemplate.exchange<Ressurs<ADRESSEBESKYTTELSEGRADERING>>(
                uriHentStrengesteGradering,
                HttpMethod.POST,
                HttpEntity(PersonIdent(ident), headers.apply { add("Nav-Tema", "ENF") }),
            )
        assertThat(response.body?.data).isEqualTo(ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG)
    }

    private fun mockPdlKall(
        ident: String,
        barnsIdent: String,
        persongradering: String,
        barngradering: String,
    ) {
        stubFor(
            post(urlEqualTo("/graphql"))
                .withHeader(
                    "Tema",
                    equalTo("ENF"),
                ).withRequestBody(
                    matchingJsonPath(
                        "$.variables.identer[0]",
                        equalTo(ident),
                    ),
                ).willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            readfile("pdlPersonMedRelasjonerOkResponse.json")
                                .replace("IDENT-PLACEHOLDER", ident)
                                .replace("GRADERING-PLACEHOLDER", persongradering),
                        ),
                ),
        )

        println(gyldigRequestIdentListe("hentpersoner-relasjoner-adressebeskyttelse.graphql"))
        stubFor(
            post(urlEqualTo("/graphql"))
                .withHeader(
                    "Tema",
                    equalTo("ENF"),
                ).withRequestBody(
                    matchingJsonPath(
                        "$.variables.identer[0]",
                        equalTo(barnsIdent),
                    ),
                ).willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            readfile("pdlPersonMedRelasjonerOkResponse.json")
                                .replace("IDENT-PLACEHOLDER", barnsIdent)
                                .replace("GRADERING-PLACEHOLDER", barngradering),
                        ),
                ),
        )
    }

    private fun gyldigRequestIdentListe(
        filnavn: String,
        ident: String = "12345678901",
    ): String =
        readfile("pdlGyldigRequestIdentListe.json")
            .replace(
                "IDENT-PLACEHOLDER",
                ident,
            ).replace(
                "GRAPHQL-PLACEHOLDER",
                readfile(filnavn).graphqlCompatible(),
            )

    private fun readfile(filnavn: String): String = this::class.java.getResource("/pdl/$filnavn").readText()

    companion object {
        const val PDL_BASE_URL = "/api/personopplysning/"
        const val TEMA = "BAR"
    }
}
