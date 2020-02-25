package no.nav.familie.integrasjoner.dokarkiv

import no.nav.familie.integrasjoner.client.rest.DokarkivRestClient
import no.nav.familie.integrasjoner.dokarkiv.api.ArkiverDokumentRequest
import no.nav.familie.integrasjoner.dokarkiv.api.ArkiverDokumentResponse
import no.nav.familie.integrasjoner.dokarkiv.api.Dokument
import no.nav.familie.integrasjoner.dokarkiv.api.FilType
import no.nav.familie.integrasjoner.dokarkiv.client.domene.*
import no.nav.familie.integrasjoner.dokarkiv.metadata.DokarkivMetadata
import no.nav.familie.integrasjoner.felles.MDCOperations
import no.nav.familie.integrasjoner.personopplysning.PersonopplysningerService
import org.springframework.stereotype.Service

@Service
class DokarkivService(private val dokarkivRestClient: DokarkivRestClient,
                      private val personopplysningerService: PersonopplysningerService,
                      private val dokarkivMetadata: DokarkivMetadata) {

    fun lagJournalpostV2(arkiverDokumentRequest: ArkiverDokumentRequest): ArkiverDokumentResponse {
        val request = mapTilOpprettJournalpostRequest(arkiverDokumentRequest)
        val response =
                dokarkivRestClient.lagJournalpost(request, arkiverDokumentRequest.forsøkFerdigstill)
        return mapTilArkiverDokumentResponse(response)
    }

    fun ferdistillJournalpost(journalpost: String, journalførendeEnhet: String) {
        dokarkivRestClient.ferdigstillJournalpost(journalpost, journalførendeEnhet)
    }

    private fun hentNavnForFnr(fnr: String?): String {
        return personopplysningerService.hentPersoninfoFor(fnr)?.navn ?: error("Kan ikke hente navn")
    }

    private fun mapTilOpprettJournalpostRequest(arkiverDokumentRequest: ArkiverDokumentRequest): OpprettJournalpostRequest {

        val fnr = arkiverDokumentRequest.fnr
        val navn = hentNavnForFnr(fnr)

        val metadata = dokarkivMetadata.getMetadata(arkiverDokumentRequest.dokumenter[0])
        val arkivdokumenter = arkiverDokumentRequest.dokumenter.map(this::mapTilArkivdokument)
        val jpsak: Sak? = if (arkiverDokumentRequest.fagsakId != null) Sak(
                fagsakId = arkiverDokumentRequest.fagsakId,
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
                                         journalfoerendeEnhet = arkiverDokumentRequest.journalførendeEnhet,
                                         sak = jpsak
        )
    }

    private fun mapTilArkivdokument(dokument: Dokument): ArkivDokument {
        val metadata = dokarkivMetadata.getMetadata(dokument)
        val variantFormat: String = if (dokument.filType == FilType.PDFA) {
            "ARKIV" //ustrukturert dokumentDto
        } else {
            "ORIGINAL" //strukturert dokumentDto
        }
        return ArkivDokument(brevkode = metadata.brevkode,
                             dokumentKategori = metadata.dokumentKategori,
                             tittel = metadata.tittel ?: dokument.tittel,
                             dokumentvarianter = listOf(DokumentVariant(dokument.filType.name,
                                                                        variantFormat,
                                                                        dokument.dokument,
                                                                        dokument.filnavn)))
    }

    private fun mapTilArkiverDokumentResponse(response: OpprettJournalpostResponse): ArkiverDokumentResponse {
        return ArkiverDokumentResponse(response.journalpostId,
                                       response.journalpostferdigstilt)
    }

}