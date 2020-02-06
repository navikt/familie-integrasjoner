package no.nav.familie.integrasjoner.oppgave

import no.nav.familie.integrasjoner.client.rest.OppgaveRestClient
import no.nav.familie.integrasjoner.oppgave.domene.OppgaveJsonDto
import no.nav.familie.integrasjoner.oppgave.domene.StatusEnum
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.sbl.util.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.context.annotation.ApplicationScope

@Service @ApplicationScope
class OppgaveService constructor(private val oppgaveRestClient: OppgaveRestClient) {

    fun oppdaterOppgave(request: Oppgave): Long {
        val oppgaveJsonDto: OppgaveJsonDto = if (StringUtils.nullOrEmpty(request.eksisterendeOppgaveId)) {
            oppgaveRestClient.finnOppgave(request)
        } else {
            oppgaveRestClient.finnOppgave(request.eksisterendeOppgaveId!!)
        }
        if (oppgaveJsonDto.status === StatusEnum.FERDIGSTILT) {
            LOG.info("Ignorerer oppdatering av oppgave som er ferdigstilt for aktørId={} journalpostId={} oppgaveId={}",
                     oppgaveJsonDto.aktoerId,
                     oppgaveJsonDto.journalpostId,
                     oppgaveJsonDto.id)
        } else {
            oppgaveRestClient.oppdaterOppgave(oppgaveJsonDto, request.beskrivelse)
        }
        return oppgaveJsonDto.id
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(OppgaveService::class.java)
    }

}