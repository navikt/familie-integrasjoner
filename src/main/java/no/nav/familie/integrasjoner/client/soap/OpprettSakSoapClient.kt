package no.nav.familie.integrasjoner.client.soap

import no.nav.familie.http.client.AbstractSoapClient
import no.nav.familie.integrasjoner.infotrygdsak.OpprettSakRequest
import no.nav.infotrygd.sb.opprettsak.OpprettSak
import no.nav.infotrygd.sb.opprettsak.OpprettSakResponse
import org.springframework.stereotype.Component
import javax.xml.ws.Holder

@Component
class OpprettSakSoapClient(private val opprettSak: OpprettSak)
    : AbstractSoapClient("GosysInfotrygdSak") {


    fun opprettInfotrygdsak(opprettSakRequest: OpprettSakRequest): OpprettSakResponse {

        val saksidHolder: Holder<String?> = Holder()
        val statusHolder: Holder<String?> = Holder()
        val bekreftelsesbrevSendtHolder: Holder<Boolean> = Holder()

        executeMedMetrics {
            opprettSak.opprettSak(opprettSakRequest.fnr,
                                  opprettSakRequest.stonadsklassifisering1,
                                  opprettSakRequest.stonadsklassifisering2,
                                  opprettSakRequest.mottattDato,
                                  opprettSakRequest.regNavEnhetId,
                                  opprettSakRequest.regNavBrukerId,
                                  opprettSakRequest.behNavEnhetId,
                                  opprettSakRequest.bekreftelsesbrev,
                                  saksidHolder,
                                  statusHolder,
                                  bekreftelsesbrevSendtHolder)
        }
        return OpprettSakResponse().apply {
            this.saksid = saksidHolder.value
            this.status = saksidHolder.value
            this.isBekreftelsesbrevSendt = bekreftelsesbrevSendtHolder.value
        }
    }
}
