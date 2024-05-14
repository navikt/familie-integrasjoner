package no.nav.familie.integrasjoner.tilgangskontroll

import no.nav.familie.kontrakter.felles.tilgangskontroll.Tilgang
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/api/tilgang"])
@Profile("e2e")
class TilgangskontrollControllerE2E {
    @PostMapping(path = ["/personer"])
    @ProtectedWithClaims(issuer = "azuread")
    fun tilgangTilPersoner(
        @RequestBody personIdenter: List<String>,
    ): List<Tilgang> {
        return personIdenter.map { Tilgang(it, true) }
    }
}
