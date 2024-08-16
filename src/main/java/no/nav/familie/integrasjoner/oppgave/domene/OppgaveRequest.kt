package no.nav.familie.integrasjoner.oppgave.domene

import no.nav.familie.kontrakter.felles.oppgave.FinnOppgaveRequest
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.min

const val LIMIT_MOT_OPPGAVE = 50L
const val DEFAULT_SORTERINGSFELT = "OPPRETTET_TIDSPUNKT"
const val DEFAULT_STATUSKATEGORI = "AAPEN"
const val DEFAULT_SORTERINGSREKKEFOLGE = "DESC"

data class OppgaveRequest(
    val statuskategori: String = DEFAULT_STATUSKATEGORI,
    val tema: String,
    val sorteringsfelt: String = DEFAULT_SORTERINGSFELT,
    val sorteringsrekkefolge: String = DEFAULT_SORTERINGSREKKEFOLGE,
    val limit: Long = LIMIT_MOT_OPPGAVE,
    val offset: Long = 0,
    val behandlingstema: String?,
    val behandlingstype: String?,
    val oppgavetype: String?,
    val erUtenMappe: Boolean?,
    val tildeltEnhetsnr: String?,
    val tildeltRessurs: Boolean?,
    val tilordnetRessurs: String?,
    val journalpostId: String?,
    val saksreferanse: String?,
    val opprettetFom: LocalDateTime?,
    val opprettetTom: LocalDateTime?,
    val fristFom: LocalDate?,
    val fristTom: LocalDate?,
    val aktivDatoFom: LocalDate?,
    val aktivDatoTom: LocalDate?,
    val mappeId: Long?,
    val aktoerId: String?,
)

fun FinnOppgaveRequest.toDto() =
    OppgaveRequest(
        offset = this.offset ?: 0,
        limit = min(this.limit ?: LIMIT_MOT_OPPGAVE, LIMIT_MOT_OPPGAVE),
        tema = this.tema.name,
        behandlingstema = this.behandlingstema?.value,
        behandlingstype = this.behandlingstype?.value,
        oppgavetype = this.oppgavetype?.value,
        erUtenMappe = this.erUtenMappe,
        tildeltEnhetsnr = this.enhet,
        tildeltRessurs = this.tildeltRessurs,
        tilordnetRessurs = this.tilordnetRessurs ?: this.saksbehandler,
        journalpostId = this.journalpostId,
        opprettetFom = this.opprettetFomTidspunkt,
        opprettetTom = this.opprettetTomTidspunkt,
        fristFom = this.fristFomDato,
        fristTom = this.fristTomDato,
        aktivDatoFom = this.aktivFomDato,
        aktivDatoTom = this.aktivTomDato,
        mappeId = this.mappeId,
        aktoerId = this.aktørId,
        saksreferanse = this.saksreferanse,
        sorteringsfelt = this.sorteringsfelt?.name ?: DEFAULT_SORTERINGSFELT,
        sorteringsrekkefolge = this.sorteringsrekkefølge?.name ?: DEFAULT_SORTERINGSREKKEFOLGE,
    )
