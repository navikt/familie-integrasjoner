package no.nav.familie.integrasjoner.dokdist

import no.nav.familie.integrasjoner.client.rest.DokdistRestClient
import no.nav.familie.integrasjoner.dokdist.domene.DistribuerJournalpostRequestTo
import no.nav.familie.integrasjoner.dokdist.domene.DistribuerJournalpostResponseTo
import no.nav.familie.kontrakter.felles.dokdist.DistribuerJournalpostRequest
import org.springframework.stereotype.Service

@Service
class DokdistService(val dokdistRestClient: DokdistRestClient) {
    fun distribuerDokumentForJournalpost(request: DistribuerJournalpostRequest): DistribuerJournalpostResponseTo? {
        return dokdistRestClient.distribuerJournalpost(mapTilDistribuerJournalpostRequestTo(request))
    }

    private fun mapTilDistribuerJournalpostRequestTo(request: DistribuerJournalpostRequest): DistribuerJournalpostRequestTo {
        return DistribuerJournalpostRequestTo(journalpostId = request.journalpostId,
            bestillendeFagsystem = request.bestillendeFagsystem.name,
            dokumentProdApp = request.dokumentProdApp)
    }
}