package no.nav.familie.integrasjoner.dokarkiv

import no.nav.familie.integrasjoner.client.rest.DokarkivLogiskVedleggRestClient
import no.nav.familie.integrasjoner.client.rest.DokarkivRestClient
import no.nav.familie.integrasjoner.dokarkiv.DokarkivController.LogiskVedleggRequest
import no.nav.familie.integrasjoner.dokarkiv.DokarkivController.LogiskVedleggResponse
import no.nav.familie.integrasjoner.dokarkiv.api.*
import no.nav.familie.integrasjoner.dokarkiv.client.domene.*
import no.nav.familie.integrasjoner.dokarkiv.client.domene.Bruker
import no.nav.familie.integrasjoner.dokarkiv.client.domene.IdType
import no.nav.familie.integrasjoner.dokarkiv.client.domene.Sak
import no.nav.familie.integrasjoner.dokarkiv.metadata.DokarkivMetadata
import no.nav.familie.integrasjoner.felles.MDCOperations
import no.nav.familie.integrasjoner.personopplysning.PersonopplysningerService
import org.springframework.stereotype.Service

@Service
class DokarkivService(private val dokarkivRestClient: DokarkivRestClient,
                      private val personopplysningerService: PersonopplysningerService,
                      private val dokarkivMetadata: DokarkivMetadata,
                      private val dokarkivLogiskVedleggRestClient: DokarkivLogiskVedleggRestClient) {

    fun ferdistillJournalpost(journalpost: String, journalførendeEnhet: String) {
        dokarkivRestClient.ferdigstillJournalpost(journalpost, journalførendeEnhet)
    }

    fun lagJournalpostV2(deprecatedArkiverDokumentRequest: DeprecatedArkiverDokumentRequest): DeprecatedArkiverDokumentResponse {
        val request = mapTilOpprettJournalpostRequest(deprecatedArkiverDokumentRequest)
        val response =
                dokarkivRestClient.lagJournalpost(request, deprecatedArkiverDokumentRequest.forsøkFerdigstill)
        return mapTilArkiverDokumentResponse(response)
    }

    fun oppdaterJournalpost(request: OppdaterJournalpostRequest, journalpostId: String): OppdaterJournalpostResponse {
        return dokarkivRestClient.oppdaterJournalpost(supplerDefaultVerdier(request), journalpostId)
    }

    private fun hentNavnForFnr(fnr: String?): String {
        return personopplysningerService.hentPersoninfoFor(fnr)?.navn ?: error("Kan ikke hente navn")
    }

    private fun mapTilOpprettJournalpostRequest(deprecatedArkiverDokumentRequest: DeprecatedArkiverDokumentRequest): OpprettJournalpostRequest {

        val fnr = deprecatedArkiverDokumentRequest.fnr
        val navn = hentNavnForFnr(fnr)

        val metadata = dokarkivMetadata.getMetadata(deprecatedArkiverDokumentRequest.dokumenter[0])
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
                                         bruker = Bruker(IdType.FNR, fnr),
                                         dokumenter = arkivdokumenter,
                                         eksternReferanseId = MDCOperations.getCallId(),
                                         journalfoerendeEnhet = deprecatedArkiverDokumentRequest.journalførendeEnhet,
                                         sak = jpsak
        )
    }

    private fun supplerDefaultVerdier(request: OppdaterJournalpostRequest): OppdaterJournalpostRequest {
        return request.copy(sak = request.sak?.copy(sakstype = request.sak.sakstype ?: "FAGSAK"))
    }

    private fun mapTilArkivdokument(deprecatedDokument: DeprecatedDokument): ArkivDokument {
        val metadata = dokarkivMetadata.getMetadata(deprecatedDokument)
        val variantFormat: String = if (deprecatedDokument.filType == DeprecatedFilType.PDFA) {
            "ARKIV" //ustrukturert dokumentDto
        } else {
            "ORIGINAL" //strukturert dokumentDto
        }
        return ArkivDokument(brevkode = metadata.brevkode,
                             dokumentKategori = metadata.dokumentKategori,
                             tittel = metadata.tittel ?: deprecatedDokument.tittel,
                             dokumentvarianter = listOf(DokumentVariant(deprecatedDokument.filType.name,
                                                                        variantFormat,
                                                                        deprecatedDokument.dokument,
                                                                        deprecatedDokument.filnavn)))
    }

    private fun mapTilArkiverDokumentResponse(response: OpprettJournalpostResponse): DeprecatedArkiverDokumentResponse {
        return DeprecatedArkiverDokumentResponse(response.journalpostId,
                                                 response.journalpostferdigstilt)
    }

    fun lagNyttLogiskVedlegg(dokumentInfoId: String,
                             request: LogiskVedleggRequest): LogiskVedleggResponse {
        return dokarkivLogiskVedleggRestClient.opprettLogiskVedlegg(dokumentInfoId, request)
    }

    fun slettLogiskVedlegg(dokumentInfoId: String, logiskVedleggId: String) {
        dokarkivLogiskVedleggRestClient.slettLogiskVedlegg(dokumentInfoId, logiskVedleggId)
    }

}