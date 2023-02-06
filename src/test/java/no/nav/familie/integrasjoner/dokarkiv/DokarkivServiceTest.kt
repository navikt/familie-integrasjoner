package no.nav.familie.integrasjoner.dokarkiv

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.integrasjoner.client.rest.DokarkivLogiskVedleggRestClient
import no.nav.familie.integrasjoner.client.rest.DokarkivRestClient
import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.familie.integrasjoner.dokarkiv.client.domene.OpprettJournalpostRequest
import no.nav.familie.integrasjoner.dokarkiv.client.domene.OpprettJournalpostResponse
import no.nav.familie.integrasjoner.dokarkiv.metadata.BarnetrygdVedleggMetadata
import no.nav.familie.integrasjoner.dokarkiv.metadata.BarnetrygdVedtakMetadata
import no.nav.familie.integrasjoner.dokarkiv.metadata.DokarkivMetadata
import no.nav.familie.integrasjoner.dokarkiv.metadata.KontanstøtteSøknadMetadata
import no.nav.familie.integrasjoner.dokarkiv.metadata.KontanstøtteSøknadVedleggMetadata
import no.nav.familie.integrasjoner.førstesidegenerator.FørstesideGeneratorService
import no.nav.familie.integrasjoner.personopplysning.PersonopplysningerService
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING
import no.nav.familie.integrasjoner.personopplysning.internal.Person
import no.nav.familie.kontrakter.felles.BrukerIdType
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Språkkode
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokarkiv.DokarkivBruker
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import no.nav.familie.kontrakter.felles.dokarkiv.OppdaterJournalpostRequest
import no.nav.familie.kontrakter.felles.dokarkiv.OppdaterJournalpostResponse
import no.nav.familie.kontrakter.felles.dokarkiv.Sak
import no.nav.familie.kontrakter.felles.dokarkiv.v2.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Dokument
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Filtype
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Førsteside
import no.nav.familie.kontrakter.felles.personopplysning.SIVILSTAND
import no.nav.familie.log.mdc.MDCConstants
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import java.util.UUID
import kotlin.test.assertTrue

class DokarkivServiceTest {

    private val navn = "Navn Navnesen"

    @MockK
    lateinit var dokarkivRestClient: DokarkivRestClient

    @MockK
    lateinit var førstesideGeneratorService: FørstesideGeneratorService

    @MockK
    lateinit var dokarkivLogiskVedleggRestClient: DokarkivLogiskVedleggRestClient

    @MockK
    lateinit var personopplysningerService: PersonopplysningerService

    private lateinit var dokarkivService: DokarkivService

    @BeforeEach
    fun setUp() {
        MDC.put(MDCConstants.MDC_CALL_ID, UUID.randomUUID().toString()) // settes vanligvis i LogFilter
        MockKAnnotations.init(this)
        dokarkivService = DokarkivService(
            dokarkivRestClient,
            personopplysningerService,
            DokarkivMetadata(
                KontanstøtteSøknadMetadata,
                KontanstøtteSøknadVedleggMetadata,
                BarnetrygdVedtakMetadata,
                BarnetrygdVedleggMetadata,
            ),
            dokarkivLogiskVedleggRestClient,
            førstesideGeneratorService,
        )
    }

    @AfterEach
    internal fun tearDown() {
        MDC.remove(MDCConstants.MDC_CALL_ID)
    }

    @Test
    fun `oppdaterJournalpost skal legge til default sakstype`() {
        val slot = slot<OppdaterJournalpostRequest>()
        every { dokarkivRestClient.oppdaterJournalpost(capture(slot), any()) }
            .answers { OppdaterJournalpostResponse(JOURNALPOST_ID) }

        val bruker = DokarkivBruker(BrukerIdType.FNR, "12345678910")
        val dto = OppdaterJournalpostRequest(bruker = bruker, tema = Tema.ENF, sak = Sak("11111111", "fagsaksystem"))

        dokarkivService.oppdaterJournalpost(dto, JOURNALPOST_ID)

        val request = slot.captured
        verify {
            dokarkivRestClient.oppdaterJournalpost(slot.captured, JOURNALPOST_ID)
        }
        assertThat(request.sak?.sakstype == "FAGSAK")
    }

