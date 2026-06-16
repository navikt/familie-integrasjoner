package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.integrasjoner.dokdistkanal.domene.BestemDistribusjonskanalRequest
import no.nav.familie.integrasjoner.dokdistkanal.domene.BestemDistribusjonskanalResponse
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.felles.UriUtil
import no.nav.familie.log.mdc.MDCConstants
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.net.URI

@Component
class DokdistkanalRestClient(
    @Value("\${DOKDISTKANAL_URL}") private val dokdistkanalUri: URI,
    @Qualifier("dokdistkanalRestClient") private val restClient: RestClient,
) {
    val uri = UriUtil.uri(dokdistkanalUri, PATH_BESTEM_DISTRIBUSJONSKANAL)

    fun bestemDistribusjonskanal(req: BestemDistribusjonskanalRequest): BestemDistribusjonskanalResponse =
        try {
            restClient
                .post()
                .uri(uri)
                .headers { h ->
                    httpHeaders().forEach { (key, values) -> h.addAll(key, values) }
                }.body(req)
                .retrieve()
                .body<BestemDistribusjonskanalResponse>()!!
        } catch (e: Exception) {
            throw OppslagException(
                "Feil ved henting av distribusjonskanal",
                "dokdist.kanal.bestemDistribusjonskanal",
                OppslagException.Level.MEDIUM,
                HttpStatus.INTERNAL_SERVER_ERROR,
                e,
            )
        }

    private fun httpHeaders(): HttpHeaders =
        HttpHeaders().apply {
            add(X_CORRELATION_ID, MDC.get(MDCConstants.MDC_CALL_ID))
        }

    companion object {
        private const val PATH_BESTEM_DISTRIBUSJONSKANAL = "rest/bestemDistribusjonskanal"
        private const val X_CORRELATION_ID = "X-Correlation-ID"
    }
}
