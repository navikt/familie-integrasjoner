package no.nav.familie.integrasjoner.personopplysning

import ch.qos.logback.classic.Logger
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
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
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.util.UriComponentsBuilder
import java.nio.charset.StandardCharsets
import java.nio.file.Files

@ActiveProfiles("integrasjonstest", "mock-personopplysninger", "mock-sts")
class PersonopplysningerControllerTest : OppslagSpringRunnerTest() {

    private val testLogger = LoggerFactory.getLogger(PersonopplysningerControllerTest::class.java) as Logger
    @get:Rule
    val mockServerRule = MockServerRule(this, MOCK_SERVER_PORT)
    private lateinit var uriHentPersoninfo: String

    @Before
    fun setUp() {
        testLogger.addAppender(listAppender)
        headers.apply {
            add("Nav-Personident", "12345678901")
        }.setBearerAuth(JwtTokenGenerator.signedJWTAsString("testbruker"))
        uriHentPersoninfo = UriComponentsBuilder.fromHttpUrl(localhost(PDL_BASE_URL) + "v1/info/BAR").toUriString()
    }

    @Test
    fun `hent personinfo skal returnere fødselsnummer og status ok`() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withPath("/rest/pdl/graphql")
                                .withBody(testdata("pdlGyldigRequest.json"))
                )
                .respond(HttpResponse.response().withBody(testdata("pdlOkResponse.json"))
                                 .withHeaders(Header("Content-Type", "application/json")))



        val response: ResponseEntity<Ressurs<Person>> = restTemplate.exchange(uriHentPersoninfo,
                                                                                           HttpMethod.GET,
                                                                                           HttpEntity<String>(headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(response.body?.data?.fødselsdato).isEqualTo(FØDSELSDATO)
    }

    @Test
    fun `hent personinfo returnerer med feil hvis fødslelsdato ikke oppgis for`() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withPath("/rest/pdl/graphql")
                                .withBody(testdata("pdlGyldigRequest.json"))
                )
                .respond(HttpResponse.response().withBody(testdata("pdlManglerFoedselResponse.json"))
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
                                .withBody(testdata("pdlGyldigRequest.json"))
                )
                .respond(HttpResponse.response().withBody(testdata("pdlPersonIkkeFunnetResponse.json"))
                                 .withHeaders(Header("Content-Type", "application/json")))

        val response: ResponseEntity<Ressurs<Map<String, String>>> = restTemplate.exchange(uriHentPersoninfo,
                                                                                           HttpMethod.GET,
                                                                                           HttpEntity<String>(headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
    }

    @Test
    fun `hent personinfo returnerer med feil hvis ikke pdl responderer med 200`() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withPath("/rest/pdl/graphql")
                                .withBody(testdata("pdlGyldigRequest.json"))
                )
                .respond(HttpResponse.response().withStatusCode(500))

        val response: ResponseEntity<Ressurs<Map<String, String>>> = restTemplate.exchange(uriHentPersoninfo,
                                                                                           HttpMethod.GET,
                                                                                           HttpEntity<String>(headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
    }


    private fun testdata(filnavn: String): String {
        return Files.readString(ClassPathResource("pdl/$filnavn").file.toPath(), StandardCharsets.UTF_8)
    }

    companion object {
        const val MOCK_SERVER_PORT = 18321
        const val FØDSELSDATO = "1955-09-13"
        const val PDL_BASE_URL = "/api/personopplysning/"
    }
}