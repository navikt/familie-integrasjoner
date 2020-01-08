package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.util.UriUtil
import no.nav.familie.integrasjoner.dokarkiv.client.KanIkkeFerdigstilleJournalpostException
import no.nav.familie.integrasjoner.dokarkiv.client.domene.FerdigstillJournalPost
import no.nav.familie.integrasjoner.dokarkiv.client.domene.OpprettJournalpostRequest
import no.nav.familie.integrasjoner.dokarkiv.client.domene.OpprettJournalpostResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestOperations
import java.net.URI

@Component
class DokarkivRestClient(@Value("\${DOKARKIV_V1_URL}") private val dokarkivUrl: URI,
                         @Qualifier("sts") private val restOperations: RestOperations)
    : AbstractPingableRestClient(restOperations, "dokarkiv.opprett") {

    override val pingUri: URI = UriUtil.uri(dokarkivUrl, PATH_PING)

    private val ferdigstillJournalPostClient = FerdigstillJourrnalPostClient(restOperations, dokarkivUrl)

    fun lagJournalpostUri(ferdigstill: Boolean): URI =
            UriUtil.uri(dokarkivUrl, PATH_JOURNALPOST, String.format(QUERY_FERDIGSTILL, ferdigstill))

    fun lagJournalpost(jp: OpprettJournalpostRequest,
                       ferdigstill: Boolean): OpprettJournalpostResponse {
        val uri = lagJournalpostUri(ferdigstill)
        return postForEntity(uri, jp) ?: error("Feil ved opprettelse av journalpost for sak ${jp.sak} ")
    }

    fun ferdigstillJournalpost(journalpostId: String, journalførendeEnhet: String) {
        ferdigstillJournalPostClient.ferdigstillJournalpost(journalpostId, journalførendeEnhet)
    }

    /**
     * Privat klasse for å gi egne metrics for ferdigstilling av jpurnalpost.
     *
     */
    private class FerdigstillJourrnalPostClient(restOperations: RestOperations, private val dokarkivUrl: URI)
        : AbstractRestClient(restOperations, "dokarkiv.ferdigstill") {

        private fun ferdigstillJournalpostUri(journalpostId: String): URI {
            return UriUtil.uri(dokarkivUrl, String.format(PATH_FERDIGSTILL_JOURNALPOST, journalpostId))
        }

        fun ferdigstillJournalpost(journalpostId: String, journalførendeEnhet: String) {
            val uri = ferdigstillJournalpostUri(journalpostId)
            try {
                patchForEntity<Any>(uri, FerdigstillJournalPost(journalførendeEnhet))
            } catch (e: RestClientResponseException) {
                if (e.rawStatusCode == HttpStatus.BAD_REQUEST.value()) {
                    throw KanIkkeFerdigstilleJournalpostException("Kan ikke ferdigstille journalpost $journalpostId")
                }
                throw e
            }
        }
    }

    companion object {
        private const val PATH_PING = "isAlive"
        private const val PATH_JOURNALPOST = "rest/journalpostapi/v1/journalpost"
        private const val QUERY_FERDIGSTILL = "foersoekFerdigstill=%b"
        private const val PATH_FERDIGSTILL_JOURNALPOST = "rest/journalpostapi/v1/journalpost/%s/ferdigstill"
    }
}