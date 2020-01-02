package no.nav.familie.integrasjoner.dokarkiv

import no.nav.familie.integrasjoner.client.rest.DokarkivRestClient
import no.nav.familie.integrasjoner.dokarkiv.api.ArkiverDokumentRequest
import no.nav.familie.integrasjoner.dokarkiv.api.Dokument
import no.nav.familie.integrasjoner.dokarkiv.api.FilType
import no.nav.familie.integrasjoner.dokarkiv.client.DokarkivClient
import no.nav.familie.integrasjoner.dokarkiv.client.domene.IdType
import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.familie.integrasjoner.dokarkiv.client.domene.OpprettJournalpostRequest
import no.nav.familie.integrasjoner.dokarkiv.client.domene.OpprettJournalpostResponse
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

class DokarkivServiceTest {

    private val navn = "Navn Navnesen"
    private val dokarkivClient = Mockito.mock(DokarkivClient::class.java)
    private val dokarkivRestClient = Mockito.mock(DokarkivRestClient::class.java)
    private lateinit var dokarkivService: DokarkivService
    private val personopplysningerService = Mockito.mock(PersonopplysningerService::class.java)

    @Before fun setUp() {
        dokarkivService = DokarkivService(dokarkivClient,
                                          dokarkivRestClient,
                                          personopplysningerService,
                                          DokarkivMetadata(KontanstøtteSøknadMetadata(), KontanstøtteSøknadVedleggMetadata()))
    }

    @Test fun `skal mappe request til opprettJournalpostRequest av type arkiv pdfa`() {
        val captor = ArgumentCaptor.forClass(OpprettJournalpostRequest::class.java)
        Mockito.`when`(dokarkivClient.lagJournalpost(any<OpprettJournalpostRequest>(), anyBoolean()))
                .thenReturn(OpprettJournalpostResponse())
        Mockito.`when`(personopplysningerService.hentPersoninfoFor(FNR))
                .thenReturn(Personinfo.Builder().medPersonIdent(PERSON_IDENT)
                                    .medFødselsdato(LocalDate.now())
                                    .medNavn(navn)
                                    .build())
        val dto = ArkiverDokumentRequest(FNR,
                                         false,
                                         listOf(Dokument(PDF_DOK, FilType.PDFA, FILNAVN, "KONTANTSTØTTE_SØKNAD")))

        dokarkivService.lagInngåendeJournalpost(dto)

        Mockito.verify(dokarkivClient).lagJournalpost(captor.capture(), eq(false))
        val request = captor.value
        assertOpprettJournalpostRequest(request, "PDFA", PDF_DOK, ARKIV_VARIANTFORMAT)
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
                                         listOf(Dokument(JSON_DOK, FilType.JSON, FILNAVN, "KONTANTSTØTTE_SØKNAD")))

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
                                         listOf(Dokument(JSON_DOK, FilType.JSON, FILNAVN, "KONTANTSTØTTE_SØKNAD")))

        val arkiverDokumentResponse = dokarkivService.lagInngåendeJournalpost(dto)

        Assertions.assertThat(arkiverDokumentResponse.journalpostId).isEqualTo(JOURNALPOST_ID)
        Assertions.assertThat(arkiverDokumentResponse.ferdigstilt).isTrue()
    }

    @Test fun `skal kaste exception hvis navn er null`() {
        Mockito.`when`(personopplysningerService.hentPersoninfoFor(FNR)).thenReturn(null)
        val dto = ArkiverDokumentRequest(FNR,
                                         false,
                                         listOf(Dokument(PDF_DOK, FilType.PDFA, FILNAVN, "KONTANTSTØTTE_SØKNAD")))

        val thrown = Assertions.catchThrowable { dokarkivService.lagInngåendeJournalpost(dto) }

        Assertions.assertThat(thrown).isInstanceOf(RuntimeException::class.java)
                .withFailMessage("Kan ikke hente navn")
    }

    private fun assertOpprettJournalpostRequest(request: OpprettJournalpostRequest,
                                                pdfa: String,
                                                pdfDok: ByteArray,
                                                arkivVariantformat: String) {
        Assertions.assertThat(request.avsenderMottaker!!.id).isEqualTo(FNR)
        Assertions.assertThat(request.avsenderMottaker!!.idType).isEqualTo(IdType.FNR)
        Assertions.assertThat(request.bruker!!.id).isEqualTo(FNR)
        Assertions.assertThat(request.bruker!!.idType).isEqualTo(IdType.FNR)
        Assertions.assertThat(request.behandlingstema).isEqualTo(KontanstøtteSøknadMetadata().behandlingstema)
        Assertions.assertThat(request.journalpostType).isEqualTo(JournalpostType.INNGAAENDE)
        Assertions.assertThat(request.kanal).isEqualTo(KontanstøtteSøknadMetadata().kanal)
        Assertions.assertThat(request.tema).isEqualTo(KontanstøtteSøknadMetadata().tema)
        Assertions.assertThat(request.sak).isNull()
        Assertions.assertThat(request.dokumenter[0].tittel).isEqualTo(KontanstøtteSøknadMetadata().tittel)
        Assertions.assertThat(request.dokumenter[0].brevkode).isEqualTo(KontanstøtteSøknadMetadata().brevkode)
        Assertions.assertThat(request.dokumenter[0].dokumentKategori).isEqualTo(KontanstøtteSøknadMetadata().dokumentKategori)
        Assertions.assertThat(request.dokumenter[0].dokumentvarianter[0].filtype).isEqualTo(pdfa)
        Assertions.assertThat(request.dokumenter[0].dokumentvarianter[0].fysiskDokument).isEqualTo(pdfDok)
        Assertions.assertThat(request.dokumenter[0].dokumentvarianter[0].variantformat).isEqualTo(arkivVariantformat)
        Assertions.assertThat(request.dokumenter[0].dokumentvarianter[0].filnavn).isEqualTo(FILNAVN)
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
    }
}