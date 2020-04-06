package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.integrasjoner.felles.MDCOperations
import no.nav.familie.integrasjoner.journalpost.JournalpostRestClientException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class SafHentDokumentRestClient(@Value("\${SAF_URL}") safBaseUrl: URI,
                                @Qualifier("propagateAuth") val restTemplate: RestOperations)
    : AbstractPingableRestClient(restTemplate, "saf.journalpost") {

    override val pingUri: URI = UriComponentsBuilder.fromUri(safBaseUrl).path(PATH_PING).build().toUri()
    private val safHentdokumentUri = UriComponentsBuilder.fromUri(safBaseUrl).path(PATH_HENT_DOKUMENT)

    private fun httpHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            add(NAV_CALL_ID, MDCOperations.getCallId())
            accept = listOf(MediaType.ALL)
        }
    }

    fun hentDokument(journalpostId: String, dokumentInfoId: String, variantFormat: String?): ByteArray {
        val hentDokumentUri = safHentdokumentUri.buildAndExpand(journalpostId, dokumentInfoId, variantFormat ?: "ARKIV").toUri()
        try {
            return getForEntity(hentDokumentUri, httpHeaders())
        } catch (e: Exception) {
            throw JournalpostRestClientException(e.message, e, journalpostId)
        }
    }

    companion object {
        private const val PATH_PING = "/isAlive"
        private const val PATH_HENT_DOKUMENT = "/rest/hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}"
        private const val NAV_CALL_ID = "Nav-Callid"
    }
}
