package no.nav.familie.integrasjoner.dokarkiv

import no.nav.familie.integrasjoner.client.rest.DokarkivLogiskVedleggRestClient
import no.nav.familie.integrasjoner.client.rest.DokarkivRestClient
import no.nav.familie.integrasjoner.client.rest.PersonInfoQuery
import no.nav.familie.integrasjoner.dokarkiv.DokarkivController.LogiskVedleggRequest
import no.nav.familie.integrasjoner.dokarkiv.DokarkivController.LogiskVedleggResponse
import no.nav.familie.integrasjoner.dokarkiv.client.domene.*
import no.nav.familie.integrasjoner.dokarkiv.metadata.DokarkivMetadata
import no.nav.familie.integrasjoner.felles.MDCOperations
import no.nav.familie.integrasjoner.personopplysning.PersonopplysningerService
import no.nav.familie.kontrakter.felles.arkivering.ArkiverDokumentResponse
import no.nav.familie.kontrakter.felles.arkivering.Dokument
import no.nav.familie.kontrakter.felles.arkivering.FilType
import no.nav.familie.kontrakter.felles.arkivering.v2.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.oppgave.Tema
import org.springframework.stereotype.Service
import no.nav.familie.kontrakter.felles.arkivering.ArkiverDokumentRequest as DeprecatedArkiverDokumentRequest

