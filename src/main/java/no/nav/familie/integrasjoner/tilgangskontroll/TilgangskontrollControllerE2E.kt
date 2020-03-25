package no.nav.familie.integrasjoner.tilgangskontroll

import no.nav.familie.integrasjoner.tilgangskontroll.domene.Tilgang
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(value = ["/api/tilgang"])
@Profile("e2e")
class TilgangskontrollControllerE2E {

    @GetMapping(path = ["/person"])
    @ProtectedWithClaims(issuer = "azuread")
    fun tilgangTilPerson(@RequestHeader(name = "Nav-Personident") personIdent: String): Tilgang {
        return Tilgang(harTilgang = true)
    }

    @PostMapping(path = ["/personer"])
    @ProtectedWithClaims(issuer = "azuread")
    fun tilgangTilPersoner(@RequestBody personIdenter: List<String>): List<Tilgang> {
        return listOf(Tilgang(harTilgang = true))
    }
}
