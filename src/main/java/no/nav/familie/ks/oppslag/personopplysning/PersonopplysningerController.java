package no.nav.familie.ks.oppslag.personopplysning;

import no.nav.familie.ks.oppslag.personopplysning.domene.PersonhistorikkInfo;
import no.nav.familie.ks.oppslag.personopplysning.domene.Personinfo;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "historikk")
    public ResponseEntity<PersonhistorikkInfo> historikk(@NotNull @RequestHeader(name = "Nav-Personident") String personIdent,
                                         @NotNull @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fomDato,
                                         @NotNull @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tomDato) {
        return personopplysningerService.hentHistorikkFor(personIdent, fomDato, tomDato);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "info")
    public ResponseEntity<Personinfo> personInfo(@NotNull @RequestHeader(name = "Nav-Personident") String personIdent) {
        return personopplysningerService.hentPersoninfoFor(personIdent);
    }
}
