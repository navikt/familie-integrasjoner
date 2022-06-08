package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.http.util.UriUtil
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.førstesidegenerator.domene.PostFørstesideRequest
import no.nav.familie.integrasjoner.førstesidegenerator.domene.PostFørstesideResponse
import no.nav.familie.log.mdc.MDCConstants
import org.slf4j.MDC
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
class FørstesidegeneratorClient(
    @Value("\${FORSTESIDEGENERATOR_URL}") private val førstesidegeneratorURI: URI,
    @Qualifier("sts") private val restTemplate: RestOperations
) :
    AbstractPingableRestClient(restTemplate, "førstesidegenterator") {

    override val pingUri: URI = UriUtil.uri(førstesidegeneratorURI, PATH_PING)

    fun genererFørsteside(dto: PostFørstesideRequest): PostFørstesideResponse {
        val uri = UriComponentsBuilder.fromUri(førstesidegeneratorURI).path(PATH_GENERER).build().toUri()
        return Result.runCatching { postForEntity<PostFørstesideResponse>(uri, dto, httpHeaders()) }
            .onFailure {
                var feilmelding = "Feil ved oppretting av førsteside for ${dto.førstesidetype}."
                if (it is HttpStatusCodeException) {
                    feilmelding += " Response fra førstesidegenerator = ${it.responseBodyAsString}"
                }

                throw OppslagException(
                    feilmelding,
                    "førstesidegenerator.genererFørsteside",
                    OppslagException.Level.MEDIUM,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    it
                )
            }
            .getOrThrow()
    }

    private fun httpHeaders(): HttpHeaders = HttpHeaders().apply {
        add(X_CORRELATION_ID, MDC.get(MDCConstants.MDC_CALL_ID))
        add(NAV_CONSUMER_ID, "familie-integrasjoner")
    }

    companion object {

        private const val PATH_PING = "internal/isAlive"
        private const val PATH_GENERER = "/api/foerstesidegenerator/v1/foersteside"
        private const val X_CORRELATION_ID = "X-Correlation-ID"
        private const val NAV_CONSUMER_ID = "Nav-Consumer-Id"
    }
}
