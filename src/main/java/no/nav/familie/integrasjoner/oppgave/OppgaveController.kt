package no.nav.familie.integrasjoner.oppgave

import no.nav.familie.kontrakter.felles.Ressurs
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

