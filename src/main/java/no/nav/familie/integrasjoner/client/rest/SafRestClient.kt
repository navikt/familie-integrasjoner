package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.http.util.UriUtil
import no.nav.familie.integrasjoner.felles.MDCOperations
import no.nav.familie.integrasjoner.felles.graphqlQuery
import no.nav.familie.integrasjoner.journalpost.JournalpostForBrukerException
import no.nav.familie.integrasjoner.journalpost.JournalpostRestClientException
import no.nav.familie.integrasjoner.journalpost.internal.SafJournalpostBrukerData
import no.nav.familie.integrasjoner.journalpost.internal.SafJournalpostData
import no.nav.familie.integrasjoner.journalpost.internal.SafJournalpostRequest
import no.nav.familie.integrasjoner.journalpost.internal.SafJournalpostResponse
import no.nav.familie.integrasjoner.journalpost.internal.SafRequestVariabler
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.JournalposterForBrukerRequest
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
                    @Qualifier("jwtBearerOboOgSts") val restTemplate: RestOperations)
    : AbstractPingableRestClient(restTemplate, "saf.journalpost") {

    override val pingUri: URI = UriUtil.uri(safBaseUrl, PATH_PING)
    private val safUri = UriUtil.uri(safBaseUrl, PATH_GRAPHQL)
    private val safHentdokumentUri = UriComponentsBuilder.fromUri(safBaseUrl).path(PATH_HENT_DOKUMENT)

    fun hentJournalpost(journalpostId: String): Journalpost {
        val safJournalpostRequest = SafJournalpostRequest(SafRequestVariabler(journalpostId),
                                                          graphqlQuery("/saf/journalpostForId.graphql"))
        try {
            val response = postForEntity<SafJournalpostResponse<SafJournalpostData>>(safUri,
                                                                                     safJournalpostRequest,
                                                                                     httpHeaders())
            if (!response.harFeil()) {
                return response.data?.journalpost ?: throw JournalpostRestClientException("Kan ikke hente journalpost",
                                                                                          null,
                                                                                          journalpostId)
            } else {
                responsFailure.increment()
                throw JournalpostRestClientException("Kan ikke hente journalpost " + response.errors?.toString(),
                                                     null,
                                                     journalpostId)
            }
        } catch (e: Exception) {
            throw JournalpostRestClientException(e.message, e, journalpostId)
        }
    }

    fun finnJournalposter(journalposterForBrukerRequest: JournalposterForBrukerRequest): List<Journalpost> {
        val safJournalpostRequest = SafJournalpostRequest(journalposterForBrukerRequest,
                                                          graphqlQuery("/saf/journalposterForBruker.graphql"))
        try {
            val response =
                    postForEntity<SafJournalpostResponse<SafJournalpostBrukerData>>(safUri,
                                                                                    safJournalpostRequest,
                                                                                    httpHeaders())
            if (!response.harFeil()) {
                return response.data?.dokumentoversiktBruker?.journalposter
                       ?: throw JournalpostForBrukerException("Kan ikke hente journalposter",
                                                              null,
                                                              journalposterForBrukerRequest)
            } else {
                responsFailure.increment()
                throw JournalpostForBrukerException("Kan ikke hente journalposter " + response.errors?.toString(),
                                                    null,
                                                    journalposterForBrukerRequest)
            }
        } catch (e: Exception) {
            throw JournalpostForBrukerException(e.message, e, journalposterForBrukerRequest)
        }
    }

    fun hentDokument(journalpostId: String, dokumentInfoId: String, variantFormat: String): ByteArray {
        val hentDokumentUri = safHentdokumentUri.buildAndExpand(journalpostId, dokumentInfoId, variantFormat).toUri()
        try {
            return getForEntity(hentDokumentUri, httpHeaders())
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

        private const val PATH_HENT_DOKUMENT = "/rest/hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}"
        private const val PATH_PING = "isAlive"
        private const val PATH_GRAPHQL = "graphql"
        private const val NAV_CALL_ID = "Nav-Callid"
    }
}
