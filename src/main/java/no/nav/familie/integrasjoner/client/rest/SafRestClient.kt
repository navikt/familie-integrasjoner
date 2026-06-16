package no.nav.familie.integrasjoner.client.rest

import io.micrometer.core.instrument.Metrics
import no.nav.familie.integrasjoner.config.incrementLoggFeil
import no.nav.familie.integrasjoner.felles.MDCOperations
import no.nav.familie.integrasjoner.felles.UriUtil
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
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.net.URI

@Service
class SafRestClient(
    @Value("\${SAF_URL}") safBaseUrl: URI,
    @Qualifier("safRestClient") private val restClient: RestClient,
) {
    private val safUri = UriUtil.uri(safBaseUrl, PATH_GRAPHQL)
    private val responsFailure = Metrics.counter("restclient.response.failure", "client", "saf.journalpost")

    fun hentJournalpost(journalpostId: String): Journalpost {
        val safJournalpostRequest =
            SafJournalpostRequest(
                SafRequestVariabler(journalpostId),
                graphqlQuery("/saf/journalpostForId.graphql"),
            )
        val response =
            restClient
                .post()
                .uri(safUri)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(NAV_CALL_ID, MDCOperations.getCallId())
                .body(safJournalpostRequest)
                .retrieve()
                .body<SafJournalpostResponse<SafJournalpostData>>()!!
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
            restClient
                .post()
                .uri(safUri)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(NAV_CALL_ID, MDCOperations.getCallId())
                .body(safJournalpostRequest)
                .retrieve()
                .body<SafJournalpostResponse<SafJournalpostBrukerData>>()!!

        if (!response.harFeil()) {
            return response.data?.dokumentoversiktBruker?.journalposter
                ?: throw JournalpostRequestException(
                    "Kan ikke hente journalposter",
                    null,
                    safJournalpostRequest,
                ).also { incrementLoggFeil("saf.finnJournalposter") }
        } else {
            val tilgangFeil =
                response.errors?.firstOrNull {
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

    companion object {
        private const val PATH_GRAPHQL = "graphql"
        private const val NAV_CALL_ID = "Nav-Callid"
    }
}
