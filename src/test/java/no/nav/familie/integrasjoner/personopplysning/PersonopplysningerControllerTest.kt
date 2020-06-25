package no.nav.familie.integrasjoner.personopplysning

import ch.qos.logback.classic.Logger
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.integrasjoner.felles.graphqlCompatible
import no.nav.familie.integrasjoner.personopplysning.domene.Ident
import no.nav.familie.integrasjoner.personopplysning.internal.IdentInformasjon
import no.nav.familie.integrasjoner.personopplysning.internal.Person
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.personinfo.SIVILSTAND
import no.nav.security.token.support.test.JwtTokenGenerator
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
import org.springframework.http.*
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.util.UriComponentsBuilder
import kotlin.test.assertNull

@ActiveProfiles("integrasjonstest", "mock-personopplysninger", "mock-sts")
class PersonopplysningerControllerTest : OppslagSpringRunnerTest() {

    private val testLogger = LoggerFactory.getLogger(PersonopplysningerControllerTest::class.java) as Logger
    @get:Rule
    val mockServerRule = MockServerRule(this, MOCK_SERVER_PORT)
    private lateinit var uriHentPersoninfo: String
    private lateinit var uriHentPersoninfoEnkel: String
    private lateinit var uriHentIdenter: String
    private lateinit var uriHentHistoriskeIdenter: String
    private lateinit var uriHentAktørId: String

