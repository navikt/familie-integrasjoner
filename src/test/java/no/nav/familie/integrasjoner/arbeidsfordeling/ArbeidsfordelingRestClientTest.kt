package no.nav.familie.integrasjoner.arbeidsfordeling

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.felles.OppslagException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestClient
import java.net.URI

class ArbeidsfordelingRestClientTest {
    private val restClient: RestClient = mockk(relaxed = true)
    private val responseSpec: RestClient.ResponseSpec = mockk()

    private val arbeidsfordelingRestClient: ArbeidsfordelingRestClient =
        ArbeidsfordelingRestClient(
            norg2Uri = URI.create("http://norg2"),
            restClient = restClient,
        )

    @Nested
    inner class HentEnhet {
        @Test
        fun `skal kaste OppslagException ved feil mot Norg2`() {
            every { responseSpec.body(any<Class<*>>()) } throws RuntimeException("Noe gikk galt")

            val oppslagException = assertThrows<OppslagException> { arbeidsfordelingRestClient.hentEnhet("oslo") }

            assertThat(oppslagException.message).isEqualTo("Feil ved henting av enhet")
            assertThat(oppslagException.kilde).isEqualTo("norg2.hentEnhet")
            assertThat(oppslagException.level).isEqualTo(OppslagException.Level.MEDIUM)
            assertThat(oppslagException.httpStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @Nested
    inner class HentNavkontor {
        @Test
        fun `skal kaste OppslagException ved feil mot Norg2`() {
            every { responseSpec.body(any<Class<*>>()) } throws RuntimeException("Noe gikk galt")

            val oppslagException = assertThrows<OppslagException> { arbeidsfordelingRestClient.hentNavkontor("1234") }

            assertThat(oppslagException.message).isEqualTo("Feil ved henting av navkontor")
            assertThat(oppslagException.kilde).isEqualTo("norg2.hentNavkontor")
            assertThat(oppslagException.level).isEqualTo(OppslagException.Level.MEDIUM)
            assertThat(oppslagException.httpStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @Nested
    inner class FinnBehandlendeEnhetMedBesteMatch {
        @Test
        fun `skal kaste OppslagException ved feil mot Norg2`() {
            every { responseSpec.body(any<Class<*>>()) } throws RuntimeException("Noe gikk galt")

            val oppslagException =
                assertThrows<OppslagException> {
                    arbeidsfordelingRestClient.finnBehandlendeEnhetMedBesteMatch(
                        ArbeidsfordelingKriterie(
                            "",
                            geografiskOmraade = "Oslo",
                            diskresjonskode = null,
                            skjermet = false,
                        ),
                    )
                }

            assertThat(oppslagException.message).isEqualTo("Feil ved oppslag av best matchende behandlende enhet")
            assertThat(oppslagException.kilde).isEqualTo("norg2.finnBehandlendeEnhetMedBesteMatch")
            assertThat(oppslagException.level).isEqualTo(OppslagException.Level.MEDIUM)
            assertThat(oppslagException.httpStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
