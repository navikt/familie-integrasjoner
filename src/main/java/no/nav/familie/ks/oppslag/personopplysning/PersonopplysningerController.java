package no.nav.familie.ks.oppslag.personopplysning;

import no.nav.familie.ks.oppslag.felles.MDCOperations;
import no.nav.familie.ks.oppslag.personopplysning.domene.AktørId;
import no.nav.familie.ks.oppslag.personopplysning.domene.PersonhistorikkInfo;
import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.security.oidc.api.Unprotected;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@RestController
@ProtectedWithClaims(issuer = "intern")
@RequestMapping("/api/personopplysning")
public class PersonopplysningerController {

    private PersonopplysningerService personopplysningerService;

    public PersonopplysningerController(PersonopplysningerService personopplysningerService) {
        this.personopplysningerService = personopplysningerService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "historikk")
    public PersonhistorikkInfo historikk(@NotNull @RequestParam(name = "id") String aktørId) {
        MDCOperations.putCallId(); // FIXME: Midlertidig, bør settes generelt i et filter elns
        LocalDate idag = LocalDate.now();
        return personopplysningerService.hentHistorikkFor(new AktørId(aktørId), idag.minusYears(5), idag);
    }
}
