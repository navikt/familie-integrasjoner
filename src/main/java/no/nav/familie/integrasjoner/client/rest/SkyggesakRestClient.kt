package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.sak.Skyggesak
import no.nav.familie.log.mdc.MDCConstants
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestOperations
import java.net.URI

@Component
class SkyggesakRestClient(@Value("\${SKYGGE_SAK_URL}") private val skyggesakUrl: String,
                          @Qualifier("sts") private val restTemplate: RestOperations)
    : AbstractPingableRestClient(restTemplate, "skyggesak.sak") {

    override val pingUri: URI = URI.create("$skyggesakUrl/internal/alive")
    private val sakUri = URI.create("$skyggesakUrl/v1/saker")

    private val logger = LoggerFactory.getLogger(SkyggesakRestClient::class.java)

    fun opprettSak(request: Skyggesak) {
        try {
            postForEntity<Skyggesak>(sakUri, request, httpHeaders())
        } catch (e: HttpClientErrorException.Conflict) {
            logger.info("Skyggesak allerede opprettet for fagsak ${request.fagsakNr}")
        } catch (e: RestClientException) {
            var feilmelding = "Feil ved oppretting av skyggesak i sak."
            if (e is HttpStatusCodeException && e.responseBodyAsString.isNotEmpty()) {
                feilmelding += " Response fra Sak = ${e.responseBodyAsString}"
            }
            throw OppslagException(feilmelding,
                                   "skyggesak",
                                   OppslagException.Level.MEDIUM,
                                   HttpStatus.INTERNAL_SERVER_ERROR,
                                   e)
        }
    }

    private fun httpHeaders(): HttpHeaders = HttpHeaders().apply {
        add(X_CORRELATION_ID, MDC.get(MDCConstants.MDC_CALL_ID))
    }

    companion object {

        private const val X_CORRELATION_ID = "X-Correlation-ID"
    }
}

