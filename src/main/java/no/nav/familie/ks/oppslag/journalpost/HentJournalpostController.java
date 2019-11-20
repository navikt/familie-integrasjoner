package no.nav.familie.ks.oppslag.journalpost;

import no.nav.familie.ks.kontrakter.sak.Ressurs;
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

    @ExceptionHandler(JournalpostRestClientException.class)
    public ResponseEntity<Ressurs> handleRestClientException(
            JournalpostRestClientException ex) {
        String errorMessage = "Feil ved henting av journalpost=" + ex.getJournalpostId();
        if (ex.getCause() instanceof HttpStatusCodeException) {
            HttpStatusCodeException cex = (HttpStatusCodeException) ex.getCause();
            errorMessage += String.format(" statuscode=%s body=%s", cex.getStatusCode(), cex.getResponseBodyAsString());
        } else {
            errorMessage += " klientfeilmelding=" + ex.getMessage();
        }
        LOG.warn(errorMessage, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Ressurs.Companion.failure(errorMessage, ex));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Ressurs> handleRequestParserException(
            RuntimeException ex) {
        String errorMessage = "Feil ved henting av journalpost. " + ex.getMessage();
        LOG.warn(errorMessage, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Ressurs.Companion.failure(errorMessage, ex));
    }


    @GetMapping("sak")
    public ResponseEntity<Ressurs> hentSaksnummer(@RequestParam(name = "journalpostId") String journalpostId) {
        String saksnummer = journalpostService.hentSaksnummer(journalpostId);
        if (saksnummer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Ressurs.Companion.failure("Sak mangler for journalpostId=" + journalpostId, null));
        } else {
            return ResponseEntity.ok(Ressurs.Companion.success(Map.of("saksnummer", saksnummer), "OK"));
        }
    }

    @Deprecated(since = "TODO slettes når mottak bytter endepunkt")
    @GetMapping("/{journalpostId}/sak")
    public ResponseEntity<String> hentSaksnummerMedPathVariable(@PathVariable(name = "journalpostId") String journalpostId) {
        String saksnummer = journalpostService.hentSaksnummer(journalpostId);
        if (saksnummer == null) {
            return new ResponseEntity<>("Sak mangler for journalpostId=" + journalpostId, HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(saksnummer, HttpStatus.OK);
        }
    }

    @Deprecated(since = "TODO slettes når mottak bytter endepunkt")
    @GetMapping("/kanalreferanseid/{kanalReferanseId}")
    public ResponseEntity<String> hentJournalpostIdPathVariable(@PathVariable(name = "kanalReferanseId") String kanalReferanseId) {
        String journalpostId = journalpostService.hentJournalpostId(kanalReferanseId);
        if (journalpostId == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(journalpostService.hentJournalpostId(kanalReferanseId));
    }

    @GetMapping
    public ResponseEntity<Ressurs> hentJournalpostId(@RequestParam(name = "kanalReferanseId") String kanalReferanseId) {
        String journalpostId = journalpostService.hentJournalpostId(kanalReferanseId);
        if (journalpostId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Ressurs.Companion.failure("journalpost ikke funnet", null));
        }
        return ResponseEntity.ok(Ressurs.Companion.success(Map.of("journalpostId", journalpostId), "OK"));
    }
}