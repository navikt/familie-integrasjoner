package no.nav.familie.integrasjoner.oppgave

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.oppgave.FinnMappeRequest
import no.nav.familie.kontrakter.felles.oppgave.FinnMappeResponseDto
import no.nav.familie.kontrakter.felles.oppgave.FinnOppgaveRequest
import no.nav.familie.kontrakter.felles.oppgave.FinnOppgaveResponseDto
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.kontrakter.felles.oppgave.OppgavePrioritet
import no.nav.familie.kontrakter.felles.oppgave.OppgaveResponse
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgave
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgaveRequest
import no.nav.familie.kontrakter.felles.oppgave.StatusEnum
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.validation.constraints.Pattern

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/oppgave")
class OppgaveController(private val oppgaveService: OppgaveService) {

    @GetMapping(path = ["/{oppgaveId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hentOppgave(@PathVariable(name = "oppgaveId") oppgaveId: String)
            : ResponseEntity<Ressurs<Oppgave>> {
        val oppgave = oppgaveService.hentOppgave(oppgaveId.toLong())
        return ResponseEntity.ok().body(success(oppgave, "Hent Oppgave OK"))
    }

    @GetMapping(path = ["/v3"])
    @Deprecated("Bruk v4 endepunktet")
    fun finnOppgaverV3(finnOppgaveRequest: FinnOppgaveRequest): Ressurs<DeprecatedFinnOppgaveResponseDto> {
        return success(oppgaveService.finnOppgaverV3(finnOppgaveRequest))
    }

    @PostMapping(path = ["/v4"])
    fun finnOppgaverV4(@RequestBody finnOppgaveRequest: FinnOppgaveRequest): Ressurs<FinnOppgaveResponseDto> {
        return success(oppgaveService.finnOppgaver(finnOppgaveRequest))
    }

    @PostMapping(path = ["/mappe/sok"])
    @Deprecated(message = "Bruk get under")
    fun finnMapperV1Deprecated(finnMappeRequest: FinnMappeRequest): Ressurs<FinnMappeResponseDto> {
        return success(oppgaveService.finnMapper(finnMappeRequest))
    }

    @GetMapping(path = ["/mappe/sok"])
    fun finnMapperV1(finnMappeRequest: FinnMappeRequest): Ressurs<FinnMappeResponseDto> {
        return success(oppgaveService.finnMapper(finnMappeRequest))
    }

    @DateTimeFormat
    @PostMapping(path = ["/{oppgaveId}/fordel"])
    fun fordelOppgave(@PathVariable(name = "oppgaveId") oppgaveId: Long,
                      @RequestParam("saksbehandler") saksbehandler: String?): ResponseEntity<Ressurs<OppgaveResponse>> {
        Result.runCatching {
            if (saksbehandler == null) oppgaveService.tilbakestillFordelingPåOppgave(oppgaveId)
            else oppgaveService.fordelOppgave(oppgaveId, saksbehandler)
        }.fold(onSuccess = {
            return ResponseEntity.ok(success(OppgaveResponse(oppgaveId = oppgaveId),
                                             if (saksbehandler !== null) "Oppgaven ble tildelt saksbehandler $saksbehandler"
                                             else "Fordeling på oppgaven ble tilbakestilt"))
        },
               onFailure = {
                   return ResponseEntity.badRequest().body(Ressurs.failure(errorMessage = it.message))
               }
        )
    }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/oppdater"])
    fun oppdaterOppgave(@RequestBody oppgave: Oppgave): ResponseEntity<Ressurs<OppgaveResponse>> {
        val oppgaveId = oppgaveService.oppdaterOppgave(oppgave)
        return ResponseEntity.ok().body(success(OppgaveResponse(oppgaveId = oppgaveId), "Oppdatering av oppgave OK"))
    }

    @PatchMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/{oppgaveId}/oppdater"])
    fun patchOppgave(@RequestBody oppgave: Oppgave): ResponseEntity<Ressurs<OppgaveResponse>> {
        val oppgaveId = oppgaveService.patchOppgave(oppgave)
        return ResponseEntity.ok().body(success(OppgaveResponse(oppgaveId = oppgaveId), "Oppdatering av oppgave OK"))
    }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    @Deprecated("Bruk v2-endepunkt")
    fun opprettOppgaveV1(@RequestBody oppgave: OpprettOppgave): ResponseEntity<Ressurs<OppgaveResponse>> {
        val oppgaveId = oppgaveService.opprettOppgaveV1(oppgave)
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success(OppgaveResponse(oppgaveId = oppgaveId), "Opprett oppgave OK"))
    }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/opprett"])
    fun opprettOppgaveV2(@RequestBody oppgave: OpprettOppgaveRequest): ResponseEntity<Ressurs<OppgaveResponse>> {
        val oppgaveId = oppgaveService.opprettOppgave(oppgave)
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success(OppgaveResponse(oppgaveId = oppgaveId), "Opprett oppgave OK"))
    }

