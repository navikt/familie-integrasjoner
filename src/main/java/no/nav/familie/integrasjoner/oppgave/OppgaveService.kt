package no.nav.familie.integrasjoner.oppgave

import no.nav.familie.integrasjoner.client.rest.OppgaveRestClient
import no.nav.familie.integrasjoner.oppgave.domene.OppgaveJsonDto
import no.nav.familie.integrasjoner.oppgave.domene.PrioritetEnum
import no.nav.familie.integrasjoner.oppgave.domene.StatusEnum
import no.nav.familie.kontrakter.felles.oppgave.IdentType
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgave
import no.nav.sbl.util.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.context.annotation.ApplicationScope
import java.time.format.DateTimeFormatter

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
        return oppgaveJsonDto.id!!
    }

    fun opprettOppgave(request: OpprettOppgave): Long {
        val oppgave = OppgaveJsonDto(
                aktoerId = if (request.ident.type == IdentType.Aktør) request.ident.ident else null,
                orgnr = if (request.ident.type == IdentType.Organisasjon) request.ident.ident else null,
                behandlesAvApplikasjon = GOSYS_APP_ID,
                saksreferanse = request.saksId,
                prioritet = PrioritetEnum.NORM,
                tema = request.tema.name,
                tildeltEnhetsnr = request.enhetsnummer,
                behandlingstema = request.behandlingstema,
                fristFerdigstillelse = request.fristFerdigstillelse.format(DateTimeFormatter.ISO_DATE),
                aktivDato = request.aktivFra.format(DateTimeFormatter.ISO_DATE),
                oppgavetype = BEHANLDE_SAK_OPPGAVE,
                beskrivelse = request.beskrivelse
        )

        return oppgaveRestClient.opprettOppgave(oppgave)
    }


    companion object {
        private val LOG = LoggerFactory.getLogger(OppgaveService::class.java)
        private const val GOSYS_APP_ID = "FS22"
        private const val BEHANLDE_SAK_OPPGAVE = "BEH_SAK"
    }

}