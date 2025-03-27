package no.nav.familie.integrasjoner.client.rest

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.axsys.TilgangV2DTO
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.kontrakter.felles.NavIdent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestOperations
import org.springframework.web.client.exchange
import java.net.URI

class AxsysRestClientTest {
    private val restOperations: RestOperations = mockk()
    private val axsysRestClient: AxsysRestClient =
        AxsysRestClient(
            enhetBaseUrl = URI("http://localhost:8080"),
            restTemplate = restOperations,
        )

    @Nested
    inner class HentEnheterNavIdentHarTilgangTil {
        @Test
        fun `skal kaste OppslagException ved feil mot axsys`() {
            // Arrange
            every { restOperations.exchange<TilgangV2DTO>(any<URI>(), eq(HttpMethod.GET), any<HttpEntity<Void>>()) } throws RuntimeException("Noe gikk galt")

            // Act & Assert
            val oppslagException = assertThrows<OppslagException> { axsysRestClient.hentEnheterNavIdentHarTilgangTil(NavIdent("1234")) }

            assertThat(oppslagException.message).isEqualTo("Feil ved henting av enheter Nav identer har tilgang til")
            assertThat(oppslagException.kilde).isEqualTo("axsys.hentEnheterNavIdentHarTilgangTil")
            assertThat(oppslagException.level).isEqualTo(OppslagException.Level.MEDIUM)
            assertThat(oppslagException.httpStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
