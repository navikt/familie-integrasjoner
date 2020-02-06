package no.nav.familie.integrasjoner.tilgangskontroll

import no.nav.familie.integrasjoner.tilgangskontroll.domene.Tilgang
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/api/tilgang"])
class TilgangskontrollController(private val tilgangskontrollService: TilgangskontrollService) {

    @GetMapping(path = ["/person"]) @ProtectedWithClaims(issuer = "azuread")
    fun tilgangTilPerson(@RequestHeader(name = "Nav-Personident") personIdent: String): Tilgang {
        return tilgangskontrollService.sjekkTilgangTilBruker(personIdent)
    }

    @GetMapping(path = ["/personer"])
    @ProtectedWithClaims(issuer = "azuread") fun tilgangTilPersoner(@RequestHeader(name = "Nav-Personident")
                                                                    personIdenter: List<String>): List<Tilgang> {
        return tilgangskontrollService.sjekkTilgangTilBrukere(personIdenter)
    }
}
