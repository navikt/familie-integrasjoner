package no.nav.familie.integrasjoner.organisasjon

import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.notFound
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.serverError
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.kontrakter.felles.organisasjon.Gyldighetsperiode
import no.nav.familie.kontrakter.felles.organisasjon.Organisasjon
import no.nav.familie.kontrakter.felles.organisasjon.OrganisasjonAdresse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock
import java.time.LocalDate

@ActiveProfiles("integrasjonstest")
@TestPropertySource(properties = ["ORGANISASJON_URL=http://localhost:28085"])
@EnableWireMock(
    ConfigureWireMock(name = "OrganisasjonServiceTest", port = 28085),
)
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
                adresse =
                    OrganisasjonAdresse(
                        type = "Forretningsadresse",
                        kommunenummer = "0301",
                        adresselinje1 = "Sannergata 2",
                        adresselinje2 = null,
                        adresselinje3 = null,
                        postnummer = "0557",
                        gyldighetsperiode = Gyldighetsperiode(fom = LocalDate.of(2007, 8, 23), tom = null),
                    ),
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

    @Test
    internal fun `hentOrganisasjon kaster OppslagException med status NOT_FOUND og level LAV når EREG returnerer 404`() {
        stubFor(
            get(urlEqualTo("/v2/organisasjon/2/noekkelinfo"))
                .willReturn(notFound().withBody(bodyOrgIkkeFunnet)),
        )

        assertThatThrownBy { organisasjonService.hentOrganisasjon("2") }
            .isInstanceOfSatisfying(OppslagException::class.java) {
                assertThat(it.httpStatus).isEqualTo(HttpStatus.NOT_FOUND)
                assertThat(it.level).isEqualTo(OppslagException.Level.LAV)
                assertThat(it.kilde).isEqualTo("organisasjon.hent")
            }
    }

    private val bodyOrg =
        """
        {
          "navn": {
           "sammensattnavn": "NAV AS"
          },
          "adresse":{
            "type":"Forretningsadresse",
            "adresselinje1":"Sannergata 2",
            "postnummer":"0557",
            "landkode":"NO",
            "kommunenummer":"0301",
            "gyldighetsperiode":
              {
                "fom":"2007-08-23"
              }
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
