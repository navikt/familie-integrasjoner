package no.nav.familie.integrasjoner.medlemskap

import com.github.tomakehurst.wiremock.client.WireMock
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.integrasjoner.medlemskap.domain.Medlemskapsinfo
import no.nav.familie.kontrakter.felles.Ressurs
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import java.nio.charset.StandardCharsets
import java.nio.file.Files

@ActiveProfiles("integrasjonstest", "mock-sts")
@TestPropertySource(properties = ["MEDL2_URL=http://localhost:28085"])
@AutoConfigureWireMock(port = 28085)
class MedlemskapControllerTest : OppslagSpringRunnerTest() {

    @Before
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
    }

    @Test
    fun `skal korrekt behandle returobjekt`() {
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/api/v1/medlemskapsunntak"))
                                 .willReturn(WireMock.aResponse()
                                                     .withStatus(200)
                                                     .withHeader("Content-Type", "application/json")
                                                     .withBody(gyldigOppgaveResponse("medlrespons.json"))))

        val response: ResponseEntity<Ressurs<Medlemskapsinfo>> = restTemplate.exchange(localhost(GET_MEDLEMSKAP_URL),
                                                                                       HttpMethod.GET,
                                                                                       HttpEntity(null, headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.status).isEqualTo(Ressurs.success(null).status)
        assertThat(response.body?.data?.personIdent).isEqualTo("12345678911")
    }

    @Test
    fun `skal kaste feil for ikke funnet`() {
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/api/v1/medlemskapsunntak"))
                                 .willReturn(WireMock.aResponse()
                                                     .withStatus(404)
                                                     .withHeader("Content-Type", "application/json")))

        val response: ResponseEntity<Ressurs<Medlemskapsinfo>> = restTemplate.exchange(localhost(GET_MEDLEMSKAP_URL),
                                                                                       HttpMethod.GET,
                                                                                       HttpEntity(null, headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    private fun gyldigOppgaveResponse(filnavn: String): String {
        return Files.readString(ClassPathResource("medlemskap/$filnavn").file.toPath(),
                                StandardCharsets.UTF_8)
    }

    companion object {
        private const val GET_MEDLEMSKAP_URL = "api/medlemskap/v1?id=6549687"
    }
}
