package no.nav.familie.integrasjoner.infotrygdsak

import no.nav.familie.integrasjoner.client.soap.InfotrygdsakSoapClient
import no.nav.familie.kontrakter.felles.infotrygdsak.FinnInfotrygdSakerRequest
import no.nav.familie.kontrakter.felles.infotrygdsak.InfotrygdSak
import no.nav.familie.kontrakter.felles.infotrygdsak.OpprettInfotrygdSakRequest
import no.nav.familie.kontrakter.felles.infotrygdsak.OpprettInfotrygdSakResponse
import org.springframework.stereotype.Service

@Service
class InfotrygdService(private val infotrygdsakSoapClient: InfotrygdsakSoapClient) {

    fun opprettInfotrygdsakGosys(opprettInfotrygdSakRequest: OpprettInfotrygdSakRequest): OpprettInfotrygdSakResponse {

        val asboRequest = OpprettInfotrygdSakMapper.tilAsboRequest(opprettInfotrygdSakRequest)
        val opprettInfotrygdsak = infotrygdsakSoapClient.opprettInfotrygdsak(asboRequest)
        return OpprettInfotrygdSakMapper.fraAsboResponse(opprettInfotrygdsak)

    }

    fun hentInfotrygdSaker(finnInfotrygdSakerRequest: FinnInfotrygdSakerRequest): List<InfotrygdSak> {
        val request = FinnInfotrygdSakerMapper.tilRequest(finnInfotrygdSakerRequest)
        val response = infotrygdsakSoapClient.hentInfotrygdSakListe(request)
        return FinnInfotrygdSakerMapper.fraResponse(response)
    }
}
