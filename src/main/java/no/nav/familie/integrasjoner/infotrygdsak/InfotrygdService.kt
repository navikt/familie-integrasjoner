package no.nav.familie.integrasjoner.infotrygdsak

import no.nav.familie.integrasjoner.client.soap.InfotrygdsakSoapClient
import org.springframework.stereotype.Service

@Service
class InfotrygdService(private val infotrygdsakSoapClient: InfotrygdsakSoapClient) {

    fun opprettInfotrygdsakGosys(opprettInfotrygdSakRequest: OpprettInfotrygdSakRequest): OpprettInfotrygdSakResponse {

        val asboRequest = OpprettInfotrygdSakMapper.tilAsboRequest(opprettInfotrygdSakRequest)
        val opprettInfotrygdsak = infotrygdsakSoapClient.opprettInfotrygdsak(asboRequest)
        return OpprettInfotrygdSakMapper.fraAsboResponse(opprettInfotrygdsak)

    }
}
