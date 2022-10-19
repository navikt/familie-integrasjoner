package no.nav.familie.integrasjoner.dokarkiv

import no.nav.familie.integrasjoner.client.rest.DokarkivLogiskVedleggRestClient
import no.nav.familie.integrasjoner.client.rest.DokarkivRestClient
import no.nav.familie.integrasjoner.client.rest.PersonInfoQuery
import no.nav.familie.integrasjoner.dokarkiv.client.domene.ArkivDokument
import no.nav.familie.integrasjoner.dokarkiv.client.domene.Dokumentvariant
import no.nav.familie.integrasjoner.dokarkiv.client.domene.OpprettJournalpostRequest
import no.nav.familie.integrasjoner.dokarkiv.client.domene.OpprettJournalpostResponse
import no.nav.familie.integrasjoner.dokarkiv.metadata.DokarkivMetadata
import no.nav.familie.integrasjoner.felles.MDCOperations
import no.nav.familie.integrasjoner.førstesidegenerator.FørstesideGeneratorService
import no.nav.familie.integrasjoner.personopplysning.PersonopplysningerService
import no.nav.familie.kontrakter.felles.BrukerIdType
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokarkiv.ArkiverDokumentResponse
import no.nav.familie.kontrakter.felles.dokarkiv.AvsenderMottaker
import no.nav.familie.kontrakter.felles.dokarkiv.DokarkivBruker
import no.nav.familie.kontrakter.felles.dokarkiv.FilType
import no.nav.familie.kontrakter.felles.dokarkiv.LogiskVedleggRequest
import no.nav.familie.kontrakter.felles.dokarkiv.LogiskVedleggResponse
import no.nav.familie.kontrakter.felles.dokarkiv.OppdaterJournalpostRequest
import no.nav.familie.kontrakter.felles.dokarkiv.OppdaterJournalpostResponse
import no.nav.familie.kontrakter.felles.dokarkiv.Sak
import no.nav.familie.kontrakter.felles.dokarkiv.v2.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Dokument
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Filtype
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import no.nav.familie.kontrakter.felles.arkivering.ArkiverDokumentRequest as DeprecatedArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.dokarkiv.ArkiverDokumentRequest as DeprecatedArkiverDokumentRequest2
import no.nav.familie.kontrakter.felles.dokarkiv.Dokument as DeprecatedDokument

