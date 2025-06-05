package no.nav.familie.integrasjoner.arbeidoginntekt

import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.status
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Status.FEILET
import no.nav.familie.kontrakter.felles.Ressurs.Status.SUKSESS
import no.nav.familie.kontrakter.felles.getDataOrThrow
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.postForObject
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@ActiveProfiles("integrasjonstest", "mock-oauth")
@TestPropertySource(properties = ["ARBEID_INNTEKT_URL=http://localhost:28085"])
@AutoConfigureWireMock(port = 28085)
class ArbeidOgInntektControllerTest : OppslagSpringRunnerTest() {
    @BeforeEach
    fun setup() {
        headers.setBearerAuth(lokalTestToken)
    }

    @Test
    fun `skal returnere url for a-inntekt`() {
        val urlResponse = "https://www.gyldig-url.no"
        stubFor(get("/api/v2/redirect/sok/a-inntekt").willReturn(ok(urlResponse)))

        val response =
            restTemplate.postForObject<Ressurs<String>>(
                localhost(ARBEID_INNTEKT_HENT_URL),
                HttpEntity(
                    PersonIdent(IDENT),
                    headers,
                ),
            )

        assertThat(response?.status).isEqualTo(SUKSESS)
        assertThat(response?.getDataOrThrow()).isEqualTo(urlResponse)
    }

    @Test
    fun `skal returnere feilmelding hvis noe går galt mot ekstern kilde`() {
        stubFor(get("/api/v2/redirect/sok/a-inntekt").willReturn(status(404)))

        val response =
            restTemplate.postForObject<Ressurs<String>>(
                localhost(ARBEID_INNTEKT_HENT_URL),
                HttpEntity(
                    PersonIdent(IDENT),
                    headers,
                ),
            )

        assertThat(response?.status).isEqualTo(FEILET)
        assertThat(response?.melding).contains("[ainntekt.hentUrlTilArbeidOgInntekt][Feil ved oppslag av url for a-inntekt.][org.springframework.web.client.HttpClientErrorException\$NotFound]")
    }

    companion object {
        private const val ARBEID_INNTEKT_HENT_URL = "/api/arbeid-og-inntekt/hent-url"
        private const val IDENT = "01012012345"
    }
}
