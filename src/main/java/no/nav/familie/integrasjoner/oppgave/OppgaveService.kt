package no.nav.familie.integrasjoner.oppgave

import no.nav.familie.integrasjoner.client.rest.OppgaveRestClient
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.felles.OppslagException.Level
import no.nav.familie.kontrakter.felles.oppgave.*
import no.nav.sbl.util.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.context.annotation.ApplicationScope
import java.time.format.DateTimeFormatter

@Service @ApplicationScope
class OppgaveService constructor(private val oppgaveRestClient: OppgaveRestClient) {

    fun finnOppgaver(tema: String,
                     behandlingstema: String?,
                     oppgaveType: String?,
                     enhet: String?,
                     saksbehandler: String?,
                     journalpostId: String?): List<Oppgave> {
        return oppgaveRestClient.finnOppgaver(tema, behandlingstema, oppgaveType, enhet, saksbehandler, journalpostId)
    }

    fun finnOppgaver(finnOppgaveRequest: FinnOppgaveRequest): FinnOppgaveResponseDto {
        return oppgaveRestClient.finnOppgaver(finnOppgaveRequest)
    }

    fun finnOppgaverV2(deprecatedFinnOppgaveRequest: DeprecatedFinnOppgaveRequest): FinnOppgaveResponseDto {
        return oppgaveRestClient.finnOppgaverV2(deprecatedFinnOppgaveRequest)
    }

    fun hentOppgave(oppgaveId: String): Oppgave {
        return oppgaveRestClient.finnOppgaveMedId(oppgaveId)
    }

    fun oppdaterOppgave(request: Oppgave): Long {
        val oppgave: Oppgave = if (StringUtils.nullOrEmpty(request.eksisterendeOppgaveId)) {
            oppgaveRestClient.finnOppgave(request)
        } else {
            oppgaveRestClient.finnOppgaveMedId(request.eksisterendeOppgaveId!!)
        }
        if (oppgave.status === StatusEnum.FERDIGSTILT) {
            LOG.info("Ignorerer oppdatering av oppgave som er ferdigstilt for aktørId={} journalpostId={} oppgaveId={}",
                     oppgave.aktoerId,
                     oppgave.journalpostId,
                     oppgave.id)
        } else {
            val patchOppgaveDto = oppgave.copy(
                    id = oppgave.id,
                    versjon = oppgave.versjon,
                    beskrivelse = oppgave.beskrivelse + request.beskrivelse
            )
            oppgaveRestClient.oppdaterOppgave(patchOppgaveDto)
        }
        return oppgave.id!!
    }

    fun fordelOppgave(oppgaveId: Long, saksbehandler: String): Long {
        val oppgave = oppgaveRestClient.finnOppgaveMedId(oppgaveId.toString())

        if (oppgave.status === StatusEnum.FERDIGSTILT) {
            error("Kan ikke fordele oppgave med id $oppgaveId som allerede er ferdigstilt")
        }
        val oppdatertOppgaveDto = oppgave.copy(
                id = oppgave.id,
                versjon = oppgave.versjon,
                tilordnetRessurs = saksbehandler
        )
        oppgaveRestClient.oppdaterOppgave(oppdatertOppgaveDto)
        return oppgave.id!!
    }

    fun tilbakestillFordelingPåOppgave(oppgaveId: Long): Long {
        val oppgave = oppgaveRestClient.finnOppgaveMedId(oppgaveId.toString())

        if (oppgave.status === StatusEnum.FERDIGSTILT) {
            error("Kan ikke tilbakestille fordeling på oppgave med id $oppgaveId som allerede er ferdigstilt")
        }

        val oppdatertOppgaveDto = oppgave.copy(
                id = oppgave.id,
                versjon = oppgave.versjon,
                tilordnetRessurs = ""
        )
        oppgaveRestClient.oppdaterOppgave(oppdatertOppgaveDto)
        return oppgave.id!!
    }

    fun opprettOppgave(request: OpprettOppgave): Long {
        val oppgave = Oppgave(
                aktoerId = if (request.ident?.type == IdentType.Aktør) request.ident!!.ident else null,
                orgnr = if (request.ident?.type == IdentType.Organisasjon) request.ident!!.ident else null,
                saksreferanse = request.saksId,
                //TODO oppgave-gjengen mente vi kunne sette denne til vår applikasjon, og så kan de gjøre en filtrering på sin
                //men da må vi få applikasjonen vår inn i Felles kodeverk ellers så får vi feil: Fant ingen kode 'BA' i felles
                // kodeverk under kodeverk 'Applikasjoner'
                // behandlesAvApplikasjon = request.tema.fagsaksystem,
                journalpostId = request.journalpostId,
                prioritet = request.prioritet,
                tema = request.tema,
                tildeltEnhetsnr = request.enhetsnummer,
                behandlingstema = request.behandlingstema,
                fristFerdigstillelse = request.fristFerdigstillelse.format(DateTimeFormatter.ISO_DATE),
                aktivDato = request.aktivFra.format(DateTimeFormatter.ISO_DATE),
                oppgavetype = request.oppgavetype.value,
                beskrivelse = request.beskrivelse,
                eksisterendeOppgaveId = null,
                behandlingstype = request.behandlingstype
        )

        return oppgaveRestClient.opprettOppgave(oppgave)
    }

    fun ferdigstill(oppgaveId: Long) {
        val oppgave = oppgaveRestClient.finnOppgaveMedId(oppgaveId.toString())

        when (oppgave.status) {
            StatusEnum.OPPRETTET, StatusEnum.AAPNET, StatusEnum.UNDER_BEHANDLING -> {
                val patchOppgaveDto = oppgave.copy(
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
    }
}