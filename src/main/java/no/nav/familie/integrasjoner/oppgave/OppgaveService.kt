package no.nav.familie.integrasjoner.oppgave

import DatoFormat
import com.fasterxml.jackson.annotation.JsonInclude
import no.nav.familie.integrasjoner.aktør.AktørService
import no.nav.familie.integrasjoner.client.rest.OppgaveRestClient
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.felles.OppslagException.Level
import no.nav.familie.integrasjoner.saksbehandler.SaksbehandlerService
import no.nav.familie.integrasjoner.sikkerhet.SikkerhetsContext
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.oppgave.FinnMappeRequest
import no.nav.familie.kontrakter.felles.oppgave.FinnMappeResponseDto
import no.nav.familie.kontrakter.felles.oppgave.FinnOppgaveRequest
import no.nav.familie.kontrakter.felles.oppgave.FinnOppgaveResponseDto
import no.nav.familie.kontrakter.felles.oppgave.IdentGruppe
import no.nav.familie.kontrakter.felles.oppgave.MappeDto
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.kontrakter.felles.oppgave.OppgaveIdentV2
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgaveRequest
import no.nav.familie.kontrakter.felles.oppgave.StatusEnum
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.context.annotation.ApplicationScope
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
@ApplicationScope
class OppgaveService constructor(
    private val oppgaveRestClient: OppgaveRestClient,
    private val aktørService: AktørService,
    private val saksbehandlerService: SaksbehandlerService,
) {

    private val logger = LoggerFactory.getLogger(OppgaveService::class.java)

    fun finnOppgaver(finnOppgaveRequest: FinnOppgaveRequest): FinnOppgaveResponseDto {
        return oppgaveRestClient.finnOppgaver(finnOppgaveRequest)
    }

    fun hentOppgave(oppgaveId: Long): Oppgave {
        return oppgaveRestClient.finnOppgaveMedId(oppgaveId)
    }

    fun oppdaterOppgave(request: Oppgave): Long {
        val oppgave: Oppgave = if (request.id == null) {
            oppgaveRestClient.finnÅpenBehandleSakOppgave(request)
        } else {
            oppgaveRestClient.finnOppgaveMedId(request.id!!)
        }
        if (oppgave.status === StatusEnum.FERDIGSTILT) {
            logger.info(
                "Ignorerer oppdatering av oppgave som er ferdigstilt for aktørId={} journalpostId={} oppgaveId={}",
                oppgave.aktoerId,
                oppgave.journalpostId,
                oppgave.id,
            )
        } else {
            val patchOppgaveDto = oppgave.copy(
                id = oppgave.id,
                versjon = request.versjon ?: oppgave.versjon,
                beskrivelse = oppgave.beskrivelse + request.beskrivelse,
            )
            oppgaveRestClient.oppdaterOppgave(patchOppgaveDto)
        }
        return oppgave.id!!
    }

    fun patchOppgave(patchOppgave: Oppgave): Long {
        return oppgaveRestClient.oppdaterOppgave(patchOppgave)?.id!!
    }

    fun fordelOppgave(oppgaveId: Long, saksbehandler: String, versjon: Int?): Long {
        val oppgave = oppgaveRestClient.finnOppgaveMedId(oppgaveId)

        if (oppgave.status === StatusEnum.FERDIGSTILT) {
            throw OppslagException(
                "Kan ikke fordele oppgave med id $oppgaveId som allerede er ferdigstilt",
                "Oppgave.fordel",
                Level.LAV,
                HttpStatus.BAD_REQUEST,
            )
        }

        if (versjon != null && versjon != oppgave.versjon) {
            throw OppslagException(
                "Kan ikke fordele oppgave med id $oppgaveId fordi det finnes en nyere versjon av oppgaven.",
                "Oppgave.fordel",
                Level.LAV,
                HttpStatus.CONFLICT,
            )
        }

        val oppdatertOppgaveDto = oppgave.copy(
            id = oppgave.id,
            versjon = versjon ?: oppgave.versjon,
            tilordnetRessurs = saksbehandler,
            beskrivelse = lagOppgaveBeskrivelseFordeling(oppgave = oppgave, nySaksbehandlerIdent = saksbehandler),
        )
        oppgaveRestClient.oppdaterOppgave(oppdatertOppgaveDto)

        return oppgaveId
    }

    fun tilbakestillFordelingPåOppgave(oppgaveId: Long, versjon: Int?): Long {
        val oppgave = oppgaveRestClient.finnOppgaveMedId(oppgaveId)

        if (oppgave.status === StatusEnum.FERDIGSTILT) {
            throw OppslagException(
                "Kan ikke tilbakestille fordeling på oppgave med id $oppgaveId som allerede er ferdigstilt",
                "Oppgave.tilbakestill",
                Level.LAV,
                HttpStatus.BAD_REQUEST,
            )
        }

        if (versjon != null && versjon != oppgave.versjon) {
            throw OppslagException(
                "Kan ikke fordele oppgave med id $oppgaveId fordi det finnes en nyere versjon av oppgaven.",
                "Oppgave.fordel",
                Level.LAV,
                HttpStatus.CONFLICT,
            )
        }

        val oppdatertOppgaveDto = oppgave.copy(
            id = oppgave.id,
            versjon = versjon ?: oppgave.versjon,
            tilordnetRessurs = "",
            beskrivelse = lagOppgaveBeskrivelseFordeling(oppgave = oppgave),
        )
        oppgaveRestClient.oppdaterOppgave(oppdatertOppgaveDto)

        return oppgaveId
    }

    private fun lagOppgaveBeskrivelseFordeling(oppgave: Oppgave, nySaksbehandlerIdent: String? = null): String {
        val innloggetSaksbehandlerIdent = SikkerhetsContext.hentSaksbehandlerEllerSystembruker()
        val saksbehandlerNavn = SikkerhetsContext.hentSaksbehandlerNavn(strict = false)

        val formatertDato = LocalDateTime.now().format(DatoFormat.GOSYS_DATE_TIME)

        val prefix = "--- $formatertDato $saksbehandlerNavn ($innloggetSaksbehandlerIdent) ---\n"
        val endring = "Oppgave er flyttet fra ${oppgave.tilordnetRessurs ?: "<ingen>"} til ${nySaksbehandlerIdent ?: "<ingen>"}"

        val nåværendeBeskrivelse = if (oppgave.beskrivelse != null) {
            "\n\n${oppgave.beskrivelse}"
        } else {
            ""
        }

        return prefix + endring + nåværendeBeskrivelse
    }

    fun opprettOppgave(request: OpprettOppgaveRequest): Long {
        val oppgave = Oppgave(
            aktoerId = if (erAktørIdEllerFnr(request.ident)) getAktørId(request.ident!!, request.tema) else null,
            orgnr = if (request.ident?.gruppe == IdentGruppe.ORGNR) request.ident!!.ident else null,
            samhandlernr = if (request.ident?.gruppe == IdentGruppe.SAMHANDLERNR) request.ident!!.ident else null,
            saksreferanse = request.saksId,
            journalpostId = request.journalpostId,
            prioritet = request.prioritet,
            tema = request.tema,
            tildeltEnhetsnr = request.enhetsnummer,
            behandlingstema = request.behandlingstema,
            fristFerdigstillelse = request.fristFerdigstillelse.format(DateTimeFormatter.ISO_DATE),
            aktivDato = request.aktivFra.format(DateTimeFormatter.ISO_DATE),
            oppgavetype = request.oppgavetype.value,
            beskrivelse = request.beskrivelse,
            behandlingstype = request.behandlingstype,
            tilordnetRessurs = request.tilordnetRessurs?.let { saksbehandlerService.hentNavIdent(it) },
            behandlesAvApplikasjon = request.behandlesAvApplikasjon,
            mappeId = request.mappeId,
        )

        return oppgaveRestClient.opprettOppgave(oppgave)
    }

    fun ferdigstill(oppgaveId: Long, versjon: Int?) {
        val oppgave = oppgaveRestClient.finnOppgaveMedId(oppgaveId)

        validerVersjon(versjon, oppgave)

        when (oppgave.status) {
            StatusEnum.OPPRETTET, StatusEnum.AAPNET, StatusEnum.UNDER_BEHANDLING -> {
                val patchOppgaveDto = oppgave.copy(
                    id = oppgave.id,
                    versjon = versjon ?: oppgave.versjon,
                    status = StatusEnum.FERDIGSTILT,
                )
                oppgaveRestClient.oppdaterOppgave(patchOppgaveDto)
            }

            StatusEnum.FERDIGSTILT -> logger.info("Oppgave er allerede ferdigstilt. oppgaveId=$oppgaveId")
            StatusEnum.FEILREGISTRERT -> throw OppslagException(
                "Oppgave har status feilregistrert og kan ikke oppdateres. " +
                    "oppgaveId=$oppgaveId",
                "Oppgave.ferdigstill",
                Level.MEDIUM,
                HttpStatus.BAD_REQUEST,
            )

            null -> throw OppslagException(
                "Oppgave har ingen status og kan ikke oppdateres. " +
                    "oppgaveId=$oppgaveId",
                "Oppgave.ferdigstill",
                Level.MEDIUM,
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    private fun validerVersjon(
        versjon: Int?,
        oppgave: Oppgave,
    ) {
        if (versjon != null && versjon != oppgave.versjon) {
            throw OppslagException(
                "Oppgave har har feil versjon og kan ikke ferdigstilles. " +
                    "oppgaveId=${oppgave.id}",
                "Oppgave.ferdigstill",
                Level.LAV,
                HttpStatus.CONFLICT,
            )
        }
    }

    private fun erAktørIdEllerFnr(oppgaveIdent: OppgaveIdentV2?) =
        oppgaveIdent?.gruppe == IdentGruppe.FOLKEREGISTERIDENT || oppgaveIdent?.gruppe == IdentGruppe.AKTOERID

    private fun getAktørId(oppgaveIdentV2: OppgaveIdentV2, tema: Tema): String {
        return if (oppgaveIdentV2.gruppe == IdentGruppe.AKTOERID) {
            oppgaveIdentV2.ident ?: throw IllegalArgumentException("Mangler ident for gruppe=${oppgaveIdentV2.gruppe}")
        } else {
            aktørService.getAktørIdFraPdl(oppgaveIdentV2.ident!!, tema)
        }
    }

    fun finnMapper(finnMappeRequest: FinnMappeRequest): FinnMappeResponseDto {
        val mappeRespons = oppgaveRestClient.finnMapper(finnMappeRequest)
        if (mappeRespons.antallTreffTotalt > mappeRespons.mapper.size) {
            logger.error(
                "Det finnes flere mapper (${mappeRespons.antallTreffTotalt}) " +
                    "enn vi har hentet ut (${mappeRespons.mapper.size}). Sjekk limit. ",
            )
        }
        return mappeRespons.mapperUtenTema()
    }

    fun finnMapper(enhetNr: String): List<MappeDto> {
        val finnMappeRequest = FinnMappeRequest(enhetsnr = enhetNr, limit = 1000)
        return finnMapper(finnMappeRequest).mapper
    }

    fun tilordneEnhet(oppgaveId: Long, enhet: String, fjernMappeFraOppgave: Boolean, versjon: Int?) {
        val oppgave = oppgaveRestClient.finnOppgaveMedId(oppgaveId)
        val mappeId = if (fjernMappeFraOppgave) null else oppgave.mappeId
        oppgaveRestClient.oppdaterEnhet(OppgaveByttEnhet(oppgaveId, enhet, versjon ?: oppgave.versjon!!, mappeId))
    }
}

data class OppgaveByttEnhet(
    val id: Long,
    val tildeltEnhetsnr: String,
    val versjon: Int,
    @JsonInclude(JsonInclude.Include.ALWAYS) val mappeId: Long? = null,
)

/**
 * Vil filtrere bort mapper med tema siden disse er spesifikke for andre ytelser enn våre (f.eks Pensjon og Bidrag)
 **/
private fun FinnMappeResponseDto.mapperUtenTema(): FinnMappeResponseDto {
    val mapperUtenTema = this.mapper.filter { it.tema.isNullOrBlank() }
    return this.copy(mapper = mapperUtenTema, antallTreffTotalt = mapperUtenTema.size)
}
