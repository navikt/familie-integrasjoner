package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.http.util.UriUtil
import no.nav.familie.integrasjoner.felles.MDCOperations
import no.nav.familie.integrasjoner.journalpost.JournalpostRestClientException
import no.nav.familie.integrasjoner.journalpost.domene.Journalpost
import no.nav.familie.integrasjoner.journalpost.internal.SafJournalpostRequest
import no.nav.familie.integrasjoner.journalpost.internal.SafJournalpostResponse
import no.nav.familie.integrasjoner.journalpost.internal.SafRequestVariable
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class SafRestClient(@Value("\${SAF_URL}") safBaseUrl: URI,
                    @Qualifier("sts") val restTemplate: RestOperations)
    : AbstractPingableRestClient(restTemplate, "saf.journalpost") {

    override val pingUri: URI = UriUtil.uri(safBaseUrl, PATH_PING)
    private val safGraphQlUri = UriUtil.uri(safBaseUrl, PATH_GRAPHQL)
    private val safHentdokumentUri = UriComponentsBuilder.fromUri(safBaseUrl).path(PATH_HENT_DOKUMENT)


    fun hentJournalpost(journalpostId: String): Journalpost {
        val safJournalpostRequest = SafJournalpostRequest(SafRequestVariable(journalpostId))
        try {
            val response = postForEntity<SafJournalpostResponse>(safGraphQlUri,
                                                                 safJournalpostRequest,
                                                                 httpHeaders())
            if (response != null && !response.harFeil()) {
                return response.data?.journalpost ?: throw JournalpostRestClientException("Kan ikke hente journalpost", null, journalpostId)

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

    fun hentDokument(journalpostId: String, dokumentInfoId: String, variantFormat: String?): ByteArray {
        val hentDokumentUri = safHentdokumentUri.buildAndExpand(journalpostId, dokumentInfoId, variantFormat ?: "ARKIV").toUri()
        try {
            return getForEntity(hentDokumentUri)
        } catch (e: Exception) {
            throw JournalpostRestClientException(e.message, e, journalpostId)
        }
    }

    companion object {
        private const val PATH_PING = "isAlive"
        private const val PATH_GRAPHQL = "graphql"
        private const val PATH_HENT_DOKUMENT = "/rest/hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}"
        private const val NAV_CALL_ID = "Nav-Callid"
    }
}
