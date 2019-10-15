package no.nav.familie.ks.oppslag.dokarkiv;

import no.nav.familie.ks.oppslag.dokarkiv.api.ArkiverDokumentRequest;
import no.nav.familie.ks.oppslag.dokarkiv.api.ArkiverDokumentResponse;
import no.nav.familie.ks.oppslag.dokarkiv.client.KanIkkeFerdigstilleJournalpostException;
import no.nav.familie.ks.oppslag.felles.MDCOperations;
import no.nav.security.oidc.api.ProtectedWithClaims;
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
@ProtectedWithClaims(issuer = "intern")
@RequestMapping("/api/arkiv")
public class DokarkivController {
    private static final Logger LOG = LoggerFactory.getLogger(DokarkivController.class);

    private DokarkivService journalføringService;

    DokarkivController(DokarkivService journalføringService) {
        this.journalføringService = journalføringService;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        LOG.warn("Valideringsfeil av input ved arkivering: " + errors);
        return errors;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public Map<String, String> handleValidationExceptions(
            RuntimeException ex) {
        LOG.warn("Uventet arkiveringsfeil: ", ex);
        return Map.of("message", ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(KanIkkeFerdigstilleJournalpostException.class)
    public Map<String, String> handleKanIkkeFerdigstilleException(
            KanIkkeFerdigstilleJournalpostException ex) {
        LOG.warn("Feil ved ferdigstilling ", ex.getMessage());
        return Map.of("message", ex.getMessage());
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ArkiverDokumentResponse arkiverDokument(@Valid @RequestBody ArkiverDokumentRequest arkiverDokumentRequest) {
        return journalføringService.lagInngåendeJournalpost(arkiverDokumentRequest);
    }

    @PutMapping("/{journalpostId}/ferdigstill")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> ferdigstillJournalpost(@PathVariable(name = "journalpostId") String journalpostId) {
        if (journalpostId == null) {
            return new ResponseEntity("journalpostId er null",HttpStatus.BAD_REQUEST);
        }
        journalføringService.ferdistillJournalpost(journalpostId);
        return ResponseEntity.ok(null);
    }
}
