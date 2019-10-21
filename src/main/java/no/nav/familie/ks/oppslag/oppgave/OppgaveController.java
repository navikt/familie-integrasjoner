package no.nav.familie.ks.oppslag.oppgave;

import no.nav.familie.ks.kontrakter.oppgave.Oppgave;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@ProtectedWithClaims(issuer = "intern")
@RequestMapping("/api/oppgave")
public class OppgaveController {

    private OppgaveService oppgaveService;

    OppgaveController(OppgaveService oppgaveService) {
        this.oppgaveService = oppgaveService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "/oppdater")
    public ResponseEntity oppdaterOppgave(@RequestBody Oppgave request) {
        return oppgaveService.oppdaterOppgave(request);
    }
}