    @Test
    fun `skal mappe request til opprettJournalpostRequest av type arkiv pdfa`() {
        val slot = slot<OpprettJournalpostRequest>()
        every { dokarkivRestClient.lagJournalpost(capture(slot), any()) }
            .answers { OpprettJournalpostResponse(journalpostId = "", journalpostferdigstilt = false) }

        every { personopplysningerService.hentPersoninfo(FNR, any(), any()) }
            .answers {
                Person(
                    fødselsdato = "1980-05-12",
                    navn = navn,
                    kjønn = "KVINNE",
                    familierelasjoner = emptySet(),
                    adressebeskyttelseGradering = ADRESSEBESKYTTELSEGRADERING.UGRADERT,
                    sivilstand = SIVILSTAND.UGIFT,
                )
            }
        val dto = ArkiverDokumentRequest(
            FNR,
            false,
            listOf(
                Dokument(
                    PDF_DOK,
                    Filtype.PDFA,
                    FILNAVN,
                    null,
                    Dokumenttype.KONTANTSTØTTE_SØKNAD,
                ),
            ),
        )

        dokarkivService.lagJournalpost(dto)

        val request = slot.captured
        verify {
            dokarkivRestClient.lagJournalpost(slot.captured, false)
        }
        assertOpprettJournalpostRequest(request, "PDFA", PDF_DOK, ARKIV_VARIANTFORMAT)
    }

    @Test
    fun `skal generere førsteside hvis førsteside er med i request`() {
        val slot = slot<OpprettJournalpostRequest>()

        every { førstesideGeneratorService.genererForside(any<Førsteside>(), any(), any()) }
            .answers { PDF_DOK }

        every { dokarkivRestClient.lagJournalpost(capture(slot), any()) }
            .answers { OpprettJournalpostResponse(journalpostId = "", journalpostferdigstilt = false) }

        every { personopplysningerService.hentPersoninfo(FNR, any(), any()) }
            .answers {
                Person(
                    fødselsdato = "1980-05-12",
                    navn = navn,
                    kjønn = "KVINNE",
                    familierelasjoner = emptySet(),
                    adressebeskyttelseGradering = ADRESSEBESKYTTELSEGRADERING.UGRADERT,
                    sivilstand = SIVILSTAND.UGIFT,
                )
            }
        val dto = ArkiverDokumentRequest(
            FNR,
            false,
            listOf(
                Dokument(
                    PDF_DOK,
                    Filtype.PDFA,
                    FILNAVN,
                    null,
                    Dokumenttype.KONTANTSTØTTE_SØKNAD,
                ),
            ),
            førsteside = Førsteside(
                språkkode = Språkkode.NB,
                navSkjemaId = "123",
                overskriftstittel = "Testoverskrift",
            ),
        )

        dokarkivService.lagJournalpost(dto)

        val request = slot.captured
        verify {
            dokarkivRestClient.lagJournalpost(slot.captured, false)
        }

        assertThat(request.dokumenter.find { it.tittel == "Testoverskrift" }).isNotNull
    }

    @Test
    fun `skal ikke generere førsteside hvis førsteside mangler i request`() {
        val slot = slot<OpprettJournalpostRequest>()

        every { førstesideGeneratorService.genererForside(any<Førsteside>(), any(), any()) }
            .answers { PDF_DOK }

        every { dokarkivRestClient.lagJournalpost(capture(slot), any()) }
            .answers { OpprettJournalpostResponse(journalpostId = "", journalpostferdigstilt = false) }

        every { personopplysningerService.hentPersoninfo(FNR, any(), any()) }
            .answers {
                Person(
                    fødselsdato = "1980-05-12",
                    navn = navn,
                    kjønn = "KVINNE",
                    familierelasjoner = emptySet(),
                    adressebeskyttelseGradering = ADRESSEBESKYTTELSEGRADERING.UGRADERT,
                    sivilstand = SIVILSTAND.UGIFT,
                )
            }
        val dto = ArkiverDokumentRequest(
            FNR,
            false,
            listOf(
                Dokument(
                    PDF_DOK,
                    Filtype.PDFA,
                    FILNAVN,
                    null,
                    Dokumenttype.KONTANTSTØTTE_SØKNAD,
                ),
            ),
        )

        dokarkivService.lagJournalpost(dto)

        val request = slot.captured
        verify {
            dokarkivRestClient.lagJournalpost(slot.captured, false)
        }

        assertThat(request.dokumenter.find { it.tittel == "Testoverskrift" }).isNull()
    }

