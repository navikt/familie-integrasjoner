package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.http.sts.StsRestClient
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.kontrakter.felles.dokarkiv.LogiskVedleggRequest
import no.nav.familie.kontrakter.felles.dokarkiv.LogiskVedleggResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class DokarkivLogiskVedleggRestClient(@Value("\${DOKARKIV_V1_URL}") private val dokarkivUrl: URI,
                                      @Qualifier("jwt-sts") private val restOperations: RestOperations,
                                      private val stsRestClient: StsRestClient)
    : AbstractRestClient(restOperations, "dokarkiv.logiskvedlegg.opprett") {

    private val slettVedleggClient = SlettLogiskVedleggClient(restOperations)

    fun opprettLogiskVedlegg(dokumentInfoId: String, request: LogiskVedleggRequest): LogiskVedleggResponse {
        val uri = UriComponentsBuilder
                .fromUri(dokarkivUrl)
                .path(PATH_LOGISKVEDLEGG)
                .buildAndExpand(dokumentInfoId)
                .toUri()
        try {
            return postForEntity(uri, request, httpHeaders(stsRestClient))
        } catch (e: RuntimeException) {
            val responsebody = if (e is HttpStatusCodeException) e.responseBodyAsString else ""
            val message = "Kan ikke opprette logisk vedlegg for dokumentinfo $dokumentInfoId $responsebody"
            throw OppslagException(message,
                                   "Dokarkiv.logiskVedlegg.opprett",
                                   OppslagException.Level.MEDIUM,
                                   HttpStatus.INTERNAL_SERVER_ERROR,
                                   e)
        }
    }

    fun slettLogiskVedlegg(dokumentInfoId: String, logiskVedleggId: String) {
        val uri = UriComponentsBuilder
                .fromUri(dokarkivUrl)
                .path(PATH_SLETT_LOGISK_VEDLEGG)
                .buildAndExpand(dokumentInfoId, logiskVedleggId)
                .toUri()
        try {
            slettVedleggClient.slettLogiskVedlegg(uri, stsRestClient)
        } catch (e: RuntimeException) {
            val responsebody = if (e is HttpStatusCodeException) e.responseBodyAsString else ""
            val message = "Kan ikke slette logisk vedlegg for dokumentinfo $dokumentInfoId $responsebody"
            throw OppslagException(message,
                                   "Dokarkiv.logiskVedlegg.slett",
                                   OppslagException.Level.MEDIUM,
                                   HttpStatus.INTERNAL_SERVER_ERROR,
                                   e)
        }
    }

    //Egen klasse for egne metrikker
    private class SlettLogiskVedleggClient(restOperations: RestOperations)
        : AbstractRestClient(restOperations, "dokarkiv.logiskvedlegg.slett") {

        fun slettLogiskVedlegg(uri: URI, stsRestClient: StsRestClient) {
            deleteForEntity<String>(uri, null, httpHeaders(stsRestClient))
        }
    }

    companion object {
        private const val PATH_LOGISKVEDLEGG = "rest/journalpostapi/v1/dokumentInfo/{dokumentInfo}/logiskVedlegg/"
        private const val PATH_SLETT_LOGISK_VEDLEGG = "$PATH_LOGISKVEDLEGG/{logiskVedleggId}"
    }
}

private fun httpHeaders(stsRestClient: StsRestClient) = HttpHeaders().apply {
    add("Nav-Consumer-Token", "Bearer ${stsRestClient.systemOIDCToken}")
}