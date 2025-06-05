package no.nav.familie.integrasjoner.client.rest

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.medlemskap.MedlemskapsunntakResponse
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

class MedlRestClientTest {
    private val restOperations: RestOperations = mockk()
    private val medlRestClient: MedlRestClient = MedlRestClient(medl2BaseUrl = URI.create("medl"), restTemplate = restOperations)

    @Nested
    inner class HentMedlemskapsUnntakResponse {
        @Test
        fun `skal kaste OppslagException hvis henting av medlemskapsunntak feiler`() {
            // Arrange
            every { restOperations.exchange<List<MedlemskapsunntakResponse>>(any<URI>(), eq(HttpMethod.GET), any<HttpEntity<Void>>()) } throws RuntimeException("Noe gikk galt")

            // Act & Assert
            val oppslagException =
                assertThrows<OppslagException> {
                    medlRestClient.hentMedlemskapsUnntakResponse(personident = "1234")
                }

            assertThat(oppslagException.message).isEqualTo("Feil ved henting av medlemskapsunntak")
            assertThat(oppslagException.kilde).isEqualTo("medl.unntak")
            assertThat(oppslagException.level).isEqualTo(OppslagException.Level.MEDIUM)
            assertThat(oppslagException.httpStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