    @Test
    fun `skal mappe request til opprettJournalpostRequest for barnetrygd vedtak`() {
        val slot = slot<OpprettJournalpostRequest>()

        every { dokarkivRestClient.lagJournalpost(capture(slot), any()) }
            .answers { OpprettJournalpostResponse(journalpostId = "", journalpostferdigstilt = false) }

        every { personopplysningerService.hentPersoninfo(FNR, any(), any()) }
            .answers {
                Person(
                    fødselsdato = "1980-05-12",
                    navn = navn,
                    kjønn = "KVINNE",
                    familierelasjoner = emptySet(),
                    adressebeskyttelseGradering = ADRESSEBESKYTTELSEGRADERING.UGRADERT,
                    sivilstand = SIVILSTAND.UGIFT,
                )
            }

        val dto = ArkiverDokumentRequest(
            FNR,
            false,
            listOf(
                Dokument(
                    PDF_DOK,
                    Filtype.PDFA,
                    FILNAVN,
                    null,
                    Dokumenttype.BARNETRYGD_VEDTAK,
                ),
            ),
            listOf(
                Dokument(
                    PDF_DOK,
                    Filtype.PDFA,
                    null,
                    TITTEL,
                    Dokumenttype.BARNETRYGD_VEDLEGG,
                ),
            ),
            fagsakId = FAGSAK_ID,
        )

        dokarkivService.lagJournalpost(dto)

        val request = slot.captured
        assertOpprettBarnetrygdVedtakJournalpostRequest(
            request,
            PDF_DOK,
            Sak(
                fagsakId = FAGSAK_ID,
                fagsaksystem = Fagsystem.BA,
                sakstype = "FAGSAK",
            ),
        )
    }

    @Test
    fun `skal mappe request til opprettJournalpostRequest av type ORIGINAL JSON`() {
        val slot = slot<OpprettJournalpostRequest>()
        every { dokarkivRestClient.lagJournalpost(capture(slot), any()) }
            .answers { OpprettJournalpostResponse(journalpostId = "", journalpostferdigstilt = false) }
        every { personopplysningerService.hentPersoninfo(FNR, any(), any()) }
            .answers {
                Person(
                    fødselsdato = "1980-05-12",
                    navn = navn,
                    kjønn = "KVINNE",
                    familierelasjoner = emptySet(),
                    adressebeskyttelseGradering = ADRESSEBESKYTTELSEGRADERING.UGRADERT,
                    sivilstand = SIVILSTAND.UGIFT,
                )
            }

        val dto = ArkiverDokumentRequest(
            FNR,
            false,
            listOf(
                Dokument(
                    JSON_DOK,
                    Filtype.JSON,
                    FILNAVN,
                    null,
                    Dokumenttype.KONTANTSTØTTE_SØKNAD,
                ),
            ),
        )

        dokarkivService.lagJournalpost(dto)

        val request = slot.captured

        verify {
            dokarkivRestClient.lagJournalpost(request, false)
        }

        assertOpprettJournalpostRequest(
            request,
            "JSON",
            JSON_DOK,
            STRUKTURERT_VARIANTFORMAT,
        )
    }

    @Test
    fun `response fra klient skal returnere arkiverDokumentResponse`() {
        every { dokarkivRestClient.lagJournalpost(any(), any()) }
            .answers { OpprettJournalpostResponse(journalpostId = JOURNALPOST_ID, journalpostferdigstilt = true) }
        every { personopplysningerService.hentPersoninfo(FNR, any(), any()) }
            .answers {
                Person(
                    fødselsdato = "1980-05-12",
                    navn = navn,
                    kjønn = "KVINNE",
                    familierelasjoner = emptySet(),
                    adressebeskyttelseGradering = ADRESSEBESKYTTELSEGRADERING.UGRADERT,
                    sivilstand = SIVILSTAND.UGIFT,
                )
            }

        val dto = ArkiverDokumentRequest(
            FNR,
            false,
            listOf(
                Dokument(
                    JSON_DOK,
                    Filtype.JSON,
                    FILNAVN,
                    null,
                    Dokumenttype.KONTANTSTØTTE_SØKNAD,
                ),
            ),
        )

        val arkiverDokumentResponse = dokarkivService.lagJournalpost(dto)

        assertThat(arkiverDokumentResponse.journalpostId).isEqualTo(JOURNALPOST_ID)
        assertTrue(arkiverDokumentResponse.ferdigstilt)
    }

