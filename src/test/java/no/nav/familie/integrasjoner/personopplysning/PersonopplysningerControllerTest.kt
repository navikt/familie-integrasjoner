package no.nav.familie.integrasjoner.personopplysning

import ch.qos.logback.classic.Logger
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.integrasjoner.felles.graphqlCompatible
import no.nav.familie.integrasjoner.personopplysning.internal.IdentInformasjon
import no.nav.familie.integrasjoner.personopplysning.internal.Person
import no.nav.familie.kontrakter.felles.Ressurs
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
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.util.UriComponentsBuilder

@ActiveProfiles("integrasjonstest", "mock-personopplysninger", "mock-sts")
class PersonopplysningerControllerTest : OppslagSpringRunnerTest() {

    private val testLogger = LoggerFactory.getLogger(PersonopplysningerControllerTest::class.java) as Logger
    @get:Rule
    val mockServerRule = MockServerRule(this, MOCK_SERVER_PORT)
    private lateinit var uriHentPersoninfo: String
    private lateinit var uriHentPersoninfoEnkel: String
    private lateinit var uriHentIdenter: String

    @Before
    fun setUp() {
        testLogger.addAppender(listAppender)
        headers.apply {
            add("Nav-Personident", "12345678901")
        }.setBearerAuth(JwtTokenGenerator.signedJWTAsString("testbruker"))
        uriHentPersoninfo = UriComponentsBuilder.fromHttpUrl("${localhost(PDL_BASE_URL)}v1/info/$TEMA").toUriString()
        uriHentPersoninfoEnkel = UriComponentsBuilder.fromHttpUrl("${localhost(PDL_BASE_URL)}v1/infoEnkel/$TEMA").toUriString()
        uriHentIdenter = UriComponentsBuilder.fromHttpUrl("${localhost(PDL_BASE_URL)}identer/$TEMA").toUriString()
    }

    @Test
    fun `hent personinfo skal returnere persondata og status ok`() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withPath("/rest/pdl/graphql")
                                .withHeader("Tema", TEMA)
                                .withBody(gyldigRequest())
                )
                .respond(HttpResponse.response().withBody(readfile("pdlOkResponse.json"))
                                 .withHeaders(Header("Content-Type", "application/json")))


        val response: ResponseEntity<Ressurs<Person>> = restTemplate.exchange(uriHentPersoninfo,
                                                                              HttpMethod.GET,
                                                                              HttpEntity<String>(headers))

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
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withPath("/rest/pdl/graphql")
                                .withHeader("Tema", TEMA)
                                .withBody(gyldigRequest())
                )
                .respond(HttpResponse.response().withBody(readfile("pdlOkResponseEnkel.json"))
                                 .withHeaders(Header("Content-Type", "application/json")))


        val response: ResponseEntity<Ressurs<Person>> = restTemplate.exchange(uriHentPersoninfo,
                                                                              HttpMethod.GET,
                                                                              HttpEntity<String>(headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(response.body?.data?.fødselsdato).isEqualTo(FØDSELSDATO)
        assertThat(response.body?.data?.navn).isEqualTo(NAVN)
        assertThat(response.body?.data?.kjønn).isEqualTo(KJØNN)
        assertThat(response.body?.data?.familierelasjoner).isEmpty()
    }

    @Test
    fun `hent personinfo returnerer med feil hvis forventede persondata ikke oppgis`() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withPath("/rest/pdl/graphql")
                                .withBody(gyldigRequest())
                )
                .respond(HttpResponse.response().withBody(readfile("pdlManglerFoedselResponse.json"))
                                 .withHeaders(Header("Content-Type", "application/json")))

        val response: ResponseEntity<Ressurs<Map<String, String>>> = restTemplate.exchange(uriHentPersoninfo,
                                                                                           HttpMethod.GET,
                                                                                           HttpEntity<String>(headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
    }

    @Test
    fun `hent personinfo returnerer med feil hvis person ikke finnes`() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withPath("/rest/pdl/graphql")
                                .withBody(gyldigRequest())
                )
                .respond(HttpResponse.response().withBody(readfile("pdlPersonIkkeFunnetResponse.json"))
                                 .withHeaders(Header("Content-Type", "application/json")))

        val response: ResponseEntity<Ressurs<Map<String, String>>> = restTemplate.exchange(uriHentPersoninfo,
                                                                                           HttpMethod.GET,
                                                                                           HttpEntity<String>(headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
    }

    @Test
    fun `hent personinfo returnerer med feil hvis ikke pdl responderer med 200`() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withPath("/rest/pdl/graphql")
                                .withBody(gyldigRequest())
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
                                .withBody(gyldigIdenterRequest())
                )
                .respond(HttpResponse.response()
                                 .withBody(readfile("pdlAktorIdResponse.json"))
                                 .withHeaders(Header("Content-Type", "application/json"))
                                 .withStatusCode(200))

        val response: ResponseEntity<Ressurs<List<IdentInformasjon>>> = restTemplate.exchange(uriHentIdenter,
                                                                                              HttpMethod.GET,
                                                                                              HttpEntity<String>(headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
    }

    @Test
    fun `hentAktørId returnerer aktørId fra pdl`() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withPath("/rest/pdl/graphql")
                                .withBody(gyldigIdenterRequest())
                )
                .respond(HttpResponse.response()
                                 .withBody(readfile("pdlAktorIdResponse.json"))
                                 .withHeaders(Header("Content-Type", "application/json"))
                                 .withStatusCode(200))

        val response: ResponseEntity<Ressurs<List<String>>> = restTemplate.exchange(uriHentIdenter,
                                                                                    HttpMethod.GET,
                                                                                    HttpEntity<String>(headers))
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
    }

    private fun gyldigRequest(): String {
        return readfile("pdlGyldigRequest.json")
                .replace(
                        "GRAPHQL-PLACEHOLDER",
                        readfile("hentperson-med-relasjoner.graphql").graphqlCompatible()
                )
    }

    private fun gyldigIdenterRequest(): String {
        return readfile("pdlGyldigRequest.json")
                .replace(
                        "GRAPHQL-PLACEHOLDER",
                        readfile("hentIdenter.graphql").graphqlCompatible()
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
    }
}
