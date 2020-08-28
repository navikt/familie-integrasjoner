package no.nav.familie.integrasjoner.infotrygdsak

import no.nav.familie.integrasjoner.felles.ws.DateUtil
import no.nav.familie.integrasjoner.infotrygdsak.OpprettInfotrygdSakRequest
import no.nav.familie.integrasjoner.infotrygdsak.OpprettInfotrygdSakResponse
import no.nav.gosys.asbo.infotrygdsak.ASBOGOSYSBestillInfotrygdSakRequest
import no.nav.gosys.asbo.infotrygdsak.ASBOGOSYSBestillInfotrygdSakResponse
import java.time.ZoneId
import java.util.*

object OpprettInfotrygdSakMapper {

    fun tilAsboRequest(opprettInfotrygdSakRequest: OpprettInfotrygdSakRequest): ASBOGOSYSBestillInfotrygdSakRequest {
        val asboRequest = ASBOGOSYSBestillInfotrygdSakRequest()
        asboRequest.fagomrade = opprettInfotrygdSakRequest.fagomrade
        asboRequest.fnr = opprettInfotrygdSakRequest.fnr
        asboRequest.motattdato = GregorianCalendar.from(opprettInfotrygdSakRequest.motattdato?.atStartOfDay(ZoneId.systemDefault()))
        asboRequest.mottakerOrganisasjonsEnhetsId = opprettInfotrygdSakRequest.mottakerOrganisasjonsEnhetsId
        asboRequest.oppgaveId = opprettInfotrygdSakRequest.oppgaveId
        asboRequest.oppgaveOrganisasjonsenhetId = opprettInfotrygdSakRequest.oppgaveOrganisasjonsenhetId
        asboRequest.opprettetAv = opprettInfotrygdSakRequest.opprettetAv
        asboRequest.opprettetAvOrganisasjonsEnhetsId = opprettInfotrygdSakRequest.opprettetAvOrganisasjonsEnhetsId
        asboRequest.sendBekreftelsesbrev = opprettInfotrygdSakRequest.sendBekreftelsesbrev
        asboRequest.type = opprettInfotrygdSakRequest.type
        asboRequest.stonadsklassifisering2 = opprettInfotrygdSakRequest.stonadsklassifisering_2
        asboRequest.stonadsklassifisering3 = opprettInfotrygdSakRequest.stonadsklassifisering_3
        return asboRequest
    }

    fun fraAsboResponse(asboResponse: ASBOGOSYSBestillInfotrygdSakResponse): OpprettInfotrygdSakResponse {
        val response = OpprettInfotrygdSakResponse()
        response.bekreftelsesbrevSendt = asboResponse.bekreftelsesbrevSendt
        response.saksId = asboResponse.saksId
        response.status = asboResponse.status
        return response
    }
}