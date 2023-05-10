package no.nav.familie.integrasjoner.personopplysning

import ch.qos.logback.classic.Logger
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
import org.junit.jupiter.api.extension.ExtendWith
import org.mockserver.integration.ClientAndServer
import org.mockserver.junit.jupiter.MockServerExtension
import org.mockserver.junit.jupiter.MockServerSettings
import org.mockserver.model.Header
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.slf4j.LoggerFactory
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.util.UriComponentsBuilder

@ActiveProfiles("integrasjonstest", "mock-personopplysninger", "mock-oauth")
@ExtendWith(MockServerExtension::class)
@MockServerSettings(ports = [OppslagSpringRunnerTest.MOCK_SERVER_PORT])
class PersonopplysningerControllerTest(val client: ClientAndServer) : OppslagSpringRunnerTest() {

    private val testLogger = LoggerFactory.getLogger(PersonopplysningerControllerTest::class.java) as Logger

    private lateinit var uriHentIdenter: String
    private lateinit var uriHentAktørId: String
    private lateinit var uriHentStrengesteGradering: String

    @BeforeEach
    fun setUp() {
        client.reset()
        testLogger.addAppender(listAppender)
        headers.apply {
            add("Nav-Personident", "12345678901")
        }.setBearerAuth(lagToken("testbruker"))
        uriHentIdenter = UriComponentsBuilder.fromHttpUrl("${localhost(PDL_BASE_URL)}v1/identer/$TEMA").toUriString()
        uriHentAktørId = UriComponentsBuilder.fromHttpUrl("${localhost(PDL_BASE_URL)}aktorId/$TEMA").toUriString()
        uriHentStrengesteGradering =
            UriComponentsBuilder.fromHttpUrl("${localhost(PDL_BASE_URL)}strengeste-adressebeskyttelse-for-person-med-relasjoner")
                .toUriString()
    }

    @Test
    fun `hent identer til en person`() {
        val ident = "12345678901"
        client.`when`(
            HttpRequest.request()
                .withMethod("POST")
                .withPath("/rest/pdl/graphql")
                .withHeader("Tema", TEMA),
        )
            .respond(
                HttpResponse.response().withBody(readfile("pdlIdenterResponse.json"))
                    .withHeaders(Header("Content-Type", "application/json")),
            )
        val response = restTemplate.exchange<Ressurs<FinnPersonidenterResponse>>(
            uriHentIdenter,
            HttpMethod.POST,
            HttpEntity(Ident(ident), headers),
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(response.body?.data!!.identer).hasSize(1)
        assertThat(response.body?.data!!.identer.first().personIdent).isEqualTo(ident)
    }

    @Test
    fun `hent strengeste adressebeskyttelsegradering for person med relasjoner`() {
        val ident = "12345678901"
        val barnsIdent = "12345678910"
        mockPdlKall(ident, barnsIdent, "UGRADERT", "STRENGT_FORTROLIG")
        val response = restTemplate.exchange<Ressurs<ADRESSEBESKYTTELSEGRADERING>>(
            uriHentStrengesteGradering,
            HttpMethod.POST,
            HttpEntity(PersonIdent(ident), headers.apply { add("Nav-Tema", "ENF") }),
        )
        assertThat(response.body?.data).isEqualTo(ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG)
    }

    private fun mockPdlKall(ident: String, barnsIdent: String, persongradering: String, barngradering: String) {
        client.`when`(
            HttpRequest.request()
                .withMethod("POST")
                .withPath("/rest/pdl/graphql")
                .withBody(gyldigRequestIdentListe("hentpersoner-relasjoner-adressebeskyttelse.graphql"))
                .withHeader("Tema", "ENF"),
        )
            .respond(
                HttpResponse.response().withBody(
                    readfile("pdlPersonMedRelasjonerOkResponse.json").replace("IDENT-PLACEHOLDER", ident)
                        .replace("GRADERING-PLACEHOLDER", persongradering),
                )
                    .withHeaders(Header("Content-Type", "application/json")),
            )

        client.`when`(
            HttpRequest.request()
                .withMethod("POST")
                .withPath("/rest/pdl/graphql")
                .withBody(
                    gyldigRequestIdentListe(
                        "hentpersoner-relasjoner-adressebeskyttelse.graphql",
                        ident = barnsIdent,
                    ),
                )
                .withHeader("Tema", "ENF"),
        )
            .respond(
                HttpResponse.response()
                    .withBody(
                        readfile("pdlPersonMedRelasjonerOkResponse.json").replace("IDENT-PLACEHOLDER", barnsIdent)
                            .replace("GRADERING-PLACEHOLDER", barngradering),
                    )
                    .withHeaders(Header("Content-Type", "application/json")),
            )
    }

    private fun gyldigRequestIdentListe(filnavn: String, ident: String = "12345678901"): String {
        return readfile("pdlGyldigRequestIdentListe.json")
            .replace(
                "IDENT-PLACEHOLDER",
                ident,
            ).replace(
                "GRAPHQL-PLACEHOLDER",
                readfile(filnavn).graphqlCompatible(),
            )
    }

    private fun readfile(filnavn: String): String {
        return this::class.java.getResource("/pdl/$filnavn").readText()
    }

    companion object {

        const val PDL_BASE_URL = "/api/personopplysning/"
        const val TEMA = "BAR"
    }
}
