package no.nav.familie.ks.oppslag.medlemskap;

import no.nav.familie.ks.kontrakter.sak.Ressurs;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("api/medlemskap")
public class MedlemskapController {

    private MedlemskapService medlemskapService;

    public MedlemskapController(MedlemskapService service) {
        this.medlemskapService = service;
    }

    @GetMapping("v1")
    public ResponseEntity<Ressurs> hentMedlemskapsUnntak(@RequestParam("id") String aktørId) {
        return ResponseEntity.ok(Ressurs.Companion.success(medlemskapService.hentMedlemskapsUnntak(aktørId), "Henting av medlemskapsunntak OK"));
    }
}
