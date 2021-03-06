package no.nav.familie.integrasjoner.tilgangskontroll

import no.nav.familie.integrasjoner.felles.Tema
import no.nav.familie.kontrakter.felles.tilgangskontroll.Tilgang
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.springframework.stereotype.Service

@Service
class TilgangskontrollService(private val cachedTilgangskontrollService: CachedTilgangskontrollService) {

    fun sjekkTilgangTilBrukere(personIdenter: List<String>, tema: Tema = Tema.BAR): List<Tilgang> {
        return personIdenter.map { ident -> sjekkTilgangTilBruker(ident, tema) }
    }

    fun sjekkTilgangTilBruker(personIdent: String, tema: Tema = Tema.BAR): Tilgang {
        val jwtToken = SpringTokenValidationContextHolder().tokenValidationContext.getJwtToken("azuread")
        return cachedTilgangskontrollService.sjekkTilgang(personIdent, jwtToken, tema)
    }

    fun sjekkTilgang(personIdent: String, jwtToken: JwtToken, tema: Tema = Tema.BAR): Tilgang {
        return cachedTilgangskontrollService.sjekkTilgang(personIdent, jwtToken, tema)
    }

}
