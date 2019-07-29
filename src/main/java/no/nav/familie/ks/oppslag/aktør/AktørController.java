package no.nav.familie.ks.oppslag.aktør;

import no.nav.familie.ks.oppslag.felles.MDCOperations;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public String getAktoerIdForPersonIdent(@NotNull @RequestParam(name = "ident") String personIdent) {
        MDCOperations.putCallId(); // FIXME: Midlertidig, bør erstattes med en interceptor
        return aktørService.getAktørId(personIdent);
    }
}
