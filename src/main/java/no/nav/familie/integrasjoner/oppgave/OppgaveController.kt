package no.nav.familie.integrasjoner.oppgave

import no.nav.familie.integrasjoner.oppgave.domene.FinnOppgaveResponseDto
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.failure
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.kontrakter.felles.oppgave.OppgaveResponse
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgave
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/oppgave")
class OppgaveController(private val oppgaveService: OppgaveService) {

    @GetMapping(path = ["/{oppgaveId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hentOppgave(@PathVariable(name = "oppgaveId") oppgaveId: String)
            : ResponseEntity<Ressurs<Oppgave>> {
        val oppgave = oppgaveService.hentOppgave(oppgaveId)
        return ResponseEntity.ok().body(success(oppgave, "Hent Oppgave OK"))
    }

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @Deprecated("Bruk v2 endepunktet")
    fun finnOppgaver(@RequestParam("tema") tema: String,
                     @RequestParam("behandlingstema", required = false) behandlingstema: String?,
                     @RequestParam("oppgavetype", required = false) oppgavetype: String?,
                     @RequestParam("enhet", required = false) enhet: String?,
                     @RequestParam("saksbehandler", required = false) saksbehandler: String?,
                     @RequestParam("journalpostId", required = false) journalpostId: String?)
            : ResponseEntity<Ressurs<List<Oppgave>>> {
        val oppgaver = oppgaveService.finnOppgaver(tema,
                                                   behandlingstema,
                                                   oppgavetype,
                                                   enhet,
                                                   saksbehandler,
                                                   journalpostId)
        return ResponseEntity.ok().body(success(oppgaver, "Finn oppgaver OK"))
    }

    @PostMapping(path = ["/v2"], consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun finnOppgaverV2(@RequestBody finnOppgaveRequest: FinnOppgaveRequest)
            : ResponseEntity<Ressurs<FinnOppgaveResponseDto>> {
        return when {
            finnOppgaveRequest.tema == null -> ResponseEntity.ok().body(Ressurs.failure("påkrevd felt 'tema' mangler"))
            else -> ResponseEntity.ok().body(success(oppgaveService.finnOppgaverV2(finnOppgaveRequest), "Finn oppgaver OK"))
        }
    }

    @PostMapping(path = ["/{oppgaveId}/fordel"])
    fun fordelOppgave(@PathVariable(name = "oppgaveId") oppgaveId: Long,
                      @RequestParam("saksbehandler") saksbehandler: String?
    ): ResponseEntity<Ressurs<OppgaveResponse>> {
        Result.runCatching {
            if (saksbehandler == null) oppgaveService.tilbakestillFordelingPåOppgave(oppgaveId)
            else oppgaveService.fordelOppgave(oppgaveId, saksbehandler)
        }.fold(
                onSuccess = {
                    return ResponseEntity.ok(success(OppgaveResponse(oppgaveId = oppgaveId),
                            if (saksbehandler !== null) "Oppgaven ble tildelt saksbehandler $saksbehandler"
                            else "Fordeling på oppgaven ble tilbakestilt"
                    ))
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

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun opprettOppgave(@RequestBody oppgave: OpprettOppgave): ResponseEntity<Ressurs<OppgaveResponse>> {
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

class FinnOppgaveRequest(val tema: String? = null,
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