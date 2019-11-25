package no.nav.familie.ks.oppslag.personopplysning;

import no.nav.familie.ks.kontrakter.sak.Ressurs;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/personopplysning")
public class PersonopplysningerController {

    private PersonopplysningerService personopplysningerService;

    public PersonopplysningerController(PersonopplysningerService personopplysningerService) {
        this.personopplysningerService = personopplysningerService;
    }

    @ExceptionHandler({HttpClientErrorException.NotFound.class})
    public ResponseEntity<Ressurs> handleRestClientResponseException(HttpClientErrorException.NotFound e) {
        return ResponseEntity
                .status(e.getRawStatusCode())
                .body(Ressurs.Companion.failure("Feil mot personopplysning. " + e.getRawStatusCode() + " Message=" + e.getMessage(), null));
    }

    @ExceptionHandler({HttpClientErrorException.Forbidden.class})
    public ResponseEntity<Ressurs> handleRestClientResponseException(HttpClientErrorException.Forbidden e) {
        return ResponseEntity
                .status(e.getRawStatusCode())
                .body(Ressurs.Companion.ikkeTilgang("Ikke tilgang mot personopplysning " + e.getMessage()));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "v1/historikk")
    public ResponseEntity<Ressurs> historikk(@NotNull @RequestHeader(name = "Nav-Personident") String personIdent,
                                             @NotNull @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fomDato,
                                             @NotNull @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tomDato) {
        return ResponseEntity.ok().body(Ressurs.Companion.success(personopplysningerService.hentHistorikkFor(personIdent, fomDato, tomDato), "Hent personhistorikk OK"));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "v1/info")
    public ResponseEntity<Ressurs> personInfo(@NotNull @RequestHeader(name = "Nav-Personident") String personIdent) {
        return ResponseEntity.ok().body(Ressurs.Companion.success(personopplysningerService.hentPersoninfoFor(personIdent), "Hent personinfo OK"));
    }
}
