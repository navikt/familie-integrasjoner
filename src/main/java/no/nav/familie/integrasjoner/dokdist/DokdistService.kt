package no.nav.familie.integrasjoner.dokdist

import no.nav.familie.integrasjoner.client.rest.DokdistRestClient
import no.nav.familie.integrasjoner.dokdist.api.DistribuerJournalpostRequest
import no.nav.familie.integrasjoner.dokdist.domene.DistribuerJournalpostRequestTo
import no.nav.familie.integrasjoner.dokdist.domene.DistribuerJournalpostResponseTo
import org.springframework.stereotype.Service

@Service
class DokdistService(val dokdistRestClient: DokdistRestClient) {
    fun distribuerDokumentForJournalpost(request: DistribuerJournalpostRequest): DistribuerJournalpostResponseTo? {
        return dokdistRestClient.distribuerJournalpost(mapTilDistribuerJournalpostRequestTo(request))
    }

    private fun mapTilDistribuerJournalpostRequestTo(request: DistribuerJournalpostRequest): DistribuerJournalpostRequestTo {
        return DistribuerJournalpostRequestTo(journalpostId = request.journalpostId,
            bestillendeFagsystem = request.bestillendeFagsystem,
            dokumentProdApp = request.dokumentProdApp)
    }
}