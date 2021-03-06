package no.nav.familie.integrasjoner.tilgangskontroll

import no.nav.familie.integrasjoner.felles.Tema
import no.nav.familie.kontrakter.felles.tilgangskontroll.Tilgang
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(value = ["/api/tilgang"])
@Profile("!e2e")
class TilgangskontrollController(private val tilgangskontrollService: TilgangskontrollService) {

    @GetMapping(path = ["/person"])
    @ProtectedWithClaims(issuer = "azuread")
    fun tilgangTilPerson(@RequestHeader(name = "Nav-Personident") personIdent: String): Tilgang {
        return tilgangskontrollService.sjekkTilgangTilBruker(personIdent)
    }

    @PostMapping(path = ["/personer"])
    @ProtectedWithClaims(issuer = "azuread")
    fun tilgangTilPersoner(@RequestBody personIdenter: List<String>): List<Tilgang> {
        return tilgangskontrollService.sjekkTilgangTilBrukere(personIdenter)
    }

    @GetMapping(path = ["/v2/person"])
    @ProtectedWithClaims(issuer = "azuread")
    fun tilgangTilPerson(@RequestHeader(name = "Nav-Personident") personIdent: String, @RequestHeader(name = "Nav-Tema") tema: Tema): Tilgang {
        return tilgangskontrollService.sjekkTilgangTilBruker(personIdent, tema)
    }

    @PostMapping(path = ["/v2/personer"])
    @ProtectedWithClaims(issuer = "azuread")
    fun tilgangTilPersoner(@RequestBody personIdenter: List<String>, @RequestHeader(name = "Nav-Tema") tema: Tema): List<Tilgang> {
        return tilgangskontrollService.sjekkTilgangTilBrukere(personIdenter, tema)
    }
}
