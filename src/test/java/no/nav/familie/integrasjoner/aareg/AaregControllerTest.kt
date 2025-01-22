package no.nav.familie.integrasjoner.aareg

import com.github.tomakehurst.wiremock.client.WireMock.anyUrl
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.status
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.integrasjoner.aareg.domene.Arbeidsforhold
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Status.FEILET
import no.nav.familie.kontrakter.felles.Ressurs.Status.SUKSESS
import no.nav.familie.kontrakter.felles.getDataOrThrow
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.postForObject
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.time.LocalDate

@ActiveProfiles("integrasjonstest", "mock-oauth")
@TestPropertySource(properties = ["AAREG_URL=http://localhost:28085"])
@AutoConfigureWireMock(port = 28085)
class AaregControllerTest : OppslagSpringRunnerTest() {
    @BeforeEach
    fun setup() {
        headers.setBearerAuth(lokalTestToken)
    }

    @Test
    fun `skal returnere arbeidsforhold`() {
        stubFor(get(anyUrl()).willReturn(okJson(gyldigResponse("gyldigResponse.json"))))

        val response =
            restTemplate.postForObject<Ressurs<List<Arbeidsforhold>>>(
                localhost(ARBEIDSFORHOLD_URL),
                HttpEntity(
                    ArbeidsforholdRequest(
                        IDENT,
                        LocalDate.now(),
                    ),
                    headers,
                ),
            )

        assertThat(response?.status).isEqualTo(SUKSESS)
        assertThat(response?.getDataOrThrow()).hasSize(1)
        val arbeidsforhold = objectMapper.convertValue(response?.getDataOrThrow()!!.first(), Arbeidsforhold::class.java)
        assertThat(arbeidsforhold.arbeidstaker?.offentligIdent).isEqualTo(IDENT)
        assertThat(arbeidsforhold.arbeidsgiver?.organisasjonsnummer).isEqualTo("998877665")
        assertThat(arbeidsforhold.ansettelsesperiode?.periode?.fom).isEqualTo(LocalDate.of(2000, 8, 4))
    }

    @Test
    fun `skal returnere feilmelding hvis noe går galt mot ekstern kilde`() {
        stubFor(get(anyUrl()).willReturn(status(404)))

        val response =
            restTemplate.postForObject<Ressurs<List<Arbeidsforhold>>>(
                localhost(ARBEIDSFORHOLD_URL),
                HttpEntity(
                    ArbeidsforholdRequest(
                        IDENT,
                        LocalDate.now(),
                    ),
                    headers,
                ),
            )

        assertThat(response?.status).isEqualTo(FEILET)
        assertThat(response?.melding).contains("[Feil ved oppslag av arbeidsforhold.][org.springframework.web.client.HttpClientErrorException")
    }

    private fun gyldigResponse(filnavn: String): String =
        Files.readString(
            ClassPathResource("aareg/$filnavn").file.toPath(),
            StandardCharsets.UTF_8,
        )

    companion object {
        private const val AAREG_URL = "/api/aareg"
        private const val ARBEIDSFORHOLD_URL = "$AAREG_URL/arbeidsforhold"
        private const val IDENT = "01012012345"
    }
}
