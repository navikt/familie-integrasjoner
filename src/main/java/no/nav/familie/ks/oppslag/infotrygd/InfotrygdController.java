package no.nav.familie.ks.oppslag.infotrygd;

import no.nav.familie.ks.oppslag.infotrygd.domene.AktivKontantstøtteInfo;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Map;
import javax.validation.constraints.NotNull;

@RestController
@ProtectedWithClaims(issuer = "intern")
@RequestMapping("/api/infotrygd")
public class InfotrygdController {
    private static final Logger LOG = LoggerFactory.getLogger(InfotrygdController.class);
    private static final Logger secureLogger = LoggerFactory.getLogger("secureLogger");

    private InfotrygdService infotrygdService;

    public InfotrygdController(InfotrygdService infotrygdService) {
        this.infotrygdService = infotrygdService;
    }

    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<Map<String, String>> handleExceptions(HttpStatusCodeException ex) {
        LOG.error("Oppslag mot infotrygd-kontantstotte feilet. Status code: " + ex.getStatusCode());
        secureLogger.error(ex.getMessage(), ex);
        return new ResponseEntity<>(Map.of("error", "" + ex.getStatusCode()), ex.getStatusCode());
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "harBarnAktivKontantstotte")
    public ResponseEntity<AktivKontantstøtteInfo> aktivKontantstøtte(@NotNull @RequestHeader(name = "Nav-Personident") String fnr) {
        return new ResponseEntity<>(infotrygdService.hentAktivKontantstøtteFor(fnr), HttpStatus.OK);
    }
}
