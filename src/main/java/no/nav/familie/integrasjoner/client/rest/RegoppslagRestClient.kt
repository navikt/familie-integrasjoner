package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.http.util.UriUtil
import no.nav.familie.integrasjoner.personopplysning.internal.PostadresseRequest
import no.nav.familie.integrasjoner.personopplysning.internal.PostadresseResponse
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.log.mdc.MDCConstants
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestOperations
import java.net.URI

@Component
class RegoppslagRestClient(
    @Value("\${REGOPPSLAG_URL}") private val regoppslagUri: URI,
    @Qualifier("jwtBearer") private val restTemplate: RestOperations,
) : AbstractPingableRestClient(restTemplate, "regoppslag") {
    override val pingUri: URI = UriUtil.uri(regoppslagUri, PATH_PING)

    val uri = UriUtil.uri(regoppslagUri, PATH_POSTADRESSE)

    fun hentPostadresse(
        ident: String,
        tema: Tema,
    ): PostadresseResponse? =
        try {
            postForEntity(
                uri,
                payload =
                    PostadresseRequest(
                        ident = ident,
                        tema = tema.name,
                    ),
                httpHeaders(),
            )
        } catch (e: RestClientResponseException) {
            when (e.statusCode) {
                HttpStatus.NOT_FOUND,
                HttpStatus.GONE,
                -> null // Person er død og har ukjent adresse
                else -> throw e
            }
        }

    private fun httpHeaders(): HttpHeaders =
        HttpHeaders().apply {
            add(X_CORRELATION_ID, MDC.get(MDCConstants.MDC_CALL_ID))
        }

    companion object {
        private const val PATH_PING = "actuator/health/liveness"
        private const val PATH_POSTADRESSE = "rest/postadresse"
        private const val X_CORRELATION_ID = "X-Correlation-ID"
    }
}
