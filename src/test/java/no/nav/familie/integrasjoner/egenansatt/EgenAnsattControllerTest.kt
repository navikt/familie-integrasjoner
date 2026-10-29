package no.nav.familie.integrasjoner.egenansatt

import com.github.tomakehurst.wiremock.client.WireMock
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.integrasjoner.client.rest.EgenAnsattRestClient
import no.nav.familie.kontrakter.felles.jsonMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock

@ActiveProfiles("integrasjonstest", "mock-oauth")
@TestPropertySource(properties = ["EGEN_ANSATT_URL=http://localhost:28085"])
@EnableWireMock(
    ConfigureWireMock(name = "EgenAnsattControllerTest", port = 28085),
)
class EgenAnsattControllerTest : OppslagSpringRunnerTest() {
    @Autowired
    lateinit var egenAnsattRestClient: EgenAnsattRestClient

    @Test
    fun `Person er egenansatt`() {
        stubGetEgenAnsatt(true)
        assertThat(egenAnsattRestClient.erEgenAnsatt(FNR)).isTrue
    }

    @Test
    fun `Person er ikke egenansatt`() {
        stubGetEgenAnsatt(false)
        assertThat(egenAnsattRestClient.erEgenAnsatt(FNR)).isFalse
    }

    @Test
    fun `Bolk henter skjerming med maskin-til-maskin-token`() {
        WireMock.stubFor(
            WireMock
                .post("/skjermetBulk")
                .withHeader(HttpHeaders.AUTHORIZATION, WireMock.equalTo("Bearer mock-m2m-token"))
                .withRequestBody(WireMock.equalToJson("""{"personidenter":["$FNR"]}"""))
                .willReturn(
                    WireMock
                        .aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"$FNR":true}"""),
                ),
        )

        assertThat(egenAnsattRestClient.erEgenAnsatt(setOf(FNR))).containsEntry(FNR, true)
    }

    private fun stubGetEgenAnsatt(erEgenAnsatt: Boolean) {
        WireMock.stubFor(
            WireMock
                .post(PATH_OG_QUERY)
                .withHeader(HttpHeaders.AUTHORIZATION, WireMock.equalTo("Bearer mock-m2m-token"))
                .withRequestBody(WireMock.equalToJson("""{"personident":"$FNR"}"""))
                .willReturn(
                    WireMock
                        .aResponse()
                        .withStatus(200)
                        .withHeader(
                            "Content-Type",
                            "application/json",
                        ).withBody(
                            jsonMapper.writeValueAsString(
                                erEgenAnsatt,
                            ),
                        ),
                ),
        )
    }

    companion object {
        const val FNR = "12345678910"
        private const val PATH_OG_QUERY = "/skjermet"
    }
}
