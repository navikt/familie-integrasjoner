package no.nav.familie.integrasjoner.infotrygdsak

import no.nav.familie.kontrakter.felles.infotrygdsak.OpprettInfotrygdSakRequest
import no.nav.familie.kontrakter.felles.infotrygdsak.OpprettInfotrygdSakResponse
import no.nav.gosys.asbo.infotrygdsak.ASBOGOSYSBestillInfotrygdSakRequest
import no.nav.gosys.asbo.infotrygdsak.ASBOGOSYSBestillInfotrygdSakResponse
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

object OpprettInfotrygdSakMapper {

    fun tilAsboRequest(opprettInfotrygdSakRequest: OpprettInfotrygdSakRequest): ASBOGOSYSBestillInfotrygdSakRequest {
        val asboRequest = ASBOGOSYSBestillInfotrygdSakRequest()
        asboRequest.fagomrade = opprettInfotrygdSakRequest.fagomrade
        asboRequest.fnr = opprettInfotrygdSakRequest.fnr
        asboRequest.motattdato = GregorianCalendar.from((opprettInfotrygdSakRequest.mottattdato ?: LocalDate.now())
                                                                .atStartOfDay(ZoneId.systemDefault()))
        asboRequest.mottakerOrganisasjonsEnhetsId = opprettInfotrygdSakRequest.mottakerOrganisasjonsEnhetsId
        asboRequest.oppgaveId = opprettInfotrygdSakRequest.oppgaveId
        asboRequest.oppgaveOrganisasjonsenhetId = opprettInfotrygdSakRequest.oppgaveOrganisasjonsenhetId
        asboRequest.opprettetAv = opprettInfotrygdSakRequest.opprettetAv
        asboRequest.opprettetAvOrganisasjonsEnhetsId = opprettInfotrygdSakRequest.opprettetAvOrganisasjonsEnhetsId
        asboRequest.sendBekreftelsesbrev = opprettInfotrygdSakRequest.sendBekreftelsesbrev
        asboRequest.type = opprettInfotrygdSakRequest.type
        asboRequest.stonadsklassifisering2 = opprettInfotrygdSakRequest.stonadsklassifisering2
        asboRequest.stonadsklassifisering3 = opprettInfotrygdSakRequest.stonadsklassifisering3
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