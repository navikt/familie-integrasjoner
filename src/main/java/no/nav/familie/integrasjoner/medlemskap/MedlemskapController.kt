package no.nav.familie.integrasjoner.medlemskap

import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.medlemskap.Medlemskapsinfo
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("api/medlemskap")
class MedlemskapController(private val medlemskapService: MedlemskapService) {

    @PostMapping("v3")
    fun hentMedlemskapsunntakForIdentEllerAktørIdV3(
        @RequestBody(required = true) personIdent: PersonIdent
    ): Ressurs<Medlemskapsinfo> {
        return Ressurs.success(medlemskapService.hentMedlemskapsunntak(personIdent.ident))
    }
}
