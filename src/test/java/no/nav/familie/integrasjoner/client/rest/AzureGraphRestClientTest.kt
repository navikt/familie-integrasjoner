package no.nav.familie.integrasjoner.client.rest

import io.mockk.every
import io.mockk.mockk
import java.net.URI
import no.nav.familie.integrasjoner.axsys.TilgangV2DTO
import no.nav.familie.integrasjoner.azure.domene.AzureAdBruker
import no.nav.familie.integrasjoner.azure.domene.AzureAdBrukere
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

class AzureGraphRestClientTest {
    private val restOperations: RestOperations = mockk()
    private val azureGraphRestClient: AzureGraphRestClient = AzureGraphRestClient(
        restTemplate = restOperations,
        aadGraphURI = URI("http://localhost:8080"),
    )

    @Nested
    inner class FinnSaksbehandler {
        @Test
        fun `skal kaste OppslagException ved feil mot azure`() {
            // Arrange
            every { restOperations.exchange<AzureAdBrukere>(any<URI>(), eq(HttpMethod.GET), any<HttpEntity<Void>>()) } throws RuntimeException("Noe gikk galt")

            // Act & Assert
            val oppslagException = assertThrows<OppslagException> { azureGraphRestClient.finnSaksbehandler("1234") }

            assertThat(oppslagException.message).isEqualTo("Feil ved henting av saksbehandler med nav ident")
            assertThat(oppslagException.kilde).isEqualTo("azure.saksbehandler.navIdent")
            assertThat(oppslagException.level).isEqualTo(OppslagException.Level.MEDIUM)
            assertThat(oppslagException.httpStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @Nested
    inner class HentSaksbehandler {
        @Test
        fun `skal kaste OppslagException ved feil mot azure`() {
            // Arrange
            every { restOperations.exchange<AzureAdBruker>(any<URI>(), eq(HttpMethod.GET), any<HttpEntity<Void>>()) } throws RuntimeException("Noe gikk galt")

            // Act & Assert
            val oppslagException = assertThrows<OppslagException> { azureGraphRestClient.hentSaksbehandler("1234") }

            assertThat(oppslagException.message).isEqualTo("Feil ved henting av saksbehandler med id")
            assertThat(oppslagException.kilde).isEqualTo("azure.saksbehandler.id")
            assertThat(oppslagException.level).isEqualTo(OppslagException.Level.MEDIUM)
            assertThat(oppslagException.httpStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}