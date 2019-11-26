package no.nav.familie.integrasjoner.aktør;

import no.nav.familie.integrasjoner.personopplysning.domene.AktørId;
import no.nav.familie.ks.kontrakter.sak.Ressurs;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.Map;

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/aktoer")
public class AktørController {

    private AktørService aktørService;

    @Autowired
    AktørController(AktørService aktørService) {
        this.aktørService = aktørService;
    }

    @GetMapping("v1")
    public ResponseEntity<Ressurs> getAktørIdForPersonIdent(@NotNull @RequestHeader(name = "Nav-Personident") String personIdent) {
        return ResponseEntity
                .ok()
                .body(Ressurs.Companion.success(
                        Map.of("aktørId", aktørService.getAktørId(personIdent)), "Hent aktør for personident OK")
                );
    }

    @GetMapping(path = "v1/fraaktorid")
    public ResponseEntity<Ressurs> getPersonIdentForAktørId(@NotNull @RequestHeader(name = "Nav-Aktorid") String aktørId) {
        return ResponseEntity
                .ok()
                .body(Ressurs.Companion.success(
                        Map.of("personIdent", aktørService.getPersonIdent(new AktørId(aktørId))), "Hent personIdent for aktør OK")
                );
    }
}
