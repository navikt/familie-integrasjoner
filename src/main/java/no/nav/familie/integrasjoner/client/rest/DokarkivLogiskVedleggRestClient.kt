package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.integrasjoner.felles.MDCOperations
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.kontrakter.felles.dokarkiv.BulkOppdaterLogiskVedleggRequest
import no.nav.familie.kontrakter.felles.dokarkiv.LogiskVedleggRequest
import no.nav.familie.kontrakter.felles.dokarkiv.LogiskVedleggResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class DokarkivLogiskVedleggRestClient(
    @Value("\${DOKARKIV_V1_URL}") private val dokarkivUrl: URI,
    @Qualifier("dokarkivRestClient") private val restClient: RestClient,
) {
    fun opprettLogiskVedlegg(
        dokumentInfoId: String,
        request: LogiskVedleggRequest,
    ): LogiskVedleggResponse {
        val uri =
            UriComponentsBuilder
                .fromUri(dokarkivUrl)
                .path(PATH_LOGISKVEDLEGG)
                .buildAndExpand(dokumentInfoId)
                .toUri()
        try {
            return restClient
                .post()
                .uri(uri)
                .headers { it.putAll(headers()) }
                .body(request)
                .retrieve()
                .body<LogiskVedleggResponse>()!!
        } catch (e: RuntimeException) {
            val responsebody = if (e is HttpStatusCodeException) e.responseBodyAsString else ""
            val message = "Kan ikke opprette logisk vedlegg for dokumentinfo $dokumentInfoId $responsebody"
            throw OppslagException(
                message,
                "dokarkiv.logiskVedlegg",
                OppslagException.Level.MEDIUM,
                HttpStatus.INTERNAL_SERVER_ERROR,
                e,
            )
        }
    }

    fun oppdaterLogiskeVedlegg(
        dokumentinfoId: String,
        request: BulkOppdaterLogiskVedleggRequest,
    ) {
        val uri =
            UriComponentsBuilder
                .fromUri(dokarkivUrl)
                .path(PATH_OPPDATER_LOGISKVEDLEGG)
                .buildAndExpand(dokumentinfoId)
                .toUri()
        try {
            restClient
                .put()
                .uri(uri)
                .headers { it.putAll(headers()) }
                .body(request)
                .retrieve()
                .body<Any>()
        } catch (e: RuntimeException) {
            val responsebody = if (e is HttpStatusCodeException) e.responseBodyAsString else ""
            val message = "Kan ikke bulk oppdatere logiske vedlegg for dokumentinfo $dokumentinfoId $responsebody"
            throw OppslagException(
                message,
                "dokarkiv.logiskVedlegg.oppdater",
                OppslagException.Level.MEDIUM,
                HttpStatus.INTERNAL_SERVER_ERROR,
                e,
            )
        }
    }

    fun slettLogiskVedlegg(
        dokumentInfoId: String,
        logiskVedleggId: String,
    ) {
        val uri =
            UriComponentsBuilder
                .fromUri(dokarkivUrl)
                .path(PATH_SLETT_LOGISK_VEDLEGG)
                .buildAndExpand(dokumentInfoId, logiskVedleggId)
                .toUri()
        try {
            restClient
                .delete()
                .uri(uri)
                .headers { it.putAll(headers()) }
                .retrieve()
                .body<String>()
        } catch (e: RuntimeException) {
            val responsebody = if (e is HttpStatusCodeException) e.responseBodyAsString else ""
            val message = "Kan ikke slette logisk vedlegg for dokumentinfo $dokumentInfoId $responsebody"
            throw OppslagException(
                message,
                "Dokarkiv.logiskVedlegg.slett",
                OppslagException.Level.MEDIUM,
                HttpStatus.INTERNAL_SERVER_ERROR,
                e,
            )
        }
    }

    companion object {
        private const val PATH_LOGISKVEDLEGG = "rest/journalpostapi/v1/dokumentInfo/{dokumentInfo}/logiskVedlegg/"
        private const val PATH_OPPDATER_LOGISKVEDLEGG = "rest/journalpostapi/v1/dokumentInfo/{dokumentInfo}/logiskVedlegg"
        private const val PATH_SLETT_LOGISK_VEDLEGG = "$PATH_LOGISKVEDLEGG/{logiskVedleggId}"

        private const val NAV_CALL_ID = "Nav-Callid"

        private fun headers(): HttpHeaders =
            HttpHeaders().apply {
                add(NAV_CALL_ID, MDCOperations.getCallId())
            }
    }
}
