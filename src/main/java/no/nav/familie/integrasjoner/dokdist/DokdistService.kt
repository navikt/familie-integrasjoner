package no.nav.familie.integrasjoner.dokdist

import no.nav.familie.integrasjoner.client.rest.DokdistRestClient
import no.nav.familie.integrasjoner.dokdist.domene.DistribuerJournalpostRequestTo
import no.nav.familie.integrasjoner.dokdist.domene.DistribuerJournalpostResponseTo
import org.springframework.stereotype.Service

@Service
class DokdistService(val dokdistRestClient: DokdistRestClient) {
    fun distribuerDokumentForJournalpost(journalpostId: String, dokumentProdApp: String): DistribuerJournalpostResponseTo? {
        val request = DistribuerJournalpostRequestTo(journalpostId = journalpostId,
                                                 bestillendeFagsystem = "IT01",
                                                 dokumentProdApp = dokumentProdApp)
        return dokdistRestClient.distribuerJournalpost(request)
    }
}