    @Before
    fun setUp() {
        testLogger.addAppender(listAppender)
        headers.apply {
            add("Nav-Personident", "12345678901")
        }.setBearerAuth(JwtTokenGenerator.signedJWTAsString("testbruker"))
        uriHentPersoninfo = UriComponentsBuilder.fromHttpUrl("${localhost(PDL_BASE_URL)}v1/info/$TEMA").toUriString()
        uriHentPersoninfoEnkel = UriComponentsBuilder.fromHttpUrl("${localhost(PDL_BASE_URL)}v1/infoEnkel/$TEMA").toUriString()
        uriHentIdenter = UriComponentsBuilder.fromHttpUrl("${localhost(PDL_BASE_URL)}identer/$TEMA").toUriString()
        uriHentHistoriskeIdenter = UriComponentsBuilder.fromHttpUrl("${localhost(PDL_BASE_URL)}identer/$TEMA/historikk")
                .toUriString()
        uriHentAktørId = UriComponentsBuilder.fromHttpUrl("${localhost(PDL_BASE_URL)}aktorId/$TEMA").toUriString()
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
                FAMILIERELASJON_PERSONIDENT)
        assertThat(response.body?.data?.familierelasjoner?.stream()?.findFirst()?.get()?.relasjonsrolle).isEqualTo(
                FAMILIERELASJON_RELASJONSROLLE)
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
        val response: ResponseEntity<Ressurs<Person>> = hentPersonInfoFraMockedPdlResponse("pdlUkjentBostedAdresseOkResponse.json")
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
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withPath("/rest/pdl/graphql")
                                .withBody(gyldigRequest("hentperson-med-relasjoner.graphql"))
                )
                .respond(HttpResponse.response().withStatusCode(500))

        val response: ResponseEntity<Ressurs<Map<String, String>>> = restTemplate.exchange(uriHentPersoninfo,
                                                                                           HttpMethod.GET,
                                                                                           HttpEntity<String>(headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
    }

    @Test
    fun `hentIdenter returnerer identer fra pdl`() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withPath("/rest/pdl/graphql")
                                .withBody(gyldigRequest("hentIdenter.graphql"))
                )
                .respond(HttpResponse.response()
                                 .withBody(readfile("pdlAktorIdResponse.json"))
                                 .withHeaders(Header("Content-Type", "application/json"))
                                 .withStatusCode(200))
        val response: ResponseEntity<Ressurs<List<IdentInformasjon>>> = restTemplate.exchange(uriHentIdenter,
                                                                                              HttpMethod.POST,
                                                                                              HttpEntity(Ident("12345678901"), headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(response.body?.data)
                .hasSize(2)
                .extracting<String> {it.ident}
                .contains(AKTIV_FNR_IDENT, AKTIV_AKTØR_IDENT)
    }

    @Test
    fun `hentIdenter kaster bad request hvis input mangler`() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withPath("/rest/pdl/graphql")
                                .withBody(gyldigRequest("hentIdenter.graphql"))
                )
                .respond(HttpResponse.response()
                                 .withBody(readfile("pdlAktorIdResponse.json"))
                                 .withHeaders(Header("Content-Type", "application/json"))
                                 .withStatusCode(200))
        val response: ResponseEntity<Ressurs<List<IdentInformasjon>>> = restTemplate.exchange(uriHentIdenter,
                                                                                              HttpMethod.POST,
                                                                                              HttpEntity(null, headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
    }

    @Test
    fun `hentIdenter finner ingen response fra graphql og returnerer errorMelding istedet`() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withPath("/rest/pdl/graphql")
                                .withBody(gyldigRequest("hentIdenter.graphql"))
                )
                .respond(HttpResponse.response().withBody(readfile("pdlPersonIkkeFunnetResponse.json"))
                                 .withHeaders(Header("Content-Type", "application/json")))
        val response: ResponseEntity<Ressurs<List<IdentInformasjon>>> = restTemplate.exchange(uriHentIdenter,
                                                                                              HttpMethod.POST,
                                                                                              HttpEntity(Ident("12345678901"), headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        assertThat(response.body?.melding).contains("Fant ikke identer for person: Fant ikke person, Ikke tilgang")
    }

    @Test
    fun `hentIdenterMedHistorikk returnerer identer alle historiske identer fra pdl`() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withPath("/rest/pdl/graphql")
                                .withBody(gyldigRequest("hentIdenter.graphql"))
                )
                .respond(HttpResponse.response()
                                 .withBody(readfile("pdlAktorIdResponse.json"))
                                 .withHeaders(Header("Content-Type", "application/json"))
                                 .withStatusCode(200))

        val response: ResponseEntity<Ressurs<List<IdentInformasjon>>> = restTemplate.exchange(uriHentHistoriskeIdenter,
                                                                                              HttpMethod.POST,
                                                                                              HttpEntity(Ident("12345678901"), headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(response.body?.data)
                .hasSize(3)
                .extracting<String> {it.ident}
                .contains(AKTIV_FNR_IDENT, AKTIV_AKTØR_IDENT, HISTORISK_AKTØR_IDENT)
    }

    @Test
    fun `hentAktørId returnerer aktørId fra pdl`() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withPath("/rest/pdl/graphql")
                                .withBody(gyldigRequest("hentIdenter.graphql"))
                )
                .respond(HttpResponse.response()
                                 .withBody(readfile("pdlAktorIdResponse.json"))
                                 .withHeaders(Header("Content-Type", "application/json"))
                                 .withStatusCode(200))

        val response: ResponseEntity<Ressurs<List<String>>> = restTemplate.exchange(uriHentAktørId,
                                                                                    HttpMethod.POST,
                                                                                    HttpEntity(Ident("12345678901"), headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(response.body?.data).containsExactly(AKTIV_AKTØR_IDENT)
    }

    @Test
    fun `hentDødsfall returnerer et dødsfall med dødsdato`() {
        val uri = UriComponentsBuilder.fromHttpUrl("${localhost(PDL_BASE_URL)}doedsfall/$TEMA").toUriString()
        lagMockForPdl("doedsfall.graphql", "pdlDoedsfallResponse.json")

        val response: ResponseEntity<Ressurs<DødsfallResponse>> = restTemplate.exchange(uri,
                HttpMethod.POST,
                HttpEntity(Ident("12345678901"), headers))

        assertThat(response.body?.data!!.dødsdato).isEqualTo("2019-07-02")
        assertThat(response.body?.data!!.erDød).isTrue()
    }

    @Test
    fun `hentDødsfall skal returnere et dødsfall uten dødsdato`() {
        val uri = UriComponentsBuilder.fromHttpUrl("${localhost(PDL_BASE_URL)}doedsfall/$TEMA").toUriString()
        lagMockForPdl("doedsfall.graphql", "pdlDoedsfallUtenDatoResponse.json")

        val response: ResponseEntity<Ressurs<DødsfallResponse>> = restTemplate.exchange(uri,
                                                                                        HttpMethod.POST,
                                                                                        HttpEntity(Ident("12345678901"), headers))

        assertThat(response.body?.data!!.dødsdato).isEqualTo(null)
        assertThat(response.body?.data!!.erDød).isTrue()
    }

    @Test
    fun `hentDødsfall returnerer ikke et dødsfall`() {
        val uri = UriComponentsBuilder.fromHttpUrl("${localhost(PDL_BASE_URL)}doedsfall/$TEMA").toUriString()
        lagMockForPdl("doedsfall.graphql", "pdlDoedsfallIkkeDoedResponse.json")

        val response: ResponseEntity<Ressurs<DødsfallResponse>> = restTemplate.exchange(uri,
                                                                                        HttpMethod.POST,
                                                                                        HttpEntity(Ident("12345678901"), headers))

        assertThat(response.body?.data!!.dødsdato).isEqualTo(null)
        assertThat(response.body?.data!!.erDød).isFalse()
    }

    @Test
    fun `harVergeEllerFullmektig returnerer true`() {
        val uri = UriComponentsBuilder.fromHttpUrl("${localhost(PDL_BASE_URL)}harVerge/$TEMA").toUriString()
        lagMockForPdl("verge.graphql", "pdlVergeFinnesResponse.json")

        val response: ResponseEntity<Ressurs<VergeResponse>> = restTemplate.exchange(uri,
                                                                                        HttpMethod.POST,
                                                                                        HttpEntity(Ident("12345678901"), headers))

        assertThat(response.body?.data!!.harVerge).isTrue()
    }

    @Test
    fun `harVergeEllerFullmektig returnerer false`() {
        val uri = UriComponentsBuilder.fromHttpUrl("${localhost(PDL_BASE_URL)}harVerge/$TEMA").toUriString()
        lagMockForPdl("verge.graphql", "pdlVergeFinnesIkkeResponse.json")

        val response: ResponseEntity<Ressurs<VergeResponse>> = restTemplate.exchange(uri,
                                                                                     HttpMethod.POST,
                                                                                     HttpEntity(Ident("12345678901"), headers))

        assertThat(response.body?.data!!.harVerge).isFalse()
    }

    private fun lagMockForPdl(graphqlQueryFilnavn: String, jsonResponseFilnavn: String) {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withPath("/rest/pdl/graphql")
                                .withBody(gyldigRequest(graphqlQueryFilnavn))
                )
                .respond(HttpResponse.response().withBody(readfile(jsonResponseFilnavn))
                                 .withHeaders(Header("Content-Type", "application/json")))
    }


    private fun hentPersonInfoFraMockedPdlResponse(responseFile: String): ResponseEntity<Ressurs<Person>> {
        mockServerRule.client
                .`when`(HttpRequest.request()
                        .withMethod("POST")
                        .withPath("/rest/pdl/graphql")
                        .withHeader("Tema", TEMA)
                        .withBody(gyldigRequest("hentperson-med-relasjoner.graphql"))
                )
                .respond(HttpResponse.response().withBody(readfile(responseFile))
                        .withHeaders(Header("Content-Type", "application/json")))

        return restTemplate.exchange(uriHentPersoninfo, HttpMethod.GET, HttpEntity<String>(headers))
    }

    private fun gyldigRequest(filnavn: String): String {
        return readfile("pdlGyldigRequest.json")
                .replace(
                        "GRAPHQL-PLACEHOLDER",
                        readfile(filnavn).graphqlCompatible()
                )
    }

    private fun readfile(filnavn: String): String {
        return this::class.java.getResource("/pdl/$filnavn").readText()
    }

    companion object {
        const val MOCK_SERVER_PORT = 18321
        const val FØDSELSDATO = "1955-09-13"
        const val NAVN = "ENGASJERT FYR"
        const val KJØNN = "MANN"
        const val FAMILIERELASJON_PERSONIDENT = "12345678910"
        const val FAMILIERELASJON_RELASJONSROLLE = "BARN"
        const val PDL_BASE_URL = "/api/personopplysning/"
        const val TEMA = "BAR"
        const val AKTIV_AKTØR_IDENT = "2872543507203"
        const val AKTIV_FNR_IDENT = "21127725540"
        const val HISTORISK_AKTØR_IDENT = "2872543000000"
    }
}
