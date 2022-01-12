package no.nav.familie.integrasjoner.personopplysning

import ch.qos.logback.classic.Logger
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.integrasjoner.felles.graphqlCompatible
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING
import no.nav.familie.integrasjoner.personopplysning.internal.Person
import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.personopplysning.FinnPersonidenterResponse
import no.nav.familie.kontrakter.felles.personopplysning.Ident
import no.nav.familie.kontrakter.felles.personopplysning.SIVILSTAND
import no.nav.security.token.support.test.JwtTokenGenerator
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
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.util.UriComponentsBuilder
import kotlin.test.assertNull

@ActiveProfiles("integrasjonstest", "mock-personopplysninger", "mock-sts")
@ExtendWith(MockServerExtension::class)
@MockServerSettings(ports = [OppslagSpringRunnerTest.MOCK_SERVER_PORT])
class PersonopplysningerControllerTest(val client: ClientAndServer) : OppslagSpringRunnerTest() {

    private val testLogger = LoggerFactory.getLogger(PersonopplysningerControllerTest::class.java) as Logger

    private lateinit var uriHentPersoninfo: String
    private lateinit var uriHentPersoninfoEnkel: String
    private lateinit var uriHentIdenter: String
    private lateinit var uriHentAktørId: String
    private lateinit var uriHentStrengesteGradering: String

    @BeforeEach
    fun setUp() {
        client.reset()
        testLogger.addAppender(listAppender)
        headers.apply {
            add("Nav-Personident", "12345678901")
        }.setBearerAuth(JwtTokenGenerator.signedJWTAsString("testbruker"))
        uriHentPersoninfo = UriComponentsBuilder.fromHttpUrl("${localhost(PDL_BASE_URL)}v1/info/$TEMA").toUriString()
        uriHentPersoninfoEnkel = UriComponentsBuilder.fromHttpUrl("${localhost(PDL_BASE_URL)}v1/infoEnkel/$TEMA").toUriString()
        uriHentIdenter = UriComponentsBuilder.fromHttpUrl("${localhost(PDL_BASE_URL)}v1/identer/$TEMA").toUriString()
        uriHentAktørId = UriComponentsBuilder.fromHttpUrl("${localhost(PDL_BASE_URL)}aktorId/$TEMA").toUriString()
        uriHentStrengesteGradering =
            UriComponentsBuilder.fromHttpUrl("${localhost(PDL_BASE_URL)}strengeste-adressebeskyttelse-for-person-med-relsjoner")
                .toUriString()
    }

    @Test
    fun `hent personinfo skal returnere persondata og status ok`() {
        val response: ResponseEntity<Ressurs<Person>> = hentPersonInfoFraMockedPdlResponse("pdlOkResponse.json")

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(response.body?.data?.fødselsdato).isEqualTo(FØDSELSDATO)
        assertThat(response.body?.data?.navn).isEqualTo(NAVN)
        assertThat(response.body?.data?.kjønn).isEqualTo(KJØNN)
        assertThat(response.body?.data?.familierelasjoner?.stream()?.findFirst()?.get()?.personIdent?.id).isEqualTo(
            FAMILIERELASJON_PERSONIDENT
        )
        assertThat(response.body?.data?.familierelasjoner?.stream()?.findFirst()?.get()?.relasjonsrolle).isEqualTo(
            FAMILIERELASJON_RELASJONSROLLE
        )
    }

    @Test
    fun `hent personinfoEnkel skal returnere persondata og status ok`() {
        val response: ResponseEntity<Ressurs<Person>> = hentPersonInfoFraMockedPdlResponse("pdlOkResponseEnkel.json")

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(response.body?.data?.fødselsdato).isEqualTo(FØDSELSDATO)
        assertThat(response.body?.data?.navn).isEqualTo(NAVN)
        assertThat(response.body?.data?.kjønn).isEqualTo(KJØNN)
        assertThat(response.body?.data?.familierelasjoner).isEmpty()
        assertThat(response.body?.data?.sivilstand).isEqualTo(SIVILSTAND.UGIFT)
        assertThat(response.body?.data?.bostedsadresse?.vegadresse?.husnummer).isEqualTo("3")
        assertThat(response.body?.data?.bostedsadresse?.vegadresse?.matrikkelId).isEqualTo(1234)
        assertNull(response.body?.data?.bostedsadresse?.matrikkeladresse)
        assertNull(response.body?.data?.bostedsadresse?.ukjentBosted)
    }

    @Test
    fun `hent personinfo skal returnere persondata med tom adresse og status ok`() {
        val response: ResponseEntity<Ressurs<Person>> = hentPersonInfoFraMockedPdlResponse("pdlTomAdresseOkResponse.json")
        assertNull(response.body?.data?.bostedsadresse)
    }

    @Test
    fun `hent personinfo skal returnere persondata med matrikkel adresse og status ok`() {
        val response: ResponseEntity<Ressurs<Person>> = hentPersonInfoFraMockedPdlResponse("pdlMatrikkelAdresseOkResponse.json")
        assertThat(response.body?.data?.bostedsadresse?.matrikkeladresse?.postnummer).isEqualTo("0274")
        assertThat(response.body?.data?.bostedsadresse?.matrikkeladresse?.matrikkelId).isEqualTo(2147483649)
    }

    @Test
    fun `hent personinfo skal returnere persondata med ukjent bostedadresse og manglende sivilstand`() {
        val response: ResponseEntity<Ressurs<Person>> =
            hentPersonInfoFraMockedPdlResponse("pdlUkjentBostedAdresseOkResponse.json")
        assertThat(response.body?.data?.bostedsadresse?.ukjentBosted?.bostedskommune).isEqualTo("Oslo")
        assertNull(response.body?.data?.sivilstand)
    }

