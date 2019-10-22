package no.nav.familie.ks.oppslag.tilgangskontroll;

import no.nav.familie.ks.oppslag.personopplysning.PersonopplysningerService;
import no.nav.familie.ks.oppslag.personopplysning.domene.Personinfo;
import no.nav.familie.ks.oppslag.tilgangskontroll.domene.Tilgang;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping(value = "/api/tilgang")
public class TilgangskontrollController {


    private TilgangsKontrollService tilgangService;

    private PersonopplysningerService personService;

    @Autowired
    public TilgangskontrollController(TilgangsKontrollService tilgangsKontrollService, PersonopplysningerService personopplysningerService) {
        this.tilgangService = tilgangsKontrollService;
        this.personService = personopplysningerService;
    }

    @GetMapping(path = "/person")
    //@ProtectedWithClaims(issuer = AZURE) TODO bruk Azure, og hent saksbehandler fra securitycontext
    @ProtectedWithClaims(issuer = "intern")
    public ResponseEntity tilgangTilPerson(@NotNull @RequestHeader(name = "Nav-Personident") String personIdent, @NotNull @RequestHeader(name = "saksbehandlerId") String saksbehandlerId) {
        return sjekkTilgangTilBruker(saksbehandlerId, personIdent);
    }

    private ResponseEntity sjekkTilgangTilBruker(String saksbehandlerId, String personIdent) {
        Personinfo personInfo = personService.hentPersoninfo(personIdent);
        Tilgang tilgang = tilgangService.sjekkTilgang(personIdent, saksbehandlerId, personInfo);
        return lagRespons(tilgang);
    }

    private ResponseEntity lagRespons(Tilgang tilgang) {
            return ok()
                    .contentType(APPLICATION_JSON)
                    .body(tilgang);
    }
}
