package no.nav.familie.integrasjoner.saksbehandler

import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.saksbehandler.Saksbehandler
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockserver.integration.ClientAndServer
import org.mockserver.junit.jupiter.MockServerExtension
import org.mockserver.junit.jupiter.MockServerSettings
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.mockserver.model.Parameter
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.util.UriComponentsBuilder
import java.util.UUID

@ActiveProfiles("integrasjonstest", "mock-sts", "mock-oauth")
@ExtendWith(MockServerExtension::class)
@MockServerSettings(ports = [OppslagSpringRunnerTest.MOCK_SERVER_PORT])
class SaksbehandlerControllerTest(
    val client: ClientAndServer,
) : OppslagSpringRunnerTest() {
    @BeforeEach
    fun setUp() {
        client.reset()
        headers.setBearerAuth(lokalTestToken)
    }

    @Test
    fun `skal kalle korrekt tjeneste for oppslag på id`() {
        val id = UUID.randomUUID()
        client
            .`when`(
                HttpRequest
                    .request()
                    .withMethod("GET")
                    .withPath("/users/$id"),
            ).respond(
                HttpResponse
                    .response()
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        """{
                                           "givenName": "Bob",
                                           "surname": "Burger",
                                           "id": "$id",
                                           "userPrincipalName": "Bob.Burger@nav.no",
                                           "onPremisesSamAccountName": "B857496",
                                           "streetAddress": "4415",
                                           "city": "Skien"
                                           }""",
                    ),
            )
        val uri =
            UriComponentsBuilder
                .fromHttpUrl(localhost(BASE_URL))
                .pathSegment(id.toString())
                .toUriString()

        val response: ResponseEntity<Ressurs<Saksbehandler>> =
            restTemplate.exchange(
                uri,
                HttpMethod.GET,
                HttpEntity<String>(headers),
            )
        print(id)
        val saksbehandler = response.body!!.data!!
        assertThat(saksbehandler.fornavn).isEqualTo("Bob")
        assertThat(saksbehandler.etternavn).isEqualTo("Burger")
        assertThat(saksbehandler.azureId).isEqualTo(id)
        assertThat(saksbehandler.navIdent).isEqualTo("B857496")
        assertThat(saksbehandler.enhet).isEqualTo("4415")
    }

    @Test
    fun `skal kalle korrekt tjeneste for oppslag på navIdent`() {
        val navIdent = "B857496"
        val id = UUID.randomUUID()

        client
            .`when`(
                HttpRequest
                    .request()
                    .withMethod("GET")
                    .withPath("/users")
                    .withQueryStringParameters(
                        Parameter("\$search", "\"onPremisesSamAccountName:B857496\""),
                        Parameter(
                            "\$select",
                            "givenName,surname,onPremisesSamAccountName,id," +
                                "userPrincipalName,streetAddress,city",
                        ),
                    ),
            ).respond(
                HttpResponse
                    .response()
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        """{
                                           "value": [
                                               {
                                                 "givenName": "Bob",
                                                 "surname": "Burger",
                                                 "id": "$id",
                                                 "userPrincipalName": "Bob.Burger@nav.no",
                                                 "onPremisesSamAccountName": "$navIdent",
                                                 "streetAddress": "4415",
                                                 "city": "Skien"
                                               }
                                           ]
                                           }""",
                    ),
            )
        val uri =
            UriComponentsBuilder
                .fromHttpUrl(localhost(BASE_URL))
                .pathSegment(navIdent)
                .toUriString()

        val response: ResponseEntity<Ressurs<Saksbehandler>> =
            restTemplate.exchange(
                uri,
                HttpMethod.GET,
                HttpEntity<String>(headers),
            )
        val saksbehandler = response.body!!.data!!
        assertThat(saksbehandler.fornavn).isEqualTo("Bob")
        assertThat(saksbehandler.etternavn).isEqualTo("Burger")
        assertThat(saksbehandler.azureId).isEqualTo(id)
        assertThat(saksbehandler.navIdent).isEqualTo(navIdent)
        assertThat(saksbehandler.enhet).isEqualTo("4415")
    }

    companion object {
        const val BASE_URL = "/api/saksbehandler"
    }
}
