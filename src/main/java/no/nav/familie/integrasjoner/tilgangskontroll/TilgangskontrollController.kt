package no.nav.familie.integrasjoner.tilgangskontroll

import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.tilgangskontroll.Tilgang
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/api/tilgang"])
@Profile("!e2e")
class TilgangskontrollController(private val tilgangskontrollService: TilgangskontrollService) {
    @GetMapping(path = ["/person"])
    @ProtectedWithClaims(issuer = "azuread")
    fun tilgangTilPerson(
        @RequestHeader(name = "Nav-Personident") personIdent: String,
    ): Tilgang {
        return tilgangskontrollService.sjekkTilgangTilBruker(personIdent)
    }

    @PostMapping(path = ["/personer"])
    @ProtectedWithClaims(issuer = "azuread")
    fun tilgangTilPersoner(
        @RequestBody personIdenter: List<String>,
    ): List<Tilgang> {
        return tilgangskontrollService.sjekkTilgangTilBrukere(personIdenter)
    }

    @GetMapping(path = ["/v2/person"])
    @ProtectedWithClaims(issuer = "azuread")
    fun tilgangTilPerson(
        @RequestHeader(name = "Nav-Personident") personIdent: String,
        @RequestHeader(name = "Nav-Tema") tema: Tema,
    ): Tilgang {
        return tilgangskontrollService.sjekkTilgangTilBruker(personIdent, tema)
    }

    @PostMapping(path = ["/v2/personer"])
    @ProtectedWithClaims(issuer = "azuread")
    fun tilgangTilPersoner(
        @RequestBody personIdenter: List<String>,
        @RequestHeader(name = "Nav-Tema") tema: Tema,
    ): List<Tilgang> {
        return tilgangskontrollService.sjekkTilgangTilBrukere(personIdenter, tema)
    }

    @PostMapping(path = ["/person-med-relasjoner"])
    @ProtectedWithClaims(issuer = "azuread")
    fun tilgangTilPersonMedRelasjoner(
        @RequestBody personIdent: PersonIdent,
        @RequestHeader(name = "Nav-Tema") tema: Tema,
    ): Tilgang {
        return tilgangskontrollService.sjekkTilgangTilPersonMedRelasjoner(personIdent.ident, tema)
    }

    @GetMapping("/foo/{id}")
    @ResponseBody
    @Unprotected
    fun foo(
        @PathVariable id: String,
    ) {
    }

    @PostMapping("/føø")
    @ResponseBody
    @Unprotected
    fun føø(
        @RequestParam id: String,
    ) {
    }
}
