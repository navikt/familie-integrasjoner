package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.util.UriUtil
import no.nav.familie.integrasjoner.medlemskap.MedlemskapService
import no.nav.familie.integrasjoner.medlemskap.MedlemskapsUnntakResponse
import no.nav.familie.log.NavHttpHeaders
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import java.net.URI

@Component
class MedlRestClient(@Value("\${MEDL2_URL}") private val medl2BaseUrl: URI,
                     @Qualifier("sts") private val restTemplate: RestOperations)
    : AbstractPingableRestClient(restTemplate, "medlemskap") {

    override val pingUri = UriUtil.uri(medl2BaseUrl, PATH_PING )

    val medlemskapsunntakUri = UriUtil.uri(medl2BaseUrl, PATH_MEDLEMSKAPSUNNTAK)

    fun hentMedlemskapsUnntakResponse(aktørId: String?): List<MedlemskapsUnntakResponse> {

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