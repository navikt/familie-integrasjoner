package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.integrasjoner.medlemskap.MedlemskapsunntakResponse
import no.nav.familie.log.NavHttpHeaders
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class MedlRestClient(@Value("\${MEDL2_URL}") private val medl2BaseUrl: URI,
                     @Qualifier("sts") private val restTemplate: RestOperations)
    : AbstractPingableRestClient(restTemplate, "medlemskap") {

    override val pingUri: URI = UriComponentsBuilder.fromUri(medl2BaseUrl).pathSegment(PATH_PING).build().toUri()

    val medlemskapsunntakUri: URI = UriComponentsBuilder.fromUri(medl2BaseUrl).pathSegment(PATH_MEDLEMSKAPSUNNTAK).build().toUri()

    fun hentMedlemskapsUnntakResponse(aktørId: String?): List<MedlemskapsunntakResponse> {

        val httpHeaders = org.springframework.http.HttpHeaders().apply {
            add(NavHttpHeaders.NAV_PERSONIDENT.asString(), aktørId)
        }

        try {
            return getForEntity(medlemskapsunntakUri, httpHeaders)
        } catch (e: Exception) {
            throw RuntimeException("Feil ved kall til MEDL2", e)
        }
    }

    companion object {
        private const val PATH_PING = "internal/isAlive"
        private const val PATH_MEDLEMSKAPSUNNTAK = "api/v1/medlemskapsunntak"
    }

}
