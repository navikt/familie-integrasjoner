package no.nav.familie.integrasjoner.oppgave

import no.nav.familie.integrasjoner.client.rest.OppgaveRestClient
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.felles.OppslagException.Level
import no.nav.familie.integrasjoner.oppgave.domene.OppgaveJsonDto
import no.nav.familie.integrasjoner.oppgave.domene.PrioritetEnum
import no.nav.familie.integrasjoner.oppgave.domene.StatusEnum
import no.nav.familie.kontrakter.felles.oppgave.IdentType
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgave
import no.nav.sbl.util.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.context.annotation.ApplicationScope
import java.time.format.DateTimeFormatter

@Service @ApplicationScope
class OppgaveService constructor(private val oppgaveRestClient: OppgaveRestClient) {

    fun finnOppgaverKnyttetTilSaksbehandlerOgEnhet(saksbehandler: String, enhet: String): List<OppgaveJsonDto> {
        return finnOppgaverKnyttetTilSaksbehandlerOgEnhet(saksbehandler, enhet)
    }

    fun oppdaterOppgave(request: Oppgave): Long {
        val oppgaveJsonDto: OppgaveJsonDto = if (StringUtils.nullOrEmpty(request.eksisterendeOppgaveId)) {
            oppgaveRestClient.finnOppgave(request)
        } else {
            oppgaveRestClient.finnOppgaveMedId(request.eksisterendeOppgaveId!!)
        }
        if (oppgaveJsonDto.status === StatusEnum.FERDIGSTILT) {
            LOG.info("Ignorerer oppdatering av oppgave som er ferdigstilt for aktørId={} journalpostId={} oppgaveId={}",
                     oppgaveJsonDto.aktoerId,
                     oppgaveJsonDto.journalpostId,
                     oppgaveJsonDto.id)
        } else {
            val patchOppgaveDto = OppgaveJsonDto(
                    id = oppgaveJsonDto.id,
                    versjon = oppgaveJsonDto.versjon,
                    beskrivelse = oppgaveJsonDto.beskrivelse + request.beskrivelse
            )
            oppgaveRestClient.oppdaterOppgave(patchOppgaveDto)
        }
        return oppgaveJsonDto.id!!
    }

    fun opprettOppgave(request: OpprettOppgave): Long {
        val oppgave = OppgaveJsonDto(
                aktoerId = if (request.ident.type == IdentType.Aktør) request.ident.ident else null,
                orgnr = if (request.ident.type == IdentType.Organisasjon) request.ident.ident else null,
                behandlesAvApplikasjon = GOSYS_APP_ID,
                saksreferanse = request.saksId,
                journalpostId = request.journalpostId,
                prioritet = PrioritetEnum.NORM,
                tema = request.tema.name,
                tildeltEnhetsnr = request.enhetsnummer,
                behandlingstema = request.behandlingstema,
                fristFerdigstillelse = request.fristFerdigstillelse.format(DateTimeFormatter.ISO_DATE),
                aktivDato = request.aktivFra.format(DateTimeFormatter.ISO_DATE),
                oppgavetype = request.oppgavetype.value,
                beskrivelse = request.beskrivelse
        )

        return oppgaveRestClient.opprettOppgave(oppgave)
    }

    fun ferdigstill(oppgaveId: Long) {
        val oppgave = oppgaveRestClient.finnOppgaveMedId(oppgaveId.toString())

        when (oppgave.status) {
            StatusEnum.OPPRETTET, StatusEnum.AAPNET, StatusEnum.UNDER_BEHANDLING -> {
                val patchOppgaveDto = OppgaveJsonDto(
                        id = oppgave.id,
                        versjon = oppgave.versjon,
                        status = StatusEnum.FERDIGSTILT
                )
                oppgaveRestClient.oppdaterOppgave(patchOppgaveDto)
            }

            StatusEnum.FERDIGSTILT -> LOG.info("Oppgave er allerede ferdigstilt. oppgaveId=${oppgaveId}")
            StatusEnum.FEILREGISTRERT -> throw OppslagException("Oppgave har status feilregistrert og kan ikke oppdateres. " +
                                                                "oppgaveId=${oppgaveId}",
                                                                "Oppgave.ferdigstill",
                                                                Level.MEDIUM,
                                                                HttpStatus.BAD_REQUEST)
        }

    }


    companion object {
        private val LOG = LoggerFactory.getLogger(OppgaveService::class.java)
        private const val GOSYS_APP_ID = "FS22"
    }

}