package no.nav.familie.integrasjoner.client.rest

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.dokdist.domene.DistribuerJournalpostRequestTo
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.dokdist.Distribusjonstidspunkt
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestClient
import java.net.URI

class DokdistRestClientTest {
    private val restClient: RestClient = mockk()
    private val requestBodyUriSpec: RestClient.RequestBodyUriSpec = mockk()
    private val requestBodySpec: RestClient.RequestBodySpec = mockk()
    private val responseSpec: RestClient.ResponseSpec = mockk()
    private val dokdistRestClient: DokdistRestClient =
        DokdistRestClient(
            dokdistUri = URI.create("http://dokdist"),
            restClient = restClient,
        )

    @Nested
    inner class DistribuerJournalpost {
        @Test
        fun `skal kaste OppslagException når kall mot dokdist feiler`() {
            // Arrange
            every { restClient.post() } returns requestBodyUriSpec
            every { requestBodyUriSpec.uri(any<URI>()) } returns requestBodySpec
            every { requestBodySpec.body(any()) } returns requestBodySpec
            every { requestBodySpec.retrieve() } returns responseSpec
            every { responseSpec.body(any<Class<*>>()) } throws RuntimeException("Noe gikk galt")

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
