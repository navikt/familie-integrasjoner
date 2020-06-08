package no.nav.familie.integrasjoner.egenansatt

import io.mockk.every
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.integrasjoner.client.soap.EgenAnsattSoapClient
import no.nav.familie.integrasjoner.egenansatt.domene.EgenAnsattResponse
import no.nav.familie.kontrakter.felles.Ressurs
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.exchange
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@ActiveProfiles("integrasjonstest", "mock-sts", "mock-egenansatt")
@TestPropertySource(properties = ["MEDL2_URL=http://localhost:28085"])
@AutoConfigureWireMock(port = 28085)
class EgenAnsattControllerTest : OppslagSpringRunnerTest() {

    @Autowired
    lateinit var egenAnsattSoapClient: EgenAnsattSoapClient

    @Before
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
        headers.set("Content-Type", "application/json")
    }

    @Test
    fun `skal korrekt behandle request`() {
        every { egenAnsattSoapClient.erEgenAnsatt(any()) } returns true
        val response: ResponseEntity<Ressurs<EgenAnsattResponse>> =
                restTemplate.exchange(localhost("/api/egenansatt"),
                                      HttpMethod.POST,
                                      HttpEntity<Any>("{\"ident\": \"1\"}", headers))

        assertThat(response.body.data!!.erEgenAnsatt).isTrue()

    }

    @Test
    fun `skal feile når body ikke inneholder noe`() {
        val response: ResponseEntity<Ressurs<EgenAnsattResponse>> =
                restTemplate.exchange(localhost("/api/egenansatt"),
                                      HttpMethod.POST,
                                      HttpEntity<Any>("{}", headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }
}