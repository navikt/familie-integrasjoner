package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.integrasjoner.config.incrementLoggFeil
import no.nav.familie.integrasjoner.felles.MDCOperations
import no.nav.familie.integrasjoner.journalpost.JournalpostForbiddenException
import no.nav.familie.integrasjoner.journalpost.JournalpostRestClientException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

/**
 * Henting av dokumenter krever saksbehandler context.
 * Bruk av denne clienten vil fungere med et azure systemtoken,
 * men vil stange i saf sin implementasjon mot abac.
 */
@Service
class SafHentDokumentRestClient(
    @Value("\${SAF_URL}") safBaseUrl: URI,
    @Qualifier("jwtBearer") val restTemplate: RestOperations,
) : AbstractRestClient(restTemplate, "saf.journalpost") {
    private val safHentdokumentUri = UriComponentsBuilder.fromUri(safBaseUrl).path(PATH_HENT_DOKUMENT)

    private fun httpHeaders(): HttpHeaders =
        HttpHeaders().apply {
            accept = listOf(MediaType.ALL)
            add(NAV_CALL_ID, MDCOperations.getCallId())
        }

    fun hentDokument(
        journalpostId: String,
        dokumentInfoId: String,
        variantFormat: String,
    ): ByteArray {
        val hentDokumentUri = safHentdokumentUri.buildAndExpand(journalpostId, dokumentInfoId, variantFormat).toUri()
        try {
            return getForEntity(hentDokumentUri, httpHeaders())
        } catch (e: HttpClientErrorException.Forbidden) {
            incrementLoggFeil("saf.dokument.forbidden")
            throw JournalpostForbiddenException(e.message, e)
        } catch (e: Exception) {
            incrementLoggFeil("saf.dokument")
            throw JournalpostRestClientException(e.message, e, journalpostId)
        }
    }

    companion object {
        private const val PATH_HENT_DOKUMENT = "/rest/hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}"
        private const val NAV_CALL_ID = "Nav-Callid"
    }
}
