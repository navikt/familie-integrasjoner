package no.nav.familie.integrasjoner.infotrygdsak

import no.nav.familie.integrasjoner.client.soap.InfotrygdsakSoapClient
import no.nav.familie.integrasjoner.client.soap.OpprettSakSoapClient
import no.nav.infotrygd.sb.opprettsak.OpprettSakResponse
import org.springframework.stereotype.Service

@Service
class InfotrygdService(private val infotrygdsakSoapClient: InfotrygdsakSoapClient,
                       private val opprettSakSoapClient: OpprettSakSoapClient) {

    fun opprettInfotrygdsakGosys(opprettInfotrygdSakRequest: OpprettInfotrygdSakRequest): OpprettInfotrygdSakResponse {

        val asboRequest = OpprettInfotrygdSakMapper.tilAsboRequest(opprettInfotrygdSakRequest)
        val opprettInfotrygdsak = infotrygdsakSoapClient.opprettInfotrygdsak(asboRequest)
        return OpprettInfotrygdSakMapper.fraAsboResponse(opprettInfotrygdsak)

    }

    fun opprettInfotrygdsak(opprettInfotrygdSakRequest: OpprettSakRequest): OpprettSakResponse {

        return opprettSakSoapClient.opprettInfotrygdsak(opprettInfotrygdSakRequest)
    }
}