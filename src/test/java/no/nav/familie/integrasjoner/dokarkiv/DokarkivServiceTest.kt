package no.nav.familie.integrasjoner.dokarkiv

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.whenever
import no.nav.familie.integrasjoner.client.rest.DokarkivRestClient
import no.nav.familie.integrasjoner.dokarkiv.api.ArkiverDokumentRequest
import no.nav.familie.integrasjoner.dokarkiv.api.Dokument
import no.nav.familie.integrasjoner.dokarkiv.api.FilType
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
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito
import java.time.LocalDate
import com.nhaarman.mockitokotlin2.any as kotlinAny
import com.nhaarman.mockitokotlin2.mock as kotlinMock
import com.nhaarman.mockitokotlin2.verify as kotlinVerify

class DokarkivServiceTest {

    private val navn = "Navn Navnesen"
    private val dokarkivClient = Mockito.mock(DokarkivClient::class.java)
    private val dokarkivRestClient = kotlinMock<DokarkivRestClient>()
    private lateinit var dokarkivService: DokarkivService
    private val personopplysningerService = Mockito.mock(PersonopplysningerService::class.java)

    @Before fun setUp() {
        dokarkivService = DokarkivService(dokarkivClient,
                                          dokarkivRestClient,
                                          personopplysningerService,
                                          DokarkivMetadata(KontanstøtteSøknadMetadata, KontanstøtteSøknadVedleggMetadata, BarnetrygdVedtakMetadata))
    }

    @Test fun `skal mappe request til opprettJournalpostRequest av type arkiv pdfa`() {
        val captor = ArgumentCaptor.forClass(OpprettJournalpostRequest::class.java)
        Mockito.`when`(dokarkivClient.lagJournalpost(kotlinAny(), anyBoolean()))
                .thenReturn(OpprettJournalpostResponse())
        Mockito.`when`(personopplysningerService.hentPersoninfoFor(FNR))
                .thenReturn(Personinfo.Builder().medPersonIdent(PERSON_IDENT)
                                    .medFødselsdato(LocalDate.now())
                                    .medNavn(navn)
                                    .build())
        val dto = ArkiverDokumentRequest(FNR,
                                         false,
                                         listOf(Dokument(PDF_DOK, FilType.PDFA, FILNAVN, null, "KONTANTSTØTTE_SØKNAD")))

        dokarkivService.lagInngåendeJournalpost(dto)

        Mockito.verify(dokarkivClient).lagJournalpost(captor.capture(), eq(false))
        val request = captor.value
        assertOpprettJournalpostRequest(request, "PDFA", PDF_DOK, ARKIV_VARIANTFORMAT)
    }

    @Test fun `skal mappe request til opprettJournalpostRequest for barnetrygd vedtak`() {
        doAnswer { OpprettJournalpostResponse() }
                .whenever(dokarkivRestClient)
                .lagJournalpost(kotlinAny(), anyBoolean())
        Mockito.`when`(personopplysningerService.hentPersoninfoFor(FNR))
                .thenReturn(Personinfo.Builder().medPersonIdent(PERSON_IDENT)
                        .medFødselsdato(LocalDate.now())
                        .medNavn(navn)
                        .build())
        val dto = ArkiverDokumentRequest(FNR,
                false,
                listOf(Dokument(PDF_DOK, FilType.PDFA, FILNAVN, null, "BARNETRYGD_VEDTAK")),
                fagsakId = FAGSAK_ID)

        dokarkivService.lagJournalpostV2(dto)

        val captor= argumentCaptor<OpprettJournalpostRequest>()
        kotlinVerify(dokarkivRestClient).lagJournalpost(captor.capture(), kotlinAny())
        val request = captor.lastValue
        assertOpprettBarnetrygdVedtakJournalpostRequest(request, PDF_DOK, Sak(fagsakId = FAGSAK_ID, fagsaksystem = "FS36", sakstype = "FAGSAK"))
    }

    @Test fun `skal mappe request til opprettJournalpostRequest av type ORIGINAL JSON`() {
        val captor = ArgumentCaptor.forClass(OpprettJournalpostRequest::class.java)
        Mockito.`when`(dokarkivClient.lagJournalpost(any<OpprettJournalpostRequest>(), anyBoolean()))
                .thenReturn(OpprettJournalpostResponse())
        Mockito.`when`(personopplysningerService.hentPersoninfoFor(FNR))
                .thenReturn(Personinfo.Builder()
                                    .medPersonIdent(PERSON_IDENT)
                                    .medFødselsdato(LocalDate.now())
                                    .medNavn(navn)
                                    .build())
        val dto = ArkiverDokumentRequest(FNR,
                                         false,
                                         listOf(Dokument(JSON_DOK, FilType.JSON, FILNAVN, null, "KONTANTSTØTTE_SØKNAD")))

        dokarkivService.lagInngåendeJournalpost(dto)

        Mockito.verify(dokarkivClient).lagJournalpost(captor.capture(), eq(false))
        val request = captor.value
        assertOpprettJournalpostRequest(request,
                                        "JSON",
                                        JSON_DOK,
                                        STRUKTURERT_VARIANTFORMAT)
    }

    @Test fun `response fra klient skal returnere arkiverDokumentResponse`() {
        Mockito.`when`(dokarkivClient.lagJournalpost(any(OpprettJournalpostRequest::class.java), anyBoolean()))
                .thenReturn(OpprettJournalpostResponse(journalpostId = JOURNALPOST_ID, journalpostferdigstilt = true))
        Mockito.`when`(personopplysningerService.hentPersoninfoFor(FNR))
                .thenReturn(Personinfo.Builder()
                                    .medPersonIdent(PERSON_IDENT)
                                    .medFødselsdato(LocalDate.now())
                                    .medNavn(navn)
                                    .build())
        val dto = ArkiverDokumentRequest(FNR,
                                         false,
                                         listOf(Dokument(JSON_DOK, FilType.JSON, FILNAVN, null, "KONTANTSTØTTE_SØKNAD")))

        val arkiverDokumentResponse = dokarkivService.lagInngåendeJournalpost(dto)

        Assertions.assertThat(arkiverDokumentResponse.journalpostId).isEqualTo(JOURNALPOST_ID)
        Assertions.assertThat(arkiverDokumentResponse.ferdigstilt).isTrue()
    }

    @Test fun `skal kaste exception hvis navn er null`() {
        Mockito.`when`(personopplysningerService.hentPersoninfoFor(FNR)).thenReturn(null)
        val dto = ArkiverDokumentRequest(FNR,
                                         false,
                                         listOf(Dokument(PDF_DOK, FilType.PDFA, FILNAVN, null, "KONTANTSTØTTE_SØKNAD")))

        val thrown = Assertions.catchThrowable { dokarkivService.lagInngåendeJournalpost(dto) }

        Assertions.assertThat(thrown).isInstanceOf(RuntimeException::class.java)
                .withFailMessage("Kan ikke hente navn")
    }

    private fun assertOpprettJournalpostRequest(request: OpprettJournalpostRequest,
                                                pdfa: String,
                                                pdfDok: ByteArray,
                                                arkivVariantformat: String,
                                                sak: Sak?= null) {
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
        if(sak!= null){
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
        private val FAGSAK_ID= "s200"
    }
}