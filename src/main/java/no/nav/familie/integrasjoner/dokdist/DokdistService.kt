package no.nav.familie.integrasjoner.dokdist

import no.nav.familie.integrasjoner.client.rest.DokdistRestClient
import no.nav.familie.integrasjoner.dokdist.domene.DistribuerJournalpostRequestTo
import no.nav.familie.integrasjoner.dokdist.domene.DistribuerJournalpostResponseTo
import org.springframework.stereotype.Service

@Service
class DokdistService(val dokdistRestClient: DokdistRestClient) {
    fun distribuerDokumentForJournalpost(journalpostId: String): DistribuerJournalpostResponseTo? {
        val req = DistribuerJournalpostRequestTo(journalpostId = journalpostId,
                                                 bestillendeFagsystem = "familie-distribusjon",
                                                 dokumentProdApp = "ba-sak")
        return dokdistRestClient.distribuerJournalpost(req)
    }
}