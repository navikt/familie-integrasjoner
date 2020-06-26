package no.nav.familie.integrasjoner.tilgangskontroll

import no.nav.familie.integrasjoner.tilgangskontroll.domene.Tilgang
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.springframework.stereotype.Service

@Service
class TilgangskontrollService(private val cachedTilgangskontrollService: CachedTilgangskontrollService) {

    fun sjekkTilgangTilBrukere(personIdenter: List<String>): List<Tilgang> {
        return personIdenter.map(this::sjekkTilgangTilBruker)
    }

    fun sjekkTilgangTilBruker(personIdent: String): Tilgang {
        val jwtToken = SpringTokenValidationContextHolder().tokenValidationContext.getJwtToken("azuread")
        return cachedTilgangskontrollService.sjekkTilgang(personIdent, jwtToken)
    }

    fun sjekkTilgang(personIdent: String, jwtToken: JwtToken): Tilgang {
        return cachedTilgangskontrollService.sjekkTilgang(personIdent, jwtToken)
    }

}
