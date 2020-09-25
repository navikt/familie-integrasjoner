package no.nav.familie.integrasjoner.client.soap

import no.nav.familie.http.client.AbstractSoapClient
import no.nav.gosys.asbo.infotrygdsak.ASBOGOSYSBestillInfotrygdSakRequest
import no.nav.gosys.asbo.infotrygdsak.ASBOGOSYSBestillInfotrygdSakResponse
import no.nav.inf.GOSYSInfotrygdSak
import org.springframework.stereotype.Component

@Component
class InfotrygdsakSoapClient(private val gosysInfotrygdSak: GOSYSInfotrygdSak)
    : AbstractSoapClient("GosysInfotrygdSak") {

    fun opprettInfotrygdsak(asbogosysBestillInfotrygdSakRequest: ASBOGOSYSBestillInfotrygdSakRequest)
            : ASBOGOSYSBestillInfotrygdSakResponse {
        return executeMedMetrics {
            gosysInfotrygdSak.bestillInfotrygdSak(asbogosysBestillInfotrygdSakRequest)
        }
    }
}
