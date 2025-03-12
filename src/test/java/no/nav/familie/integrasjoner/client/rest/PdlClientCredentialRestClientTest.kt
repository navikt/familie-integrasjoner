package no.nav.familie.integrasjoner.client.rest

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.personopplysning.internal.PdlBolkResponse
import no.nav.familie.integrasjoner.personopplysning.internal.PdlPersonBolkRequest
import no.nav.familie.integrasjoner.personopplysning.internal.PdlPersonMedRelasjonerOgAdressebeskyttelse
import no.nav.familie.kontrakter.felles.Tema
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

class PdlClientCredentialRestClientTest {
    private val restOperations: RestOperations = mockk()
    private val pdlClientCredentialRestClient: PdlClientCredentialRestClient = PdlClientCredentialRestClient(pdlBaseUrl = URI.create("pdl"), restTemplate = restOperations)

    @Nested
    inner class HentPersonMedRelasjonerOgAdressebeskyttelse {
        @Test
        fun `skal kaste OppslagException når kall mot PDL feiler`() {
            // Arrange
            every { restOperations.exchange<PdlBolkResponse<PdlPersonMedRelasjonerOgAdressebeskyttelse>>(any<URI>(), eq(HttpMethod.POST), any<HttpEntity<PdlPersonBolkRequest>>()) } throws RuntimeException("Noe gikk galt")

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
