package no.nav.familie.integrasjoner.journalpost

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.baks.søknad.BaksVersjonertSøknadService
import no.nav.familie.integrasjoner.baks.søknad.lagKontantstøtteSøknad
import no.nav.familie.integrasjoner.tilgangskontroll.TilgangskontrollService
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.journalpost.DokumentInfo
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.søknad.MissingVersionException
import no.nav.familie.kontrakter.felles.søknad.UnsupportedVersionException
import no.nav.familie.kontrakter.felles.tilgangskontroll.Tilgang
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BaksTilgangsstyrtJournalpostServiceTest {
    private val baksVersjonertSøknadService: BaksVersjonertSøknadService = mockk()
    private val tilgangskontrollService: TilgangskontrollService = mockk()

    private val baksTilgangsstyrtJournalpostService: BaksTilgangsstyrtJournalpostService =
        BaksTilgangsstyrtJournalpostService(
            baksVersjonertSøknadService = baksVersjonertSøknadService,
            tilgangskontrollService = tilgangskontrollService,
        )

    @Test
    fun `skal mappe om liste av Journalpost til liste av TilgangsstyrtJournalpost med harTilgang satt til true dersom digital søknad og saksbehandler har tilgang til personer i søknad`() {
        // Arrange
        val journalpost1 = mockk<Journalpost>()
        val dokumentInfo = mockk<DokumentInfo>()

        every { journalpost1.journalpostId } returns "1"
        every { journalpost1.tema } returns Tema.KON.name
        every { journalpost1.harDigitalSøknad(any()) } returns true
        every { journalpost1.dokumenter } returns listOf(dokumentInfo)
        every { dokumentInfo.dokumentInfoId } returns "1"
        every { dokumentInfo.erDigitalSøknad(any()) } returns true

        val søkerFnr = "12345678910"
        val barnFnr = "12345678911"

        val kontantstøtteSøknad = lagKontantstøtteSøknad(søkerFnr, barnFnr)

        every { baksVersjonertSøknadService.hentBaksSøknadBase(any(), any()) } returns kontantstøtteSøknad
        every { tilgangskontrollService.sjekkTilgangTilBrukere(any(), any()) } returns listOf(Tilgang(søkerFnr, true), Tilgang(barnFnr, true))

        // Act
        val tilgangsstyrteJournalposter = baksTilgangsstyrtJournalpostService.mapTilTilgangsstyrteJournalposter(listOf(journalpost1))

        // Assert
        assertThat(tilgangsstyrteJournalposter).hasSize(1)
        val tilgangsstyrtJournalpost = tilgangsstyrteJournalposter.find { it.journalpost.journalpostId == journalpost1.journalpostId && it.harTilgang }
        assertThat(tilgangsstyrtJournalpost).isNotNull
    }

    @Test
    fun `skal mappe om liste av Journalpost til liste av TilgangsstyrtJournalpost med harTilgang satt til false dersom digital søknad og saksbehandler ikke har tilgang til personer i søknad`() {
        // Arrange
        val journalpost1 = mockk<Journalpost>()
        val dokumentInfo = mockk<DokumentInfo>()

        every { journalpost1.journalpostId } returns "1"
        every { journalpost1.tema } returns Tema.KON.name
        every { journalpost1.harDigitalSøknad(any()) } returns true
        every { journalpost1.dokumenter } returns listOf(dokumentInfo)
        every { dokumentInfo.dokumentInfoId } returns "1"
        every { dokumentInfo.erDigitalSøknad(any()) } returns true

        val søkerFnr = "12345678910"
        val barnFnr = "12345678911"

        val kontantstøtteSøknad = lagKontantstøtteSøknad(søkerFnr, barnFnr)

        every { baksVersjonertSøknadService.hentBaksSøknadBase(any(), any()) } returns kontantstøtteSøknad
        every { tilgangskontrollService.sjekkTilgangTilBrukere(any(), any()) } returns listOf(Tilgang(søkerFnr, false), Tilgang(barnFnr, false))

        // Act
        val tilgangsstyrteJournalposter = baksTilgangsstyrtJournalpostService.mapTilTilgangsstyrteJournalposter(listOf(journalpost1))

        // Assert
        assertThat(tilgangsstyrteJournalposter).hasSize(1)
        val tilgangsstyrtJournalpost = tilgangsstyrteJournalposter.find { it.journalpost.journalpostId == journalpost1.journalpostId && !it.harTilgang }
        assertThat(tilgangsstyrtJournalpost).isNotNull
    }

    @Test
    fun `skal mappe om liste av Journalpost til liste av TilgangsstyrtJournalpost med harTilgang satt til true dersom ikke digital søknad`() {
        // Arrange
        val journalpost = mockk<Journalpost>()

        every { journalpost.journalpostId } returns "1"
        every { journalpost.tema } returns Tema.BAR.name
        every { journalpost.harDigitalSøknad(any()) } returns false

        // Act
        val tilgangsstyrteJournalposter = baksTilgangsstyrtJournalpostService.mapTilTilgangsstyrteJournalposter(listOf(journalpost))

        // Assert
        assertThat(tilgangsstyrteJournalposter).hasSize(1)
        val tilgangsstyrtJournalpost = tilgangsstyrteJournalposter.find { it.journalpost.journalpostId == journalpost.journalpostId && it.harTilgang }
        assertThat(tilgangsstyrtJournalpost).isNotNull
    }

    @Test
    fun `skal mappe om liste av Journalpost til liste av TilgangsstyrtJournalpost med harTilgang satt til true dersom tema ikke er satt`() {
        // Arrange
        val journalpost = mockk<Journalpost>()

        every { journalpost.journalpostId } returns "1"
        every { journalpost.tema } returns null

        // Act
        val tilgangsstyrteJournalposter = baksTilgangsstyrtJournalpostService.mapTilTilgangsstyrteJournalposter(listOf(journalpost))

        // Assert
        assertThat(tilgangsstyrteJournalposter).hasSize(1)
        val tilgangsstyrtJournalpost = tilgangsstyrteJournalposter.find { it.journalpost.journalpostId == journalpost.journalpostId && it.harTilgang }
        assertThat(tilgangsstyrtJournalpost).isNotNull
    }

    @Test
    fun `skal mappe om liste av Journalpost til liste av TilgangsstyrtJournalpost med harTilgang satt til true dersom digital søknad og feilen UnsupportedVersionException kastes ved deserialisering`() {
        // Arrange
        val journalpost = mockk<Journalpost>()
        val dokumentInfo = mockk<DokumentInfo>()

        every { journalpost.journalpostId } returns "1"
        every { journalpost.tema } returns Tema.KON.name
        every { journalpost.harDigitalSøknad(any()) } returns true
        every { journalpost.dokumenter } returns listOf(dokumentInfo)

        every { baksVersjonertSøknadService.hentBaksSøknadBase(any(), any()) } throws MissingVersionException("JSON-String inneholder ikke feltet 'kontraktVersjon'")
        // Act
        val tilgangsstyrteJournalposter = baksTilgangsstyrtJournalpostService.mapTilTilgangsstyrteJournalposter(listOf(journalpost))

        // Assert
        assertThat(tilgangsstyrteJournalposter).hasSize(1)
        val tilgangsstyrtJournalpost = tilgangsstyrteJournalposter.find { it.journalpost.journalpostId == journalpost.journalpostId && it.harTilgang }
        assertThat(tilgangsstyrtJournalpost).isNotNull
    }

    @Test
    fun `skal mappe om liste av Journalpost til liste av TilgangsstyrtJournalpost med harTilgang satt til false dersom digital søknad og feilen MissingVersionImplementationException kastes ved deserialisering`() {
        // Arrange
        val journalpost = mockk<Journalpost>()
        val dokumentInfo = mockk<DokumentInfo>()

        every { journalpost.journalpostId } returns "1"
        every { journalpost.tema } returns Tema.KON.name
        every { journalpost.harDigitalSøknad(any()) } returns true
        every { journalpost.dokumenter } returns listOf(dokumentInfo)

        every { baksVersjonertSøknadService.hentBaksSøknadBase(any(), any()) } throws UnsupportedVersionException("Har ikke implementert støtte for deserialisering av søknadsversjonen")
        // Act
        val tilgangsstyrteJournalposter = baksTilgangsstyrtJournalpostService.mapTilTilgangsstyrteJournalposter(listOf(journalpost))

        // Assert
        assertThat(tilgangsstyrteJournalposter).hasSize(1)
        val tilgangsstyrtJournalpost = tilgangsstyrteJournalposter.find { it.journalpost.journalpostId == journalpost.journalpostId && !it.harTilgang }
        assertThat(tilgangsstyrtJournalpost).isNotNull
    }
}