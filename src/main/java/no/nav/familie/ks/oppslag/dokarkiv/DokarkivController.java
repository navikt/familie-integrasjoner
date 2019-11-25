package no.nav.familie.ks.oppslag.dokarkiv;

import no.nav.familie.ks.kontrakter.sak.Ressurs;
import no.nav.familie.ks.oppslag.dokarkiv.api.ArkiverDokumentRequest;
import no.nav.familie.ks.oppslag.dokarkiv.client.KanIkkeFerdigstilleJournalpostException;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/arkiv")
public class DokarkivController {
    private static final Logger LOG = LoggerFactory.getLogger(DokarkivController.class);

    private final DokarkivService journalføringService;

    DokarkivController(DokarkivService journalføringService) {
        this.journalføringService = journalføringService;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Ressurs> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        LOG.warn("Valideringsfeil av input ved arkivering: " + errors);
        return ResponseEntity.badRequest().body(Ressurs.Companion.failure("Valideringsfeil av input ved arkivering " + errors, ex));
    }

    @ExceptionHandler(KanIkkeFerdigstilleJournalpostException.class)
    public ResponseEntity<Ressurs> handleKanIkkeFerdigstilleException(
            KanIkkeFerdigstilleJournalpostException ex) {
        LOG.warn("Feil ved ferdigstilling {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Ressurs.Companion.failure(null, ex));
    }

    @PostMapping(path = "v1", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Ressurs> arkiverDokument(@Valid @RequestBody ArkiverDokumentRequest arkiverDokumentRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Ressurs.Companion.success(journalføringService.lagInngåendeJournalpost(arkiverDokumentRequest), "Arkivert journalpost OK"));
    }

    @PutMapping("v1/{journalpostId}/ferdigstill")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Ressurs> ferdigstillJournalpost(@PathVariable(name = "journalpostId") String journalpostId) {
        if (journalpostId == null) {
            return ResponseEntity.badRequest().body(Ressurs.Companion.failure("journalpostId er null", null));
        }
        journalføringService.ferdistillJournalpost(journalpostId);
        return ResponseEntity
                .ok(Ressurs.Companion.success(Map.of("journalpostId", journalpostId), "Ferdigstilt journalpost " + journalpostId));
    }
}
