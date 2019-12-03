package no.nav.familie.integrasjoner.client.rest

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
class MedlClient(@Value("\${MEDL2_URL}") val medl2BaseUrl: String,
                 @Qualifier("sts") val restTemplate: RestOperations) : AbstractRestClient(restTemplate) {

    override val pingUri = URI.create(String.format("%s/client/isAlive", medl2BaseUrl))
    val medl2Uri: URI = URI.create(String.format("%s/api/v1/medlemskapsunntak", medl2BaseUrl))

    fun hentMedlemskapsUnntakResponse(aktørId: String?): List<MedlemskapsUnntakResponse> {

        val httpHeaders = org.springframework.http.HttpHeaders().apply {
            add(NavHttpHeaders.NAV_PERSONIDENT.asString(), aktørId)
        }

        try {
            return getForEntity(medl2Uri, httpHeaders)
        } catch (e: Exception) {
            throw RuntimeException("Feil ved kall til MEDL2", e)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(MedlemskapService::class.java)
    }

}