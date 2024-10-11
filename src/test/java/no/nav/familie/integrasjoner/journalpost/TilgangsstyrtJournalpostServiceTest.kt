package no.nav.familie.integrasjoner.journalpost

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.mottak.BaksMottakService
import no.nav.familie.integrasjoner.tilgangskontroll.TilgangskontrollService
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.tilgangskontroll.Tilgang
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class TilgangsstyrtJournalpostServiceTest {
    private val baksMottakService: BaksMottakService = mockk()

    private val tilgangskontrollService: TilgangskontrollService = mockk()

    private val tilgangsstyrtJournalpostService: TilgangsstyrtJournalpostService =
        TilgangsstyrtJournalpostService(
            baksMottakService = baksMottakService,
            tilgangskontrollService = tilgangskontrollService,
        )

    @Test
    fun `skal mappe om liste av Journalpost til liste av TilgangsstyrtJournalpost med harTilgang satt til true dersom digital søknad og saksbehandler har tilgang til personer i søknad`() {
        // Arrange
        val journalpost1 = mockk<Journalpost>()

        every { journalpost1.journalpostId } returns "1"
        every { journalpost1.tema } returns Tema.BAR.name
        every { journalpost1.erDigitalSøknad(any()) } returns true

        every { baksMottakService.hentPersonerIDigitalSøknad(Tema.BAR, any()) } returns listOf("123", "456")
        every { tilgangskontrollService.sjekkTilgangTilBrukere(any(), any()) } returns listOf(Tilgang("123", true), Tilgang("456", true))

        // Act
        val tilgangsstyrteJournalposter = tilgangsstyrtJournalpostService.tilTilgangstyrteJournalposter(listOf(journalpost1))

        // Assert
        assertThat(tilgangsstyrteJournalposter).hasSize(1)
        val tilgangsstyrtJournalpost = tilgangsstyrteJournalposter.find { it.journalpost.journalpostId == journalpost1.journalpostId && it.harTilgang }
        assertNotNull(tilgangsstyrtJournalpost)
    }

    @Test
    fun `skal mappe om liste av Journalpost til liste av TilgangsstyrtJournalpost med harTilgang satt til false dersom digital søknad og saksbehandler ikke har tilgang til personer i søknad`() {
        // Arrange
        val journalpost1 = mockk<Journalpost>()

        every { journalpost1.journalpostId } returns "1"
        every { journalpost1.tema } returns Tema.BAR.name
        every { journalpost1.erDigitalSøknad(any()) } returns true

        every { baksMottakService.hentPersonerIDigitalSøknad(Tema.BAR, any()) } returns listOf("123", "456")
        every { tilgangskontrollService.sjekkTilgangTilBrukere(any(), any()) } returns listOf(Tilgang("123", false), Tilgang("456", false))

        // Act
        val tilgangsstyrteJournalposter = tilgangsstyrtJournalpostService.tilTilgangstyrteJournalposter(listOf(journalpost1))

        // Assert
        assertThat(tilgangsstyrteJournalposter).hasSize(1)
        val tilgangsstyrtJournalpost = tilgangsstyrteJournalposter.find { it.journalpost.journalpostId == journalpost1.journalpostId && !it.harTilgang }
        assertNotNull(tilgangsstyrtJournalpost)
    }

    @Test
    fun `skal mappe om liste av Journalpost til liste av TilgangsstyrtJournalpost med harTilgang satt til true dersom ikke digital søknad`() {
        // Arrange
        val journalpost1 = mockk<Journalpost>()

        every { journalpost1.journalpostId } returns "1"
        every { journalpost1.tema } returns Tema.BAR.name
        every { journalpost1.erDigitalSøknad(any()) } returns false

        // Act
        val tilgangsstyrteJournalposter = tilgangsstyrtJournalpostService.tilTilgangstyrteJournalposter(listOf(journalpost1))

        // Assert
        assertThat(tilgangsstyrteJournalposter).hasSize(1)
        val tilgangsstyrtJournalpost = tilgangsstyrteJournalposter.find { it.journalpost.journalpostId == journalpost1.journalpostId && it.harTilgang }
        assertNotNull(tilgangsstyrtJournalpost)
    }

    @Test
    fun `skal mappe om liste av Journalpost til liste av TilgangsstyrtJournalpost med harTilgang satt til true dersom tema ikke er satt`() {
        // Arrange
        val journalpost1 = mockk<Journalpost>()

        every { journalpost1.journalpostId } returns "1"
        every { journalpost1.tema } returns null

        // Act
        val tilgangsstyrteJournalposter = tilgangsstyrtJournalpostService.tilTilgangstyrteJournalposter(listOf(journalpost1))

        // Assert
        assertThat(tilgangsstyrteJournalposter).hasSize(1)
        val tilgangsstyrtJournalpost = tilgangsstyrteJournalposter.find { it.journalpost.journalpostId == journalpost1.journalpostId && it.harTilgang }
        assertNotNull(tilgangsstyrtJournalpost)
    }
}
