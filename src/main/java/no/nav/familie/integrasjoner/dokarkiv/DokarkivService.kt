package no.nav.familie.integrasjoner.dokarkiv

import no.nav.familie.integrasjoner.aktør.AktørService
import no.nav.familie.integrasjoner.dokarkiv.api.*
import no.nav.familie.integrasjoner.dokarkiv.client.DokarkivClient
import no.nav.familie.integrasjoner.dokarkiv.client.domene.*
import no.nav.familie.integrasjoner.dokarkiv.metadata.KontanstøtteSøknadMetadata
import no.nav.familie.integrasjoner.dokarkiv.metadata.KontanstøtteSøknadVedleggMetadata
import no.nav.familie.integrasjoner.felles.MDCOperations
import no.nav.familie.integrasjoner.personopplysning.PersonopplysningerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.util.Assert

@Service
class DokarkivService @Autowired constructor(private val dokarkivClient: DokarkivClient,
                                             private val personopplysningerService: PersonopplysningerService,
                                             private val aktørService: AktørService) {
    fun lagInngåendeJournalpost(arkiverDokumentRequest: ArkiverDokumentRequest): ArkiverDokumentResponse {
        val fnr = arkiverDokumentRequest.fnr
        val navn = hentNavnForFnr(fnr)
        val request = mapTilOpprettJournalpostRequest(fnr, navn, arkiverDokumentRequest.dokumenter)
        val response = dokarkivClient.lagJournalpost(request, arkiverDokumentRequest.isForsøkFerdigstill, fnr)
        return mapTilArkiverDokumentResponse(response)
    }

    fun ferdistillJournalpost(journalpost: String, journalførendeEnhet: String?) {
        dokarkivClient.ferdigstillJournalpost(journalpost, journalførendeEnhet)
    }

    private fun hentNavnForFnr(fnr: String?): String {
        var navn: String? = null
        val personInfoResponse = personopplysningerService.hentPersoninfoFor(fnr)
        if (personInfoResponse != null) {
            navn = personInfoResponse.navn
        }
        if (navn == null) {
            throw RuntimeException("Kan ikke hente navn")
        }
        return navn
    }

    private fun mapTilOpprettJournalpostRequest(fnr: String,
                                                navn: String,
                                                dokumenter: List<Dokument>): OpprettJournalpostRequest {
        val metadataHoveddokument =
                METADATA[dokumenter[0].dokumentType.name]!!
        Assert.notNull(metadataHoveddokument,
                       "Ukjent dokumenttype " + dokumenter[0].dokumentType)
        val arkivdokumenter = dokumenter.map(this::mapTilArkivdokument)

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

    private fun mapTilArkivdokument(dokument: Dokument): ArkivDokument {
        val metadata = METADATA[dokument.dokumentType.name] ?: error("Ukjent dokumenttype ${dokument.dokumentType}")
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