@Service
class DokarkivService(private val dokarkivRestClient: DokarkivRestClient,
                      private val personopplysningerService: PersonopplysningerService,
                      private val dokarkivMetadata: DokarkivMetadata,
                      private val dokarkivLogiskVedleggRestClient: DokarkivLogiskVedleggRestClient) {

    fun ferdistillJournalpost(journalpost: String, journalførendeEnhet: String) {
        dokarkivRestClient.ferdigstillJournalpost(journalpost, journalførendeEnhet)
    }

    fun lagJournalpostV2(deprecatedArkiverDokumentRequest: DeprecatedArkiverDokumentRequest): ArkiverDokumentResponse {
        val request = mapTilOpprettJournalpostRequest(deprecatedArkiverDokumentRequest)
        val response =
                dokarkivRestClient.lagJournalpost(request, deprecatedArkiverDokumentRequest.forsøkFerdigstill)
        return mapTilArkiverDokumentResponse(response)
    }

    fun lagJournalpostV3(arkiverDokumentRequest: ArkiverDokumentRequest): ArkiverDokumentResponse {
        val request = mapTilOpprettJournalpostRequest(arkiverDokumentRequest)
        val response =
                dokarkivRestClient.lagJournalpost(request, arkiverDokumentRequest.forsøkFerdigstill)
        return mapTilArkiverDokumentResponse(response)
    }

    private fun mapTilOpprettJournalpostRequest(arkiverDokumentRequest: ArkiverDokumentRequest): OpprettJournalpostRequest {
        val fnr = arkiverDokumentRequest.fnr

        val metadata = dokarkivMetadata.getMetadata(arkiverDokumentRequest.hoveddokumentvarianter[0])
        val hoveddokument = mapHoveddokument(arkiverDokumentRequest.hoveddokumentvarianter)
        val vedleggsdokumenter = arkiverDokumentRequest.vedleggsdokumenter.map(this::mapTilArkivdokument)
        val jpsak: Sak? = if (arkiverDokumentRequest.fagsakId != null)
            Sak(fagsakId = arkiverDokumentRequest.fagsakId,
                sakstype = "FAGSAK",
                fagsaksystem = metadata.fagsakSystem) else null

        val navn = hentNavnForFnr(fnr = arkiverDokumentRequest.fnr, behandlingstema = metadata.behandlingstema)

        return OpprettJournalpostRequest(journalpostType = metadata.journalpostType,
                                         behandlingstema = metadata.behandlingstema,
                                         kanal = metadata.kanal,
                                         tittel = metadata.tittel,
                                         tema = metadata.tema,
                                         avsenderMottaker = AvsenderMottaker(fnr, IdType.FNR, navn),
                                         bruker = DokarkivBruker(IdType.FNR, fnr),
                                         dokumenter =  listOf(hoveddokument) + vedleggsdokumenter,
                                         eksternReferanseId = MDCOperations.getCallId(),
                                         journalfoerendeEnhet = arkiverDokumentRequest.journalførendeEnhet,
                                         sak = jpsak
        )

    }

    private fun mapTilOpprettJournalpostRequest(deprecatedArkiverDokumentRequest: DeprecatedArkiverDokumentRequest)
            : OpprettJournalpostRequest {


        val metadata = dokarkivMetadata.getMetadata(deprecatedArkiverDokumentRequest.dokumenter[0])

        val fnr = deprecatedArkiverDokumentRequest.fnr
        val navn = hentNavnForFnr(fnr = fnr, behandlingstema = metadata.behandlingstema)

        val arkivdokumenter = deprecatedArkiverDokumentRequest.dokumenter.map(this::mapTilArkivdokument)
        val jpsak: Sak? = if (deprecatedArkiverDokumentRequest.fagsakId != null) Sak(
                fagsakId = deprecatedArkiverDokumentRequest.fagsakId,
                sakstype = "FAGSAK",
                fagsaksystem = metadata.fagsakSystem
        ) else null

        return OpprettJournalpostRequest(journalpostType = metadata.journalpostType,
                                         behandlingstema = metadata.behandlingstema,
                                         kanal = metadata.kanal,
                                         tittel = metadata.tittel,
                                         tema = metadata.tema,
                                         avsenderMottaker = AvsenderMottaker(fnr, IdType.FNR, navn),
                                         bruker = DokarkivBruker(IdType.FNR, fnr),
                                         dokumenter = arkivdokumenter,
                                         eksternReferanseId = MDCOperations.getCallId(),
                                         journalfoerendeEnhet = deprecatedArkiverDokumentRequest.journalførendeEnhet,
                                         sak = jpsak
        )
    }

    fun oppdaterJournalpost(request: OppdaterJournalpostRequest, journalpostId: String): OppdaterJournalpostResponse {
        return dokarkivRestClient.oppdaterJournalpost(supplerDefaultVerdier(request), journalpostId)
    }

    private fun hentNavnForFnr(fnr: String, behandlingstema: String?): String {
        return personopplysningerService.hentPersoninfo(fnr, behandlingstema ?: Tema.BAR.toString(), PersonInfoQuery.ENKEL).navn
    }

    private fun supplerDefaultVerdier(request: OppdaterJournalpostRequest): OppdaterJournalpostRequest {
        return request.copy(sak = request.sak?.copy(sakstype = request.sak.sakstype ?: "FAGSAK"))
    }

    private fun mapHoveddokument(dokumenter: List<Dokument>): ArkivDokument {
        val dokument = dokumenter[0]
        val metadata = dokarkivMetadata.getMetadata(dokument)
        val dokumentvarianter = dokumenter.map {
            val variantFormat: String = hentVariantformat(it)
            DokumentVariant(it.filType.name, variantFormat, it.dokument, it.filnavn)
        }

        return ArkivDokument(brevkode = metadata.brevkode,
                             dokumentKategori = metadata.dokumentKategori,
                             tittel = metadata.tittel ?: dokument.tittel,
                             dokumentvarianter = dokumentvarianter)
    }

    private fun hentVariantformat(dokument: Dokument): String {
        return if (dokument.filType == FilType.PDFA) {
            "ARKIV" //ustrukturert dokumentDto
        } else {
            "ORIGINAL" //strukturert dokumentDto
        }
    }

    private fun mapTilArkivdokument(dokument: Dokument): ArkivDokument {
        val metadata = dokarkivMetadata.getMetadata(dokument)
        val variantFormat: String = hentVariantformat(dokument)
        return ArkivDokument(brevkode = metadata.brevkode,
                             dokumentKategori = metadata.dokumentKategori,
                             tittel = metadata.tittel ?: dokument.tittel,
                             dokumentvarianter = listOf(DokumentVariant(dokument.filType.name,
                                                                        variantFormat,
                                                                        dokument.dokument,
                                                                        dokument.filnavn)))
    }

    private fun mapTilArkiverDokumentResponse(response: OpprettJournalpostResponse): ArkiverDokumentResponse {
        return ArkiverDokumentResponse(response.journalpostId!!,
                                       response.journalpostferdigstilt ?: false)
    }

    fun lagNyttLogiskVedlegg(dokumentInfoId: String,
                             request: LogiskVedleggRequest): LogiskVedleggResponse {
        return dokarkivLogiskVedleggRestClient.opprettLogiskVedlegg(dokumentInfoId, request)
    }

    fun slettLogiskVedlegg(dokumentInfoId: String, logiskVedleggId: String) {
        dokarkivLogiskVedleggRestClient.slettLogiskVedlegg(dokumentInfoId, logiskVedleggId)
    }

}
