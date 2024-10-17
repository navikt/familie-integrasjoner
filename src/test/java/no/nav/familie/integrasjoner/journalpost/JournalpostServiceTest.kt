package no.nav.familie.integrasjoner.journalpost

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.integrasjoner.client.rest.SafHentDokumentRestClient
import no.nav.familie.integrasjoner.client.rest.SafRestClient
import no.nav.familie.kontrakter.felles.BrukerIdType
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.journalpost.Bruker
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.JournalposterForBrukerRequest
import no.nav.familie.kontrakter.felles.journalpost.TilgangsstyrtJournalpost
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class JournalpostServiceTest {
    private val safRestClient: SafRestClient = mockk()

    private val safHentDokumentRestClient: SafHentDokumentRestClient = mockk()

    private val baksTilgangsstyrtJournalpostService: BaksTilgangsstyrtJournalpostService = mockk()

    private val journalpostService =
        JournalpostService(
            safRestClient = safRestClient,
            safHentDokumentRestClient = safHentDokumentRestClient,
            baksTilgangsstyrtJournalpostService = baksTilgangsstyrtJournalpostService,
        )

    @Nested
    inner class FinnTilgangsstyrteJournalposter {
        @Test
        fun`skal hente journalposter fra saf og deretter mappe om lista til tilgangsstyrte journalposter`() {
            // Arrange
            val journalposterForBrukerRequest = JournalposterForBrukerRequest(brukerId = Bruker("1", BrukerIdType.FNR), antall = 100, tema = listOf(Tema.BAR))
            val journalposter = listOf(mockk<Journalpost>())
            every { safRestClient.finnJournalposter(journalposterForBrukerRequest) } returns journalposter
            every { baksTilgangsstyrtJournalpostService.tilTilgangstyrteJournalposter(journalposter) } returns listOf(mockk<TilgangsstyrtJournalpost>())

            // Act
            journalpostService.finnTilgangsstyrteBaksJournalposter(journalposterForBrukerRequest)

            // Assert
            verify(exactly = 1) { safRestClient.finnJournalposter(journalposterForBrukerRequest) }
            verify(exactly = 1) { baksTilgangsstyrtJournalpostService.tilTilgangstyrteJournalposter(journalposter) }
        }
    }
}
