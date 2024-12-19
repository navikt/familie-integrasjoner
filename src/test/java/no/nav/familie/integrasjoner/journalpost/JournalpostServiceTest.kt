package no.nav.familie.integrasjoner.journalpost

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.integrasjoner.client.rest.SafHentDokumentRestClient
import no.nav.familie.integrasjoner.client.rest.SafRestClient
import no.nav.familie.kontrakter.felles.BrukerIdType
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.journalpost.Bruker
import no.nav.familie.kontrakter.felles.journalpost.Dokumentvariantformat
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.JournalposterForBrukerRequest
import no.nav.familie.kontrakter.felles.journalpost.TilgangsstyrtJournalpost
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertNotNull

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
    inner class FinnTilgangsstyrteBaksJournalposter {
        @Test
        fun `skal hente journalposter fra saf og deretter mappe om lista til tilgangsstyrte journalposter`() {
            // Arrange
            val journalposterForBrukerRequest = JournalposterForBrukerRequest(brukerId = Bruker("1", BrukerIdType.FNR), antall = 100, tema = listOf(Tema.BAR))
            val journalposter = listOf(mockk<Journalpost>())
            every { safRestClient.finnJournalposter(journalposterForBrukerRequest) } returns journalposter
            every { baksTilgangsstyrtJournalpostService.mapTilTilgangsstyrteJournalposter(journalposter) } returns listOf(mockk<TilgangsstyrtJournalpost>())

            // Act
            journalpostService.finnTilgangsstyrteBaksJournalposter(journalposterForBrukerRequest)

            // Assert
            verify(exactly = 1) { safRestClient.finnJournalposter(journalposterForBrukerRequest) }
            verify(exactly = 1) { baksTilgangsstyrtJournalpostService.mapTilTilgangsstyrteJournalposter(journalposter) }
        }
    }

    @Nested
    inner class HentTilgangsstyrtBaksDokument {
        @Test
        fun `skal hente dokument dersom dokument er digital søknad og saksbehandler har tilgang`() {
            // Arrange
            val journalpostId = "1234"
            val dokumentId = "5678"
            val variantFormat = Dokumentvariantformat.ARKIV
            val dokumentString = "TestDokument"
            val testDokument = dokumentString.toByteArray()

            every { safRestClient.hentJournalpost(journalpostId) } returns mockk()
            every { baksTilgangsstyrtJournalpostService.harTilgangTilJournalpost(any()) } returns true
            every { safHentDokumentRestClient.hentDokument(journalpostId, dokumentId, variantFormat.name) } returns testDokument

            // Act
            val dokument = journalpostService.hentTilgangsstyrtBaksDokument(journalpostId, dokumentId, variantFormat.name)

            // Assert
            assertNotNull(dokument)
            assertThat(dokument.decodeToString()).isEqualTo(dokumentString)
            verify(exactly = 1) { safRestClient.hentJournalpost(journalpostId) }
            verify(exactly = 1) { baksTilgangsstyrtJournalpostService.harTilgangTilJournalpost(any()) }
            verify(exactly = 1) { safHentDokumentRestClient.hentDokument(journalpostId, dokumentId, variantFormat.name) }
        }

        @Test
        fun `skal kaste JournalpostForbiddenException dersom dokument er digital søknad og saksbehandler ikke har tilgang`() {
            // Arrange
            val journalpostId = "1234"
            val dokumentId = "5678"
            val variantFormat = Dokumentvariantformat.ARKIV

            every { safRestClient.hentJournalpost(journalpostId) } returns mockk()
            every { baksTilgangsstyrtJournalpostService.harTilgangTilJournalpost(any()) } returns false

            // Act & Assert
            val dokument = assertThrows<JournalpostForbiddenException> { journalpostService.hentTilgangsstyrtBaksDokument(journalpostId, dokumentId, variantFormat.name) }

            assertNotNull(dokument)
            assertThat(dokument.message).isEqualTo("Kan ikke hente dokument. Krever ekstra tilganger.")
            verify(exactly = 1) { safRestClient.hentJournalpost(journalpostId) }
            verify(exactly = 1) { baksTilgangsstyrtJournalpostService.harTilgangTilJournalpost(any()) }
            verify(exactly = 0) { safHentDokumentRestClient.hentDokument(journalpostId, dokumentId, variantFormat.name) }
        }
    }

    @Nested
    inner class HentTilgangsstyrtBaksJournalpost {
        @Test
        fun `skal hente journalpost dersom journalpost inneholder digital søknad og saksbehandler har tilgang`() {
            // Arrange
            val journalpostId = "1234"

            every { safRestClient.hentJournalpost(journalpostId) } returns mockk()
            every { baksTilgangsstyrtJournalpostService.harTilgangTilJournalpost(any()) } returns true

            // Act
            val journalpost = journalpostService.hentTilgangsstyrtBaksJournalpost(journalpostId)

            // Assert
            assertNotNull(journalpost)
            verify(exactly = 1) { safRestClient.hentJournalpost(journalpostId) }
            verify(exactly = 1) { baksTilgangsstyrtJournalpostService.harTilgangTilJournalpost(any()) }
        }

        @Test
        fun `skal kaste JournalpostForbiddenException dersom journalpost inneholder digital søknad og saksbehandler ikke har tilgang`() {
            // Arrange
            val journalpostId = "1234"

            every { safRestClient.hentJournalpost(journalpostId) } returns mockk()
            every { baksTilgangsstyrtJournalpostService.harTilgangTilJournalpost(any()) } returns false

            // Act & Assert
            val journalpostForbiddenException = assertThrows<JournalpostForbiddenException> { journalpostService.hentTilgangsstyrtBaksJournalpost(journalpostId) }

            assertNotNull(journalpostForbiddenException)
            assertThat(journalpostForbiddenException.message).isEqualTo("Kan ikke hente journalpost. Krever ekstra tilganger.")
            verify(exactly = 1) { safRestClient.hentJournalpost(journalpostId) }
            verify(exactly = 1) { baksTilgangsstyrtJournalpostService.harTilgangTilJournalpost(any()) }
        }
    }
}
