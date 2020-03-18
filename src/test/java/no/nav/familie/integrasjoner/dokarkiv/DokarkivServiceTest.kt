package no.nav.familie.integrasjoner.dokarkiv

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.integrasjoner.client.rest.DokarkivRestClient
import no.nav.familie.integrasjoner.dokarkiv.api.ArkiverDokumentRequest
import no.nav.familie.integrasjoner.dokarkiv.api.Dokument
import no.nav.familie.integrasjoner.dokarkiv.api.FilType
import no.nav.familie.integrasjoner.dokarkiv.api.TilknyttFagsakRequest
import no.nav.familie.integrasjoner.dokarkiv.client.DokarkivClient
import no.nav.familie.integrasjoner.dokarkiv.client.domene.*
import no.nav.familie.integrasjoner.dokarkiv.metadata.BarnetrygdVedtakMetadata
import no.nav.familie.integrasjoner.dokarkiv.metadata.DokarkivMetadata
import no.nav.familie.integrasjoner.dokarkiv.metadata.KontanstøtteSøknadMetadata
import no.nav.familie.integrasjoner.dokarkiv.metadata.KontanstøtteSøknadVedleggMetadata
import no.nav.familie.integrasjoner.personopplysning.PersonopplysningerService
import no.nav.familie.integrasjoner.personopplysning.domene.PersonIdent
import no.nav.familie.integrasjoner.personopplysning.domene.Personinfo
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class DokarkivServiceTest {

    private val navn = "Navn Navnesen"

    @MockK
    lateinit var dokarkivClient: DokarkivClient

    @MockK
    lateinit var dokarkivRestClient: DokarkivRestClient

    @MockK
    lateinit var personopplysningerService: PersonopplysningerService

    private lateinit var dokarkivService: DokarkivService

    @Before fun setUp() {
        MockKAnnotations.init(this)
        dokarkivService = DokarkivService(dokarkivClient,
                                          dokarkivRestClient,
                                          personopplysningerService,
                                          DokarkivMetadata(KontanstøtteSøknadMetadata,
                                                           KontanstøtteSøknadVedleggMetadata,
                                                           BarnetrygdVedtakMetadata))
    }

    @Test fun `oppdaterJournalpost skal konverte TilKnyttFagsakRequest til OppdaterJournalpostRequest og legge til sakstype`() {
        val slot = slot<OppdaterJournalpostRequest>()
        every { dokarkivRestClient.oppdaterJournalpost(capture(slot), any()) }
            .answers { OppdaterJournalpostResponse(JOURNALPOST_ID) }

        val idType = no.nav.familie.integrasjoner.dokarkiv.api.IdType.FNR
        val sak = no.nav.familie.integrasjoner.dokarkiv.api.Sak("11111111", "fagsaksystem")
        val bruker = no.nav.familie.integrasjoner.dokarkiv.api.Bruker(idType, "12345678910")
        val dto = TilknyttFagsakRequest(bruker, "tema", sak)

        dokarkivService.oppdaterJournalpost(dto, JOURNALPOST_ID)

        val request = slot.captured
        verify {
            dokarkivRestClient.oppdaterJournalpost(slot.captured, JOURNALPOST_ID)
        }
        assertOppdaterJournalpostRequest(request, dto)
    }

    @Test fun `skal mappe request til opprettJournalpostRequest av type arkiv pdfa`() {
        val slot = slot<OpprettJournalpostRequest>()
        every { dokarkivRestClient.lagJournalpost(capture(slot), any()) }
            .answers { OpprettJournalpostResponse() }

        every { personopplysningerService.hentPersoninfoFor(FNR) }
                .answers { Personinfo.Builder().medPersonIdent(PERSON_IDENT)
                                    .medFødselsdato(LocalDate.now())
                                    .medNavn(navn)
                                    .build() }
        val dto = ArkiverDokumentRequest(FNR,
                                         false,
                                         listOf(Dokument(PDF_DOK, FilType.PDFA, FILNAVN, null, "KONTANTSTØTTE_SØKNAD")))

        dokarkivService.lagJournalpostV2(dto)

        val request = slot.captured
        verify {
            dokarkivRestClient.lagJournalpost(slot.captured, false)
        }
        assertOpprettJournalpostRequest(request, "PDFA", PDF_DOK, ARKIV_VARIANTFORMAT)
    }

    @Test fun `skal mappe request til opprettJournalpostRequest for barnetrygd vedtak`() {
        val slot = slot<OpprettJournalpostRequest>()

        every { dokarkivRestClient.lagJournalpost(capture(slot), any()) }
                .answers { OpprettJournalpostResponse() }

        every { personopplysningerService.hentPersoninfoFor(FNR) }
                .answers { Personinfo.Builder().medPersonIdent(PERSON_IDENT)
                .medFødselsdato(LocalDate.now())
                .medNavn(navn)
                .build() }

        val dto = ArkiverDokumentRequest(FNR,
                                         false,
                                         listOf(Dokument(PDF_DOK, FilType.PDFA, FILNAVN, null, "BARNETRYGD_VEDTAK")),
                                         fagsakId = FAGSAK_ID)

        dokarkivService.lagJournalpostV2(dto)

        val request = slot.captured
        assertOpprettBarnetrygdVedtakJournalpostRequest(request,
                                                        PDF_DOK,
                                                        Sak(fagsakId = FAGSAK_ID, fagsaksystem = "BA", sakstype = "FAGSAK"))
    }

    @Test fun `skal mappe request til opprettJournalpostRequest av type ORIGINAL JSON`() {
        val slot = slot<OpprettJournalpostRequest>()
        every { dokarkivRestClient.lagJournalpost(capture(slot), any()) }
            .answers { OpprettJournalpostResponse() }
        every { personopplysningerService.hentPersoninfoFor(FNR) }
            .answers { Personinfo.Builder().medPersonIdent(PERSON_IDENT)
                .medFødselsdato(LocalDate.now())
                .medNavn(navn)
                .build() }

        val dto = ArkiverDokumentRequest(FNR,
                                         false,
                                         listOf(Dokument(JSON_DOK, FilType.JSON, FILNAVN, null, "KONTANTSTØTTE_SØKNAD")))

        dokarkivService.lagJournalpostV2(dto)

        val request = slot.captured

        verify {
            dokarkivRestClient.lagJournalpost(request, false)
        }

        assertOpprettJournalpostRequest(request,
                                        "JSON",
                                        JSON_DOK,
                                        STRUKTURERT_VARIANTFORMAT)
    }

    @Test fun `response fra klient skal returnere arkiverDokumentResponse`() {
        every { dokarkivRestClient.lagJournalpost(any(), any()) }
            .answers { OpprettJournalpostResponse(journalpostId = JOURNALPOST_ID, journalpostferdigstilt = true) }
        every { personopplysningerService.hentPersoninfoFor(FNR) }
            .answers { Personinfo.Builder().medPersonIdent(PERSON_IDENT)
                .medFødselsdato(LocalDate.now())
                .medNavn(navn)
                .build() }

        val dto = ArkiverDokumentRequest(FNR,
                                         false,
                                         listOf(Dokument(JSON_DOK, FilType.JSON, FILNAVN, null, "KONTANTSTØTTE_SØKNAD")))

        val arkiverDokumentResponse = dokarkivService.lagJournalpostV2(dto)

        Assertions.assertThat(arkiverDokumentResponse.journalpostId).isEqualTo(JOURNALPOST_ID)
        Assertions.assertThat(arkiverDokumentResponse.ferdigstilt).isTrue()
    }

    @Test fun `skal kaste exception hvis navn er null`() {
        every { personopplysningerService.hentPersoninfoFor(FNR) }
            .answers { null }

        val dto = ArkiverDokumentRequest(FNR,
                                         false,
                                         listOf(Dokument(PDF_DOK, FilType.PDFA, FILNAVN, null, "KONTANTSTØTTE_SØKNAD")))

        val thrown = Assertions.catchThrowable { dokarkivService.lagJournalpostV2(dto) }

        Assertions.assertThat(thrown).isInstanceOf(RuntimeException::class.java)
                .withFailMessage("Kan ikke hente navn")
    }

    private fun assertOppdaterJournalpostRequest(request: OppdaterJournalpostRequest, dto: TilknyttFagsakRequest) {
        Assertions.assertThat(request.bruker!!.id).isEqualTo(dto.bruker.id)
        Assertions.assertThat(request.bruker!!.idType.name).isEqualTo(dto.bruker.idType.name)
        Assertions.assertThat(request.sak!!.fagsakId).isEqualTo(dto.sak.fagsakId)
        Assertions.assertThat(request.sak!!.sakstype).isEqualTo("FAGSAK")
        Assertions.assertThat(request.tema).isEqualTo(dto.tema)
    }

    private fun assertOpprettJournalpostRequest(request: OpprettJournalpostRequest,
                                                pdfa: String,
                                                pdfDok: ByteArray,
                                                arkivVariantformat: String,
                                                sak: Sak? = null) {
        Assertions.assertThat(request.avsenderMottaker!!.id).isEqualTo(FNR)
        Assertions.assertThat(request.avsenderMottaker!!.idType).isEqualTo(IdType.FNR)
        Assertions.assertThat(request.bruker!!.id).isEqualTo(FNR)
        Assertions.assertThat(request.bruker!!.idType).isEqualTo(IdType.FNR)
        Assertions.assertThat(request.behandlingstema).isEqualTo(KontanstøtteSøknadMetadata.behandlingstema)
        Assertions.assertThat(request.journalpostType).isEqualTo(JournalpostType.INNGAAENDE)
        Assertions.assertThat(request.kanal).isEqualTo(KontanstøtteSøknadMetadata.kanal)
        Assertions.assertThat(request.tema).isEqualTo(KontanstøtteSøknadMetadata.tema)
        Assertions.assertThat(request.sak).isNull()
        Assertions.assertThat(request.dokumenter[0].tittel).isEqualTo(KontanstøtteSøknadMetadata.tittel)
        Assertions.assertThat(request.dokumenter[0].brevkode).isEqualTo(KontanstøtteSøknadMetadata.brevkode)
        Assertions.assertThat(request.dokumenter[0].dokumentKategori).isEqualTo(KontanstøtteSøknadMetadata.dokumentKategori)
        Assertions.assertThat(request.dokumenter[0].dokumentvarianter[0].filtype).isEqualTo(pdfa)
        Assertions.assertThat(request.dokumenter[0].dokumentvarianter[0].fysiskDokument).isEqualTo(pdfDok)
        Assertions.assertThat(request.dokumenter[0].dokumentvarianter[0].variantformat).isEqualTo(arkivVariantformat)
        Assertions.assertThat(request.dokumenter[0].dokumentvarianter[0].filnavn).isEqualTo(FILNAVN)
        if (sak != null) {
            Assertions.assertThat(request.sak!!.fagsakId).isEqualTo(sak.fagsakId)
            Assertions.assertThat(request.sak!!.fagsaksystem).isEqualTo(sak.fagsaksystem)
            Assertions.assertThat(request.sak!!.fagsaksystem).isEqualTo(sak.sakstype)
        }
    }

    private fun assertOpprettBarnetrygdVedtakJournalpostRequest(request: OpprettJournalpostRequest,
                                                                pdfDok: ByteArray,
                                                                sak: Sak) {
        Assertions.assertThat(request.avsenderMottaker!!.id).isEqualTo(FNR)
        Assertions.assertThat(request.avsenderMottaker!!.idType).isEqualTo(IdType.FNR)
        Assertions.assertThat(request.bruker!!.id).isEqualTo(FNR)
        Assertions.assertThat(request.bruker!!.idType).isEqualTo(IdType.FNR)
        Assertions.assertThat(request.behandlingstema).isEqualTo(BarnetrygdVedtakMetadata.behandlingstema)
        Assertions.assertThat(request.journalpostType).isEqualTo(BarnetrygdVedtakMetadata.journalpostType)
        Assertions.assertThat(request.kanal).isEqualTo(BarnetrygdVedtakMetadata.kanal)
        Assertions.assertThat(request.tema).isEqualTo(BarnetrygdVedtakMetadata.tema)
        Assertions.assertThat(request.dokumenter[0].tittel).isEqualTo(BarnetrygdVedtakMetadata.tittel)
        Assertions.assertThat(request.dokumenter[0].brevkode).isEqualTo(BarnetrygdVedtakMetadata.brevkode)
        Assertions.assertThat(request.dokumenter[0].dokumentKategori).isEqualTo(BarnetrygdVedtakMetadata.dokumentKategori)
        Assertions.assertThat(request.dokumenter[0].dokumentvarianter[0].filtype).isEqualTo("PDFA")
        Assertions.assertThat(request.dokumenter[0].dokumentvarianter[0].fysiskDokument).isEqualTo(pdfDok)
        Assertions.assertThat(request.dokumenter[0].dokumentvarianter[0].variantformat).isEqualTo("ARKIV")
        Assertions.assertThat(request.dokumenter[0].dokumentvarianter[0].filnavn).isEqualTo(FILNAVN)
        Assertions.assertThat(request.sak!!.fagsakId).isEqualTo(sak.fagsakId)
        Assertions.assertThat(request.sak!!.fagsaksystem).isEqualTo(sak.fagsaksystem)
        Assertions.assertThat(request.sak!!.sakstype).isEqualTo(sak.sakstype)
    }

    companion object {
        private const val FNR = "fnr"
        private val PDF_DOK = "dok".toByteArray()
        private const val ARKIV_VARIANTFORMAT = "ARKIV"
        private val JSON_DOK = "{}".toByteArray()
        private const val STRUKTURERT_VARIANTFORMAT = "ORIGINAL"
        private const val JOURNALPOST_ID = "123"
        private const val FILNAVN = "filnavn"
        private val PERSON_IDENT = PersonIdent(FNR)
        private val FAGSAK_ID = "s200"
    }
}