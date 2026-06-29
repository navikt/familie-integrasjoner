package no.nav.familie.integrasjoner.client.rest

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.felles.tokenklient.entraid.EntraIDRestClientFactory
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
    private val restClient: RestClient = mockk(relaxed = true)
    private val responseSpec: RestClient.ResponseSpec = mockk()
    private val factory: EntraIDRestClientFactory =
        mockk {
            every { lagHybridRestKlient(any(), any()) } returns restClient
        }
    private val dokdistRestClient: DokdistRestClient =
        DokdistRestClient(
            dokdistUri = URI.create("http://dokdist"),
            scope = "dummy-scope",
            entraIDRestClientFactory = factory,
        )

    @Nested
    inner class DistribuerJournalpost {
        @Test
        fun `skal kaste OppslagException når kall mot dokdist feiler`() {
            every { responseSpec.body(any<Class<*>>()) } throws RuntimeException("Noe gikk galt")

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
