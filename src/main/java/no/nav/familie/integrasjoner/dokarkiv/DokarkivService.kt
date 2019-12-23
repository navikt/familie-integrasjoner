package no.nav.familie.integrasjoner.dokarkiv

import no.nav.familie.integrasjoner.dokarkiv.api.*
import no.nav.familie.integrasjoner.dokarkiv.client.DokarkivClient
import no.nav.familie.integrasjoner.dokarkiv.client.domene.*
import no.nav.familie.integrasjoner.dokarkiv.metadata.AbstractDokumentMetadata
import no.nav.familie.integrasjoner.dokarkiv.metadata.KontanstøtteSøknadMetadata
import no.nav.familie.integrasjoner.dokarkiv.metadata.KontanstøtteSøknadVedleggMetadata
import no.nav.familie.integrasjoner.felles.MDCOperations
import no.nav.familie.integrasjoner.personopplysning.PersonopplysningerService
import org.springframework.stereotype.Service

@Service
class DokarkivService(private val dokarkivClient: DokarkivClient,
                      private val personopplysningerService: PersonopplysningerService) {

    fun lagInngåendeJournalpost(arkiverDokumentRequest: ArkiverDokumentRequest): ArkiverDokumentResponse {
        val request = mapTilOpprettJournalpostRequest(arkiverDokumentRequest)
        val response = dokarkivClient.lagJournalpost(request, arkiverDokumentRequest.isForsøkFerdigstill)
        return mapTilArkiverDokumentResponse(response)
    }

    fun ferdistillJournalpost(journalpost: String, journalførendeEnhet: String?) {
        dokarkivClient.ferdigstillJournalpost(journalpost, journalførendeEnhet)
    }

    private fun hentNavnForFnr(fnr: String?): String {
        return personopplysningerService.hentPersoninfoFor(fnr)?.navn ?: error("Kan ikke hente navn")
    }

    private fun mapTilOpprettJournalpostRequest(arkiverDokumentRequest: ArkiverDokumentRequest): OpprettJournalpostRequest {

        val fnr = arkiverDokumentRequest.fnr
        val navn = hentNavnForFnr(arkiverDokumentRequest.fnr)

        val metadataHoveddokument = getMetadata(arkiverDokumentRequest.dokumenter[0])
        val arkivdokumenter = arkiverDokumentRequest.dokumenter.map(this::mapTilArkivdokument)

        return OpprettJournalpostRequest(journalpostType = JournalpostType.INNGAAENDE,
                                         behandlingstema = metadataHoveddokument.behandlingstema,
                                         kanal = metadataHoveddokument.kanal,
                                         tittel = metadataHoveddokument.tittel,
                                         tema = metadataHoveddokument.tema,
                                         avsenderMottaker = AvsenderMottaker(fnr, IdType.FNR, navn),
                                         bruker = Bruker(IdType.FNR, fnr),
                                         dokumenter = arkivdokumenter,
                                         eksternReferanseId = MDCOperations.getCallId()
                // sak = når vi tar over fagsak, så må dennne settes til vår. For BRUT001 behandling, så kan ikke denne settes
        )
    }

    private fun getMetadata(dokument: Dokument): AbstractDokumentMetadata {
        return METADATA[dokument.dokumentType.name] ?: error("Ukjent dokumenttype ${dokument.dokumentType}")
    }

    private fun mapTilArkivdokument(dokument: Dokument): ArkivDokument {
        val metadata = getMetadata(dokument)
        val variantFormat: String = if (dokument.filType == FilType.PDFA) {
            "ARKIV" //ustrukturert dokumentDto
        } else {
            "ORIGINAL" //strukturert dokumentDto
        }
        return ArkivDokument(brevkode = metadata.brevkode,
                             dokumentKategori = metadata.dokumentKategori,
                             tittel = metadata.tittel,
                             dokumentvarianter = listOf(DokumentVariant(dokument.filType.name,
                                                                        variantFormat,
                                                                        dokument.dokument,
                                                                        dokument.filnavn)))
    }

    private fun mapTilArkiverDokumentResponse(response: OpprettJournalpostResponse): ArkiverDokumentResponse {
        return ArkiverDokumentResponse(response.journalpostId,
                                       response.journalpostferdigstilt)
    }

    companion object {
        private val METADATA =
                mapOf(DokumentType.KONTANTSTØTTE_SØKNAD.name to KontanstøtteSøknadMetadata(),
                      DokumentType.KONTANTSTØTTE_SØKNAD_VEDLEGG.name to KontanstøtteSøknadVedleggMetadata())
    }

}