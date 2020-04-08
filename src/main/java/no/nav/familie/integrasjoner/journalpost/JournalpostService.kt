package no.nav.familie.integrasjoner.journalpost

import no.nav.familie.integrasjoner.client.rest.SafHentDokumentRestClient
import no.nav.familie.integrasjoner.client.rest.SafRestClient
import no.nav.familie.integrasjoner.journalpost.domene.Journalpost
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class JournalpostService @Autowired constructor(private val safRestClient: SafRestClient,
                                                private val safHentDokumentRestClient: SafHentDokumentRestClient) {

    fun hentSaksnummer(journalpostId: String): String? {
        val journalpost = safRestClient.hentJournalpost(journalpostId)
        return if (journalpost.sak != null && "GSAK" == journalpost.sak.arkivsaksystem) {
            journalpost.sak.arkivsaksnummer
        } else null
    }

    fun hentJournalpost(journalpostId: String): Journalpost {
        return safRestClient.hentJournalpost(journalpostId)
    }

    fun hentDokument(journalpostId: String, dokumentInfoId: String, variantFormat: String): ByteArray {
        return safHentDokumentRestClient.hentDokument(journalpostId, dokumentInfoId, variantFormat)
    }
}