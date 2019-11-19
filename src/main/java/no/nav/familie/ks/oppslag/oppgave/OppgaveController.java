package no.nav.familie.ks.oppslag.oppgave;

import no.nav.familie.ks.kontrakter.oppgave.Oppgave;
import no.nav.familie.ks.kontrakter.oppgave.OppgaveKt;
import no.nav.familie.ks.kontrakter.sak.Ressurs;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/oppgave")
public class OppgaveController {
    private static final Logger LOG = LoggerFactory.getLogger(OppgaveController.class);

    private OppgaveService oppgaveService;

    OppgaveController(OppgaveService oppgaveService) {
        this.oppgaveService = oppgaveService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "/oppdater")
    public ResponseEntity<Ressurs> oppdaterOppgave(@RequestBody String oppgaveJson) {
        Oppgave request = OppgaveKt.toOppgave(oppgaveJson);
        return ResponseEntity.ok().body(Ressurs.Companion.success(Map.of("oppgaveId", oppgaveService.oppdaterOppgave(request)), "Oppslag mot oppgave OK"));
    }
}
