package no.nav.familie.integrasjoner.organisasjon

import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.notFound
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.kontrakter.felles.organisasjon.Organisasjon
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@ActiveProfiles("integrasjonstest")
@TestPropertySource(properties = ["ORGANISASJON_URL=http://localhost:28085"])
@AutoConfigureWireMock(port = 28085)
internal class OrganisasjonServiceTest : OppslagSpringRunnerTest() {
    @Autowired
    lateinit var organisasjonService: OrganisasjonService

    @Test
    internal fun `skal mappe orgnr og navn`() {
        stubFor(
            get(urlEqualTo("/v2/organisasjon/1/noekkelinfo"))
                .willReturn(okJson(bodyOrg)),
        )
        assertThat(organisasjonService.hentOrganisasjon("1")).isEqualTo(
            Organisasjon(
                organisasjonsnummer = "1",
                navn = "NAV AS",
            ),
        )
    }

    @Test
    internal fun `skal returnere true hvis tjenesten returnerer info`() {
        stubFor(
            get(urlEqualTo("/v2/organisasjon/1/noekkelinfo"))
                .willReturn(okJson(bodyOrg)),
        )
        assertThat(organisasjonService.validerOrganisasjon("1")).isTrue
    }

    @Test
    internal fun `skal returnere false hvis tjenesten returnerer 404`() {
        stubFor(
            get(urlEqualTo("/v2/organisasjon/1/noekkelinfo"))
                .willReturn(notFound().withBody(bodyOrgIkkeFunnet)),
        )
        assertThat(organisasjonService.validerOrganisasjon("1")).isFalse
    }

    private val bodyOrg =
        """
        {
          "navn": {
           "sammensattnavn": "NAV AS"
          }
        }
        """.trimIndent()

    private val bodyOrgIkkeFunnet =
        """
        {
          "melding": "Ingen organisasjon med organisasjonsnummer 111 ble funnet"
        }
        """.trimIndent()
}
