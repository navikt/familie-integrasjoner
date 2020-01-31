package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.http.util.UriUtil
import no.nav.familie.integrasjoner.felles.MDCOperations
import no.nav.familie.integrasjoner.journalpost.JournalpostRestClientException
import no.nav.familie.integrasjoner.journalpost.internal.Journalpost
import no.nav.familie.integrasjoner.journalpost.internal.SafJournalpostRequest
import no.nav.familie.integrasjoner.journalpost.internal.SafJournalpostResponse
import no.nav.familie.integrasjoner.journalpost.internal.SafRequestVariable
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import java.net.URI

@Service
class SafRestClient(@Value("\${SAF_URL}") safBaseUrl: URI,
                    @Qualifier("sts") val restTemplate: RestOperations)
    : AbstractPingableRestClient(restTemplate, "saf.journalpost") {

    override val pingUri: URI = UriUtil.uri(safBaseUrl, PATH_PING)
    private val safUri = UriUtil.uri(safBaseUrl, PATH_GRAPHQL)

    fun hentJournalpost(journalpostId: String): Journalpost {
        val safJournalpostRequest = SafJournalpostRequest(SafRequestVariable(journalpostId))
        try {
            val response = postForEntity<SafJournalpostResponse>(safUri,
                                                                 safJournalpostRequest,
                                                                 httpHeaders())
            if (response != null && !response.harFeil()) {
                return  response?.data?.journalpost ?: throw JournalpostRestClientException("Kan ikke hente journalpost", null, journalpostId)

            } else {
                responsFailure.increment()
                throw JournalpostRestClientException("Kan ikke hente journalpost " + response?.errors?.toString(),
                                                     null,
                                                     journalpostId)
            }
        } catch (e: Exception) {
            throw JournalpostRestClientException(e.message, e, journalpostId)
        }
    }

    private fun httpHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            add(NAV_CALL_ID, MDCOperations.getCallId())
            contentType = MediaType.APPLICATION_JSON
            accept = listOf(MediaType.APPLICATION_JSON)
        }
    }

    companion object {
        private const val PATH_PING = "isAlive"
        private const val PATH_GRAPHQL = "graphql"
        private const val NAV_CALL_ID = "Nav-Callid"
    }

}
