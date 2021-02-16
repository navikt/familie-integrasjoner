package no.nav.familie.integrasjoner.client.soap

import no.nav.familie.http.client.AbstractSoapClient
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.gosys.asbo.infotrygdsak.ASBOGOSYSBestillInfotrygdSakRequest
import no.nav.gosys.asbo.infotrygdsak.ASBOGOSYSBestillInfotrygdSakResponse
import no.nav.gosys.asbo.infotrygdsak.ASBOGOSYSHentInfotrygdSakListeRequest
import no.nav.gosys.asbo.infotrygdsak.ASBOGOSYSInfotrygdSakListe
import no.nav.inf.BestillInfotrygdSakFaultGOSYSGeneriskMsg
import no.nav.inf.GOSYSInfotrygdSak
import no.nav.inf.HentSakListeFaultGOSYSGeneriskMsg
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class InfotrygdsakSoapClient(private val gosysInfotrygdSak: GOSYSInfotrygdSak)
    : AbstractSoapClient("GosysInfotrygdSak") {

    fun opprettInfotrygdsak(asbogosysBestillInfotrygdSakRequest: ASBOGOSYSBestillInfotrygdSakRequest)
            : ASBOGOSYSBestillInfotrygdSakResponse {
        try {
            return executeMedMetrics {
                gosysInfotrygdSak.bestillInfotrygdSak(asbogosysBestillInfotrygdSakRequest)
            }
        } catch (e: BestillInfotrygdSakFaultGOSYSGeneriskMsg) {
            throw OppslagException("Opprettelse av sak i Infotrygd feilet",
                                   "Infotrygd.opprettInfotrygdsak",
                                   OppslagException.Level.MEDIUM,
                                   HttpStatus.INTERNAL_SERVER_ERROR,
                                   e)
        }
    }

    fun hentInfotrygdSakListe(asbogosysBestillInfotrygdSakRequest: ASBOGOSYSHentInfotrygdSakListeRequest)
            : ASBOGOSYSInfotrygdSakListe {
        try {
            return executeMedMetrics {
                gosysInfotrygdSak.hentSakListe(asbogosysBestillInfotrygdSakRequest)
            }
        } catch (e: HentSakListeFaultGOSYSGeneriskMsg) {
            throw OppslagException("Hent sakliste fra Infotrygd feilet",
                                   "Infotrygd.hentInfotrygdSakListe",
                                   OppslagException.Level.MEDIUM,
                                   HttpStatus.INTERNAL_SERVER_ERROR,
                                   e)
        }
    }
}
