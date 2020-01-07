package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.util.UriUtil
import no.nav.familie.integrasjoner.infotrygd.domene.AktivKontantstøtteInfo
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import java.net.URI

@Service
class InfotrygdRestClient(@Qualifier("azure") private val restTemplate: RestOperations,
                          @Value("\${INFOTRYGD_URL}") private val infotrygdURL: URI)
    : AbstractPingableRestClient(restTemplate, "infotrygd") {

    override val pingUri: URI = UriUtil.uri(infotrygdURL, PATH_PING)
    private val harKontantstøtteUri = UriUtil.uri(infotrygdURL, PATH_ATIV_KONTANTSTØTTE)

    fun hentAktivKontantstøtteFor(fnr: String): AktivKontantstøtteInfo {
        val httpHeaders = org.springframework.http.HttpHeaders().apply {
            add("fnr", fnr)
        }
        return getForEntity(harKontantstøtteUri, httpHeaders)
    }

    companion object {
        private const val PATH_PING = "actuator/health"
        private const val PATH_ATIV_KONTANTSTØTTE = "v1/harBarnAktivKontantstotte"
    }


}

