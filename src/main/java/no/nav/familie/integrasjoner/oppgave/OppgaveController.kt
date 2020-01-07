package no.nav.familie.integrasjoner.oppgave

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.familie.kontrakter.ks.oppgave.Oppgave
import no.nav.familie.kontrakter.ks.oppgave.toOppgave
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/oppgave")
class OppgaveController(private val oppgaveService: OppgaveService) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/oppdater"])
    fun oppdaterOppgave(@RequestBody oppgave: Oppgave): ResponseEntity<Ressurs<Map<String, Long>>> {
        val oppgaveId = oppgaveService.oppdaterOppgave(oppgave)
        return ResponseEntity.ok().body(success(mapOf("oppgaveId" to oppgaveId), "Oppslag mot oppgave OK"))
    }

}
