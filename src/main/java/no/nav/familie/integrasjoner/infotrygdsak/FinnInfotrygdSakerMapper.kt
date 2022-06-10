package no.nav.familie.integrasjoner.infotrygdsak

import no.nav.familie.kontrakter.felles.infotrygdsak.FinnInfotrygdSakerRequest
import no.nav.familie.kontrakter.felles.infotrygdsak.InfotrygdSak
import no.nav.gosys.asbo.infotrygdsak.ASBOGOSYSHentInfotrygdSakListeRequest
import no.nav.gosys.asbo.infotrygdsak.ASBOGOSYSInfotrygdSakListe

object FinnInfotrygdSakerMapper {

    fun tilRequest(finnInfotrygdSakerRequest: FinnInfotrygdSakerRequest): ASBOGOSYSHentInfotrygdSakListeRequest {
        val request = ASBOGOSYSHentInfotrygdSakListeRequest()
        request.gjelderId = finnInfotrygdSakerRequest.fnr
        request.fagomradeKodeListe = listOf(finnInfotrygdSakerRequest.fagomrade)
        request.hentKodebeskrivelser = true
        return request
    }

    fun fraResponse(asboResponse: ASBOGOSYSInfotrygdSakListe): List<InfotrygdSak> {
        return asboResponse.sakListe.map {
            InfotrygdSak(it.gjelderId, it.saksnr, it.registrertNavEnhetId, it.fagomradeKode)
        }
    }
}
