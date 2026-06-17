package no.nav.familie.integrasjoner.client.rest

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.kontrakter.felles.Tema
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestClient
import java.net.URI

class PdlClientCredentialRestClientTest {
    private val restClient: RestClient = mockk()
    private val requestBodyUriSpec: RestClient.RequestBodyUriSpec = mockk()
    private val requestBodySpec: RestClient.RequestBodySpec = mockk()
    private val responseSpec: RestClient.ResponseSpec = mockk()
    private val pdlClientCredentialRestClient: PdlClientCredentialRestClient =
        PdlClientCredentialRestClient(
            pdlBaseUrl = URI.create("http://pdl"),
            restClient = restClient,
        )

    @Nested
    inner class HentPersonMedRelasjonerOgAdressebeskyttelse {
        @Test
        fun `skal kaste OppslagException når kall mot PDL feiler`() {
            // Arrange
            every { restClient.post() } returns requestBodyUriSpec
            every { requestBodyUriSpec.uri(any<URI>()) } returns requestBodySpec
            every { requestBodySpec.header(any(), any()) } returns requestBodySpec
            every { requestBodySpec.body(any()) } returns requestBodySpec
            every { requestBodySpec.retrieve() } returns responseSpec
            every { responseSpec.body(any<Class<*>>()) } throws RuntimeException("Noe gikk galt")

            // Act & Assert
            val oppslagException =
                assertThrows<OppslagException> {
                    pdlClientCredentialRestClient.hentPersonMedRelasjonerOgAdressebeskyttelse(listOf("12345678910"), Tema.BAR)
                }

            assertThat(oppslagException.message).isEqualTo("Feil ved henting av person med relasjoner og adressebeskyttelse")
            assertThat(oppslagException.kilde).isEqualTo("pdl.cc.hentPersonMedRelasjonerOgAdressebeskyttelse")
            assertThat(oppslagException.level).isEqualTo(OppslagException.Level.MEDIUM)
            assertThat(oppslagException.httpStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
