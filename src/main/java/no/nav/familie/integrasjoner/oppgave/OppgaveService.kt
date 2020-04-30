package no.nav.familie.integrasjoner.oppgave

import no.nav.familie.integrasjoner.client.rest.OppgaveRestClient
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.felles.OppslagException.Level
import no.nav.familie.integrasjoner.oppgave.domene.FinnOppgaveResponseDto
import no.nav.familie.kontrakter.felles.oppgave.IdentType
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgave
import no.nav.familie.kontrakter.felles.oppgave.StatusEnum
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

    fun finnOppgaverV2(tema: String,
                       behandlingstema: String?,
                       oppgavetype: String?,
                       enhet: String?,
                       saksbehandler: String?,
                       journalpostId: String?,
                       opprettetFomTidspunkt: String?,
                       opprettetTomTidspunkt: String?,
                       fristFomDato: String?,
                       fristTomDato: String?,
                       aktivFomDato: String?,
                       aktivTomDato: String?,
                       limit: Long?,
                       offset: Long?): FinnOppgaveResponseDto {
        return oppgaveRestClient.finnOppgaverV2(tema,
                behandlingstema,
                oppgavetype,
                enhet,
                saksbehandler,
                journalpostId,
                opprettetFomTidspunkt,
                opprettetTomTidspunkt,
                fristFomDato,
                fristTomDato,
                aktivFomDato,
                aktivTomDato,
                limit,
                offset)
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

    fun opprettOppgave(request: OpprettOppgave): Long {
        val oppgave = Oppgave(
                aktoerId = if (request.ident.type == IdentType.Aktør) request.ident.ident else null,
                orgnr = if (request.ident.type == IdentType.Organisasjon) request.ident.ident else null,
                saksreferanse = request.saksId,
                //TODO oppgave-gjengen mente vi kunne sette denne til vår applikasjon, og så kan de gjøre en filtrering på sin
                //men da må vi få applikasjonen vår inn i Felles kodeverk ellers så får vi feil: Fant ingen kode 'BA' i felles kodeverk under kodeverk 'Applikasjoner'
//              behandlesAvApplikasjon = request.tema.fagsaksystem,
                journalpostId = request.journalpostId,
                prioritet = request.prioritet.name,
                tema = request.tema,
                tildeltEnhetsnr = request.enhetsnummer,
                behandlingstema = request.behandlingstema,
                fristFerdigstillelse = request.fristFerdigstillelse.format(DateTimeFormatter.ISO_DATE),
                aktivDato = request.aktivFra.format(DateTimeFormatter.ISO_DATE),
                oppgavetype = request.oppgavetype.value,
                beskrivelse = request.beskrivelse,
                eksisterendeOppgaveId = null
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