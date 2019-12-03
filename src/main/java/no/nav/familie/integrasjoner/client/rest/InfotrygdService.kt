package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.util.UriUtil
import no.nav.familie.integrasjoner.infotrygd.domene.AktivKontantstøtteInfo
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestOperations
import java.net.URI

@Service
class InfotrygdService(@Qualifier("azure") private val restTemplate: RestOperations,
                       @Value("\${INFOTRYGD_URL}")
                       private val infotrygdURL: URI) : AbstractRestClient(restTemplate) {

    override val pingUri: URI =  UriUtil.uri(infotrygdURL,"/actuator/health")



    fun hentAktivKontantstøtteFor(fnr: String): AktivKontantstøtteInfo {
        if (!fnr.matches(Regex("[0-9]+"))) {
            throw HttpClientErrorException(HttpStatus.BAD_REQUEST, "fnr må være et tall")
        }
        val httpHeaders = org.springframework.http.HttpHeaders().apply {
            add("fnr", fnr)
        }

        try {
            return getForEntity(UriUtil.uri(infotrygdURL, PATH_ATIV_KONTANTSTØTTE))
        } catch (e: Exception) {
            throw HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Ufullstendig eller tom respons.")
        }
    }


    companion object {
        private const val PATH_ATIV_KONTANTSTØTTE = "/v1/harBarnAktivKontantstotte"
    }


}

