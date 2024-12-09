package no.nav.familie.integrasjoner.baks.søknad

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.client.rest.SafHentDokumentRestClient
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.journalpost.DokumentInfo
import no.nav.familie.kontrakter.felles.journalpost.Dokumentvariantformat
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BaksVersjonertSøknadServiceTest {
    private val safHentDokumentRestClient: SafHentDokumentRestClient = mockk()
    private val baksVersjonertSøknadService: BaksVersjonertSøknadService =
        BaksVersjonertSøknadService(
            safHentDokumentRestClient = safHentDokumentRestClient,
        )

    @Nested
    inner class HentBaksSøknadBase {
        @Test
        fun `skal hente journalpost og deserialisere tilknyttet KontantstøtteSøknad når tema er KON`() {
            // Arrange
            val journalpost = mockk<Journalpost>()
            val dokumentInfo = mockk<DokumentInfo>()

            every { journalpost.journalpostId } returns "1"
            every { journalpost.dokumenter } returns listOf(dokumentInfo)
            every { dokumentInfo.dokumentInfoId } returns "1"
            every { dokumentInfo.erSøknadForTema(any()) } returns true

            val søkerFnr = "12345678910"
            val barnFnr = "12345678911"

            val kontantstøtteSøknad = lagKontantstøtteSøknad(søkerFnr = søkerFnr, barnFnr = barnFnr)

            every { safHentDokumentRestClient.hentDokument("1", "1", Dokumentvariantformat.ORIGINAL.name) } returns objectMapper.writeValueAsBytes(kontantstøtteSøknad)

            // Act
            val baksSøknadBase = baksVersjonertSøknadService.hentBaksSøknadBase(journalpost, Tema.KON)

            // Assert
            assertThat(baksSøknadBase).isNotNull
            assertThat(baksSøknadBase.kontraktVersjon).isEqualTo(5)
            assertThat(baksSøknadBase.personerISøknad()).isEqualTo(listOf(søkerFnr, barnFnr))
        }

        @Test
        fun `skal hente journalpost og deserialisere tilknyttet BarnetrygdSøknad når tema er BAR`() {
            // Arrange
            val journalpost = mockk<Journalpost>()
            val dokumentInfo = mockk<DokumentInfo>()

            every { journalpost.journalpostId } returns "1"
            every { journalpost.dokumenter } returns listOf(dokumentInfo)
            every { dokumentInfo.dokumentInfoId } returns "1"
            every { dokumentInfo.erSøknadForTema(any()) } returns true

            val søkerFnr = "12345678910"
            val barnFnr = "12345678911"

            val barnetrygdSøknad = lagBarnetrygdSøknad(søkerFnr = søkerFnr, barnFnr = barnFnr)

            every { safHentDokumentRestClient.hentDokument("1", "1", Dokumentvariantformat.ORIGINAL.name) } returns objectMapper.writeValueAsBytes(barnetrygdSøknad)

            // Act
            val baksSøknadBase = baksVersjonertSøknadService.hentBaksSøknadBase(journalpost, Tema.BAR)

            // Assert
            assertThat(baksSøknadBase).isNotNull
            assertThat(baksSøknadBase.kontraktVersjon).isEqualTo(9)
            assertThat(baksSøknadBase.personerISøknad()).isEqualTo(listOf(søkerFnr, barnFnr))
        }

        @Test
        fun `skal kaste feil dersom tema ikke er BAR eller KON`() {
            // Act & Assert
            val exception = assertThrows<IllegalArgumentException> { baksVersjonertSøknadService.hentBaksSøknadBase(mockk(), Tema.ENF) }
            assertThat(exception.message).isEqualTo("Støtter ikke deserialisering av søknad for tema ${Tema.ENF.name}")
        }
    }
}
