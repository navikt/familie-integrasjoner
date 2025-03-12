package no.nav.familie.integrasjoner.client.rest

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.dokdistkanal.domene.BestemDistribusjonskanalRequest
import no.nav.familie.integrasjoner.dokdistkanal.domene.BestemDistribusjonskanalResponse
import no.nav.familie.integrasjoner.felles.OppslagException
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

class DokdistkanalRestClientTest {
    private val restOperations: RestOperations = mockk()
    private val dokdistkanalRestClient: DokdistkanalRestClient = DokdistkanalRestClient(dokdistkanalUri = URI.create("dokdistkanal"), restTemplate = restOperations)

    @Nested
    inner class BestemDistribusjonskanal {
        @Test
        fun `skal kaste OppslagException ved feil mot dokdistkanal`() {
            // Arrange
            every { restOperations.exchange<BestemDistribusjonskanalResponse>(any<URI>(), eq(HttpMethod.POST), any<HttpEntity<BestemDistribusjonskanalRequest>>()) } throws RuntimeException("Noe gikk galt")

            // Act & Assert
            val oppslagException =
                assertThrows<OppslagException> {
                    dokdistkanalRestClient.bestemDistribusjonskanal(
                        BestemDistribusjonskanalRequest(
                            brukerId = "12345678910",
                            mottakerId = "12345678911",
                            tema = Tema.BAR,
                            dokumenttypeId = null,
                            erArkivert = null,
                            forsendelseStørrelse = null,
                        ),
                    )
                }

            assertThat(oppslagException.message).isEqualTo("Feil ved henting av distribusjonskanal")
            assertThat(oppslagException.kilde).isEqualTo("dokdist.kanal.bestemDistribusjonskanal")
            assertThat(oppslagException.level).isEqualTo(OppslagException.Level.MEDIUM)
            assertThat(oppslagException.httpStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
