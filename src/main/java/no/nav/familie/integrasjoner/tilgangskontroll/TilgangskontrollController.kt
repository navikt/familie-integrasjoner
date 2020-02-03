package no.nav.familie.integrasjoner.tilgangskontroll;

import no.nav.familie.integrasjoner.azure.domene.Saksbehandler;
import no.nav.familie.integrasjoner.client.rest.AzureGraphRestClient;
import no.nav.familie.integrasjoner.personopplysning.PersonopplysningerService;
import no.nav.familie.integrasjoner.personopplysning.domene.Personinfo;
import no.nav.familie.integrasjoner.tilgangskontroll.domene.Tilgang;
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

    private AzureGraphRestClient azureGraphRestClient;
    private TilgangsKontrollService tilgangService;
    private PersonopplysningerService personService;

    @Autowired
    public TilgangskontrollController(AzureGraphRestClient azureGraphRestClient,
                                      TilgangsKontrollService tilgangsKontrollService,
                                      PersonopplysningerService personopplysningerService) {
        this.azureGraphRestClient = azureGraphRestClient;
        this.tilgangService = tilgangsKontrollService;
        this.personService = personopplysningerService;
    }

    @GetMapping(path = "/person")
    @ProtectedWithClaims(issuer = "azuread")
    public ResponseEntity<Tilgang> tilgangTilPerson(@NotNull @RequestHeader(name = "Nav-Personident") String personIdent) {
        return sjekkTilgangTilBruker(personIdent);
    }

    private ResponseEntity<Tilgang> sjekkTilgangTilBruker(String personIdent) {
        Saksbehandler saksbehandler = azureGraphRestClient.getSaksbehandler();
        Personinfo personInfo = personService.hentPersoninfo(personIdent);
        Tilgang tilgang = tilgangService.sjekkTilgang(personIdent, saksbehandler, personInfo);
        return lagRespons(tilgang);
    }

    private ResponseEntity<Tilgang> lagRespons(Tilgang tilgang) {
        return ok()
                .contentType(APPLICATION_JSON)
                .body(tilgang);
    }
}
