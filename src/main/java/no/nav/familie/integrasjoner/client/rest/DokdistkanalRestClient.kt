package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.felles.tokenklient.entraid.EntraIDRestClientFactory
import no.nav.familie.integrasjoner.dokdistkanal.domene.BestemDistribusjonskanalRequest
import no.nav.familie.integrasjoner.dokdistkanal.domene.BestemDistribusjonskanalResponse
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.felles.UriUtil
import no.nav.familie.integrasjoner.sikkerhet.SikkerhetsContext
import no.nav.familie.log.mdc.MDCConstants
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.net.URI

@Component
class DokdistkanalRestClient(
    @Value("\${DOKDISTKANAL_URL}") private val dokdistkanalUri: URI,
    @Value("\${DOKDISTKANAL_SCOPE}") scope: String,
    entraIDRestClientFactory: EntraIDRestClientFactory,
) {
    private val restClient = entraIDRestClientFactory.lagHybridRestKlient(scope) { SikkerhetsContext.hentJwt().tokenValue }
    val uri = UriUtil.uri(dokdistkanalUri, PATH_BESTEM_DISTRIBUSJONSKANAL)

    fun bestemDistribusjonskanal(req: BestemDistribusjonskanalRequest): BestemDistribusjonskanalResponse =
        try {
            restClient
                .post()
                .uri(uri)
                .header(X_CORRELATION_ID, MDC.get(MDCConstants.MDC_CALL_ID))
                .body(req)
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

    companion object {
        private const val PATH_BESTEM_DISTRIBUSJONSKANAL = "rest/bestemDistribusjonskanal"
        private const val X_CORRELATION_ID = "X-Correlation-ID"
    }
}
