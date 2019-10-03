package no.nav.familie.ks.oppslag.journalpost;

import no.nav.security.oidc.api.ProtectedWithClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

@RestController
@ProtectedWithClaims(issuer = "intern")
@RequestMapping("/api/journalpost")
public class HentJournalpostController {
    private static final Logger LOG = LoggerFactory.getLogger(HentJournalpostController.class);

    @Autowired
    private JournalpostService journalpostService;

    @ExceptionHandler(JournalpostRestClientException.class)
    public ResponseEntity<String> handleRestClientException(
            JournalpostRestClientException ex) {
        if (ex.getCause() instanceof HttpStatusCodeException){
            HttpStatusCodeException cex = (HttpStatusCodeException) ex.getCause();
            LOG.warn("Kunne ikke hente journalpost med id={} statuscode={} body={}", ex.getJournalpostId(), cex.getStatusCode(), cex.getResponseBodyAsString());
            if(cex.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                return new ResponseEntity<>("Fant ikke journalpost med id=" + ex.getJournalpostId(), HttpStatus.NOT_FOUND);
            }
        } else {
            LOG.warn("Feil ved henting av journalpostId={} klientfeilmelding={}", ex.getJournalpostId(), ex.getMessage(), ex);
        }
        return new ResponseEntity<>("Feil ved henting av journalpostId=" + ex.getJournalpostId(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public void handleRequestParserException(
            RuntimeException ex) {
        LOG.error("Feil ved henting av journalpostId", ex);
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
}
