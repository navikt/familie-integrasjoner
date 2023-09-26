package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.http.util.UriUtil
import no.nav.familie.integrasjoner.dokdistkanal.domene.BestemDistribusjonskanalRequest
import no.nav.familie.integrasjoner.dokdistkanal.domene.BestemDistribusjonskanalResponse
import no.nav.familie.log.mdc.MDCConstants
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import java.net.URI

@Component
class DokdistkanalRestClient(
    @Value("\${DOKDISTKANAL_URL}") private val dokdistkanalUri: URI,
    @Qualifier("jwtBearer") private val restTemplate: RestOperations,
) :
    AbstractPingableRestClient(restTemplate, "dokdistkanal") {

    override val pingUri: URI = UriUtil.uri(dokdistkanalUri, PATH_PING)

    val distribuerUri = UriUtil.uri(dokdistkanalUri, PATH_BESTEM_DISTRIBUSJONSKANAL)

    fun bestemDistribusjonskanal(req: BestemDistribusjonskanalRequest): BestemDistribusjonskanalResponse {
        return postForEntity(distribuerUri, req, httpHeaders())
    }

    private fun httpHeaders(): HttpHeaders = HttpHeaders().apply {
        accept = listOf(MediaType.ALL)

        add(X_CORRELATION_ID, MDC.get(MDCConstants.MDC_CALL_ID))
    }
    companion object {
        private const val PATH_PING = "actuator/health/liveness"
        private const val PATH_BESTEM_DISTRIBUSJONSKANAL = "rest/bestemDistribusjonskanal"
        private const val X_CORRELATION_ID = "X-Correlation-ID"
    }
}
