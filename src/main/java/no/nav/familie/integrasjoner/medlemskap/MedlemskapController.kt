package no.nav.familie.integrasjoner.medlemskap

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.familie.kontrakter.felles.medlemskap.Medlemskapsinfo
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("api/medlemskap")
class MedlemskapController(private val medlemskapService: MedlemskapService) {

    @GetMapping("v1")
    fun hentMedlemskapsunntak(@RequestParam("id") aktørId: String): Ressurs<Medlemskapsinfo> {
        return success(medlemskapService.hentMedlemskapsunntak(aktørId), "Henting av medlemskapsunntak OK")
    }

    @GetMapping("forIdent")
    fun hentMedlemskapsunntakForIdent(@RequestHeader("Nav-Personident") ident: String): Medlemskapsinfo {
        return medlemskapService.hentMedlemskapsunntakForIdent(ident)
    }
}
