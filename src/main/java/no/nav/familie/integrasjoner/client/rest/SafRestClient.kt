package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.http.util.UriUtil
import no.nav.familie.integrasjoner.felles.MDCOperations
import no.nav.familie.integrasjoner.felles.graphqlQuery
import no.nav.familie.integrasjoner.journalpost.JournalpostForBrukerException
import no.nav.familie.integrasjoner.journalpost.JournalpostForbiddenException
import no.nav.familie.integrasjoner.journalpost.JournalpostRestClientException
import no.nav.familie.integrasjoner.journalpost.internal.JournalposterForVedleggRequest
import no.nav.familie.integrasjoner.journalpost.internal.SafErrorCode
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
import java.net.URI

@Service
class SafRestClient(
    @Value("\${SAF_URL}") safBaseUrl: URI,
    @Qualifier("jwtBearer") val restTemplate: RestOperations,
) :
    AbstractRestClient(restTemplate, "saf.journalpost") {

    private val safUri = UriUtil.uri(safBaseUrl, PATH_GRAPHQL)

    fun hentJournalpost(journalpostId: String): Journalpost {
        val safJournalpostRequest = SafJournalpostRequest(
            SafRequestVariabler(journalpostId),
            graphqlQuery("/saf/journalpostForId.graphql"),
        )
        val response = postForEntity<SafJournalpostResponse<SafJournalpostData>>(
            safUri,
            safJournalpostRequest,
            httpHeaders(),
        )
        if (!response.harFeil()) {
            return response.data?.journalpost ?: throw JournalpostRestClientException(
                "Kan ikke hente journalpost",
                null,
                journalpostId,
            )
        } else {
            val tilgangFeil = response.errors?.firstOrNull { it.extensions.code == SafErrorCode.forbidden }

            if (tilgangFeil != null) {
                throw JournalpostForbiddenException(tilgangFeil.message)
            } else {
                responsFailure.increment()
                throw JournalpostRestClientException(
                    "Kan ikke hente journalpost " + response.errors?.toString(),
                    null,
                    journalpostId,
                )
            }
        }
    }

    fun finnJournalposter(journalposterForVedleggRequest: JournalposterForVedleggRequest): List<Journalpost> {
        secureLogger.info("journalposterForVedleggRequest: $journalposterForVedleggRequest")
        val safJournalpostRequest = SafJournalpostRequest(
            journalposterForVedleggRequest.tilSafRequest(),
            graphqlQuery("/saf/journalposterForBruker.graphql"),
        )
        secureLogger.info("safJournalpostRequest: $safJournalpostRequest")
        secureLogger.info("variables i graphql spørring: ${safJournalpostRequest.variables}")
        return finnJournalposter(safJournalpostRequest)
    }

    fun finnJournalposter(journalposterForBrukerRequest: JournalposterForBrukerRequest): List<Journalpost> {
        val safJournalpostRequest = SafJournalpostRequest(
            journalposterForBrukerRequest,
            graphqlQuery("/saf/journalposterForBruker.graphql"),
        )
        return finnJournalposter(safJournalpostRequest)
    }

    fun finnJournalposter(safJournalpostRequest: SafJournalpostRequest): List<Journalpost> {
        val response =
            postForEntity<SafJournalpostResponse<SafJournalpostBrukerData>>(
                safUri,
                safJournalpostRequest,
                httpHeaders(),
            )

        if (!response.harFeil()) {
            return response.data?.dokumentoversiktBruker?.journalposter
                ?: throw JournalpostForBrukerException(
                    "Kan ikke hente journalposter",
                    null,
                    safJournalpostRequest,
                )
        } else {
            val tilgangFeil = response.errors?.firstOrNull { it.message?.contains("Tilgang til ressurs ble avvist") == true }

            if (tilgangFeil != null) {
                throw JournalpostForbiddenException(tilgangFeil.message)
            } else {
                responsFailure.increment()
                throw JournalpostForBrukerException(
                    "Kan ikke hente journalposter " + response.errors?.toString(),
                    null,
                    safJournalpostRequest,
                )
            }
        }
    }

    private fun httpHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            accept = listOf(MediaType.APPLICATION_JSON)
            add(NAV_CALL_ID, MDCOperations.getCallId())
        }
    }

    companion object {

        private const val PATH_GRAPHQL = "graphql"
        private const val NAV_CALL_ID = "Nav-Callid"
    }
}
