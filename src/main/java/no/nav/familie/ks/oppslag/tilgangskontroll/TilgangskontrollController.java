package no.nav.familie.ks.oppslag.tilgangskontroll;

import no.nav.familie.ks.oppslag.azure.AzureGraphService;
import no.nav.familie.ks.oppslag.azure.domene.Saksbehandler;
import no.nav.familie.ks.oppslag.personopplysning.PersonopplysningerService;
import no.nav.familie.ks.oppslag.personopplysning.domene.Personinfo;
import no.nav.familie.ks.oppslag.tilgangskontroll.domene.Tilgang;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
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

    private AzureGraphService azureGraphService;
    private TilgangsKontrollService tilgangService;
    private PersonopplysningerService personService;

    @Autowired
    public TilgangskontrollController(AzureGraphService azureGraphService, TilgangsKontrollService tilgangsKontrollService, PersonopplysningerService personopplysningerService) {
        this.azureGraphService = azureGraphService;
        this.tilgangService = tilgangsKontrollService;
        this.personService = personopplysningerService;
    }

    @GetMapping(path = "/person")
    @ProtectedWithClaims(issuer = "azuread")
    public ResponseEntity tilgangTilPerson(@NotNull @RequestHeader(name = "Nav-Personident") String personIdent) {
        return sjekkTilgangTilBruker(personIdent);
    }

    private ResponseEntity sjekkTilgangTilBruker(String personIdent) {
        Saksbehandler saksbehandler = azureGraphService.getSaksbehandler();
        Personinfo personInfo = personService.hentPersoninfo(personIdent);
        Tilgang tilgang = tilgangService.sjekkTilgang(personIdent, saksbehandler, personInfo);
        return lagRespons(tilgang);
    }

    private ResponseEntity lagRespons(Tilgang tilgang) {
            return ok()
                    .contentType(APPLICATION_JSON)
                    .body(tilgang);
    }
}
