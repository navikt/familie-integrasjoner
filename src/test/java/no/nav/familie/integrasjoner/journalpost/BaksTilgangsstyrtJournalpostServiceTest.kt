package no.nav.familie.integrasjoner.journalpost

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.baks.søknad.lagKontantstøtteSøknad
import no.nav.familie.integrasjoner.journalpost.versjonertsøknad.BaksVersjonertSøknadService
import no.nav.familie.integrasjoner.tilgangskontroll.TilgangskontrollService
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.journalpost.DokumentInfo
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.søknad.MissingVersionException
import no.nav.familie.kontrakter.felles.søknad.UnsupportedVersionException
import no.nav.familie.kontrakter.felles.tilgangskontroll.Tilgang
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.time.LocalDateTime

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
        val journalpost = mockk<Journalpost>()
        val dokumentInfo = mockk<DokumentInfo>()

        every { journalpost.journalpostId } returns "1"
        every { journalpost.tema } returns Tema.KON.name
        every { journalpost.datoMottatt } returns LocalDateTime.of(2024, 12, 12, 0, 0)
        every { journalpost.harDigitalSøknad(any()) } returns true
        every { journalpost.dokumenter } returns listOf(dokumentInfo)
        every { dokumentInfo.dokumentInfoId } returns "1"
        every { dokumentInfo.erSøknadForTema(any()) } returns true
        every { dokumentInfo.harOriginalVariant() } returns true

        val søkerFnr = "12345678910"
        val barnFnr = "12345678911"

        val kontantstøtteSøknad = lagKontantstøtteSøknad(søkerFnr, barnFnr)

        every { baksVersjonertSøknadService.hentBaksSøknadBase(any(), any()) } returns kontantstøtteSøknad
        every { tilgangskontrollService.sjekkTilgangTilBrukere(any(), any()) } returns listOf(Tilgang(søkerFnr, true), Tilgang(barnFnr, true))

        // Act
        val tilgangsstyrteJournalposter = baksTilgangsstyrtJournalpostService.mapTilTilgangsstyrteJournalposter(listOf(journalpost))

        // Assert
        assertThat(tilgangsstyrteJournalposter).hasSize(1)
        val tilgangsstyrtJournalpost = tilgangsstyrteJournalposter.find { it.journalpost.journalpostId == journalpost.journalpostId && it.journalpostTilgang.harTilgang }
        assertThat(tilgangsstyrtJournalpost).isNotNull
    }

    @Test
    fun `skal mappe om liste av Journalpost til liste av TilgangsstyrtJournalpost med harTilgang satt til false dersom digital søknad og saksbehandler ikke har tilgang til personer i søknad`() {
        // Arrange
        val journalpost = mockk<Journalpost>()
        val dokumentInfo = mockk<DokumentInfo>()

        every { journalpost.journalpostId } returns "1"
        every { journalpost.tema } returns Tema.KON.name
        every { journalpost.harDigitalSøknad(any()) } returns true
        every { journalpost.dokumenter } returns listOf(dokumentInfo)
        every { journalpost.datoMottatt } returns LocalDateTime.of(2024, 12, 12, 0, 0)
        every { dokumentInfo.dokumentInfoId } returns "1"
        every { dokumentInfo.erSøknadForTema(any()) } returns true
        every { dokumentInfo.harOriginalVariant() } returns true

        val søkerFnr = "12345678910"
        val barnFnr = "12345678911"

        val kontantstøtteSøknad = lagKontantstøtteSøknad(søkerFnr, barnFnr)

        every { baksVersjonertSøknadService.hentBaksSøknadBase(any(), any()) } returns kontantstøtteSøknad
        every { tilgangskontrollService.sjekkTilgangTilBrukere(any(), any()) } returns listOf(Tilgang(søkerFnr, false, "Bruker mangler rolle 'STRENGT_FORTROLIG_ADRESSE'"), Tilgang(barnFnr, false, "Bruker mangler rolle 'FORTROLIG_ADRESSE'"))

        // Act
        val tilgangsstyrteJournalposter = baksTilgangsstyrtJournalpostService.mapTilTilgangsstyrteJournalposter(listOf(journalpost))

        // Assert
        assertThat(tilgangsstyrteJournalposter).hasSize(1)
        val tilgangsstyrtJournalpost = tilgangsstyrteJournalposter.find { it.journalpost.journalpostId == journalpost.journalpostId && !it.journalpostTilgang.harTilgang }
        assertThat(tilgangsstyrtJournalpost).isNotNull
        assertThat(tilgangsstyrtJournalpost!!.journalpostTilgang.begrunnelse).isEqualTo("(Bruker mangler rolle 'STRENGT_FORTROLIG_ADRESSE', Bruker mangler rolle 'FORTROLIG_ADRESSE')")
    }

    @ParameterizedTest
    @EnumSource(Tema::class, names = ["BAR", "KON"])
    fun `skal mappe om liste av Journalpost til liste av TilgangsstyrtJournalpost med harTilgang satt til true uavhengig av tilgang dersom journalposten ikke inneholder original variant av søknad`(tema: Tema) {
        // Arrange
        val journalpost = mockk<Journalpost>()
        val dokumentInfo = mockk<DokumentInfo>()

        every { journalpost.journalpostId } returns "1"
        every { journalpost.tema } returns tema.name
        every { journalpost.harDigitalSøknad(any()) } returns true
        every { journalpost.dokumenter } returns listOf(dokumentInfo)
        every { journalpost.datoMottatt } returns LocalDateTime.of(2000, 12, 12, 0, 0)
        every { dokumentInfo.dokumentInfoId } returns "1"
        every { dokumentInfo.erSøknadForTema(any()) } returns true
        every { dokumentInfo.harOriginalVariant() } returns false

        val søkerFnr = "12345678910"
        val barnFnr = "12345678911"

        val kontantstøtteSøknad = lagKontantstøtteSøknad(søkerFnr, barnFnr)

        every { baksVersjonertSøknadService.hentBaksSøknadBase(any(), any()) } returns kontantstøtteSøknad
        every { tilgangskontrollService.sjekkTilgangTilBrukere(any(), any()) } returns listOf(Tilgang(søkerFnr, false), Tilgang(barnFnr, false))

        // Act
        val tilgangsstyrteJournalposter = baksTilgangsstyrtJournalpostService.mapTilTilgangsstyrteJournalposter(listOf(journalpost))

        // Assert
        assertThat(tilgangsstyrteJournalposter).hasSize(1)
        val tilgangsstyrtJournalpost = tilgangsstyrteJournalposter.find { it.journalpost.journalpostId == journalpost.journalpostId && it.journalpostTilgang.harTilgang }
        assertThat(tilgangsstyrtJournalpost).isNotNull
    }

    @Test
    fun `skal mappe om liste av Journalpost til liste av TilgangsstyrtJournalpost med harTilgang satt til true dersom ikke digital søknad`() {
        // Arrange
        val journalpost = mockk<Journalpost>()

        every { journalpost.journalpostId } returns "1"
        every { journalpost.tema } returns Tema.BAR.name
        every { journalpost.datoMottatt } returns LocalDateTime.of(2024, 12, 12, 0, 0)
        every { journalpost.harDigitalSøknad(any()) } returns false
        every { journalpost.dokumenter }

        // Act
        val tilgangsstyrteJournalposter = baksTilgangsstyrtJournalpostService.mapTilTilgangsstyrteJournalposter(listOf(journalpost))

        // Assert
        assertThat(tilgangsstyrteJournalposter).hasSize(1)
        val tilgangsstyrtJournalpost = tilgangsstyrteJournalposter.find { it.journalpost.journalpostId == journalpost.journalpostId && it.journalpostTilgang.harTilgang }
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
        val tilgangsstyrtJournalpost = tilgangsstyrteJournalposter.find { it.journalpost.journalpostId == journalpost.journalpostId && it.journalpostTilgang.harTilgang }
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
        every { journalpost.datoMottatt } returns LocalDateTime.of(2024, 12, 12, 0, 0)
        every { journalpost.dokumenter } returns listOf(dokumentInfo)
        every { dokumentInfo.erSøknadForTema(any()) } returns true
        every { dokumentInfo.harOriginalVariant() } returns true
        every { baksVersjonertSøknadService.hentBaksSøknadBase(any(), any()) } throws MissingVersionException("JSON-String inneholder ikke feltet 'kontraktVersjon'")
        // Act
        val tilgangsstyrteJournalposter = baksTilgangsstyrtJournalpostService.mapTilTilgangsstyrteJournalposter(listOf(journalpost))

        // Assert
        assertThat(tilgangsstyrteJournalposter).hasSize(1)
        val tilgangsstyrtJournalpost = tilgangsstyrteJournalposter.find { it.journalpost.journalpostId == journalpost.journalpostId && it.journalpostTilgang.harTilgang }
        assertThat(tilgangsstyrtJournalpost).isNotNull
    }

    @Test
    fun `skal mappe om liste av Journalpost til liste av TilgangsstyrtJournalpost med harTilgang satt til false dersom digital søknad og feilen MissingVersionImplementationException kastes ved deserialisering`() {
        // Arrange
        val journalpost = mockk<Journalpost>()
        val dokumentInfo = mockk<DokumentInfo>()

        every { journalpost.journalpostId } returns "1"
        every { journalpost.tema } returns Tema.KON.name
        every { journalpost.datoMottatt } returns LocalDateTime.of(2024, 12, 12, 0, 0)
        every { journalpost.harDigitalSøknad(any()) } returns true
        every { journalpost.dokumenter } returns listOf(dokumentInfo)
        every { dokumentInfo.erSøknadForTema(any()) } returns true
        every { dokumentInfo.harOriginalVariant() } returns true

        every { baksVersjonertSøknadService.hentBaksSøknadBase(any(), any()) } throws UnsupportedVersionException("Har ikke implementert støtte for deserialisering av søknadsversjonen")
        // Act
        val tilgangsstyrteJournalposter = baksTilgangsstyrtJournalpostService.mapTilTilgangsstyrteJournalposter(listOf(journalpost))

        // Assert
        assertThat(tilgangsstyrteJournalposter).hasSize(1)
        val tilgangsstyrtJournalpost = tilgangsstyrteJournalposter.find { it.journalpost.journalpostId == journalpost.journalpostId && !it.journalpostTilgang.harTilgang }
        assertThat(tilgangsstyrtJournalpost).isNotNull
    }

    @ParameterizedTest
    @EnumSource(Tema::class, names = ["BAR", "KON"], mode = EnumSource.Mode.INCLUDE)
    fun `skal mappe om liste av Journalpost til liste av TilgangsstyrtJournalpost med harTilgang satt til true dersom digital søknad og det ikke finnes noen ORIGINAL variant av søknaden`(tema: Tema) {
        // Arrange
        val journalpost = mockk<Journalpost>()
        val dokumentInfo = mockk<DokumentInfo>()

        every { journalpost.journalpostId } returns "1"
        every { journalpost.tema } returns tema.name
        every { journalpost.harDigitalSøknad(tema) } returns true
        every { journalpost.datoMottatt } returns LocalDateTime.of(2024, 12, 12, 0, 0)
        every { journalpost.dokumenter } returns listOf(dokumentInfo)
        every { dokumentInfo.harOriginalVariant() } returns false
        every { dokumentInfo.erSøknadForTema(tema = tema) } returns true

        // Act
        val tilgangsstyrteJournalposter = baksTilgangsstyrtJournalpostService.mapTilTilgangsstyrteJournalposter(listOf(journalpost))

        // Assert
        assertThat(tilgangsstyrteJournalposter).hasSize(1)
        val tilgangsstyrtJournalpost = tilgangsstyrteJournalposter.find { it.journalpost.journalpostId == journalpost.journalpostId && it.journalpostTilgang.harTilgang }
        assertThat(tilgangsstyrtJournalpost).isNotNull
    }
}
