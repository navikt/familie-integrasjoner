package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.integrasjoner.config.incrementLoggFeil
import no.nav.familie.integrasjoner.felles.UriUtil
import no.nav.familie.integrasjoner.personopplysning.internal.PostadresseRequest
import no.nav.familie.integrasjoner.personopplysning.internal.PostadresseResponse
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.log.mdc.MDCConstants
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.body
import java.net.URI

@Component
class RegoppslagRestClient(
    @Value("\${REGOPPSLAG_URL}") private val regoppslagUri: URI,
    @Qualifier("regoppslagRestClient") private val restClient: RestClient,
) {
    val uri = UriUtil.uri(regoppslagUri, PATH_POSTADRESSE)

    fun hentPostadresse(
        ident: String,
        tema: Tema,
    ): PostadresseResponse? =
        try {
            restClient
                .post()
                .uri(uri)
                .header(X_CORRELATION_ID, MDC.get(MDCConstants.MDC_CALL_ID))
                .body(
                    PostadresseRequest(
                        ident = ident,
                        tema = tema.name,
                    ),
                ).retrieve()
                .body<PostadresseResponse>()!!
        } catch (e: RestClientResponseException) {
            when (e.statusCode) {
                HttpStatus.NOT_FOUND,
                HttpStatus.GONE,
                -> {
                    null
                }

                // Person er død og har ukjent adresse
                else -> {
                    incrementLoggFeil("regoppslag.hentPostadresse")
                    throw e
                }
            }
        }

    companion object {
        private const val PATH_POSTADRESSE = "rest/postadresse"
        private const val X_CORRELATION_ID = "X-Correlation-ID"
    }
}
