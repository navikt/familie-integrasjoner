package no.nav.familie.integrasjoner.client.rest

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.felles.tokenklient.entraid.EntraIDRestClientFactory
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
    private val restClient: RestClient = mockk(relaxed = true)
    private val responseSpec: RestClient.ResponseSpec = mockk()
    private val factory: EntraIDRestClientFactory =
        mockk {
            every { lagMaskinTilMaskinRestKlient(any()) } returns restClient
        }
    private val pdlClientCredentialRestClient: PdlClientCredentialRestClient =
        PdlClientCredentialRestClient(
            pdlBaseUrl = URI.create("http://pdl"),
            scope = "dummy-scope",
            entraIDRestClientFactory = factory,
        )

    @Nested
    inner class HentPersonMedRelasjonerOgAdressebeskyttelse {
        @Test
        fun `skal kaste OppslagException når kall mot PDL feiler`() {
            every { responseSpec.body(any<Class<*>>()) } throws RuntimeException("Noe gikk galt")

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
