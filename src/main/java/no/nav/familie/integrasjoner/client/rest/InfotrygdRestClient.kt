package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.integrasjoner.infotrygd.domene.AktivKontantstøtteInfo
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class InfotrygdRestClient(
    @Qualifier("jwtBearer") private val restTemplate: RestOperations,
    @Value("\${INFOTRYGD_URL}") private val infotrygdURL: URI,
) :
    AbstractPingableRestClient(restTemplate, "infotrygd") {

    override val pingUri: URI = UriComponentsBuilder.fromUri(infotrygdURL).pathSegment(PATH_PING).build().toUri()
    private val harKontantstøtteUri =
        UriComponentsBuilder.fromUri(infotrygdURL).pathSegment(PATH_AKTIV_KONTANTSTØTTE).build().toUri()

    fun hentAktivKontantstøtteFor(fnr: String): AktivKontantstøtteInfo {
        val httpHeaders = org.springframework.http.HttpHeaders().apply {
            add("fnr", fnr)
        }
        val response = getForEntity<AktivKontantstøtteInfo>(harKontantstøtteUri, httpHeaders)
        if (response == null) {
            error("Response fra infotrygd-kontantstøtte er null")
        }
        return response
    }

    companion object {
        private const val PATH_PING = "actuator/health"
        private const val PATH_AKTIV_KONTANTSTØTTE = "v1/harBarnAktivKontantstotte"
    }
}
