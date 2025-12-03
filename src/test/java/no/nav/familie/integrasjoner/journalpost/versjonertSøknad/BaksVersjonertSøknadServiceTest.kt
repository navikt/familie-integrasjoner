package no.nav.familie.integrasjoner.journalpost.versjonertSøknad

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.baks.søknad.lagBarnetrygdSøknad
import no.nav.familie.integrasjoner.baks.søknad.lagKontantstøtteSøknad
import no.nav.familie.integrasjoner.client.rest.SafHentDokumentRestClient
import no.nav.familie.integrasjoner.client.rest.SafRestClient
import no.nav.familie.integrasjoner.journalpost.JournalpostNotFoundException
import no.nav.familie.integrasjoner.journalpost.versjonertsøknad.BaksVersjonertSøknadService
import no.nav.familie.kontrakter.felles.Brevkoder
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.journalpost.DokumentInfo
import no.nav.familie.kontrakter.felles.journalpost.Dokumentvariantformat
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.Journalposttype
import no.nav.familie.kontrakter.felles.journalpost.Journalstatus
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BaksVersjonertSøknadServiceTest {
    private val safHentDokumentRestClient: SafHentDokumentRestClient = mockk()
    private val safRestClient: SafRestClient = mockk()
    private val baksVersjonertSøknadService: BaksVersjonertSøknadService =
        BaksVersjonertSøknadService(
            safHentDokumentRestClient = safHentDokumentRestClient,
            safRestClient = safRestClient,
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
            assertThat(baksSøknadBase.kontraktVersjon).isEqualTo(6)
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

    @Nested
    inner class HentVersjonertSøknad {
        @Test
        fun `skal hente journalpost og deserialisere tilknyttet KontantstøtteSøknad når tema er KON`() {
            // Arrange
            val dokumentInfo = DokumentInfo(dokumentInfoId = "321", brevkode = Brevkoder.KONTANTSTØTTE_SØKNAD)
            val journalpost = Journalpost(journalpostId = "123", journalposttype = Journalposttype.I, journalstatus = Journalstatus.FERDIGSTILT, dokumenter = listOf(dokumentInfo))

            val søkerFnr = "12345678910"
            val barnFnr = "12345678911"
            val kontantstøtteSøknad = lagKontantstøtteSøknad(søkerFnr = søkerFnr, barnFnr = barnFnr)

            every { safRestClient.hentJournalpost(journalpost.journalpostId) } returns journalpost
            every { safHentDokumentRestClient.hentDokument(journalpost.journalpostId, dokumentInfo.dokumentInfoId, Dokumentvariantformat.ORIGINAL.name) } returns objectMapper.writeValueAsBytes(kontantstøtteSøknad)

            // Act
            val versjonertKontantstøtteSøknad = baksVersjonertSøknadService.hentVersjonertKontantstøtteSøknad(journalpost.journalpostId)

            // Assert
            assertThat(versjonertKontantstøtteSøknad).isNotNull
            assertThat(versjonertKontantstøtteSøknad.kontantstøtteSøknad.personerISøknad()).isEqualTo(listOf(søkerFnr, barnFnr))
        }

        @Test
        fun `skal hente journalpost og deserialisere tilknyttet BarnetrygdSøknad når tema er BAR`() {
            // Arrange
            val dokumentInfo = DokumentInfo(dokumentInfoId = "321", brevkode = Brevkoder.BARNETRYGD_ORDINÆR_SØKNAD)
            val journalpost = Journalpost(journalpostId = "123", journalposttype = Journalposttype.I, journalstatus = Journalstatus.FERDIGSTILT, dokumenter = listOf(dokumentInfo))

            val søkerFnr = "12345678910"
            val barnFnr = "12345678911"
            val barnetrygdSøknad = lagBarnetrygdSøknad(søkerFnr = søkerFnr, barnFnr = barnFnr)

            every { safRestClient.hentJournalpost(journalpost.journalpostId) } returns journalpost
            every { safHentDokumentRestClient.hentDokument(journalpost.journalpostId, dokumentInfo.dokumentInfoId, Dokumentvariantformat.ORIGINAL.name) } returns objectMapper.writeValueAsBytes(barnetrygdSøknad)

            // Act
            val versjonertBarnetrygdSøknad = baksVersjonertSøknadService.hentVersjonertBarnetrygdSøknad(journalpost.journalpostId)

            // Assert
            assertThat(versjonertBarnetrygdSøknad).isNotNull
            assertThat(versjonertBarnetrygdSøknad.barnetrygdSøknad.personerISøknad()).isEqualTo(listOf(søkerFnr, barnFnr))
        }

        @Test
        fun `skal kaste JournalpostNotFoundException hvis dokumenter ikke finnes`() {
            // Arrange
            val journalpost = Journalpost(journalpostId = "123", journalposttype = Journalposttype.I, journalstatus = Journalstatus.FERDIGSTILT, dokumenter = emptyList())

            every { safRestClient.hentJournalpost(journalpost.journalpostId) } returns journalpost

            // Act & Assert
            val exception =
                assertThrows<JournalpostNotFoundException> {
                    baksVersjonertSøknadService.hentVersjonertKontantstøtteSøknad(journalpost.journalpostId)
                }
            assertThat(exception.message).contains("Fant ikke dokumenter for tema")
        }
    }
}
