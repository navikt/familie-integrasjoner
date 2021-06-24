package no.nav.familie.integrasjoner.saksbehandler

import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.kontrakter.felles.saksbehandler.Saksbehandler
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockserver.junit.MockServerRule
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.mockserver.model.Parameter
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
    fun `skal kalle korrekt tjeneste for oppslag på id`() {
        val id = UUID.randomUUID()
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("GET")
                                .withPath("/users/$id"))
                .respond(HttpResponse.response().withHeader("Content-Type", "application/json")
                                 .withBody("""{
                                           "givenName": "Bob",
                                           "surname": "Burger",
                                           "id": "$id",
                                           "userPrincipalName": "Bob.Burger@nav.no",
                                           "onPremisesSamAccountName": "B857496"
                                           }"""))
        val uri = UriComponentsBuilder.fromHttpUrl(localhost(BASE_URL))
                .pathSegment(id.toString()).toUriString()

        val response: ResponseEntity<Saksbehandler> = restTemplate.exchange(uri,
                                                                            HttpMethod.GET,
                                                                            HttpEntity<String>(headers))
        print(id)
        val saksbehandler = response.body
        assertThat(saksbehandler.fornavn).isEqualTo("Bob")
        assertThat(saksbehandler.etternavn).isEqualTo("Burger")
        assertThat(saksbehandler.azureId).isEqualTo(id)
        assertThat(saksbehandler.navIdent).isEqualTo("B857496")
    }

    @Test
    fun `skal kalle korrekt tjeneste for oppslag på navIdent`() {
        val navIdent = "B857496"
        val id = UUID.randomUUID()
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("GET")
                                .withPath("/users")
                                .withQueryStringParameters(Parameter("\$search", "onPremisesSamAccountName:B857496"),
                                                           Parameter("\$select",
                                                                     "givenName,surname,onPremisesSamAccountName,id," +
                                                                     "userPrincipalName")))
                .respond(HttpResponse.response().withHeader("Content-Type", "application/json")
                                 .withBody("""{
                                           "value": [
                                               {
                                                 "givenName": "Bob",
                                                 "surname": "Burger",
                                                 "id": "$id",
                                                 "userPrincipalName": "Bob.Burger@nav.no",
                                                 "onPremisesSamAccountName": "$navIdent"
                                               }
                                           ]
                                           }"""))
        val uri = UriComponentsBuilder.fromHttpUrl(localhost(BASE_URL))
                .pathSegment(navIdent).toUriString()

        val response: ResponseEntity<Saksbehandler> = restTemplate.exchange(uri,
                                                                            HttpMethod.GET,
                                                                            HttpEntity<String>(headers))
        val saksbehandler = response.body
        assertThat(saksbehandler.fornavn).isEqualTo("Bob")
        assertThat(saksbehandler.etternavn).isEqualTo("Burger")
        assertThat(saksbehandler.azureId).isEqualTo(id)
        assertThat(saksbehandler.navIdent).isEqualTo(navIdent)
    }

    companion object {

        const val BASE_URL = "/api/saksbehandler"

        const val MOCK_SERVER_PORT = 18321
    }
}