    @PatchMapping(path = ["/{oppgaveId}/ferdigstill"])
    fun ferdigstillOppgave(@PathVariable(name = "oppgaveId") oppgaveId: Long): ResponseEntity<Ressurs<OppgaveResponse>> {
        oppgaveService.ferdigstill(oppgaveId)
        return ResponseEntity.ok(success(OppgaveResponse(oppgaveId = oppgaveId), "ferdigstill OK"))
    }
}

class DeprecatedFinnOppgaveRequest(val tema: String? = null,
                                   val behandlingstema: String? = null,
                                   val oppgavetype: String? = null,
                                   val enhet: String? = null,
                                   val saksbehandler: String? = null,
                                   val journalpostId: String? = null,
                                   val tilordnetRessurs: String? = null,
                                   val tildeltRessurs: Boolean? = null,
                                   val opprettetFomTidspunkt: String? = null,
                                   val opprettetTomTidspunkt: String? = null,
                                   val fristFomDato: String? = null,
                                   val fristTomDato: String? = null,
                                   val aktivFomDato: String? = null,
                                   val aktivTomDato: String? = null,
                                   val limit: Long? = null,
                                   val offset: Long? = null)

@Deprecated("Benytt kontrakt")
data class DeprecatedFinnOppgaveResponseDto(val antallTreffTotalt: Long,
                                            val oppgaver: List<DeprecatedOppgave>)

@JsonIgnoreProperties(ignoreUnknown = true)
@Deprecated("Benytt kontrakt")
data class DeprecatedOppgave(val id: Long? = null,
                             val tildeltEnhetsnr: String? = null,
                             val endretAvEnhetsnr: String? = null,
                             val opprettetAvEnhetsnr: String? = null,
                             val journalpostId: String? = null,
                             val journalpostkilde: String? = null,
                             val behandlesAvApplikasjon: String? = null,
                             val saksreferanse: String? = null,
                             val bnr: String? = null,
                             val samhandlernr: String? = null,
                             @field:Pattern(regexp = "[0-9]{13}")
                             val aktoerId: String? = null,
                             val orgnr: String? = null,
                             val tilordnetRessurs: String? = null,
                             val beskrivelse: String? = null,
                             val temagruppe: String? = null,
                             val tema: Tema? = null,
                             val behandlingstema: String? = null,
                             val oppgavetype: String? = null,
                             val behandlingstype: String? = null,
                             val versjon: Int? = null,
                             val mappeId: Long? = null,
                             val fristFerdigstillelse: String? = null,
                             val aktivDato: String? = null,
                             val opprettetTidspunkt: String? = null,
                             val opprettetAv: String? = null,
                             val endretAv: String? = null,
                             val ferdigstiltTidspunkt: String? = null,
                             val endretTidspunkt: String? = null,
                             val prioritet: OppgavePrioritet? = null,
                             val status: StatusEnum? = null,
                             private var metadata: MutableMap<String, String>? = null)