    @Test
    fun `hent personinfo returnerer med feil hvis forventede persondata ikke oppgis`() {
        val response: ResponseEntity<Ressurs<Person>> = hentPersonInfoFraMockedPdlResponse("pdlManglerFoedselResponse.json")

        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
    }

    @Test
    fun `hent personinfo returnerer med feil hvis person ikke finnes`() {
        val response: ResponseEntity<Ressurs<Person>> = hentPersonInfoFraMockedPdlResponse("pdlPersonIkkeFunnetResponse.json")

        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        assertThat(response.body?.melding).contains("Feil ved oppslag på person: Fant ikke person, Ikke tilgang")
    }

    @Test
    fun `hent personinfo returnerer med feil hvis ikke pdl responderer med 200`() {
        client.`when`(
            HttpRequest.request()
                .withMethod("POST")
                .withPath("/rest/pdl/graphql")
                .withBody(gyldigRequest("hentperson-med-relasjoner.graphql"))
        )
            .respond(HttpResponse.response().withStatusCode(500))

        val response: ResponseEntity<Ressurs<Map<String, String>>> = restTemplate.exchange(
            uriHentPersoninfo,
            HttpMethod.GET,
            HttpEntity<String>(headers)
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
    }

    @Test
    fun `hent identer til en person`() {
        val ident = "12345678901"
        client.`when`(
            HttpRequest.request()
                .withMethod("POST")
                .withPath("/rest/pdl/graphql")
                .withHeader("Tema", TEMA)
        )
            .respond(
                HttpResponse.response().withBody(readfile("pdlIdenterResponse.json"))
                    .withHeaders(Header("Content-Type", "application/json"))
            )
        val response = restTemplate.exchange<Ressurs<FinnPersonidenterResponse>>(
            uriHentIdenter,
            HttpMethod.POST,
            HttpEntity(Ident(ident), headers)
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
        client.`when`(
            HttpRequest.request()
                .withMethod("POST")
                .withPath("/rest/pdl/graphql")
                .withBody(gyldigRequestIdentListe("hentpersoner-relasjoner-adressebeskyttelse.graphql"))
                .withHeader("Tema", "ENF")
        )
            .respond(
                HttpResponse.response().withBody(
                    readfile("pdlPersonMedRelasjonerOkResponse.json").replace("IDENT-PLACEHOLDER", ident)
                        .replace("GRADERING-PLACEHOLDER", "UGRADERT")
                )
                    .withHeaders(Header("Content-Type", "application/json"))
            )

        client.`when`(
            HttpRequest.request()
                .withMethod("POST")
                .withPath("/rest/pdl/graphql")
                .withBody(gyldigRequestIdentListe("hentpersoner-relasjoner-adressebeskyttelse.graphql", ident = barnsIdent))
                .withHeader("Tema", "ENF")
        )
            .respond(
                HttpResponse.response()
                    .withBody(
                        readfile("pdlPersonMedRelasjonerOkResponse.json").replace("IDENT-PLACEHOLDER", barnsIdent)
                            .replace("GRADERING-PLACEHOLDER", "STRENGT_FORTROLIG")
                    )
                    .withHeaders(Header("Content-Type", "application/json"))
            )

        val response = restTemplate.exchange<Ressurs<ADRESSEBESKYTTELSEGRADERING>>(
            uriHentStrengesteGradering,
            HttpMethod.POST,
            HttpEntity(PersonIdent(ident), headers.apply { add("Nav-Tema", "ENF") })
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(response.body?.data).isEqualTo(ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG)
    }

    private fun hentPersonInfoFraMockedPdlResponse(responseFile: String): ResponseEntity<Ressurs<Person>> {
        client.`when`(
            HttpRequest.request()
                .withMethod("POST")
                .withPath("/rest/pdl/graphql")
                .withHeader("Tema", TEMA)
                .withBody(gyldigRequest("hentperson-med-relasjoner.graphql"))
        )
            .respond(
                HttpResponse.response().withBody(readfile(responseFile))
                    .withHeaders(Header("Content-Type", "application/json"))
            )

        return restTemplate.exchange(uriHentPersoninfo, HttpMethod.GET, HttpEntity<String>(headers))
    }

    private fun gyldigRequest(filnavn: String): String {
        return readfile("pdlGyldigRequest.json")
            .replace(
                "GRAPHQL-PLACEHOLDER",
                readfile(filnavn).graphqlCompatible()
            )
    }

    private fun gyldigRequestIdentListe(filnavn: String, ident: String = "12345678901"): String {
        return readfile("pdlGyldigRequestIdentListe.json")
            .replace(
                "IDENT-PLACEHOLDER",
                ident
            ).replace(
                "GRAPHQL-PLACEHOLDER",
                readfile(filnavn).graphqlCompatible()
            )
    }

    private fun readfile(filnavn: String): String {
        return this::class.java.getResource("/pdl/$filnavn").readText()
    }

    companion object {

        const val FØDSELSDATO = "1955-09-13"
        const val NAVN = "ENGASJERT FYR"
        const val KJØNN = "MANN"
        const val FAMILIERELASJON_PERSONIDENT = "12345678910"
        const val FAMILIERELASJON_RELASJONSROLLE = "BARN"
        const val PDL_BASE_URL = "/api/personopplysning/"
        const val TEMA = "BAR"
    }
}
