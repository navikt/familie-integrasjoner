package no.nav.familie.integrasjoner.client.rest

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.felles.tokenklient.entraid.EntraIDRestClientFactory
import no.nav.familie.integrasjoner.felles.OppslagException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestClient
import java.net.URI

class AzureGraphRestClientTest {
    private val restClient: RestClient = mockk(relaxed = true)
    private val responseSpec: RestClient.ResponseSpec = mockk()
    private val factory: EntraIDRestClientFactory =
        mockk {
            every { lagHybridRestKlient(any(), any()) } returns restClient
        }
    private val azureGraphRestClient: AzureGraphRestClient =
        AzureGraphRestClient(
            aadGraphURI = URI("http://localhost:8080"),
            scope = "dummy-scope",
            entraIDRestClientFactory = factory,
        )

    @Nested
    inner class FinnSaksbehandler {
        @Test
        fun `skal kaste OppslagException ved feil mot azure`() {
            every { responseSpec.body(any<Class<*>>()) } throws RuntimeException("Noe gikk galt")

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
            every { responseSpec.body(any<Class<*>>()) } throws RuntimeException("Noe gikk galt")

            val oppslagException = assertThrows<OppslagException> { azureGraphRestClient.hentSaksbehandler("1234") }

            assertThat(oppslagException.message).isEqualTo("Feil ved henting av saksbehandler med id")
            assertThat(oppslagException.kilde).isEqualTo("azure.saksbehandler.id")
            assertThat(oppslagException.level).isEqualTo(OppslagException.Level.MEDIUM)
            assertThat(oppslagException.httpStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
