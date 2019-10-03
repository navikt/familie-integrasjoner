package no.nav.familie.ks.oppslag.medlemskap;

import no.nav.familie.ks.oppslag.medlemskap.domain.MedlemskapsInfo;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@ProtectedWithClaims(issuer = "intern")
@RequestMapping("api/medlemskap")
public class MedlemskapController {

    private MedlemskapService medlemskapService;

    public MedlemskapController(MedlemskapService service) {
        this.medlemskapService = service;
    }

    @GetMapping
    public ResponseEntity<MedlemskapsInfo> hentMedlemskapsUnntak(@RequestParam("id") String aktørId) {
        return medlemskapService.hentMedlemskapsUnntak(aktørId);
    }
}
