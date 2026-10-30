package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.felles.tokenklient.entraid.EntraIDRestClientFactory
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.sak.Skyggesak
import no.nav.familie.integrasjoner.sikkerhet.SikkerhetsContext
import no.nav.familie.log.mdc.MDCConstants
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestClientException
import java.net.URI

@Component
@Profile("!gcp")
class SkyggesakRestClient(
    @Value("\${SKYGGE_SAK_URL}") private val skyggesakUrl: String,
    @Value("\${SKYGGE_SAK_SCOPE}") scope: String,
    entraIDRestClientFactory: EntraIDRestClientFactory,
) {
    private val restClient = entraIDRestClientFactory.lagHybridRestKlient(scope) { SikkerhetsContext.hentJwt().tokenValue }
    private val sakUri = URI.create("$skyggesakUrl/api/v1/saker")

    private val logger = LoggerFactory.getLogger(SkyggesakRestClient::class.java)

    fun opprettSak(request: Skyggesak) {
        try {
            restClient
                .post()
                .uri(sakUri)
                .contentType(MediaType.APPLICATION_JSON)
                .header(X_CORRELATION_ID, MDC.get(MDCConstants.MDC_CALL_ID))
                .body(request)
                .retrieve()
                .toBodilessEntity()
        } catch (e: HttpClientErrorException.Conflict) {
            logger.info("Skyggesak allerede opprettet for fagsak ${request.fagsakNr}")
        } catch (e: RestClientException) {
            var feilmelding = "Feil ved oppretting av skyggesak i sak. url=$sakUri"
            if (e is HttpStatusCodeException && e.responseBodyAsString.isNotEmpty()) {
                feilmelding += " Response fra Sak = ${e.responseBodyAsString}"
            }
            throw OppslagException(
                feilmelding,
                "skyggesak",
                OppslagException.Level.MEDIUM,
                HttpStatus.INTERNAL_SERVER_ERROR,
                e,
            )
        }
    }

    companion object {
        private const val X_CORRELATION_ID = "X-Correlation-ID"
    }
}
