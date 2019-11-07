package no.nav.familie.ks.oppslag.journalpost;

import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Map;

@RestController
@RequestMapping("/api/journalpost")
@ProtectedWithClaims(issuer = "azuread")
public class HentJournalpostController {
    private static final Logger LOG = LoggerFactory.getLogger(HentJournalpostController.class);

    private final JournalpostService journalpostService;

    public HentJournalpostController(JournalpostService journalpostService) {
        this.journalpostService = journalpostService;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(JournalpostRestClientException.class)
    public Map<String, String> handleRestClientException(
            JournalpostRestClientException ex) {
        String errorMessage = "Feil ved henting av journalpost=" + ex.getJournalpostId();
        if (ex.getCause() instanceof HttpStatusCodeException) {
            HttpStatusCodeException cex = (HttpStatusCodeException) ex.getCause();
            errorMessage += String.format(" statuscode=%s body=%s", cex.getStatusCode(), cex.getResponseBodyAsString());
        } else {
            errorMessage += " klientfeilmelding=" + ex.getMessage();
        }
        LOG.warn(errorMessage, ex);
        return Map.of("journalpost", ex.getJournalpostId(), "message", errorMessage);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public Map<String, String> handleRequestParserException(
            RuntimeException ex) {
        String errorMessage = "Feil ved henting av journalpost. " + ex.getMessage();
        LOG.warn(errorMessage, ex);
        return Map.of("message", errorMessage);
    }


    @GetMapping("/{journalpostId}/sak")
    public ResponseEntity<String> hentSaksnummer(@PathVariable(name = "journalpostId") String journalpostId) {
        String saksnummer = journalpostService.hentSaksnummer(journalpostId);
        if (saksnummer == null) {
            return new ResponseEntity<>("Sak mangler for journalpostId=" + journalpostId, HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(saksnummer, HttpStatus.OK);
        }
    }


    @GetMapping("/kanalreferanseid/{kanalReferanseId}")
    public ResponseEntity<String> hentJournalpostId(@PathVariable(name = "kanalReferanseId") String kanalReferanseId) {
        String journalpostId = journalpostService.hentJournalpostId(kanalReferanseId);
        if (journalpostId == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(journalpostService.hentJournalpostId(kanalReferanseId));
    }
}