package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.http.util.UriUtil
import no.nav.familie.integrasjoner.config.incrementLoggFeil
import no.nav.familie.integrasjoner.felles.MDCOperations
import no.nav.familie.integrasjoner.felles.graphqlQuery
import no.nav.familie.integrasjoner.journalpost.JournalpostForbiddenException
import no.nav.familie.integrasjoner.journalpost.JournalpostNotFoundException
import no.nav.familie.integrasjoner.journalpost.JournalpostRequestException
import no.nav.familie.integrasjoner.journalpost.JournalpostRestClientException
import no.nav.familie.integrasjoner.journalpost.internal.JournalposterForVedleggRequest
import no.nav.familie.integrasjoner.journalpost.internal.SafErrorCode
import no.nav.familie.integrasjoner.journalpost.internal.SafJournalpostBrukerData
import no.nav.familie.integrasjoner.journalpost.internal.SafJournalpostData
import no.nav.familie.integrasjoner.journalpost.internal.SafJournalpostRequest
import no.nav.familie.integrasjoner.journalpost.internal.SafJournalpostResponse
import no.nav.familie.integrasjoner.journalpost.internal.SafRequestVariabler
import no.nav.familie.integrasjoner.journalpost.internal.tilSafRequestForBruker
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
) : AbstractRestClient(restTemplate, "saf.journalpost") {
    private val safUri = UriUtil.uri(safBaseUrl, PATH_GRAPHQL)

    fun hentJournalpost(journalpostId: String): Journalpost {
        val safJournalpostRequest =
            SafJournalpostRequest(
                SafRequestVariabler(journalpostId),
                graphqlQuery("/saf/journalpostForId.graphql"),
            )
        val response =
            postForEntity<SafJournalpostResponse<SafJournalpostData>>(
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
            val error = response.errors?.firstOrNull()
            when (error?.extensions?.code) {
                SafErrorCode.forbidden -> {
                    incrementLoggFeil("saf.hentJournalpost.forbidden")
                    throw JournalpostForbiddenException(error.message)
                }

                SafErrorCode.not_found -> {
                    incrementLoggFeil("saf.hentJournalpost.notFound")
                    throw JournalpostNotFoundException(error.message, journalpostId)
                }

                else -> {
                    responsFailure.increment()
                    incrementLoggFeil("saf.hentJournalpost")
                    throw JournalpostRestClientException(
                        "Kan ikke hente journalpost " + response.errors?.toString(),
                        null,
                        journalpostId,
                    )
                }
            }
        }
    }

    fun finnJournalposter(journalposterForVedleggRequest: JournalposterForVedleggRequest): List<Journalpost> {
        val safJournalpostRequest =
            SafJournalpostRequest(
                journalposterForVedleggRequest.tilSafRequest(),
                graphqlQuery("/saf/journalposterForBruker.graphql"),
            )
        return finnJournalposter(safJournalpostRequest)
    }

    fun finnJournalposter(journalposterForBrukerRequest: JournalposterForBrukerRequest): List<Journalpost> {
        val safJournalpostRequest =
            SafJournalpostRequest(
                journalposterForBrukerRequest.tilSafRequestForBruker(),
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
                ?: throw JournalpostRequestException(
                    "Kan ikke hente journalposter",
                    null,
                    safJournalpostRequest,
                ).also { incrementLoggFeil("saf.finnJournalposter") }
        } else {
            val tilgangFeil = response.errors?.firstOrNull {
                it.message.contains("Tilgang til ressurs ble avvist") || it.extensions.code == SafErrorCode.forbidden
            }

            if (tilgangFeil != null) {
                incrementLoggFeil("saf.finnJournalposter.forbidden")
                throw JournalpostForbiddenException(tilgangFeil.message)
            } else {
                responsFailure.increment()
                throw JournalpostRequestException(
                    "Kan ikke hente journalposter " + response.errors?.toString(),
                    null,
                    safJournalpostRequest,
                ).also { incrementLoggFeil("saf.finnJournalposter") }
            }
        }
    }

    private fun httpHeaders(): HttpHeaders =
        HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            accept = listOf(MediaType.APPLICATION_JSON)
            add(NAV_CALL_ID, MDCOperations.getCallId())
        }

    companion object {
        private const val PATH_GRAPHQL = "graphql"
        private const val NAV_CALL_ID = "Nav-Callid"
    }
}