    private fun assertOpprettJournalpostRequest(
        request: OpprettJournalpostRequest,
        pdfa: String,
        pdfDok: ByteArray,
        arkivVariantformat: String,
        sak: Sak? = null,
    ) {
        assertThat(request.avsenderMottaker!!.id).isEqualTo(FNR)
        assertThat(request.avsenderMottaker!!.idType).isEqualTo(BrukerIdType.FNR)
        assertThat(request.bruker!!.id).isEqualTo(FNR)
        assertThat(request.bruker!!.idType).isEqualTo(BrukerIdType.FNR)
        assertThat(request.behandlingstema).isEqualTo(KontanstøtteSøknadMetadata.behandlingstema.value)
        assertThat(request.journalpostType).isEqualTo(JournalpostType.INNGAAENDE)
        assertThat(request.kanal).isEqualTo(KontanstøtteSøknadMetadata.kanal)
        assertThat(request.tema).isEqualTo(KontanstøtteSøknadMetadata.tema.toString())
        assertThat(request.sak).isNull()
        assertThat(request.dokumenter[0].tittel).isEqualTo(KontanstøtteSøknadMetadata.tittel)
        assertThat(request.dokumenter[0].brevkode).isEqualTo(KontanstøtteSøknadMetadata.brevkode)
        assertThat(request.dokumenter[0].dokumentKategori).isEqualTo(KontanstøtteSøknadMetadata.dokumentKategori)
        assertThat(request.dokumenter[0].dokumentvarianter[0].filtype).isEqualTo(pdfa)
        assertThat(request.dokumenter[0].dokumentvarianter[0].fysiskDokument).isEqualTo(pdfDok)
        assertThat(request.dokumenter[0].dokumentvarianter[0].variantformat).isEqualTo(arkivVariantformat)
        assertThat(request.dokumenter[0].dokumentvarianter[0].filnavn).isEqualTo(FILNAVN)
        if (sak != null) {
            assertThat(request.sak!!.fagsakId).isEqualTo(sak.fagsakId)
            assertThat(request.sak!!.fagsaksystem).isEqualTo(sak.fagsaksystem)
            assertThat(request.sak!!.fagsaksystem).isEqualTo(sak.sakstype)
        }
    }

    private fun assertOpprettBarnetrygdVedtakJournalpostRequest(
        request: OpprettJournalpostRequest,
        pdfDok: ByteArray,
        sak: Sak,
    ) {
        assertThat(request.avsenderMottaker!!.id).isEqualTo(FNR)
        assertThat(request.avsenderMottaker!!.idType).isEqualTo(BrukerIdType.FNR)
        assertThat(request.bruker!!.id).isEqualTo(FNR)
        assertThat(request.bruker!!.idType).isEqualTo(BrukerIdType.FNR)
        assertThat(request.behandlingstema).isEqualTo(BarnetrygdVedtakMetadata.behandlingstema.value)
        assertThat(request.journalpostType).isEqualTo(BarnetrygdVedtakMetadata.journalpostType)
        assertThat(request.kanal).isEqualTo(BarnetrygdVedtakMetadata.kanal)
        assertThat(request.tema).isEqualTo(BarnetrygdVedtakMetadata.tema.toString())
        assertThat(request.dokumenter[0].tittel).isEqualTo(BarnetrygdVedtakMetadata.tittel)
        assertThat(request.dokumenter[0].brevkode).isEqualTo(BarnetrygdVedtakMetadata.brevkode)
        assertThat(request.dokumenter[0].dokumentKategori).isEqualTo(BarnetrygdVedtakMetadata.dokumentKategori)
        assertThat(request.dokumenter[0].dokumentvarianter[0].filtype).isEqualTo("PDFA")
        assertThat(request.dokumenter[0].dokumentvarianter[0].fysiskDokument).isEqualTo(pdfDok)
        assertThat(request.dokumenter[0].dokumentvarianter[0].variantformat).isEqualTo("ARKIV")
        assertThat(request.dokumenter[0].dokumentvarianter[0].filnavn).isEqualTo(FILNAVN)
        assertThat(request.dokumenter[1].tittel).isEqualTo(TITTEL)
        assertThat(request.dokumenter[1].brevkode).isEqualTo(BarnetrygdVedleggMetadata.brevkode)
        assertThat(request.dokumenter[1].dokumentKategori).isEqualTo(BarnetrygdVedleggMetadata.dokumentKategori)
        assertThat(request.dokumenter[1].dokumentvarianter[0].filtype).isEqualTo("PDFA")
        assertThat(request.dokumenter[1].dokumentvarianter[0].fysiskDokument).isEqualTo(pdfDok)
        assertThat(request.dokumenter[1].dokumentvarianter[0].variantformat).isEqualTo("ARKIV")
        assertThat(request.dokumenter[1].dokumentvarianter[0].filnavn).isEqualTo(null)
        assertThat(request.sak!!.fagsakId).isEqualTo(sak.fagsakId)
        assertThat(request.sak!!.fagsaksystem).isEqualTo(sak.fagsaksystem)
        assertThat(request.sak!!.sakstype).isEqualTo(sak.sakstype)
    }

    companion object {

        private const val FNR = "fnr"
        private val PDF_DOK = "dok".toByteArray()
        private const val ARKIV_VARIANTFORMAT = "ARKIV"
        private val JSON_DOK = "{}".toByteArray()
        private const val STRUKTURERT_VARIANTFORMAT = "ORIGINAL"
        private const val JOURNALPOST_ID = "123"
        private const val FILNAVN = "filnavn"
        private const val TITTEL = "tittel"
        private const val FAGSAK_ID = "s200"
    }
}
