package no.nav.familie.integrasjoner.journalpost

import no.nav.familie.integrasjoner.client.rest.SafRestClient
import no.nav.familie.integrasjoner.config.SikkerhetContext
import no.nav.familie.integrasjoner.felles.ForbiddenException
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.JournalposterForBrukerRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class JournalpostService @Autowired constructor(private val safRestClient: SafRestClient) {

    fun hentSaksnummer(journalpostId: String): String? {
        val journalpost = safRestClient.hentJournalpost(journalpostId)
        return if (journalpost.sak != null && journalpost.sak?.arkivsaksystem == "GSAK") {
            journalpost.sak?.arkivsaksnummer
        } else null
    }

    fun hentJournalpost(journalpostId: String): Journalpost {
        return safRestClient.hentJournalpost(journalpostId)
    }

    fun finnJournalposter(journalposterForBrukerRequest: JournalposterForBrukerRequest): List<Journalpost> {
        return safRestClient.finnJournalposter(journalposterForBrukerRequest)
    }

    fun hentDokument(journalpostId: String, dokumentInfoId: String, variantFormat: String): ByteArray {
        if (SikkerhetContext.erSystemKontekst()) throw ForbiddenException("Systembruker har ikke tilgang til å hente dokumenter.")

        return safRestClient.hentDokument(journalpostId, dokumentInfoId, variantFormat)
    }
}