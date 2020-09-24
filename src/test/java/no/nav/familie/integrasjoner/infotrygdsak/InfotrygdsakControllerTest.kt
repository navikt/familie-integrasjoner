package no.nav.familie.integrasjoner.infotrygdsak

import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.integrasjoner.client.soap.InfotrygdsakSoapClient
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.infotrygdsak.OpprettInfotrygdSakRequest
import no.nav.familie.kontrakter.felles.infotrygdsak.OpprettInfotrygdSakResponse
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions
import org.junit.Before
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.exchange
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@ActiveProfiles("integrasjonstest", "mock-sts", "mock-infotrygdsak")
@TestPropertySource(properties = ["MEDL2_URL=http://localhost:28085"])
@AutoConfigureWireMock(port = 28085)
internal class InfotrygdsakControllerTest : OppslagSpringRunnerTest() {

    @Autowired
    lateinit var infotrygdsakSoapClient: InfotrygdsakSoapClient

    @Before
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
        headers.set("Content-Type", "application/json")
    }

    @org.junit.Test
    fun `skal korrekt behandle request`() {
        val opprettInfotrygdSakRequest =  OpprettInfotrygdSakRequest("32132132112", "ENF")

        val response: ResponseEntity<Ressurs<OpprettInfotrygdSakResponse>> =
                        restTemplate.exchange(localhost("/api/infotrygdsak"),
                                      HttpMethod.POST,
                                      HttpEntity<Any>(objectMapper.writeValueAsString(opprettInfotrygdSakRequest), headers))

        Assertions.assertThat(response.body.data).isNotNull()

    }

}