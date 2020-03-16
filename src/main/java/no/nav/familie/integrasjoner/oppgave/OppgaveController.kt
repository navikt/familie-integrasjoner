package no.nav.familie.integrasjoner.oppgave

import no.nav.familie.integrasjoner.oppgave.domene.OppgaveJsonDto
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

    @GetMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/"])
    fun finnOppgaverKnyttetTilSaksbehandlerOgEnhet(@RequestParam("saksbehandler") saksbehandler: String,
                                                   @RequestParam("enhet") enhet: String): ResponseEntity<Ressurs<List<OppgaveJsonDto>>> {
        val oppgaver = oppgaveService.finnOppgaverKnyttetTilSaksbehandlerOgEnhet(saksbehandler, enhet)
        return ResponseEntity.ok().body(success(oppgaver, "Finn oppgaver OK"))
    }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/oppdater"])
    fun oppdaterOppgave(@RequestBody oppgave: Oppgave): ResponseEntity<Ressurs<OppgaveResponse>> {
        val oppgaveId = oppgaveService.oppdaterOppgave(oppgave)
        return ResponseEntity.ok().body(success(OppgaveResponse(oppgaveId = oppgaveId), "Oppdatering av oppgave OK"))
    }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/"])
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

