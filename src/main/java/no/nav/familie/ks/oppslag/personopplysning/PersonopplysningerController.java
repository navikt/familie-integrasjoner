package no.nav.familie.ks.oppslag.personopplysning;

import no.nav.familie.ks.oppslag.felles.MDCOperations;
import no.nav.familie.ks.oppslag.personopplysning.domene.AktørId;
import no.nav.familie.ks.oppslag.personopplysning.domene.PersonhistorikkInfo;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.apache.tools.ant.taskdefs.Local;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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
    public PersonhistorikkInfo historikk(
            @NotNull @RequestParam(name = "id") String aktørId,
            @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fomDato,
            @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tomDato
    ) {
        MDCOperations.putCallId(); // FIXME: Midlertidig, bør settes generelt i et filter elns
        return personopplysningerService.hentHistorikkFor(new AktørId(aktørId), fomDato, tomDato);
    }
}
