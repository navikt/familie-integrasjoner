package no.nav.familie.ks.oppslag.oppgave;

import no.nav.familie.ks.kontrakter.oppgave.Oppgave;
import no.nav.familie.ks.kontrakter.oppgave.OppgaveKt;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@ProtectedWithClaims(issuer = "intern")
@RequestMapping("/api/oppgave")
public class OppgaveController {
    private static final Logger LOG = LoggerFactory.getLogger(OppgaveController.class);

    private OppgaveService oppgaveService;

    OppgaveController(OppgaveService oppgaveService) {
        this.oppgaveService = oppgaveService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "/oppdater")
    public ResponseEntity oppdaterOppgave(@RequestBody String oppgaveJson) {
        Oppgave request = OppgaveKt.toOppgave(oppgaveJson);
        return oppgaveService.oppdaterOppgave(request);
    }

    @ExceptionHandler(OppgaveIkkeFunnetException.class)
    public ResponseEntity<Map<String, String>> handleOppgaveIkkeFunnetException(RuntimeException e) {
        String errorMessage = "Feil ved oppdatering av Gosysoppgave: " + ExceptionUtils.getStackTrace(e);
        LOG.warn(errorMessage, e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Ingen oppgaver funnet " + e.getMessage()));
    }
}
