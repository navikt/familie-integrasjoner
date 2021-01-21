package no.nav.familie.integrasjoner.medlemskap

import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.medlemskap.Medlemskapsinfo
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("api/medlemskap")
class MedlemskapController(private val medlemskapService: MedlemskapService) {

    @GetMapping("v1")
    @Deprecated("Bruk v2")
    fun hentMedlemskapsunntak(@RequestParam("id") aktørId: String): Ressurs<Medlemskapsinfo> {
        return Ressurs.success(medlemskapService.hentMedlemskapsunntak(aktørId), "Henting av medlemskapsunntak OK")
    }

    @GetMapping("v2")
    @Deprecated("Bruk v3")
    fun hentMedlemskapsunntakForIdentEllerAktørId(@RequestHeader("Nav-Personident") ident: String)
            : Ressurs<Medlemskapsinfo> {
        return Ressurs.success(medlemskapService.hentMedlemskapsunntakForIdent(ident))
    }

    @PostMapping("v3")
    fun hentMedlemskapsunntakForIdentEllerAktørIdV3(@RequestBody(required = true) personIdent: PersonIdent)
            : Ressurs<Medlemskapsinfo> {
        return Ressurs.success(medlemskapService.hentMedlemskapsunntakForIdent(personIdent.ident))
    }
}
