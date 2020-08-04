package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.log.NavHttpHeaders
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import java.net.URI
import javax.ws.rs.core.MediaType

@Component
class AaregRestClient(@Value("\${AAREG_URL}")
                              private val aaregUrl: URI,
                      @Qualifier("sts") restOperations: RestOperations)
    : AbstractPingableRestClient(restOperations, "aareg") {

    /* NAV_CALLID og NAV_CONSUMER_ID trengs for kall til ping */
    override val pingUri: URI = URI.create("$aaregUrl/$PATH_PING")
    private val hentArbeidsforholdUri = URI.create("$aaregUrl/$PATH_ARBEIDSFORHOLD/?sporingsinformasjon=false")

    fun hentArbeidsforhold(personIdent: String): String {
        return getForEntity(hentArbeidsforholdUri, httpHeaders(personIdent))
    }

    private fun httpHeaders(personIdent: String): HttpHeaders {
        return HttpHeaders().apply {
            add(NavHttpHeaders.NAV_PERSONIDENT.asString(), personIdent)
            add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        }
    }

    companion object {
        private const val PATH_PING = "ping"
        private const val PATH_ARBEIDSFORHOLD = "v1/arbeidstaker/arbeidsforhold"
    }
}
