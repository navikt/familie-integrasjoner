package no.nav.familie.integrasjoner.client.rest

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.dokdist.domene.DistribuerJournalpostRequestTo
import no.nav.familie.integrasjoner.dokdist.domene.DistribuerJournalpostResponseTo
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.dokdist.Distribusjonstidspunkt
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

class DokdistRestClientTest {
    private val restOperations: RestOperations = mockk()
    private val dokdistRestClient: DokdistRestClient = DokdistRestClient(dokdistUri = URI.create("dokdistkanal"), restTemplate = restOperations)

    @Nested
    inner class DistribuerJournalpost {
        @Test
        fun `skal kaste OppslagException når kall mot dokdist feiler`() {
            // Arrange
            every { restOperations.exchange<DistribuerJournalpostResponseTo?>(any<URI>(), eq(HttpMethod.POST), any<HttpEntity<DistribuerJournalpostRequestTo>>()) } throws RuntimeException("Noe gikk galt")

            // Act & Assert
            val oppslagException =
                assertThrows<OppslagException> {
                    dokdistRestClient.distribuerJournalpost(
                        DistribuerJournalpostRequestTo(
                            journalpostId = "",
                            batchId = null,
                            bestillendeFagsystem = Fagsystem.BA.navn,
                            adresse = null,
                            dokumentProdApp = "",
                            distribusjonstype = null,
                            distribusjonstidspunkt = Distribusjonstidspunkt.KJERNETID,
                        ),
                    )
                }

            assertThat(oppslagException.message).isEqualTo("Feil ved distribuering av journalpost")
            assertThat(oppslagException.kilde).isEqualTo("dokdist.distribuer.distribuerJournalpost")
            assertThat(oppslagException.level).isEqualTo(OppslagException.Level.MEDIUM)
            assertThat(oppslagException.httpStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
