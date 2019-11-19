package no.nav.familie.ks.oppslag.infotrygd;

import no.nav.familie.ks.kontrakter.sak.Ressurs;
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
    public ResponseEntity<Ressurs> handleExceptions(HttpStatusCodeException ex) {
        if (ex instanceof HttpClientErrorException.NotFound) {
            LOG.info("404 mot infotrygd-kontantstotte");
        } else {
            LOG.error("Oppslag mot infotrygd-kontantstotte feilet. Status code: {}", ex.getStatusCode());
            secureLogger.error("Oppslag mot infotrygd-kontantstotte feilet. feilmelding={} responsebody={} exception={}", ex.getMessage(), ex.getResponseBodyAsString(), ex);
        }
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(Ressurs.Companion.failure("Oppslag mot infotrygd-kontanstøtte feilet " + ex.getResponseBodyAsString(), ex));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "harBarnAktivKontantstotte")
    @Deprecated(since = "TODO Slettes når endepunkt er endret")
    public ResponseEntity<AktivKontantstøtteInfo> aktivKontantstøtteGammel(@NotNull @RequestHeader(name = "Nav-Personident") String fnr) {
        return new ResponseEntity<>(infotrygdService.hentAktivKontantstøtteFor(fnr), HttpStatus.OK);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "v1/harBarnAktivKontantstotte")
    public ResponseEntity<Ressurs> aktivKontantstøtte(@NotNull @RequestHeader(name = "Nav-Personident") String fnr) {
        return ResponseEntity.ok(Ressurs.Companion.success(infotrygdService.hentAktivKontantstøtteFor(fnr), "Oppslag mot Infotrygd OK"));
    }
}
