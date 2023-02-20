package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.http.sts.StsRestClient
import no.nav.familie.integrasjoner.aareg.domene.Arbeidsforhold
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.log.NavHttpHeaders
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.time.LocalDate

@Component
class AaregRestClient(
    @Value("\${AAREG_URL}")
    private val aaregUrl: URI,
    @Qualifier("sts") restOperations: RestOperations,
    private val stsRestClient: StsRestClient,
) :
    AbstractPingableRestClient(restOperations, "aareg") {

    /* NAV_CALLID og NAV_CONSUMER_ID trengs for kall til ping */
    override val pingUri: URI = URI.create("$aaregUrl/$PATH_PING")

    fun hentArbeidsforhold(personIdent: String, ansettelsesperiodeFom: LocalDate): List<Arbeidsforhold> {
        val uri = UriComponentsBuilder.fromUri(aaregUrl)
            .path(PATH_ARBEIDSFORHOLD)
            .queryParam("sporingsinformasjon", "false")
            .queryParam("ansettelsesperiodeFom", ansettelsesperiodeFom.toString())
            .queryParam("historikk", "true")
            .build().toUri()

        return try {
            getForEntity(uri, httpHeaders(personIdent))
        } catch (e: RestClientException) {
            var feilmelding = "Feil ved oppslag av arbeidsforhold."
            if (e is HttpStatusCodeException && e.responseBodyAsString.isNotEmpty()) {
                feilmelding += " Response fra aareg = ${e.responseBodyAsString}"
            }
            throw OppslagException(
                feilmelding,
                "aareg",
                OppslagException.Level.MEDIUM,
                HttpStatus.INTERNAL_SERVER_ERROR,
                e,
                "Kan ikke hente arbeidsforhold for $personIdent",
            )
        }
    }

    private fun httpHeaders(personIdent: String): HttpHeaders {
        return HttpHeaders().apply {
            add(NavHttpHeaders.NAV_PERSONIDENT.asString(), personIdent)
            add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            add("Nav-Consumer-Token", "Bearer ${stsRestClient.systemOIDCToken}")
        }
    }

    companion object {
        private const val PATH_PING = "ping"
        private const val PATH_ARBEIDSFORHOLD = "/v1/arbeidstaker/arbeidsforhold"
    }
}