@Service
class DokarkivService(
    private val dokarkivRestClient: DokarkivRestClient,
    private val personopplysningerService: PersonopplysningerService,
    private val dokarkivMetadata: DokarkivMetadata,
    private val dokarkivLogiskVedleggRestClient: DokarkivLogiskVedleggRestClient,
    private val førstesideGeneratorService: FørstesideGeneratorService
) {

    fun ferdistillJournalpost(journalpost: String, journalførendeEnhet: String, navIdent: String? = null) {
        dokarkivRestClient.ferdigstillJournalpost(journalpost, journalførendeEnhet, navIdent)
    }

    fun lagJournalpost(arkiverDokumentRequest: ArkiverDokumentRequest, navIdent: String? = null): ArkiverDokumentResponse {
        val request = mapTilOpprettJournalpostRequest(arkiverDokumentRequest)
        val response = dokarkivRestClient.lagJournalpost(request, arkiverDokumentRequest.forsøkFerdigstill, navIdent)
        return mapTilArkiverDokumentResponse(response)
    }

    fun lagJournalpostV2(
        deprecatedArkiverDokumentRequest: DeprecatedArkiverDokumentRequest,
        navIdent: String? = null
    ): ArkiverDokumentResponse {
        val request = mapTilOpprettJournalpostRequest(deprecatedArkiverDokumentRequest)
        val response = dokarkivRestClient.lagJournalpost(request, deprecatedArkiverDokumentRequest.forsøkFerdigstill, navIdent)
        return mapTilArkiverDokumentResponse(response)
    }

    fun lagJournalpostV3(arkiverDokumentRequest: DeprecatedArkiverDokumentRequest2, navIdent: String? = null): ArkiverDokumentResponse {
        val request = mapTilOpprettJournalpostRequest(arkiverDokumentRequest)
        val response = dokarkivRestClient.lagJournalpost(request, arkiverDokumentRequest.forsøkFerdigstill, navIdent)
        return mapTilArkiverDokumentResponse(response)
    }

    private fun mapTilOpprettJournalpostRequest(arkiverDokumentRequest: ArkiverDokumentRequest): OpprettJournalpostRequest {
        val dokarkivBruker = DokarkivBruker(BrukerIdType.FNR, arkiverDokumentRequest.fnr)
        val hoveddokument = arkiverDokumentRequest.hoveddokumentvarianter[0]
        val metadata = dokarkivMetadata.getMetadata(hoveddokument)
        val avsenderMottaker = arkiverDokumentRequest.avsenderMottaker ?: arkiverDokumentRequest.fnr.let {
            val navn = hentNavnForFnr(fnr = arkiverDokumentRequest.fnr, behandlingstema = metadata.tema)
            AvsenderMottaker(it, BrukerIdType.FNR, navn)
        }

        val dokumenter = mutableListOf(mapHoveddokument(arkiverDokumentRequest.hoveddokumentvarianter))
        dokumenter.addAll(arkiverDokumentRequest.vedleggsdokumenter.map(this::mapTilArkivdokument))
        val sak = arkiverDokumentRequest.fagsakId?.let {
            Sak(fagsakId = it, sakstype = "FAGSAK", fagsaksystem = metadata.fagsakSystem)
        }

        // førsteside
        arkiverDokumentRequest.førsteside?.also {
            val bytes = førstesideGeneratorService.genererForside(it, arkiverDokumentRequest.fnr)
            dokumenter += ArkivDokument(
                brevkode = metadata.brevkode,
                dokumentKategori = metadata.dokumentKategori,
                tittel = arkiverDokumentRequest.førsteside?.overskriftstittel,
                dokumentvarianter = listOf(
                    Dokumentvariant(
                        filtype = "PDFA",
                        variantformat = "ARKIV",
                        fysiskDokument = bytes,
                        filnavn = "førsteside.pdf"
                    )
                )
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
            sak = sak
        )
    }

    private fun mapTilOpprettJournalpostRequest(arkiverDokumentRequest: DeprecatedArkiverDokumentRequest2): OpprettJournalpostRequest {
        val fnr = arkiverDokumentRequest.fnr

        val metadata = dokarkivMetadata.getMetadata(arkiverDokumentRequest.hoveddokumentvarianter[0])
        val hoveddokument = mapDeprecatedHoveddokument(arkiverDokumentRequest.hoveddokumentvarianter)
        val vedleggsdokumenter = arkiverDokumentRequest.vedleggsdokumenter.map(this::mapTilArkivdokument).toMutableList()
        val jpsak: Sak? = if (arkiverDokumentRequest.fagsakId != null) {
            Sak(
                fagsakId = arkiverDokumentRequest.fagsakId,
                sakstype = "FAGSAK",
                fagsaksystem = metadata.fagsakSystem
            )
        } else {
            null
        }

        val navn = hentNavnForFnr(fnr = arkiverDokumentRequest.fnr, behandlingstema = metadata.tema)

        // førsteside
        val førsteside: ByteArray? = if (arkiverDokumentRequest.førsteside != null) {
            førstesideGeneratorService.genererForside(arkiverDokumentRequest.førsteside!!, arkiverDokumentRequest.fnr)
        } else {
            null
        }

        if (førsteside != null) {
            vedleggsdokumenter += ArkivDokument(
                brevkode = metadata.brevkode,
                dokumentKategori = metadata.dokumentKategori,
                tittel = arkiverDokumentRequest.førsteside?.overskriftsTittel,
                dokumentvarianter = listOf(
                    Dokumentvariant(
                        filtype = "PDFA",
                        variantformat = "ARKIV",
                        fysiskDokument = førsteside,
                        filnavn = "førsteside.pdf"
                    )
                )
            )
        }

        return OpprettJournalpostRequest(
            journalpostType = metadata.journalpostType,
            behandlingstema = metadata.behandlingstema?.value,
            kanal = metadata.kanal,
            tittel = metadata.tittel,
            tema = metadata.tema.name,
            avsenderMottaker = AvsenderMottaker(fnr, BrukerIdType.FNR, navn),
            bruker = DokarkivBruker(BrukerIdType.FNR, fnr),
            dokumenter = listOf(hoveddokument) + vedleggsdokumenter,
            eksternReferanseId = MDCOperations.getCallId(),
            journalfoerendeEnhet = arkiverDokumentRequest.journalførendeEnhet,
            sak = jpsak
        )
    }

    private fun mapTilOpprettJournalpostRequest(deprecatedArkiverDokumentRequest: DeprecatedArkiverDokumentRequest): OpprettJournalpostRequest {
        val metadata = dokarkivMetadata.getMetadata(deprecatedArkiverDokumentRequest.dokumenter[0])

        val fnr = deprecatedArkiverDokumentRequest.fnr
        val navn = hentNavnForFnr(fnr = fnr, behandlingstema = metadata.tema)

        val arkivdokumenter = deprecatedArkiverDokumentRequest.dokumenter.map(this::mapTilArkivdokument)
        val jpsak: Sak? = if (deprecatedArkiverDokumentRequest.fagsakId != null) {
            Sak(
                fagsakId = deprecatedArkiverDokumentRequest.fagsakId,
                sakstype = "FAGSAK",
                fagsaksystem = metadata.fagsakSystem
            )
        } else {
            null
        }

        return OpprettJournalpostRequest(
            journalpostType = metadata.journalpostType,
            behandlingstema = metadata.behandlingstema?.value,
            kanal = metadata.kanal,
            tittel = metadata.tittel,
            tema = metadata.tema.name,
            avsenderMottaker = AvsenderMottaker(fnr, BrukerIdType.FNR, navn),
            bruker = DokarkivBruker(BrukerIdType.FNR, fnr),
            dokumenter = arkivdokumenter,
            eksternReferanseId = MDCOperations.getCallId(),
            journalfoerendeEnhet = deprecatedArkiverDokumentRequest.journalførendeEnhet,
            sak = jpsak
        )
    }

    fun oppdaterJournalpost(
        request: OppdaterJournalpostRequest,
        journalpostId: String,
        navIdent: String? = null
    ): OppdaterJournalpostResponse {
        return dokarkivRestClient.oppdaterJournalpost(supplerDefaultVerdier(request), journalpostId, navIdent)
    }

    private fun hentNavnForFnr(fnr: String, behandlingstema: Tema?): String {
        return personopplysningerService.hentPersoninfo(fnr, behandlingstema ?: Tema.BAR, PersonInfoQuery.ENKEL).navn
    }

    private fun supplerDefaultVerdier(request: OppdaterJournalpostRequest): OppdaterJournalpostRequest {
        return request.copy(sak = request.sak?.copy(sakstype = request.sak?.sakstype ?: "FAGSAK"))
    }

    private fun mapHoveddokument(dokumenter: List<Dokument>): ArkivDokument {
        val dokument = dokumenter[0]
        val metadata = dokarkivMetadata.getMetadata(dokument)
        val dokumentvarianter = dokumenter.map {
            val variantFormat: String = hentVariantformat(it)
            Dokumentvariant(it.filtype.name, variantFormat, it.dokument, it.filnavn)
        }

        return ArkivDokument(
            brevkode = metadata.brevkode,
            dokumentKategori = metadata.dokumentKategori,
            tittel = metadata.tittel ?: dokument.tittel,
            dokumentvarianter = dokumentvarianter
        )
    }

    private fun mapDeprecatedHoveddokument(dokumenter: List<DeprecatedDokument>): ArkivDokument {
        val dokument = dokumenter[0]
        val metadata = dokarkivMetadata.getMetadata(dokument)
        val dokumentvarianter = dokumenter.map {
            val variantFormat: String = hentVariantformat(it)
            Dokumentvariant(it.filType.name, variantFormat, it.dokument, it.filnavn)
        }

        return ArkivDokument(
            brevkode = metadata.brevkode,
            dokumentKategori = metadata.dokumentKategori,
            tittel = metadata.tittel ?: dokument.tittel,
            dokumentvarianter = dokumentvarianter
        )
    }

    private fun hentVariantformat(dokument: DeprecatedDokument): String {
        return if (dokument.filType == FilType.PDFA) {
            "ARKIV" // ustrukturert dokumentDto
        } else {
            "ORIGINAL" // strukturert dokumentDto
        }
    }

    private fun hentVariantformat(dokument: Dokument): String {
        return if (dokument.filtype == Filtype.PDFA) {
            "ARKIV" // ustrukturert dokumentDto
        } else {
            "ORIGINAL" // strukturert dokumentDto
        }
    }

    private fun mapTilArkivdokument(dokument: DeprecatedDokument): ArkivDokument {
        val metadata = dokarkivMetadata.getMetadata(dokument)
        val variantFormat: String = hentVariantformat(dokument)
        return ArkivDokument(
            brevkode = metadata.brevkode,
            dokumentKategori = metadata.dokumentKategori,
            tittel = metadata.tittel ?: dokument.tittel,
            dokumentvarianter = listOf(
                Dokumentvariant(
                    dokument.filType.name,
                    variantFormat,
                    dokument.dokument,
                    dokument.filnavn
                )
            )
        )
    }

    private fun mapTilArkivdokument(dokument: Dokument): ArkivDokument {
        val metadata = dokarkivMetadata.getMetadata(dokument)
        val variantFormat: String = hentVariantformat(dokument)
        return ArkivDokument(
            brevkode = metadata.brevkode,
            dokumentKategori = metadata.dokumentKategori,
            tittel = metadata.tittel ?: dokument.tittel,
            dokumentvarianter = listOf(
                Dokumentvariant(
                    dokument.filtype.name,
                    variantFormat,
                    dokument.dokument,
                    dokument.filnavn
                )
            )
        )
    }

    private fun mapTilArkiverDokumentResponse(response: OpprettJournalpostResponse): ArkiverDokumentResponse {
        return ArkiverDokumentResponse(
            response.journalpostId!!,
            response.journalpostferdigstilt ?: false,
            response.dokumenter
        )
    }

    fun lagNyttLogiskVedlegg(
        dokumentInfoId: String,
        request: LogiskVedleggRequest
    ): LogiskVedleggResponse {
        return dokarkivLogiskVedleggRestClient.opprettLogiskVedlegg(dokumentInfoId, request)
    }

    fun slettLogiskVedlegg(dokumentInfoId: String, logiskVedleggId: String) {
        dokarkivLogiskVedleggRestClient.slettLogiskVedlegg(dokumentInfoId, logiskVedleggId)
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(DokarkivService::class.java)
    }
}
