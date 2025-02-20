package no.nav.familie.integrasjoner.dokarkiv

import no.nav.familie.integrasjoner.client.rest.DokarkivLogiskVedleggRestClient
import no.nav.familie.integrasjoner.client.rest.DokarkivRestClient
import no.nav.familie.integrasjoner.dokarkiv.client.domene.ArkivDokument
import no.nav.familie.integrasjoner.dokarkiv.client.domene.Dokumentvariant
import no.nav.familie.integrasjoner.dokarkiv.client.domene.OpprettJournalpostRequest
import no.nav.familie.integrasjoner.dokarkiv.client.domene.OpprettJournalpostResponse
import no.nav.familie.integrasjoner.dokarkiv.metadata.tilMetadata
import no.nav.familie.integrasjoner.felles.MDCOperations
import no.nav.familie.integrasjoner.førstesidegenerator.FørstesideGeneratorService
import no.nav.familie.integrasjoner.personopplysning.PersonopplysningerService
import no.nav.familie.kontrakter.felles.BrukerIdType
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokarkiv.ArkiverDokumentResponse
import no.nav.familie.kontrakter.felles.dokarkiv.AvsenderMottaker
import no.nav.familie.kontrakter.felles.dokarkiv.BulkOppdaterLogiskVedleggRequest
import no.nav.familie.kontrakter.felles.dokarkiv.DokarkivBruker
import no.nav.familie.kontrakter.felles.dokarkiv.LogiskVedleggRequest
import no.nav.familie.kontrakter.felles.dokarkiv.LogiskVedleggResponse
import no.nav.familie.kontrakter.felles.dokarkiv.OppdaterJournalpostRequest
import no.nav.familie.kontrakter.felles.dokarkiv.OppdaterJournalpostResponse
import no.nav.familie.kontrakter.felles.dokarkiv.Sak
import no.nav.familie.kontrakter.felles.dokarkiv.v2.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Dokument
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Filtype
import no.nav.familie.kontrakter.felles.journalpost.AvsenderMottakerIdType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DokarkivService(
    private val dokarkivRestClient: DokarkivRestClient,
    private val personopplysningerService: PersonopplysningerService,
    private val dokarkivLogiskVedleggRestClient: DokarkivLogiskVedleggRestClient,
    private val førstesideGeneratorService: FørstesideGeneratorService,
) {
    fun ferdistillJournalpost(
        journalpost: String,
        journalførendeEnhet: String,
        navIdent: String? = null,
    ) {
        dokarkivRestClient.ferdigstillJournalpost(journalpost, journalførendeEnhet, navIdent)
    }

    fun lagJournalpost(
        arkiverDokumentRequest: ArkiverDokumentRequest,
        navIdent: String? = null,
    ): ArkiverDokumentResponse {
        val request = mapTilOpprettJournalpostRequest(arkiverDokumentRequest)
        val response = dokarkivRestClient.lagJournalpost(request, arkiverDokumentRequest.forsøkFerdigstill, navIdent)
        return mapTilArkiverDokumentResponse(response)
    }

    private fun mapTilOpprettJournalpostRequest(arkiverDokumentRequest: ArkiverDokumentRequest): OpprettJournalpostRequest {
        val dokarkivBruker = DokarkivBruker(BrukerIdType.FNR, arkiverDokumentRequest.fnr)
        val hoveddokument = arkiverDokumentRequest.hoveddokumentvarianter[0]
        val metadata = hoveddokument.dokumenttype.tilMetadata()
        val avsenderMottaker =
            arkiverDokumentRequest.avsenderMottaker ?: arkiverDokumentRequest.fnr.let {
                val navn = hentNavnForFnr(fnr = arkiverDokumentRequest.fnr, behandlingstema = metadata.tema)
                AvsenderMottaker(it, AvsenderMottakerIdType.FNR, navn)
            }

        val dokumenter = mutableListOf(mapHoveddokument(arkiverDokumentRequest.hoveddokumentvarianter))
        dokumenter.addAll(arkiverDokumentRequest.vedleggsdokumenter.map(this::mapTilArkivdokument))
        val sak =
            arkiverDokumentRequest.fagsakId?.let {
                Sak(fagsakId = it, sakstype = "FAGSAK", fagsaksystem = metadata.fagsakSystem)
            }

        // førsteside
        arkiverDokumentRequest.førsteside?.also {
            val bytes = førstesideGeneratorService.genererForside(it, arkiverDokumentRequest.fnr, metadata.tema)
            dokumenter +=
                ArkivDokument(
                    brevkode = metadata.brevkode,
                    dokumentKategori = metadata.dokumentKategori,
                    tittel = arkiverDokumentRequest.førsteside?.overskriftstittel,
                    dokumentvarianter =
                        listOf(
                            Dokumentvariant(
                                filtype = "PDFA",
                                variantformat = "ARKIV",
                                fysiskDokument = bytes,
                                filnavn = "førsteside.pdf",
                            ),
                        ),
                )
        }

        LOG.info("Journalfører fagsak ${sak?.fagsakId} med tittel ${hoveddokument.tittel ?: metadata.tittel}")
        val eksternReferanseId = arkiverDokumentRequest.eksternReferanseId ?: MDCOperations.getCallId()
        return OpprettJournalpostRequest(
            journalpostType = metadata.journalpostType,
            behandlingstema = metadata.behandlingstema?.value,
            kanal = metadata.kanal,
            tittel = hoveddokument.tittel ?: metadata.tittel,
            tema = metadata.tema.name,
            avsenderMottaker = avsenderMottaker,
            bruker = dokarkivBruker,
            dokumenter = dokumenter.toList(),
            eksternReferanseId = eksternReferanseId,
            journalfoerendeEnhet = arkiverDokumentRequest.journalførendeEnhet,
            sak = sak,
        )
    }

    fun oppdaterJournalpost(
        request: OppdaterJournalpostRequest,
        journalpostId: String,
        navIdent: String? = null,
    ): OppdaterJournalpostResponse = dokarkivRestClient.oppdaterJournalpost(supplerDefaultVerdier(request), journalpostId, navIdent)

    private fun hentNavnForFnr(
        fnr: String,
        behandlingstema: Tema,
    ): String = personopplysningerService.hentPersoninfo(fnr, behandlingstema).navn

    private fun supplerDefaultVerdier(request: OppdaterJournalpostRequest): OppdaterJournalpostRequest = request.copy(sak = request.sak?.copy(sakstype = request.sak?.sakstype ?: "FAGSAK"))

    private fun mapHoveddokument(dokumenter: List<Dokument>): ArkivDokument {
        val dokument = dokumenter[0]
        val metadata = dokument.dokumenttype.tilMetadata()
        val dokumentvarianter =
            dokumenter.map {
                val variantFormat: String = hentVariantformat(it)
                Dokumentvariant(it.filtype.name, variantFormat, it.dokument, it.filnavn)
            }

        return ArkivDokument(
            brevkode = metadata.brevkode,
            dokumentKategori = metadata.dokumentKategori,
            tittel = metadata.tittel ?: dokument.tittel,
            dokumentvarianter = dokumentvarianter,
        )
    }

    private fun hentVariantformat(dokument: Dokument): String =
        if (dokument.filtype == Filtype.PDFA) {
            "ARKIV" // ustrukturert dokumentDto
        } else {
            "ORIGINAL" // strukturert dokumentDto
        }

    private fun mapTilArkivdokument(dokument: Dokument): ArkivDokument {
        val metadata = dokument.dokumenttype.tilMetadata()
        val variantFormat: String = hentVariantformat(dokument)
        return ArkivDokument(
            brevkode = metadata.brevkode,
            dokumentKategori = metadata.dokumentKategori,
            tittel = metadata.tittel ?: dokument.tittel,
            dokumentvarianter =
                listOf(
                    Dokumentvariant(
                        dokument.filtype.name,
                        variantFormat,
                        dokument.dokument,
                        dokument.filnavn,
                    ),
                ),
        )
    }

    private fun mapTilArkiverDokumentResponse(response: OpprettJournalpostResponse): ArkiverDokumentResponse =
        ArkiverDokumentResponse(
            response.journalpostId!!,
            response.journalpostferdigstilt ?: false,
            response.dokumenter,
        )

    fun lagNyttLogiskVedlegg(
        dokumentInfoId: String,
        request: LogiskVedleggRequest,
    ): LogiskVedleggResponse = dokarkivLogiskVedleggRestClient.opprettLogiskVedlegg(dokumentInfoId, request)

    fun slettLogiskVedlegg(
        dokumentInfoId: String,
        logiskVedleggId: String,
    ) {
        dokarkivLogiskVedleggRestClient.slettLogiskVedlegg(dokumentInfoId, logiskVedleggId)
    }

    fun oppdaterLogiskeVedleggForDokument(
        dokumentinfoId: String,
        request: BulkOppdaterLogiskVedleggRequest,
    ) {
        dokarkivLogiskVedleggRestClient.oppdaterLogiskeVedlegg(dokumentinfoId, request)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DokarkivService::class.java)
    }
}
