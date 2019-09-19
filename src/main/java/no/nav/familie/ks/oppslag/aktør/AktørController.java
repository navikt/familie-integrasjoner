package no.nav.familie.ks.oppslag.aktør;

import no.nav.security.oidc.api.ProtectedWithClaims;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

@RestController
@ProtectedWithClaims(issuer = "intern")
@RequestMapping("/api/aktoer")
public class AktørController {

    private AktørService aktørService;

    AktørController(AktørService aktørService) {
        this.aktørService = aktørService;
    }

    @GetMapping
    public ResponseEntity<String> getAktørIdForPersonIdent(@NotNull @RequestHeader(name = "Nav-Personident") String personIdent) {
        return aktørService.getAktørId(personIdent);
    }
}
