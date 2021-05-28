package no.nav.familie.integrasjoner.saksbehandler

import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.kontrakter.felles.saksbehandler.Saksbehandler
import no.nav.security.token.support.test.JwtTokenGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockserver.junit.MockServerRule
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.util.UriComponentsBuilder
import java.util.UUID

@ActiveProfiles("integrasjonstest", "mock-sts", "mock-oauth")
class SaksbehandlerControllerTest : OppslagSpringRunnerTest() {

    @Autowired
    lateinit var saksbehandlerController: SaksbehandlerController

    @get:Rule
    val mockServerRule = MockServerRule(this, MOCK_SERVER_PORT)

    @Before
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
    }

    @Test
    fun `skal korrekt behandle returobjekt`() {
        val id = UUID.randomUUID().toString()
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("GET")
                                .withPath("/users/$id"))
                .respond(HttpResponse.response().withHeader("Content-Type", "application/json")
                                 .withBody("""{
                                           "businessPhones": [],
                                           "displayName": "Burger, Bob",
                                           "givenName": "Bob",
                                           "jobTitle": "BLA2830",
                                           "mail": "Bob.Burger@nav.no",
                                           "mobilePhone": null,
                                           "officeLocation": "2830 DIR YTELSESAVDELINGEN",
                                           "preferredLanguage": null,
                                           "surname": "Burger",
                                           "userPrincipalName": "Bob.Burger@nav.no",
                                           "id": "$id"
                                           }"""))
        val uri = UriComponentsBuilder.fromHttpUrl(localhost(BASE_URL))
                .pathSegment(id).toUriString()

        val response: ResponseEntity<Saksbehandler> = restTemplate.exchange(uri,
                                                                            HttpMethod.GET,
                                                                            HttpEntity<String>(headers))

        val saksbehandler = response.body
        assertThat(saksbehandler.fornavn).isEqualTo("Bob")
        assertThat(saksbehandler.etternavn).isEqualTo("Burger")
    }

    companion object {

        const val BASE_URL = "/api/saksbehandler"

        const val MOCK_SERVER_PORT = 18321
    }
}
