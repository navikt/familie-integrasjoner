package no.nav.familie.ks.oppslag.infotrygd;

import no.nav.familie.ks.oppslag.infotrygd.domene.AktivKontantstøtteInfo;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import javax.validation.constraints.NotNull;
import java.util.Map;

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/infotrygd")
public class InfotrygdController {
    private static final Logger LOG = LoggerFactory.getLogger(InfotrygdController.class);
    private static final Logger secureLogger = LoggerFactory.getLogger("secureLogger");

    private final InfotrygdService infotrygdService;

    public InfotrygdController(InfotrygdService infotrygdService) {
        this.infotrygdService = infotrygdService;
    }

    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<Map<String, String>> handleExceptions(HttpStatusCodeException ex) {
        if (ex instanceof HttpClientErrorException.NotFound) {
            LOG.info("404 mot infotrygd-kontantstotte");
        } else {
            LOG.error("Oppslag mot infotrygd-kontantstotte feilet. Status code: " + ex.getStatusCode());
            secureLogger.error("Oppslag mot infotrygd-kontantstotte feilet. feilmelding={} exception={}", ex.getMessage(), ex);
        }
        return new ResponseEntity<>(Map.of("error", ex.getStatusCode().toString()), ex.getStatusCode());
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "harBarnAktivKontantstotte")
    public ResponseEntity<AktivKontantstøtteInfo> aktivKontantstøtte(@NotNull @RequestHeader(name = "Nav-Personident") String fnr) {
        return new ResponseEntity<>(infotrygdService.hentAktivKontantstøtteFor(fnr), HttpStatus.OK);
    }
}
